import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import org.pircbotx.PircBotX;
import org.pircbotx.Colors;
import java.io.IOException;
import java.util.Properties;
import java.util.Arrays;

public class TS3PresenceEngine implements Runnable {

	private HashMap<String,String> presenceState;
	private PircBotX bot;
	private String channel;
	private boolean onSwitch;
	private long interval;
	private long backoff;
	private int timeout;
	private boolean squelch;
	private Properties props;
	private ArrayList<String> ignoreList;
	
	public TS3PresenceEngine(PircBotX bot, String channel, Properties props) {
		presenceState = new HashMap<String,String>();
		this.bot = bot;
		this.channel = channel;
		this.props = props;
		onSwitch = false;
		squelch = true;
		interval = 60000L;
		backoff = 0L;
		timeout = 10000;
		initIgnores();
	}

	
	public HashMap<String,String> getPresenceState() {
		return presenceState;
	}
	
	public ArrayList<String> getIgnoreList() {
		return ignoreList;
	}
	
	public void purgeIgnoreList() {
		ignoreList = new ArrayList<String>();
	}
	
	private void initIgnores() {
		String rawIgnores = props.getProperty("ignore_list");
		if (rawIgnores != null && !rawIgnores.isEmpty()) {
			List<String> temptlist  = Arrays.asList(rawIgnores.split("\\s*,\\s*"));
			ignoreList = new ArrayList<String>(temptlist);
		} else {
			ignoreList = new ArrayList<String>();
		}
	}

	public List<String> getIgnores() {
		return ignoreList;
	}
	
	public void setInterval(long set) {
		interval = set;
	}

	public void setTimeout(int set) {
		timeout = set;
	}
	
	public int getTimeout() {
		return timeout;
	}

	public void turnOn() {
		onSwitch = true;
	}

	public void squelchOn() {
		squelch = true;
	}

	public void squelchOff() {
		squelch = false;
	}

	public void turnOff() {
		onSwitch = false;
	}

	public Boolean isOn() {
		return onSwitch;
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(interval+backoff);
				if (onSwitch) {
					
					HashMap<String,String> hm = TS3PresenceChecker.getPresence(timeout, props);	
					
					//deal with people we already know
					for (String user : presenceState.keySet()) {
						if (!hm.containsKey(user) && !ignoreList.contains(user)) {
							//notify user has quit TS3
							bot.sendMessage(channel, "["+user+"]"+" has "+Colors.RED+"QUIT"+Colors.NORMAL+" Teamspeak.");
						} else {
							String oldChannel = presenceState.get(user);
							String currentChannel = hm.get(user);
							if (!oldChannel.equals(currentChannel) && !ignoreList.contains(user)) {
								//say that user has moved to new channel
								bot.sendMessage(channel, "["+user+"]"+" has "+Colors.CYAN+"MOVED"+Colors.NORMAL+" to Channel " +currentChannel+".");
							}
						}
					}
					//deal with new people
					for (String user : hm.keySet()) {
						if (!presenceState.containsKey(user) && !ignoreList.contains(user)) {
							//new user has joined
							bot.sendMessage(channel, "["+user+"]"+" has "+Colors.GREEN+"JOINED"+Colors.NORMAL+" Teamspeak in channel "+hm.get(user)+".");
						}
					}
					presenceState = hm;
						
				}

			} catch (InterruptedException e) {
				bot.sendMessage(channel, "Interval timer interrupted - resetting backoff and restarting clock.");
			} catch (IOException e) {
				if (backoff==0L) {
					backoff = 300000L;
					bot.sendMessage(channel, "TS3 just choked over an update check - sorry!  Backing off a bit.");
					System.out.println("API Failure - backoff is now "+backoff);
				} else if (backoff > 3600000L) {
					bot.sendMessage(channel, "Enough API calls have failed that I'm shutting down the presence engine.  Please contact my owner.");
					System.out.println("API Failure");
					System.out.println("Shutting down presence and resetting timeouts.");
					this.turnOff();
					backoff=0L;
				} else {
					backoff = backoff*2;
					bot.sendMessage(channel, "TS3 choked again.  Backing off further.");
					System.out.println("API Failure - backoff is now "+backoff);
				}
			}
		}
	}

	
	
}
