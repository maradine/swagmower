import org.pircbotx.PircBotX;
import org.pircbotx.Colors;
import org.pircbotx.User;
import java.io.IOException;
import java.util.Properties;
import java.util.Calendar;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.Set;
import java.util.List;
import com.dropbox.core.DbxException.RetryLater;

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
	private HashMap<Integer, String> playerMap;
	private StarsGameState state;
	private Boolean firstRun;

	private String SNSARN;
	private String AWSAccessKey;
	private String AWSSecretKey;
	private Boolean SNSOnSwitch;

	public StarsEngine(PircBotX bot, Properties props) {
		this.bot = bot;
		this.props = props;
		this.channel = props.getProperty("irc_channel");
		aiPlayers = new ArrayList<Integer>();
		playerMap = new HashMap<Integer, String>();
		state = new StarsGameState();
		firstRun = true;
		onSwitch = init();
		SNSOnSwitch = initSNS();

	}

	public Boolean init() {
		onSwitch = false;
		state = new StarsGameState();
		appKey = props.getProperty("dropbox_app_key");;
		appSecret = props.getProperty("dropbox_app_secret");
		accessToken = props.getProperty("dropbox_access_token");
		gamePath = props.getProperty("stars_game_path");
		gamePrefix = props.getProperty("stars_game_prefix");
		initAi();
		initMap();
		if (appKey == null || appSecret == null || accessToken == null || appKey.equals("") || appSecret.equals("") || accessToken.equals("")) {
			return false;
		} else if (gamePath == null || gamePrefix == null || gamePath.equals("") || gamePrefix.equals("")) {
			return false;
		} else {
			return true;
		}

	}
	
	private void initAi() {
		String rawAi = props.getProperty("stars_ai_list");
		if (rawAi != null && !rawAi.isEmpty()) {
			List<String> templist  = Arrays.asList(rawAi.split("\\s*,\\s*"));
			for (String s : templist) {
				addAiPlayer(new Integer(s));
			}
		}
	}

	private void initMap() {
		String rawMap = props.getProperty("stars_player_map");
		if (rawMap != null && !rawMap.isEmpty()) {
			List<String> templist  = Arrays.asList(rawMap.split("\\s*,\\s*"));
			//Should now have a list of colon separated pairs
			for (String s : templist) {
				String[] bi = s.split(":");
				mapPlayer(Integer.parseInt(bi[0]),bi[1]);
			}
		}
	}


	private Boolean initSNS() {
		AWSAccessKey = props.getProperty("aws_access_key");;
		AWSSecretKey = props.getProperty("aws_secret_key");
		SNSARN = props.getProperty("sns_arn");
		if (AWSAccessKey == null || AWSSecretKey == null || SNSARN == null || AWSAccessKey.equals("") || AWSSecretKey.equals("") || SNSARN.equals("")) {
			return false;
		} else {
			return true;
		}
	}

	public void mapPlayer(Integer i, String s) {
		playerMap.put(i,s);
	}

	public Boolean unmapPlayer(Integer i) {
		if (playerMap.remove(i) != null) {
			return true;
		} else {
			return false;
		}
	}

	public void purgePlayerMap() {
		playerMap = new HashMap<Integer, String>();
	}

	public HashMap<Integer, String> getPlayerMap() {
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

	public void SNSSubscribe(String s) {
	    SNSHelper.subscribe(AWSAccessKey, AWSSecretKey, SNSARN, s);
	}

	public void run() {
		while (true) {
			
			System.out.println("StarsEngine: entering run block");
			Calendar now;
			try {
				//bot.sendMessage(channel, "Entering run() block.");
				
				//are we even turned on?
				if (onSwitch) {
					System.out.println("StarsEngine: turned on");
					StarsGameState newState = DropboxHelper.getStarsGameState(appKey, appSecret, accessToken, gamePath, gamePrefix);
					
					if (!firstRun) {
						System.out.println("StarsEngine: not first run, processing.");
						PriorityQueue<Integer> flipped = new PriorityQueue<Integer>();
						for (Integer i : state.getState().keySet()) {
							System.out.println("StarsEngine: processing state math for int "+i);
							if (!state.getState().get(i) && newState.getState().get(i)) {
								flipped.add(i);
								System.out.println("StarsEngine: "+i+"was flipped to true, adding to list");
							}
						}
						
						if (newState.getYear() > state.getYear()) {
							//if the year has flipped, show that - most important.
							System.out.println("StarsEngine: year has advanced.");

							bot.sendMessage(channel, "The year is now "+(newState.getYear()+2400)+".  Your turn is ready.");
							if (SNSOnSwitch) {
							    SNSHelper.publish(AWSAccessKey, AWSSecretKey, SNSARN, "The year is now "+(newState.getYear()+2400)+".  Your turn is ready.");
							}
						} else if (flipped.size() > 0) {
							System.out.println("StarsEngine: flipped size greater than 0");
							//if a player has turned in, present.
							String notice;
							for (Integer i : flipped) {
								notice ="";
								if (playerMap.containsKey(i)) {
									System.out.println("StarsEngine: have a name mapping for player "+i);
									String name = playerMap.get(i);
									bot.sendMessage(channel, name+" has submitted a turn.");
								} else {
									System.out.println("StarsEngine: no name mapping for player "+i);
									bot.sendMessage(channel, "Player "+i+" has submitted a turn.");
								}
							}
						}
					
					}
					state = newState;
					firstRun = false;
					
				} else {
					//bot.sendMessage(channel, "Turned off, doing nothing.");
				}					

			} catch (RetryLater dbe) {
				bot.sendMessage(channel, "Dropbox told us to simmer down - taking 10.");
				try {Thread.sleep(600000);} catch (Exception e) {System.exit(1);}
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
				bot.sendMessage(channel, "Totally unhandled exception - check the live feed, sparky.");
			} finally {
				//caluclate time to next minute
				System.out.println("StarsEngine: setting up next run");

				now = Calendar.getInstance();
				int millis = 1000 - now.get(Calendar.MILLISECOND);
				int seconds = 59 - now.get(Calendar.SECOND);
				int sleepMillis = millis + (1000 * seconds);

				//sleep
				//bot.sendMessage(channel, "Sleeping for "+sleepMillis+" millis.");
				try {Thread.sleep(sleepMillis);} catch (Exception e) {System.exit(1);}
			}
		}
	}
}
