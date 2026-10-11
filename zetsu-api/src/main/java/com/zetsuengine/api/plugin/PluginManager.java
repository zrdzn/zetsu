package com.zetsuengine.api.plugin;

import java.util.Collection;

public interface PluginManager {
    Collection<Plugin> getPlugins();
    void loadAll();
    void enableAll();
    void disableAll();
}
