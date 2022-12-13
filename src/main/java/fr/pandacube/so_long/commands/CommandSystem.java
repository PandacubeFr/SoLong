package fr.pandacube.so_long.commands;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import fr.pandacube.lib.chat.Chat;
import fr.pandacube.lib.chat.ChatConfig.PandaTheme;
import fr.pandacube.lib.chat.ChatUtil;
import fr.pandacube.lib.commands.SuggestionsSupplier;
import fr.pandacube.lib.paper.modules.PerformanceAnalysisManager;
import fr.pandacube.lib.paper.reflect.util.WorldSaveUtil;
import fr.pandacube.lib.util.Log;
import fr.pandacube.lib.util.MemoryUtil;
import fr.pandacube.lib.util.TimeUtil;
import fr.pandacube.so_long.players.OnlinePlayer;
import fr.pandacube.so_long.players.SoLongPlayerManager;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static fr.pandacube.lib.chat.ChatStatic.chat;
import static fr.pandacube.lib.chat.ChatStatic.failureText;
import static fr.pandacube.lib.chat.ChatStatic.infoText;
import static fr.pandacube.lib.chat.ChatStatic.successText;
import static fr.pandacube.lib.chat.ChatStatic.text;

public class CommandSystem extends BrigadierCommand {
	
	@Override
	protected String getTargetPermission() {
		return "solong.system.command";
	}
	
	@Override
	protected String[] getAliases() {
		return new String[] { "syst", "mem", "memory", "lag", "gc", "uptime", "tps" };
	}
	
	@Override
	protected LiteralArgumentBuilder<BukkitBrigadierCommandSource> buildCommand() {
		return literal("system")
				.requires(hasPermission(getTargetPermission()))
				.executes(wrapCommand(this::system))
				.then(literal("world")
						.requires(hasPermission("solong.system.details"))
						.then(argument("worldName", StringArgumentType.greedyString())
								.suggests(wrapSuggestions(SuggestionsSupplier.fromCollectionsSupplier(this::getWorldList)))
								.executes(wrapCommand(this::world))
						)
				)
				.then(literal("bar")
						.executes(wrapCommand(this::bar))
				)
				.then(literal("gc")
						.requires(hasPermission("solong.system.details"))
						.executes(wrapCommand(this::gc))
				)
				.then(literal("save-gc")
						.requires(hasPermission("solong.system.details"))
						.executes(wrapCommand(this::saveGc))
				);
	}
	
	private int system(CommandContext<BukkitBrigadierCommandSource> context) {
		CommandSender sender = getCommandSender(context);
		
		sender.sendMessage(chat()
				.console(!(sender instanceof Player))
				.thenCenterText(text("Information sur les performances")));
		// mémoire
		long maxMem = Runtime.getRuntime().maxMemory();
		long allocMem = Runtime.getRuntime().totalMemory();
		long freeMem = Runtime.getRuntime().freeMemory();
		sender.sendMessage(infoText("Mémoire : Util:" + MemoryUtil.humanReadableSize(allocMem - freeMem)
				+ "/" + MemoryUtil.humanReadableSize(maxMem)
				+ " Allouée:" + MemoryUtil.humanReadableSize(allocMem)));
		double[] values_bar = new double[2];
		values_bar[0] = allocMem - freeMem;
		values_bar[1] = freeMem;
		TextColor[] colors_bar = new TextColor[2];
		colors_bar[0] = PandaTheme.CHAT_FAILURE_COLOR;
		colors_bar[1] = PandaTheme.CHAT_GREEN_1_NORMAL;
		sender.sendMessage(ChatUtil.progressBar(values_bar, colors_bar, maxMem,
				((sender instanceof Player) ? 310 : 43), !(sender instanceof Player)));

		// tps
		float tps1s = PerformanceAnalysisManager.getInstance().getTPS(1_000);
		float tps10s = PerformanceAnalysisManager.getInstance().getTPS(10_000);
		float tps1m = PerformanceAnalysisManager.getInstance().getTPS(60_000);
		
		String tps1sDisp = Float.isNaN(tps1s) ? "N/A" : (Math.round(tps1s)) + "";
		String tps10sDisp = Float.isNaN(tps10s) ? "N/A" : (Math.round(tps10s * 10) / 10D) + "";
		String tps1mDisp = Float.isNaN(tps1m) ? "N/A" : (Math.round(tps1m * 10) / 10D) + "";
		
		int[] tpsHistory = PerformanceAnalysisManager.getInstance().getTPSHistory();
		sender.sendMessage(infoText("TPS : ")
				.then(text("1s:"+tps1sDisp+"/20").color(PerformanceAnalysisManager.getInstance().tps1sGradient.pickColorAt(tps1s)))
				.thenText(" - ")
				.then(text("10s:"+tps10sDisp+"/20").color(PerformanceAnalysisManager.getInstance().tps10sGradient.pickColorAt(tps10s)))
				.thenText(" - ")
				.then(text("1m:"+tps1mDisp+"/20").color(PerformanceAnalysisManager.getInstance().tps1mGradient.pickColorAt(tps1m))));
		Chat c = infoText("[");
		for (int i = ((sender instanceof Player) ? 59 : 40); i >= 0; i--) {
			int t = tpsHistory[i];
			c.then(text("|")
					.color(PerformanceAnalysisManager.getInstance().tps1sGradient.pickColorAt(t))
					.bold()
					.hover(text("-" + i + "s : " + t + " TPS"))
			);
		}
		c.thenText("]");
		sender.sendMessage(c);

		// uptime
		long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
		sender.sendMessage(infoText("Uptime : ")
				.thenData(TimeUtil.durationToString(uptime)));
		
		// worlds
		for (World w : plugin.getServer().getWorlds()) {
			sender.sendMessage(getWorldAbstract(sender, w));
		}
		return 1;
	}
	

	private int world(CommandContext<BukkitBrigadierCommandSource> context) {
		CommandSender sender = getCommandSender(context);
		
		String world = context.getArgument("worldName", String.class);
		
		
		World w = plugin.getServer().getWorld(world);
		
		if (w == null) {
			sender.sendMessage(failureText("La map indiquée n’existe pas."));
			return 0;
		}
		
		sender.sendMessage(chat()
				.console(!(sender instanceof Player))
				.thenCenterText(text("Information sur les performances"))
				.thenNewLine()
				.then(getWorldAbstract(sender, w)));

		Map<EntityType, AtomicInteger> entityCount = new HashMap<>();
		for (Entity e : w.getEntities()) {
			entityCount.computeIfAbsent(e.getType(), t -> new AtomicInteger(0)).incrementAndGet();
		}
		
		Chat entityDisplay = chat().infoColor();
		
		entityCount.entrySet().stream()
			.sorted(Comparator.comparingInt(e -> e.getValue().get()))
			.forEachOrdered(val -> {
				String typeS = val.getKey().name().toLowerCase();
				int nbr = val.getValue().get();
				entityDisplay.thenText(typeS + ":").thenData(nbr + " ");
			});
			
		sender.sendMessage(entityDisplay);
		
		
		return 1;
	}
	
	private int bar(CommandContext<BukkitBrigadierCommandSource> context) {
		CommandSender sender = getCommandSender(context);
		Player p = (Player) sender;
		OnlinePlayer op = SoLongPlayerManager.get(p);
		if (PerformanceAnalysisManager.getInstance().barsContainsPlayer(p)) {
			PerformanceAnalysisManager.getInstance().removePlayerToBars(p);
			try {
				op.unsetConfig("system.bar");
			} catch (Exception e) { Log.severe(e); }
			sender.sendMessage(successText("Les barres de statistique ont été retirées."));
		}
		else {
			PerformanceAnalysisManager.getInstance().addPlayerToBars(p);
			try {
				op.setConfig("system.bar", "true");
			} catch (Exception e) { Log.severe(e); }
			sender.sendMessage(successText("Les barres de statistique ont été activées."));
		}
		return 1;
	}
	
	private int gc(CommandContext<BukkitBrigadierCommandSource> context) {
		PerformanceAnalysisManager.gc(getCommandSender(context));
		return 1;
	}
	
	private int saveGc(CommandContext<BukkitBrigadierCommandSource> context) {
		CommandSender sender = getCommandSender(context);
		int delay = 0;
		int interval = 2;
		for (World w : Bukkit.getWorlds()) {
			Bukkit.getScheduler().runTaskLater(plugin, () -> WorldSaveUtil.nmsSaveFlush(w), delay);
			delay += interval;
		}
		Bukkit.getScheduler().runTaskLater(plugin, () -> PerformanceAnalysisManager.gc(sender), delay);
		return 1;
	}
	
	
	
	private Chat getWorldAbstract(CommandSender sender, World w) {
		String name = w.getName();
		int nbChunkLoaded = w.getLoadedChunks().length;
		int nbEntity = w.getEntities().size();
		int nbPlayer = (int) w.getPlayers().stream()
				.filter(Player::isOnline)
				.filter(p -> !(sender instanceof Player pSender) || pSender.canSee(p))
				.count();
		int viewDistance = w.getViewDistance();
		int simDistance = w.getSimulationDistance();
		int sendDistance = w.getSendViewDistance();
		return chat()
				.infoColor()
				.clickCommand("/system world " + name)
				.thenData(name)
				.then(text(" S:")
						.hover(text("S : auto-save (Paper optimized) enabled"))
						.thenData(w.isAutoSave())
				)
				.then(text(" K:")
						.hover(text("K : keep spawn in memory"))
						.thenData(w.getKeepSpawnInMemory())
				)
				.then(text(" Cl:")
						.hover(text("Cl : loaded chunks"))
						.thenData(nbChunkLoaded)
				)
				.then(text(" E:")
						.hover(text("E: entity count"))
						.thenData(nbEntity)
				)
				.then(text(" Vd:")
						.hover(text("Vd: view distance"))
						.thenData(viewDistance)
				)
				.then(text(" Td:")
						.hover(text("Td: simulation (tick) distance"))
						.thenData(simDistance)
				)
				.then(text(" Sd:")
						.hover(text("Sd: send view distance"))
						.thenData(sendDistance)
				)
				.then(text(" P:")
						.hover(text("P: number of players"))
						.thenData(nbPlayer)
				);
	}
	
	
	
	

	private List<String> getWorldList() {
		List<String> worldsStr = new ArrayList<>();

		for (World w : plugin.getServer().getWorlds())
			worldsStr.add(w.getName());

		return worldsStr;
	}

}
