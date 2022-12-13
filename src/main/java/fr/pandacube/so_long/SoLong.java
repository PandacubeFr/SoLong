package fr.pandacube.so_long;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import fr.pandacube.lib.chat.Chat;
import fr.pandacube.lib.chat.ChatUtil;
import fr.pandacube.lib.paper.PandaLibPaper;
import fr.pandacube.lib.paper.reflect.PandalibPaperReflect;
import fr.pandacube.lib.paper.reflect.util.BedrockBambooCollisionFixer;
import fr.pandacube.lib.util.Log;
import fr.pandacube.so_long.commands.CommandBroadcast;
import fr.pandacube.so_long.commands.CommandSystem;
import fr.pandacube.so_long.modules.BackupManager;
import fr.pandacube.so_long.modules.PlayerChatManager;
import fr.pandacube.so_long.modules.SponsorMyselfInF3;
import fr.pandacube.so_long.modules.TabListHeaderFooterManager;
import fr.pandacube.so_long.players.SoLongPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.TimeZone;

import static fr.pandacube.lib.chat.ChatFilledLine.centerText;
import static fr.pandacube.lib.chat.ChatStatic.chat;
import static fr.pandacube.lib.chat.ChatStatic.text;

public class SoLong extends JavaPlugin implements Listener {

    private static SoLong instance;

    public synchronized static SoLong getPlugin() {
        return instance;
    }




    public BackupManager backupManager;




    @Override
    public void onLoad() {
        instance = this;

        Locale.setDefault(Locale.FRANCE);
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"));

        Log.setLogger(getLogger());

        PandaLibPaper.init(this);
        PandalibPaperReflect.init();
    }



    @Override
    public void onEnable() {

        SoLongPlayerManager.init();
        backupManager = new BackupManager();

        new TabListHeaderFooterManager();
        new BedrockBambooCollisionFixer();
        new PlayerChatManager();
        SponsorMyselfInF3.init();



        new CommandSystem();
        new CommandBroadcast();


        Bukkit.getPluginManager().registerEvents(this, this);



        // initialize class to avoid NCDFE when updating the plugin
        @SuppressWarnings({ "unused" })
        Class<?>
                c4 = fr.pandacube.lib.chat.ChatUtil.class;
    }



    @Override
    public void onDisable() {

        if (backupManager != null)
            backupManager.onDisable();

        PandaLibPaper.disable();
    }











    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.deathMessage() == null)
            return;

        event.deathMessage(text("SO LONG ")
                .broadcastColor()
                .hover(event.deathMessage())
                .thenPlayerName(event.getPlayer().displayName())
                .thenText(" !")
                .getAdv());
    }






    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Chat firstTime = event.getPlayer().hasPlayedBefore()
                ? chat()
                : text(" (Nouveau joueur !)");

        event.joinMessage(chat().broadcastColor()
                .thenText("[")
                .then(text("Connexion").successColor())
                .thenText("] ")
                .thenPlayerName(event.getPlayer().displayName())
                .then(firstTime)
                .getAdv()
        );
    }






    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.quitMessage(chat()
                .then(text("[").broadcastColor())
                .then(text("Déconnexion").failureColor())
                .then(text("] ").broadcastColor())
                .thenPlayerName(event.getPlayer().displayName())
                .getAdv()
        );
    }


    @EventHandler
    public void onPing(PaperServerListPingEvent event) {
        event.motd(
                chat()
                        .then(centerText(text("SO LONG !").gold().bold()).maxWidth(ChatUtil.MOTD_WIDTH).decoChar(' ').toChat())
                        .thenNewLine()
                        .then(centerText(text("La commu d’Antoine Daniel !").infoColor()).maxWidth(ChatUtil.MOTD_WIDTH).decoChar(' ').toChat())
                        .asComponent()
        );
    }


}
