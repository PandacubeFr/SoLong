package fr.pandacube.so_long.modules;

import fr.pandacube.lib.paper.modules.PerformanceAnalysisManager;
import fr.pandacube.so_long.SoLong;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static fr.pandacube.lib.chat.ChatStatic.chat;
import static fr.pandacube.lib.chat.ChatStatic.text;

public class TabListHeaderFooterManager implements Listener {


    public TabListHeaderFooterManager() {
        Bukkit.getScheduler().runTaskTimer(SoLong.getPlugin(), this::updateAll, 20, 20);
        Bukkit.getPluginManager().registerEvents(this, SoLong.getPlugin());
    }



    public void updateAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            updateOnPlayer(p);
        }
    }



    public void updateOnPlayer(Player p) {
        int playerCount = Bukkit.getOnlinePlayers().size();
        p.sendPlayerListHeaderAndFooter(
                chat()
                        .then(text("SO LONG !").gold().bold())
                        .thenInfo(" La commu d’Antoine Daniel !")
                        .thenNewLine()
                        .thenData(playerCount)
                        .thenInfo(" joueur" + (playerCount > 1 ? "s" : "") + " en ligne"),
                chat()
                        .then(PerformanceAnalysisManager.getInstance().tpsBar.bar.name())
                        .thenNewLine()
                        .then(PerformanceAnalysisManager.getInstance().memoryBar.bar.name())
                        .thenNewLine()
                        .thenInfo("Ping : ")
                        .thenData(p.getPing() + " ms")
        );
    }



    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateAll();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        updateAll();
    }




}
