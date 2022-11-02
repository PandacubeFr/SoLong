package fr.pandacube.so_long.util;

import fr.pandacube.lib.chat.Chat;
import fr.pandacube.lib.paper.reflect.wrapper.craftbukkit.CraftWorld;
import fr.pandacube.lib.paper.reflect.wrapper.minecraft.server.ChunkMap;
import fr.pandacube.lib.reflect.wrapper.ReflectWrapper;
import fr.pandacube.so_long.SoLong;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldSaveUtil {
	
	private static ChunkMap getChunkMap(World w) {
		return ReflectWrapper.wrapTyped(w, CraftWorld.class).getHandle().getChunkSource().chunkMap;
	}
	
	public static void nmsSaveFlush(World w) {
		SoLong.getPlugin().performanceAnalysisManager.setAlteredTPSTitle(
				Chat.text("Sauvegarde map ").color(NamedTextColor.GOLD).thenData(w.getName()).thenText(" ...")
				);
		
		try {
			ReflectWrapper.wrapTyped(w, CraftWorld.class).getHandle().save(null, true, false);
		} finally {
			SoLong.getPlugin().performanceAnalysisManager.setAlteredTPSTitle(null);
		}
	}
	
	public static void nmsSaveAllFlush() {
		Bukkit.getWorlds().forEach(WorldSaveUtil::nmsSaveFlush);
	}
	
}
