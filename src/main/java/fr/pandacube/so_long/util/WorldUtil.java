package fr.pandacube.so_long.util;

import fr.pandacube.lib.paper.reflect.wrapper.craftbukkit.CraftServer;
import fr.pandacube.lib.reflect.wrapper.ReflectWrapper;
import org.bukkit.Bukkit;
import org.bukkit.World.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WorldUtil {
	
	public static final List<String> PRIMARY_WORLDS;
	
	static {
		List<String> primaryWorlds = new ArrayList<>(3);

		String world = ReflectWrapper.wrapTyped(Bukkit.getServer(), CraftServer.class).getServer().getLevelIdName();

		primaryWorlds.add(world);
		if (Bukkit.getAllowNether()) primaryWorlds.add(world + "_nether");
		if (Bukkit.getAllowEnd()) primaryWorlds.add(world + "_the_end");

		PRIMARY_WORLDS = Collections.unmodifiableList(primaryWorlds);
	}
	
	
	
	
	
	
	public static Environment determineEnvironment(String world) {
		if (Bukkit.getWorld(world) != null) {
			return Bukkit.getWorld(world).getEnvironment();
		}
		
		File worldFolder = worldDir(world);
		
		if (!worldFolder.isDirectory())
			throw new IllegalStateException("The world " + world + " is not a valid world (directory not found).");
		
		if (!new File(worldFolder, "level.dat").isFile())
			throw new IllegalStateException("The world " + world + " is not a valid world (level.dat not found).");
		
		if (new File(worldFolder, "region").isDirectory())
			return Environment.NORMAL;
		
		if (new File(worldFolder, "DIM-1" + File.pathSeparator + "region").isDirectory())
			return Environment.NETHER;
		
		if (new File(worldFolder, "DIM1" + File.pathSeparator + "region").isDirectory())
			return Environment.THE_END;
		
		throw new IllegalStateException("Unable to determine the type of the world " + world + ".");
	}
	
	
	
	private static final List<String> REGION_DATA_FILES = Arrays.asList("entities", "poi", "region", "DIM-1", "DIM1");
	
	public static List<File> regionDataFiles(String world) {
		return onlyExistents(worldDir(world), REGION_DATA_FILES);
	}
	
	private static List<File> onlyExistents(File worldDir, List<String> searchList) {
		return searchList.stream()
				.map(f -> new File(worldDir, f))
				.filter(File::exists)
				.toList();
	}
	
	public static File worldDir(String world) {
		return new File(Bukkit.getWorldContainer(), world);
	}
	
	public static boolean isValidWorld(String world) {
		File d = worldDir(world);
		return d.isDirectory() && new File(d, "level.dat").isFile(); 
	}
	
	
}
