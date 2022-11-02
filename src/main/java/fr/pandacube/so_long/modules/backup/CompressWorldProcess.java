package fr.pandacube.so_long.modules.backup;

import fr.pandacube.so_long.util.WorldUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.io.FilenameFilter;

public class CompressWorldProcess extends CompressProcess {
	
	private boolean autoSave = true; 
	
	protected CompressWorldProcess(BackupManager bm, final String n) {
		super(bm, Type.WORLDS, n);
	}
	
	private World getWorld() {
		return Bukkit.getWorld(name);
	}
	
	
	public FilenameFilter getFilenameFilter() {
		return null;
	}
	
	
	@Override
	public File getSourceDir() {
		return WorldUtil.worldDir(name);
	}
	
	@Override
	protected void onCompressStart() {
		World w = getWorld();
		if (w == null)
			return;
		autoSave = w.isAutoSave();
		w.setAutoSave(false);
	}
	
	@Override
	protected void onCompressEnd(boolean success) {
		World w = getWorld();
		if (w == null)
			return;
		w.setAutoSave(autoSave);
	}
	
}
