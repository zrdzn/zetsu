package com.zetsuengine.server;

import com.zetsuengine.api.Player;
import zombie.characters.IsoPlayer;
import zombie.network.GameServer;

import java.lang.ref.WeakReference;
import java.util.Optional;

public class ZetsuPlayer implements Player {

    private final WeakReference<IsoPlayer> handle;
    private final String username;

    public ZetsuPlayer(IsoPlayer handle) {
        this.handle = new WeakReference<>(handle);
        this.username = handle.getUsername();
    }

    private Optional<IsoPlayer> getHandle() {
        IsoPlayer player = this.handle.get();
        if (player == null || !GameServer.Players.contains(player)) {
            return Optional.empty();
        }
        return Optional.of(player);
    }

    @Override
    public String getUsername() {
        return username;
    }
}
