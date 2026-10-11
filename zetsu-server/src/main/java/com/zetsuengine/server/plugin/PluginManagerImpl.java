package com.zetsuengine.server.plugin;

import com.google.gson.Gson;
import com.zetsuengine.api.plugin.Plugin;
import com.zetsuengine.api.plugin.PluginManager;
import com.zetsuengine.api.plugin.PluginMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PluginManagerImpl implements PluginManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginManagerImpl.class);

    private final Map<String, Plugin> plugins = new LinkedHashMap<>();
    private final File pluginDirectory = new File("plugins");
    private final Gson gson = new Gson();

    @Override
    public Collection<Plugin> getPlugins() {
        return Collections.unmodifiableCollection(plugins.values());
    }

    @Override
    public void loadAll() {
        if (!pluginDirectory.exists()) {
            LOGGER.info("Plugins directory does not exist, creating one...");
            boolean res = pluginDirectory.mkdirs();
            if (res) {
                LOGGER.info("Plugins directory successfully created.");
                return;
            }
            LOGGER.error("Could not create plugins directory.");
            return;
        }

        File[] files = pluginDirectory.listFiles((_, name) -> name.endsWith(".jar"));
        if (files == null) {
            LOGGER.error("Could not read plugins from directory.");
            return;
        }

        int c = 0;
        for (File file : files) {
            try (ZipFile zip = new ZipFile(file)) {
                ZipEntry entry = zip.getEntry("zetsu.json");
                if (entry == null) {
                    LOGGER.warn("Plugin {} does not contain 'zetsu.json' metadata file.", file.getName());
                    continue;
                }

                try (InputStream is = zip.getInputStream(entry)) {
                    PluginMeta pluginMeta = gson.fromJson(new InputStreamReader(is), PluginMeta.class);
                    LOGGER.info("Loading '{}' plugin...", pluginMeta.getName());
                    URLClassLoader classLoader = new URLClassLoader(
                            new URL[]{ file.toURI().toURL() },
                            this.getClass().getClassLoader()
                    );

                    Class<?> mainClass = Class.forName(pluginMeta.getMainClass(), true, classLoader);
                    Plugin plugin = (Plugin) mainClass.getDeclaredConstructor().newInstance();
                    plugins.put(pluginMeta.getName(), plugin);
                    plugin.loaded();
                    c++;
                } catch (Exception ex) {
                    LOGGER.error("Cannot read data from jar file '{}'.", file.getName(), ex);
                }
            } catch (Exception ex) {
                LOGGER.error("Jar file '{}' is not a valid archive.", file.getName(), ex);
            }
        }
        LOGGER.info("Loaded {} plugins.", c);
    }

    @Override
    public void enableAll() {
        plugins.forEach((name, plugin) -> {
            try {
                LOGGER.info("Enabling '{}' plugin...", name);
                plugin.enabled();
            } catch (Exception ex) {
                LOGGER.error("Could not enable plugin '{}'.", name, ex);
            }
        });
    }

    @Override
    public void disableAll() {
        plugins.forEach((name, plugin) -> {
            try {
                LOGGER.info("Disabling '{}' plugin...", name);
                plugin.disabled();
            } catch (Exception ex) {
                LOGGER.error("Could not disable plugin '{}'.", name, ex);
            }
        });
    }
}
