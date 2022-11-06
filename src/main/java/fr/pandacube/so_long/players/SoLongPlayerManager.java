package fr.pandacube.so_long.players;

import fr.pandacube.lib.chat.Chat;
import fr.pandacube.lib.players.standalone.AbstractPlayerManager;
import fr.pandacube.so_long.SoLong;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.UUID;

public class SoLongPlayerManager {

    private static PlayerManagerImpl instance;

    public static void init() {
        instance = new PlayerManagerImpl();
    }

    /**
     * Gets the current instance of player manager.
     * @return the player manager.
     */
    public static synchronized AbstractPlayerManager<OnlinePlayer, OffPlayer> getInstance() {
        return instance;
    }




    public static class PlayerManagerImpl extends AbstractPlayerManager<OnlinePlayer, OffPlayer> implements Listener {

        public PlayerManagerImpl() {
            Bukkit.getPluginManager().registerEvents(this, SoLong.getPlugin());
        }

        @Override
        protected OffPlayer newOffPlayerInstance(UUID p) {
            return new OffPlayer(p);
        }

        @Override
        protected void sendMessageToConsole(Component message) {
            Bukkit.getServer().getLogger().info(Chat.chatComponent(message).getLegacyText());
        }


        @EventHandler(priority = EventPriority.LOWEST)
        public void onJoin(PlayerJoinEvent event) {
            OnlinePlayer op = new OnlinePlayer(event.getPlayer());
            addPlayer(op);
        }



        @EventHandler(priority = EventPriority.MONITOR)
        public void onQuit(PlayerQuitEvent event) {
            removePlayer(event.getPlayer().getUniqueId());
        }

    }



    public static OnlinePlayer get(Player p) {
        return get(p == null ? null : p.getUniqueId());
    }

    public static OnlinePlayer get(UUID pId) {
        return getInstance().get(pId);
    }

    /**
     * Insensible à la casse
     *
     * @param p le pseudo du joueur recherché
     * @return
     */
    public static OnlinePlayer get(String p) {
        return get(Bukkit.getServer().getPlayerExact(p));
    }

    /**
     * Get a list snapshot of all connected players.
     * The returned list is not updated when a player join or leave the server,
     * to prevent comodification exception.
     *
     * @return
     */
    public static List<OnlinePlayer> getAll() {
        return getInstance().getAll();
    }

    public static boolean isOnline(Player p) {
        return getInstance().isOnline(p.getUniqueId());
    }









}
