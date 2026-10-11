package com.zetsuengine.api.plugin;

public interface Plugin {
    void loaded();
    void enabled();
    void disabled();
}
