package fr.pandacube.so_long.config;

import fr.pandacube.lib.core.config.AbstractConfig;
import fr.pandacube.lib.util.Log;
import fr.pandacube.so_long.SoLong;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultConfig {
	public static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	
	
	private final SoLong plugin = SoLong.getPlugin();

	private final FileConfiguration configFile;

	/*
	 * Backup
	 */

	public final boolean backup_enabled;
	public final String backup_scheduling;
	public final String backup_destination;

	// patterns are regex (used for String.match(regex))
	public final List<String> backup_workdirIgnore;
	
	
	

	DefaultConfig() {
		
		plugin.reloadConfig();

		configFile = plugin.getConfig();

		backup_enabled = configFile.getBoolean("backup.enabled", false);
		backup_scheduling = configFile.getString("backup.scheduling", "0 1 */2 * *"); // 1am every 2 days
		backup_destination = configFile.getString("backup.destination"); // no default value

		backup_workdirIgnore = configFile.contains("backup.workdirIgnore")
				? configFile.getStringList("backup.workdirIgnore")
				: configFile.getStringList("backup.othersIgnore.root");
		if (configFile.contains("backup.othersIgnore")) {
			Log.warning("Config entry backup.othersIgnore still present in config.yml. Please migrate the configuration into backup.workdirIgnore node.");
		}
		
	}

	private String getTranslatedColorCode(String path, String deflt) {
		return AbstractConfig.getTranslatedColorCode(configFile.getString(path, deflt));
	}
}
