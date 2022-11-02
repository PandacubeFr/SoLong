package fr.pandacube.so_long;

import fr.pandacube.lib.chat.Chat;
import fr.pandacube.lib.chat.ChatConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.Locale;
import java.util.TimeZone;

public class EnvConfig {
	
	public static final Locale LOCALE = Locale.FRANCE;
	
	public static final TimeZone TIMEZONE = TimeZone.getTimeZone("Europe/Paris");
	
	
	
	

	//public static final ChatColor CHAT_GREEN_1_NORMAL = TextColor.fromHexString("#5f9765"); // h=126 s=23 l=48
	
	public static final TextColor CHAT_GREEN_1_NORMAL = TextColor.fromHexString("#3db849"); // h=126 s=50 l=48
	public static final TextColor CHAT_GREEN_2 = TextColor.fromHexString("#5ec969"); // h=126 s=50 l=58
	public static final TextColor CHAT_GREEN_3 = TextColor.fromHexString("#85d68d"); // h=126 s=50 l=68
	public static final TextColor CHAT_GREEN_4 = TextColor.fromHexString("#abe3b0"); // h=126 s=50 l=78

	public static final TextColor CHAT_GREEN_SATMAX = TextColor.fromHexString("#00ff19"); // h=126 s=100 l=50
	public static final TextColor CHAT_GREEN_1_SAT = TextColor.fromHexString("#20d532"); // h=126 s=50 l=48
	public static final TextColor CHAT_GREEN_2_SAT = TextColor.fromHexString("#45e354"); // h=126 s=50 l=58
	public static final TextColor CHAT_GREEN_3_SAT = TextColor.fromHexString("#71ea7d"); // h=126 s=50 l=68
	public static final TextColor CHAT_GREEN_4_SAT = TextColor.fromHexString("#9df0a6"); // h=126 s=50 l=78

	public static final TextColor CHAT_BROWN_1 = TextColor.fromHexString("#b26d3a"); // h=26 s=51 l=46
	public static final TextColor CHAT_BROWN_2 = TextColor.fromHexString("#cd9265"); // h=26 s=51 l=60
	public static final TextColor CHAT_BROWN_3 = TextColor.fromHexString("#e0bb9f"); // h=26 s=51 l=75

	public static final TextColor CHAT_BROWN_1_SAT = TextColor.fromHexString("#b35c19"); // h=26 s=75 l=40
	public static final TextColor CHAT_BROWN_2_SAT = TextColor.fromHexString("#e28136"); // h=26 s=51 l=55
	public static final TextColor CHAT_BROWN_3_SAT = TextColor.fromHexString("#ecab79"); // h=26 s=51 l=70
	
	public static final TextColor CHAT_GRAY_MID = TextColor.fromHexString("#888888");
	
	public static final TextColor CHAT_BROADCAST_COLOR = NamedTextColor.YELLOW;
	
	
	public static final TextColor CHAT_DECORATION_COLOR = CHAT_GREEN_1_NORMAL;
	public static final char CHAT_DECORATION_CHAR = '-';
	public static final TextColor CHAT_URL_COLOR = CHAT_GREEN_1_NORMAL; 
	public static final TextColor CHAT_COMMAND_COLOR = CHAT_GRAY_MID;
	public static final TextColor CHAT_COMMAND_HIGHLIGHTED_COLOR = NamedTextColor.WHITE;
	public static final TextColor CHAT_INFO_COLOR = CHAT_GREEN_4;
	public static final TextColor CHAT_WARNING_COLOR = CHAT_BROWN_2_SAT;
	public static final TextColor CHAT_SUCCESS_COLOR = CHAT_GREEN_SATMAX;
	public static final TextColor CHAT_FAILURE_COLOR = TextColor.fromHexString("#ff3333");
	public static final TextColor CHAT_DATA_COLOR = CHAT_GRAY_MID;


	public static final TextColor CHAT_PM_PREFIX_DECORATION = EnvConfig.CHAT_BROWN_2_SAT;
	public static final TextColor CHAT_PM_SELF_MESSAGE = EnvConfig.CHAT_GREEN_2;
	public static final TextColor CHAT_PM_OTHER_MESSAGE = EnvConfig.CHAT_GREEN_4;
	
	
	public static final TextColor CHAT_DISCORD_LINK_COLOR = TextColor.fromHexString("#00aff4");
	

	public static Chat CHAT_MESSAGE_PREFIX() {
		return Chat.text("[")
			.broadcastColor()
			.thenDecoration("Serveur")
			.thenText("] ");
	}
	
	
	
	
	/**
	 * Number of decoration character to put between the text and the border of
	 * the line for left and right aligned text.
	 */
	public static final int CHAT_NB_CHAR_MARGIN = 1;
	
	
	
	
	
	public static void init() {
		Locale.setDefault(LOCALE);
		TimeZone.setDefault(TIMEZONE);
		ChatConfig.decorationColor = CHAT_DECORATION_COLOR;
		ChatConfig.decorationChar = CHAT_DECORATION_CHAR;
		ChatConfig.nbCharMargin = CHAT_NB_CHAR_MARGIN;
		ChatConfig.dataColor = CHAT_DATA_COLOR;
		ChatConfig.successColor = CHAT_SUCCESS_COLOR;
		ChatConfig.failureColor = CHAT_FAILURE_COLOR;
		ChatConfig.infoColor = CHAT_INFO_COLOR;
		ChatConfig.warningColor = CHAT_WARNING_COLOR;
		ChatConfig.urlColor = CHAT_URL_COLOR;
		ChatConfig.commandColor = CHAT_COMMAND_COLOR;
		ChatConfig.highlightedCommandColor = CHAT_COMMAND_HIGHLIGHTED_COLOR;
		ChatConfig.broadcastColor = CHAT_BROADCAST_COLOR;
		ChatConfig.prefix = EnvConfig::CHAT_MESSAGE_PREFIX;
		
	}
	
	


	
	
	
	
	
	static {
		// initialize class to avoid NCDFE when updating the plugin
		@SuppressWarnings({ "unused" })
		Class<?>
		c4 = fr.pandacube.lib.chat.ChatUtil.class;
	}

}
