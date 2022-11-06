package fr.pandacube.so_long.modules;

import fr.pandacube.lib.paper.scheduler.SchedulerUtil;
import fr.pandacube.lib.paper.util.BukkitEvent;
import fr.pandacube.so_long.SoLong;
import fr.pandacube.so_long.players.OnlinePlayer;
import fr.pandacube.so_long.players.SoLongPlayerManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.http.WebSocket.Listener;

import static fr.pandacube.lib.chat.ChatStatic.chat;
import static fr.pandacube.lib.chat.ChatStatic.text;

public class SponsorMyselfInF3 {
    public static void init() {
        BukkitEvent.register(PlayerJoinEvent.class, SponsorMyselfInF3::onJoin, EventPriority.MONITOR);
    }

    private static void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(SoLong.getPlugin(), () -> onJoinThenTick(event.getPlayer()), 1);
    }

    private static void onJoinThenTick(Player p) {
        if (!p.isOnline())
            return;
        OnlinePlayer op = SoLongPlayerManager.get(p);
        if (op == null)
            return;
        op.sendServerBrand(chat()
                .then(text("SO LONG !").gold().bold())
                .thenText(" / Hébergé par ")
                .then(text("Pandacube.fr").italic())
                .getLegacyText());
    }
}
