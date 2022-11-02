package fr.pandacube.so_long.commands;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import fr.pandacube.lib.chat.ChatColorUtil;
import fr.pandacube.lib.chat.ChatConfig;

import static fr.pandacube.lib.chat.ChatStatic.chat;
import static fr.pandacube.lib.chat.ChatStatic.legacyText;

public class CommandBroadcast extends BrigadierCommand {
	
	@Override
	protected LiteralArgumentBuilder<BukkitBrigadierCommandSource> buildCommand() {
		return literal("broadcast")
				.requires(hasPermission(getTargetPermission()))
				.then(argument("message", StringArgumentType.greedyString())
						.executes(wrapCommand(this::bc))
				);
	}
	
	@Override
	protected String getTargetPermission() {
		return "solong.broadcast";
	}
	
	@Override
	protected String[] getAliases() {
		return new String[] { "bc", "bcast", "shout" };
	}

	
	
	private int bc(CommandContext<BukkitBrigadierCommandSource> context) {
		plugin.getServer().broadcast(
				chat()
						.then(ChatConfig.prefix.get())
						.then(legacyText(ChatColorUtil.translateAlternateColorCodes('&', context.getArgument("message", String.class)))
								.broadcastColor()
						)
						.asComponent()
		);
		return 1;
	}

}
