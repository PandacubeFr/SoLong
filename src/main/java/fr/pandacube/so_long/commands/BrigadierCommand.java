package fr.pandacube.so_long.commands;

import fr.pandacube.lib.commands.SuggestionsSupplier;
import fr.pandacube.lib.paper.commands.PaperBrigadierCommand;
import fr.pandacube.so_long.SoLong;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

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






	//public static final SuggestionsSupplier<CommandSender> TAB_PLAYER_CURRENT_SERVER = (sender, ti, token, a) -> SuggestionsSupplier.collectFilteredStream(PaperPlayerManager.getNamesOnlyVisible(sender instanceof Player ? (Player) sender : null).stream(), token);
	
	//public static final SuggestionsSupplier<CommandSender> TAB_PLAYER_CURRENT_SERVER_THEN_OFFLINE = TAB_PLAYER_CURRENT_SERVER.orIfEmpty(PlayerFinder.TAB_PLAYER_OFFLINE());
	
	public static final SuggestionsSupplier<CommandSender> TAB_WORLDS = (s, ti, token, a) -> SuggestionsSupplier.collectFilteredStream(Bukkit.getWorlds().stream().map(World::getName), token);



}
