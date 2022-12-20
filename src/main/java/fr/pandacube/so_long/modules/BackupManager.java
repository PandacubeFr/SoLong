package fr.pandacube.so_long.modules;

import fr.pandacube.lib.paper.backup.PaperBackupConfig;
import fr.pandacube.lib.paper.backup.PaperBackupManager;
import fr.pandacube.so_long.config.ConfigManager;
import fr.pandacube.so_long.config.DefaultConfig;

import java.io.File;

public class BackupManager extends PaperBackupManager {

    public BackupManager() {
        super(generateConfig());
    }

    public void updateConfig() {
        setConfig(generateConfig());
    }


    private static PaperBackupConfig generateConfig() {
        DefaultConfig pluginConfig = ConfigManager.getInstance().getDefaultConfig();
        PaperBackupConfig cfg = new PaperBackupConfig();
        cfg.workdirBackupEnabled = pluginConfig.backup_enabled;
        cfg.worldBackupEnabled = pluginConfig.backup_enabled;
        cfg.backupDirectory = new File(pluginConfig.backup_destination);
        cfg.scheduling = pluginConfig.backup_scheduling;
        cfg.workdirIgnoreList = pluginConfig.backup_workdirIgnore;
        return cfg;
    }



}
