package fr.pandacube.so_long.modules;

import com.destroystokyo.paper.ClientOption;
import com.destroystokyo.paper.ClientOption.ChatVisibility;
import fr.pandacube.lib.chat.Chat;
import fr.pandacube.lib.chat.ChatConfig;
import fr.pandacube.lib.paper.util.BukkitEvent.EnforcedLastListener;
import fr.pandacube.lib.util.RandomUtil;
import fr.pandacube.so_long.SoLong;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static fr.pandacube.lib.chat.ChatStatic.chat;
import static fr.pandacube.lib.chat.ChatStatic.chatComponent;
import static fr.pandacube.lib.chat.ChatStatic.text;

public class PlayerChatManager implements Listener {

	public PlayerChatManager() {
		Bukkit.getPluginManager().registerEvents(this, SoLong.getPlugin());
		
		new ChatMessageDispatcher(); // special listener
	}
	
	/*
	 * List of chat event, in the execution order:   async?  adventure? deprecated?
	 * org.bukkit.event.player.AsyncPlayerChatEvent  true    false      true
	 * org.bukkit.event.player.PlayerChatEvent       false   false      true
	 * io.papermc.paper.event.player.AsyncChatEvent  true    true       false
	 * io.papermc.paper.event.player.ChatEvent       false   true       true
	 */
	
	/*
	 * Our chat tools with old chat event API:
	 * - Format ("<name> message" with colored <>, color codes in message)
	 *     before everyone in case some plugin want to add some infos (multiverse?)
	 * - Afk (update afk status)
	 *     no modification made to event, so don’t care about priority
	 *     don’t ignore cancelled
	 * - Moderation (hide some illegal stuff from message, can cancel event)
	 *     can ignore if already cancelled
	 *     high priority (so after non-monitor other plugin is fine)
	 * - Ignore (modify the recipient list in the event)
	 *     can ignore if already cancelled
	 *     high priority (so after non-monitor other plugin is fine)
	 * - Notification (ping user mentioned in a message)
	 *     can ignore if already cancelled
	 *     high priority (so after non-monitor other plugin is fine)
	 * - Dispatch (proper use of Player#sendMessage according to ignore capability of receivers)
	 *     must absolutely be the last event listener !
	 *     can ignore cancelled
	 */
	
	/*
	 * Solution with new paper chat API:
	 * - Bukkit AsyncPlayerChatEvent (lowest):
	 *     - Format (we don’t need fancy component format yet + needs to be before other plugins)
	 * - Paper AsyncChatEvent (highest):
	 *     - Afk
	 *     - Ignore
	 *     - Notification
	 *     - Dispatch
	 * - Paper AsyncChatEvent (monitor, must be very last):
	 */
	
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOWEST)
	public void onLegacyChat(org.bukkit.event.player.AsyncPlayerChatEvent event) {
		// must be done with old chat event API so other plugins also using old API can use our format as a base (multiverse prefix)
		event.setFormat(ChatColor.GRAY + "<" + ChatColor.RESET + "%s" + ChatColor.GRAY + ">" + ChatColor.RESET + " %s");
	}
	
	
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChat(AsyncChatEvent event) {
		handleNotification(event);

		handleEaserEggResponses(event);
	}


	public static class ChatMessageDispatcher extends EnforcedLastListener<AsyncChatEvent> {

		public ChatMessageDispatcher() {
			super(AsyncChatEvent.class, true);
		}

		@SuppressWarnings("OverrideOnly")
		@Override
		public void onEvent(AsyncChatEvent event) {
			Player sender = event.getPlayer();

			Component displayName = sender.displayName();
			Component message = event.message();
			ChatRenderer renderer = event.renderer();


			for (Audience audience : event.viewers()) {
				audience.sendMessage(renderer.render(sender, displayName, message, audience)); // send messages as regular system message
			}

			event.setCancelled(true);
		}
	}
	
	
	

	
	
	
	
	
	
	
	

	private void handleNotification(AsyncChatEvent event) {
		List<Player> onlineIdentified = getPlayersReferencedInMessage(chatComponent(event.message()).getPlainText());
		
		ChatRenderer previousRenderer = event.renderer();
		
		event.renderer((source, displayName, message, audience) -> {

			if ((audience instanceof Player player && source.equals(player)) || audience instanceof ConsoleCommandSender) {
				for (Player idP : onlineIdentified) {
					message = formatPlayerNameInMessage(message, idP.getName());
				}
			}
			else if (audience instanceof Player player && onlineIdentified.contains(player)) {
				message = formatPlayerNameInMessage(message, player.getName());
				if (player.getClientOption(ClientOption.CHAT_VISIBILITY) == ChatVisibility.FULL)
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.MASTER, 0.5f, 2);
			}

			@SuppressWarnings("OverrideOnly")
			Component c = previousRenderer // previous renderer contains format from older event handler (Multiverse world prefix, Pandacube format)
					// Warning : renderer from older event will convert back the message and displayname to legacytext then to component again
					// see io.papermc.paper.adventure.ChatProcessor#legacyRenderer(String)
					.render(source, displayName, message, audience);
			return c;
		});
	}
	
	

	private static Pattern generateWordPatternInSentense(String playerName) {
		return Pattern.compile("(?i)(\\P{L}|^)("+playerName+")(\\P{L}|$)");
	}
	private static final String PLAYER_NAME_PATTERN_STR = "[0-9A-Za-z_.]{3,16}";
	private static final Pattern PLAYER_NAME_PATTERN_IN_SENTENSE = generateWordPatternInSentense(PLAYER_NAME_PATTERN_STR);
	
	private static Set<String> getAllValidPlayerNamesWord(String message) {
		Matcher m = PLAYER_NAME_PATTERN_IN_SENTENSE.matcher(message);
		Set<String> validPlayerNames = new LinkedHashSet<>();
		int start = 0;
		while (m.find(start)) {
			validPlayerNames.add(m.group(2));
			start = m.end(2);
		}
		return validPlayerNames;
	}
	
	private static List<Player> getPlayersReferencedInMessage(String message) {
		Set<String> validPlayerNamesInMessage = getAllValidPlayerNamesWord(message).stream()
				.map(String::toLowerCase)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		
		return Bukkit.getOnlinePlayers().stream()
				.filter(p -> validPlayerNamesInMessage.contains(p.getName().toLowerCase()))
				.collect(Collectors.toList());
	}
	
	private static Component formatPlayerNameInMessage(Component message, String playerName) {
		return message.replaceText(TextReplacementConfig.builder()
				.match(generateWordPatternInSentense(playerName))
				.replacement((mr, builder) -> builder.color(TextColor.color(192, 255, 255)))
				.build());
	}




	private static final String WORD_PATTERN_STR = "[0-9A-Za-zàéèêïâ_.-]{3,16}";
	private static final Pattern WORD_PATTERN_IN_SENTENSE = generateWordPatternInSentense(WORD_PATTERN_STR);

	private static List<String> getAllValidWords(String message) {
		Matcher m = WORD_PATTERN_IN_SENTENSE.matcher(message);
		List<String> validWords = new ArrayList<>();
		int start = 0;
		while (m.find(start)) {
			validWords.add(m.group(2).toLowerCase());
			start = m.end(2);
		}
		return validWords;
	}








	private long lastEaserEgg = 0;

	private static final long MIN_INTERVAL_BETWEEN_EASER_EGGS = 8_000;

	private void handleEaserEggResponses(AsyncChatEvent event) {
		if (lastEaserEgg > System.currentTimeMillis() - MIN_INTERVAL_BETWEEN_EASER_EGGS) {
			return; // Too soon! Too soon!
		}
		String message = chatComponent(event.message()).getPlainText();

		List<String> words = getAllValidWords(message);

		if (handleQuoi(words)
				|| handleLuigi(words)) {
			lastEaserEgg = System.currentTimeMillis();
		}
	}


	private boolean handleQuoi(List<String> words) {
		int i;
		String response = null;
		if ((i = words.lastIndexOf("quoi")) >= 0) {
			String prev = i > 0 ? words.get(i-1) : null;
			if (prev != null
					&& (prev.equals("pour")
					|| prev.equals("en")
					|| prev.equals("sur")
					|| prev.equals("et")
					|| prev.equals("de")
					|| prev.equals("puis")
					|| prev.equals("c'est")
					|| prev.equals("avec"))) {
				response = Character.toUpperCase(prev.charAt(0)) + prev.substring(1) + " feur";
			}
			else
				response = "Feur";
		}
		else if (words.contains("pourquoi")) {
			response = "Pourfeur";
		}
		else if (words.contains("quoient")) {
			response = "feurent";
		}

		if (response != null) {
			float r = RandomUtil.rand.nextFloat();
			if (r < 1/3d) {
				response += " !";
			}
			else if (r < 2/3d) {
				response += ".";
			}
			r = RandomUtil.rand.nextFloat();
			if (r < .25) {
				response = response.toUpperCase();
			}
		}

		if (response != null
				&& willResponseOccurs(1, 0.9f, 86_400_000, new GregorianCalendar(2022, Calendar.NOVEMBER, 5).getTimeInMillis())) {
			Chat responseComp = text(response);
			runLater(() -> bc(responseComp), RandomUtil.nextIntBetween(10, 30));
			return true;
		}
		return false;
	}







	private boolean handleLuigi(List<String> words) {
		int i;
		boolean response = false;
		if (words.contains("luigi") || words.contains("mario")) {
			response = true;
		}


		if (response
				&& willResponseOccurs(1, 1, 86_400_000, new GregorianCalendar(2022, Calendar.NOVEMBER, 1).getTimeInMillis())) {
			runLater(() -> {
				bc(text("L"));
				runLater(() -> {
					bc(text("U"));
					runLater(() -> {
						bc(text("I"));
						runLater(() -> {
							bc(text("G"));
							runLater(() -> {
								bc(text("I"));
								runLater(() -> {
									bc(text("MON NOM C'EST L-U-I-G-I !"));
								}, RandomUtil.nextIntBetween(20, 30));
							}, RandomUtil.nextIntBetween(10, 15));
						}, RandomUtil.nextIntBetween(10, 15));
					}, RandomUtil.nextIntBetween(10, 15));
				}, RandomUtil.nextIntBetween(10, 15));
			}, RandomUtil.nextIntBetween(10, 30));
			return true;
		}
		return false;
	}








	private boolean handlePanneau(List<String> words) {
		int i;
		boolean response = false;
		if (words.contains("panneau")
				|| words.contains("panneaux")) {
			response = true;
		}


		if (response
				&& willResponseOccurs(1, 1, 86_400_000, new GregorianCalendar(2022, Calendar.NOVEMBER, 1).getTimeInMillis())) {
			runLater(() -> bc(text("Y a pas de panneau !")),
					RandomUtil.nextIntBetween(10, 30));
			return true;
		}
		return false;
	}








	private boolean handleLord(List<String> words) {
		int i;
		boolean response = false;
		if (words.contains("lord")
				|| words.contains("sealand")) {
			response = true;
		}


		if (response && willResponseOccurs(1, 1, 86_400_000, new GregorianCalendar(2022, Calendar.NOVEMBER, 1).getTimeInMillis())) {
			runLater(() -> bc(text("Worldwide Lord")),
					RandomUtil.nextIntBetween(10, 30));
			return true;
		}
		return false;
	}





	private boolean willResponseOccurs(float initialProba, float reason, long timePerReason, long initialTime) {
		// probability based on elapsed time since writing those lines,
		// to reduce appearance of this easer-egg (due to players being more and more annoyed)
		float rank = Math.max(System.currentTimeMillis() - initialTime, 0) / (float) timePerReason;
		float currentProba = initialProba * (float) Math.pow(reason, rank);
		return RandomUtil.rand.nextFloat() < currentProba;
	}


	private void runLater(Runnable r, long delay) {
		Bukkit.getScheduler().runTaskLater(SoLong.getPlugin(), r, delay);
	}

	private void bc(ComponentLike message) {
		SoLong.getPlugin().getServer().broadcast(
				chat()
						.then(ChatConfig.prefix.get())
						.then(chatComponent(message)
								.broadcastColor()
						)
						.asComponent()
		);
	}
	
	

	
	
	
	
}
