package fr.pandacube.so_long.config;

import fr.pandacube.lib.core.config.AbstractConfigManager;
import fr.pandacube.lib.util.Log;
import fr.pandacube.so_long.SoLong;

import java.io.IOException;

/*
 * Configuration du plugin
 */
public class ConfigManager extends AbstractConfigManager {
	private static ConfigManager instance = null;

	public synchronized static ConfigManager getInstance() {
		if (instance == null) loadNewInstance();
		return instance;
	}

	public synchronized static void loadNewInstance() {
		try {
			if (instance == null) {
				instance = new ConfigManager(); 
			}
			else {
				instance.reloadConfig();
			}
		} catch (Exception e) {
			Log.severe("Erreur de chargement de la configuration de " + SoLong.getPlugin().getName(), e);
		}
	}
	
	

	protected SoLong plugin = SoLong.getPlugin();

	private DefaultConfig defaultConfig;

	private ConfigManager() throws Exception {
		super(SoLong.getPlugin().getDataFolder());
	}

	
	@Override
	public void init() throws IOException {
		defaultConfig = new DefaultConfig();
	}
	
	@Override
	public synchronized void reloadConfig() throws IOException {
		super.reloadConfig();
		
		//ChairsManager.onConfigReload();
	}
	
	@Override
	public void close() throws IOException {
		// à compléter au besoin
	}
	
	public DefaultConfig getDefaultConfig() { return defaultConfig; }

}
