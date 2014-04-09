import org.pircbotx.PircBotX;
import org.pircbotx.Colors;
import org.pircbotx.User;
import java.io.IOException;
import java.util.Properties;
import java.util.Calendar;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.List;

public class TonightEngine implements Runnable {

	private PircBotX bot;
	private String channel;
	private boolean onSwitch;
	private Properties props;
	private HashMap<String, ArrayList<User>> tonightState;
	private int flushHour;

	public TonightEngine(PircBotX bot, Properties props) {
		this.bot = bot;
		this.props = props;
		this.channel = props.getProperty("irc_channel");
		onSwitch = true;
		tonightState = new HashMap<String, ArrayList<User>>();
		flushHour = 12;
		initTitles();
	}
	
	private void initTitles() {
		String rawTitles = props.getProperty("title_list");
		if (rawTitles != null && !rawTitles.isEmpty()) {
			List<String> templist  = Arrays.asList(rawTitles.split("\\s*,\\s*"));
			for (String s : templist) {
				registerTitle(s);
			}
		}
	}
	
	public Boolean isTitleRegistered(String title) {
		if (tonightState.containsKey(title)) {
			return true;
		} else {
			return false;
		}
	}

	public Boolean registerTitle(String title) {
		if (!tonightState.containsKey(title)) {
			ArrayList<User> users = new ArrayList<User>();
			tonightState.put(title, users);
			return true;
		} else {
			return false;
		}
	}

	public Boolean deregisterTitle(String title) {
		if (tonightState.containsKey(title)) {
			tonightState.remove(title);
			return true;
		} else {
			return false;
		}
	}
	
	public Boolean addUserToTitle(User user, String title){
		if (tonightState.containsKey(title)) {
			if (!tonightState.get(title).contains(user)) {
				tonightState.get(title).add(user);
				return true;
			}
			return false;
		} else {
			return false;
		}
	}
	
	public void addUserToAll(User user) {
		for (String title : tonightState.keySet()) {
			ArrayList<User> users = tonightState.get(title);
			if (!users.contains(user)) {
				users.add(user);
			}
		}
	}

	public Boolean removeUserFromTitle(User user, String title){
		if (tonightState.containsKey(title)) {
			if (tonightState.get(title).contains(user)){
				tonightState.get(title).remove(user);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public void removeUserFromAll(User user){
		for (String title : tonightState.keySet()) {
			ArrayList<User> users = tonightState.get(title);
			users.remove(user);
		}
	}
	
	public Set<String> getRegisteredTitles() {
		return tonightState.keySet();
	}

	public ArrayList<User> getInUsers(String title) {
		return tonightState.get(title);
	}

	public void flushTonight() {
		for (String title : tonightState.keySet()) {
			tonightState.put(title, new ArrayList<User>());
		}
	}		
	
	public void purgeTonight() {
		tonightState = new HashMap<String, ArrayList<User>>();
	}
	
	public void setFlushHour(int s) {
		flushHour = s;
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
					now = Calendar.getInstance();
					int nowHours = now.get(Calendar.HOUR_OF_DAY);
					int nowMinutes = now.get(Calendar.MINUTE);
					if (nowHours == flushHour && nowMinutes == 0) {
						flushTonight();
						bot.sendMessage(channel, "A new day dawns!");
					}

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
