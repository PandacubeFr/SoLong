package fr.pandacube.so_long.modules;

import fr.pandacube.lib.paper.modules.backup.BackupConfig;
import fr.pandacube.so_long.config.ConfigManager;
import fr.pandacube.so_long.config.DefaultConfig;

import java.io.File;

public class BackupManager extends fr.pandacube.lib.paper.modules.backup.BackupManager {

    public BackupManager() {
        super(generateConfig());
    }

    public void updateConfig() {
        setConfig(generateConfig());
    }


    private static BackupConfig generateConfig() {
        DefaultConfig pluginConfig = ConfigManager.getInstance().getDefaultConfig();
        BackupConfig cfg = new BackupConfig();
        cfg.workdirBackupEnabled = pluginConfig.backup_enabled;
        cfg.worldBackupEnabled = pluginConfig.backup_enabled;
        cfg.backupDirectory = new File(pluginConfig.backup_destination);
        cfg.scheduling = pluginConfig.backup_scheduling;
        cfg.workdirIgnoreList = pluginConfig.backup_workdirIgnore;
        return cfg;
    }



}
