package fr.pandacube.so_long.commands;

import fr.pandacube.lib.paper.commands.PaperBrigadierCommand;
import fr.pandacube.so_long.SoLong;

public abstract class BrigadierCommand extends PaperBrigadierCommand {
	
	public static void unregisterUnwantedPluginCommands() {
		restoreVanillaCommand("give");
		restoreVanillaCommand("clear");
		restoreVanillaCommand("enchant");
		restoreVanillaCommand("xp");
		restoreVanillaCommand("kill");
		restoreVanillaCommand("recipe");
		restoreVanillaCommand("weather");
	}
	
	protected SoLong plugin = SoLong.getPlugin();

	public BrigadierCommand() {
		super(SoLong.getPlugin());
	}




}
