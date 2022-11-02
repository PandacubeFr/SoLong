package fr.pandacube.so_long.modules.backup;

import fr.pandacube.lib.util.Log;
import fr.pandacube.so_long.SoLong;
import fr.pandacube.so_long.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class BackupManager implements Runnable, Listener {
	public static final int bufferSize = 16 * 1024;
	
	
	
	public final SoLong plugin = SoLong.getPlugin();
	
	public Persist persist;
	
	private final List<CompressProcess> compressQueue = new ArrayList<>();
	
	private final Set<String> compressWorlds = new HashSet<>();
	
	/* package */ AtomicReference<CompressProcess> compressRunning = new AtomicReference<>();

	private final Set<String> dirtyForSave = new HashSet<>();
	
	public BackupManager() {
		persist = new Persist(this);
		
		for (final World world : Bukkit.getWorlds()) {
			initCompressProcess(Type.WORLDS, world.getName());
		}
		
		for (final String other : ConfigManager.getInstance().getDefaultConfig().backup_othersIgnore.keySet()) {
			initCompressProcess(Type.OTHERS, other);
		}
		
		Bukkit.getServer().getScheduler().runTaskTimer(plugin, this, (60 - Calendar.getInstance().get(Calendar.SECOND)) * 20L, 60 * 20L);
		
		Bukkit.getServer().getPluginManager().registerEvents(this, SoLong.getPlugin());
		
	}
	
	
	public void onDisable() {
		
		if (compressRunning.get() != null) {
			Log.warning("[Backup] Waiting after the end of a backup...");
			CompressProcess tmp;
			while ((tmp = compressRunning.get()) != null) {
				try {
					tmp.logProgress();
					// wait 5 seconds between each progress log
					// but check if the process has ended each .5 seconds
					for (int i = 0; i < 10; i++) {
						if (compressRunning.get() == null)
							break;
						Thread.sleep(500);
					}
				} catch (Throwable e) { // could occur because of synchronization errors/interruption/...
					break;
				}
			}
		}

		// save dirty status of worlds
		for (String wName : dirtyForSave) {
			World w = Bukkit.getWorld(wName);
			if (w != null)
				persist.updateDirtyStatusAfterSave(w);
		}
		
		persist.save();
	}
	
	private void initCompressProcess(final Type type, final String name) {
		if (isCompressEnabled()) {
			if (type == Type.WORLDS) {
				if (compressWorlds.contains(name))
					return;
				compressWorlds.add(name);
			}
			CompressProcess process = type == Type.WORLDS ? new CompressWorldProcess(this, name) : new CompressOthersProcess(this, name);
			process.displayDirtynessStatus();
			compressQueue.add(process);
		}
		
		
		
	}
	
	@Override
	public void run() {
		CompressProcess tmp;
		if ((tmp = compressRunning.get()) != null) {
			tmp.logProgress();
		}
		else {
			compressQueue.sort(null);
			for (CompressProcess process : compressQueue) {
				if (System.currentTimeMillis() >= process.getNext() && process.couldRunNow()) {
					process.run();
					return;
				}
			}
		}
	}
	
	
	/* package */ boolean isCompressEnabled() {
		return getTime(ConfigManager.getInstance().getDefaultConfig().backup_compress) != 0;
	}
	
	
	
	/**
	 * @return time in second. Positive if it's an actual interval, negative if it's a daytime (number of second since midnight).
	 * 
	 * Examples : 60 means an interval of 1 minute ; -60 means 00:01
	 */
	/* package */ static int getTime(String str) {
		if (str == null)
			return 0;
		
		if ("0".equals(str)) return 0;
		
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException ignored) { }
		if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false"))
			return Boolean.parseBoolean(str) ? 60 : 0;
		
		
		if (str.contains(":")) {
			final String[] parts = str.split(":");
			if (parts.length == 2)
				return - 1 - Integer.parseInt(parts[0]) * 3600 - Integer.parseInt(parts[1]) * 60;
			else if (parts.length == 3)
				return - 1 - Integer.parseInt(parts[0]) * 86400 - Integer.parseInt(parts[1]) * 3600 - Integer.parseInt(parts[2]) * 60;
			else
				return 0;
		}
		
		if (!str.matches("0|[1-9]\\d*[smhd]")) {
			return 0;
		}
		
		int time = Integer.parseInt(str.substring(0, str.length() - 1));
		if (time < 1) return 0;
		
		final String scale = str.substring(str.length() - 1);
		switch (scale) {
			case "m" -> time *= 60;
			case "h" -> time *= 3600;
			case "d" -> time *= 86400;
		}
		
		return time;
	}
	
	
	
	
	
	
	
	

	@EventHandler(priority = EventPriority.MONITOR)
	public void onWorldLoad(WorldLoadEvent event) {
		initCompressProcess(Type.WORLDS, event.getWorld().getName());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onWorldSave(WorldSaveEvent event) {
		if (event.getWorld().getLoadedChunks().length > 0
				|| dirtyForSave.contains(event.getWorld().getName())) {
			persist.updateDirtyStatusAfterSave(event.getWorld());
			dirtyForSave.remove(event.getWorld().getName());
		}
	}





	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerChangeWorldEvent(PlayerChangedWorldEvent event) {
		dirtyForSave.add(event.getFrom().getName());
		dirtyForSave.add(event.getPlayer().getWorld().getName());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		dirtyForSave.add(event.getPlayer().getWorld().getName());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		dirtyForSave.add(event.getPlayer().getWorld().getName());
	}


	
}
