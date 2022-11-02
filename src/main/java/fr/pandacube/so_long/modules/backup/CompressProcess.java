package fr.pandacube.so_long.modules.backup;

import fr.pandacube.lib.chat.Chat;
import fr.pandacube.lib.paper.util.AutoUpdatedBossBar;
import fr.pandacube.lib.util.FileUtils;
import fr.pandacube.lib.util.Log;
import fr.pandacube.so_long.SoLong;
import fr.pandacube.so_long.config.ConfigManager;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public abstract class CompressProcess implements Comparable<CompressProcess>, Runnable {
	protected final BackupManager backupManager;
	public final Type type;
	public final String name;
	
	
	
	private ZipCompressor compressor = null;
	
	protected CompressProcess(BackupManager bm, final Type t, final String n) {
		backupManager = bm;
		type = t;
		name = n;
	}
	
	@Override
	public int compareTo(final CompressProcess process) {
		return Long.compare(getNext(), process.getNext());
	}
	
	
	public abstract FilenameFilter getFilenameFilter();
	
	public abstract File getSourceDir();
	
	protected abstract void onCompressStart();
	
	protected abstract void onCompressEnd(boolean success);
	
	
	@Override
	public void run() {
		backupManager.compressRunning.set(this);

		FilenameFilter filter = getFilenameFilter();
		File sourceDir = getSourceDir();
		
		if (!sourceDir.exists()) {
			Log.warning(String.format("%% unable to compress %s (check path: %s)", name, sourceDir.getPath()));
			backupManager.compressRunning.set(null);
			return;
		}
		
		File target = new File(ConfigManager.getInstance().getDefaultConfig().backup_destination
				+ "/" + type.toString()
				+ "/" + name 
				+ "/" + getDateFileName() + ".zip");
		
		
		BossBar bossBar = BossBar.bossBar(Chat.text("Archivage"), 0, Color.YELLOW, Overlay.NOTCHED_20);
		AutoUpdatedBossBar auBossBar = new AutoUpdatedBossBar(bossBar, (bar) -> {
			bar.setTitle(Chat.infoText("Archivage ")
					.thenData(type + "\\" + name)
					.thenText(" : ")
					.then(compressor == null
							? Chat.text("Démarrage...").get()
							: compressor.getState()
					)
			);
			bar.setProgress(compressor == null ? 0 : compressor.getProgress());
		});
		auBossBar.scheduleUpdateTimeSyncThreadAsync(100, 100);
		
		onCompressStart();
		
		Bukkit.getScheduler().runTaskAsynchronously(SoLong.getPlugin(), () -> {
			Log.info("[Backup] starting for " + ChatColor.GRAY + type + "\\" + name + ChatColor.RESET + " ...");
			
			compressor = new ZipCompressor(sourceDir, target, 9, filter);

			SoLong.getPlugin().performanceAnalysisManager.addBossBar(bossBar);
			
			boolean success = false;
			try {
				compressor.compress();
				
				success = true;
				
				Log.info("[Backup] finished for " + ChatColor.GRAY + type + "\\" + name + ChatColor.RESET);
				
				backupManager.persist.updateDirtyStatusAfterCompress(type, name);
				
				displayDirtynessStatus();
			}
			catch (final Exception e) {
				Log.severe("[Backup] Failed: " + sourceDir + " -> " + target, e);
				
				FileUtils.delete(target);
				if (target.exists())
					Log.warning("unable to delete: " + target);
			} finally {
				
				backupManager.compressRunning.set(null);
				boolean successF = success;
				Bukkit.getScheduler().runTask(SoLong.getPlugin(), () -> onCompressEnd(successF));
				
				try {
					Thread.sleep(2000);
				} catch(InterruptedException e) {
					Thread.currentThread().interrupt();
				}

				SoLong.getPlugin().performanceAnalysisManager.removeBossBar(bossBar);
			}
		});
	}
	
	

	public void displayDirtynessStatus() {
		if (hasNextScheduled() && type == Type.WORLDS) {
			Log.info("[Backup] " + ChatColor.GRAY + type + "\\" + name + ChatColor.RESET + " is dirty. Next backup on "
					+ DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date(getNext())));
		}
		else if (hasNextScheduled()) {
			Log.info("[Backup] " + ChatColor.GRAY + type + "\\" + name + ChatColor.RESET + " next backup on "
					+ DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date(getNext())));
		}
		else {
			Log.info("[Backup] " + ChatColor.GRAY + type + "\\" + name + ChatColor.RESET + " is clean. Next backup not scheduled.");
		}
	}
	
	

	private String getDateFileName() {
		final Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.YEAR)
				+ String.format("%02d", calendar.get(Calendar.MONTH) + 1)
				+ String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))
				+ "-"
				+ String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY))
				+ String.format("%02d", calendar.get(Calendar.MINUTE))
				+ String.format("%02d", calendar.get(Calendar.SECOND));
	}


	public void logProgress() {
		if (compressor == null)
			return;
		Log.info("[Backup] " + ChatColor.GRAY + type + "\\" + name + ChatColor.RESET + ": " + compressor.getState().toLegacyText());
	}
	
	
	
	public boolean couldRunNow() {
		if (!backupManager.isCompressEnabled())
			return false;
		if (!backupManager.persist.isDirty(type, name))
			return false;
		if (getNext() > System.currentTimeMillis())
			return false;
		return true;
	}
	
	
	
	
	public long getNext() {
		if (!hasNextScheduled())
			return Long.MAX_VALUE;
		return getNextCompress(backupManager.persist.isDirtySince(type, name));
	}
	
	public boolean hasNextScheduled() {
		return backupManager.persist.isDirty(type, name);
	}
	
	

	/**
	 * get the timestamp (in ms) of when the next compress will run, depending on since when the files to compress are dirty.
	 * @param dirtySince the timestamp in ms since the files are dirty
	 * @return the timestamp in ms when the next compress of the files should be run
	 */
	/* package */ static long getNextCompress(long dirtySince) {
		if (dirtySince == -1)
			return Long.MAX_VALUE;
		String compressConfig = ConfigManager.getInstance().getDefaultConfig().backup_compress;
		long interval = BackupManager.getTime(compressConfig); // in s
		// Log.info("Compress config: " + compressConfig + " - interval: " + interval);
		
		if (interval == 0)
			return 0;
		
		if (interval > 0)
			return dirtySince + interval * 1000;
		
		int dayTime = (int) -interval;

		final Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(dirtySince);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.add(Calendar.SECOND, dayTime);
		
		if (calendar.getTimeInMillis() < dirtySince)
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		
		return calendar.getTimeInMillis();
	}
	
}
