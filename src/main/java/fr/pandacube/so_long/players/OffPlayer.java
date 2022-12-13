package fr.pandacube.so_long.players;

import fr.pandacube.lib.paper.players.PaperOffPlayer;
import fr.pandacube.lib.paper.players.PaperOnlinePlayer;

import java.util.UUID;

public class OffPlayer implements PaperOffPlayer {

    private final UUID uniqueId;

    public OffPlayer(UUID playerId) {
        uniqueId = playerId;
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public String getName() {
        return getBukkitOfflinePlayer().getName();
    }

    @Override
    public PaperOnlinePlayer getOnlineInstance() {
        if (isOnline()) {
            if (this instanceof OnlinePlayer)
                return (OnlinePlayer) this;
            return SoLongPlayerManager.get(uniqueId);
        }
        return null;
    }
}
