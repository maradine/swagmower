import org.pircbotx.PircBotX;
import org.pircbotx.Colors;
import org.pircbotx.User;
import java.io.IOException;
import java.util.Properties;
import java.util.Calendar;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.Set;
import java.util.List;

public class StarsEngine implements Runnable {

	private PircBotX bot;
	private String channel;
	private Boolean onSwitch;
	private Properties props;
	private String appKey;
	private String appSecret;
	private String accessToken;
	private String gamePath;
	private String gamePrefix;
	private ArrayList<Integer> aiPlayers;
	private HashMap<Integer, User> playerMap;

	public StarsEngine(PircBotX bot, Properties props) {
		this.bot = bot;
		this.props = props;
		this.channel = props.getProperty("irc_channel");
		aiPlayers = new ArrayList<Integer>();
		playerMap = new HashMap<Integer, User>();
		onSwitch = init();

	}

	public Boolean init() {
		appKey = props.getProperty("dropbox_app_key");;
		appSecret = props.getProperty("dropbox_app_secret");
		accessToken = props.getProperty("dropbox_access_token");
		gamePath = props.getProperty("stars_game_path");
		gamePrefix = props.getProperty("stars_game_prefix");
		if (appKey == null || appSecret == null || accessToken == null || appKey.equals("") || appSecret.equals("") || accessToken.equals("")) {
			return false;
		} else if (gamePath == null || gamePrefix == null || gamePath.equals("") || gamePrefix.equals("")) {
			return false;
		} else {
			return true;
		}
	}
	
	public void mapPlayer(Integer i, User u) {
		playerMap.put(i,u);
	}

	public Boolean unmapPlayer(Integer i) {
		if (playerMap.remove(i) != null) {
			return true;
		} else {
			return false;
		}
	}

	public void purgePlayerMap() {
		playerMap = new HashMap<Integer, User>();
	}

	public HashMap<Integer, User> getPlayerMap() {
		return playerMap;
	}

	public void addAiPlayer(Integer add) {
		aiPlayers.add(add);
	}

	public Boolean removeAiPlayer(Integer remove) {
		return aiPlayers.remove(remove);
	}
	
	public ArrayList<Integer> getAiPlayers() {
		return aiPlayers;
	}

	public void purgeAiPlayers() {
		aiPlayers = new ArrayList<Integer>();
	}

	public String getAppKey() {
		return this.appKey;
	}

	public String getAppSecret() {
		return this.appSecret;
	}

	public String getAccessToken() {
		return this.accessToken;
	}

	public String getGamePath() {
		return this.gamePath;
	}

	public String getGamePrefix() {
		return this.gamePrefix;
	}

	public void turnOn() {
		onSwitch = true;
	}

	public void turnOff() {
		onSwitch = false;
	}

	public void run() {
		while (true) {
			try {
				//bot.sendMessage(channel, "Entering run() block.");
				Calendar now;
				
				//are we even turned on?
				if (onSwitch) {
					//what time is it?
					/*
					now = Calendar.getInstance();
					int nowHours = now.get(Calendar.HOUR_OF_DAY);
					int nowMinutes = now.get(Calendar.MINUTE);
					if (nowHours == flushHour && nowMinutes == 0) {
						//flushTonight();
						bot.sendMessage(channel, "A new day dawns!");
					}
					*/

				} else {
					//bot.sendMessage(channel, "Turned off, doing nothing.");
				}					
				
				//caluclate time to next minute
				now = Calendar.getInstance();
				int millis = 1000 - now.get(Calendar.MILLISECOND);
				int seconds = 59 - now.get(Calendar.SECOND);
				int sleepMillis = millis + (1000 * seconds);

				//sleep
				//bot.sendMessage(channel, "Sleeping for "+sleepMillis+" millis.");
				Thread.sleep(sleepMillis);


			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
				bot.sendMessage(channel, "Totally unhandled exception - check the live feed, sparky.");
			}
		}
	}
}
