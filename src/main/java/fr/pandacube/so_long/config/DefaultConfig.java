package fr.pandacube.so_long.config;

import fr.pandacube.lib.core.config.AbstractConfig;
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
	
	
	
	private SoLong plugin = SoLong.getPlugin();

	private FileConfiguration configFile;
	
	/*
	 * Backup
	 */
	
	public final String backup_compress;
	public final String backup_destination;
	
	// patterns are regex (used for String.match(regex))
	public final Map<String, List<String>> backup_othersIgnore;
	
	
	

	DefaultConfig() {
		
		plugin.reloadConfig();

		configFile = plugin.getConfig();

		backup_compress = configFile.getString("backup.compress", "48h");
		backup_destination = configFile.getString("backup.destination", "./minebackup");
		
		ConfigurationSection backup_othersIgnoreSection = configFile.getConfigurationSection("backup.othersIgnore");
		Map<String, List<String>> backup_othersIgnoreModifiable = new HashMap<>();
		for (String k : backup_othersIgnoreSection.getKeys(false)) {
			backup_othersIgnoreModifiable.put(k, backup_othersIgnoreSection.getStringList(k));
		}
		backup_othersIgnore = Collections.unmodifiableMap(backup_othersIgnoreModifiable);
		
	}

	private String getTranslatedColorCode(String path, String deflt) {
		return AbstractConfig.getTranslatedColorCode(configFile.getString(path, deflt));
	}
}
