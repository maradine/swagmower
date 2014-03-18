import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;
import java.util.Properties;
import java.util.Arrays;
import java.util.logging.Level;

import org.pircbotx.PircBotX;
import org.pircbotx.Colors;
import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;
import com.github.theholywaffle.teamspeak3.api.event.TS3EventType;

public class TS3PresenceEngine {

	private HashMap<String,PresenceState> presenceState;
	private PircBotX bot;
	private String channel;
	private boolean onSwitch;
	private boolean squelch;
	private Properties props;
	private ArrayList<String> ignoreList;
	private TS3Query query;
	private TS3Api api;
	
	public TS3PresenceEngine(PircBotX bot, String channel, Properties props) {
		presenceState = new HashMap<String,PresenceState>();
		this.bot = bot;
		this.channel = channel;
		this.props = props;
		onSwitch = false;
		squelch = true;
		query = null;
		api = null;
		initIgnores();
	}

	
	public HashMap<String,PresenceState> getPresenceState() {
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

	public void turnOn() {
		this.connect();
		onSwitch = true;
	}

	public void squelchOn() {
		squelch = true;
	}

	public void squelchOff() {
		squelch = false;
	}

	public void turnOff() {
		this.disconnect();
		onSwitch = false;
	}

	public Boolean isOn() {
		this.connect();
		return onSwitch;
	}

	public void clientJoin(int clientID) {
		ClientInfo ci = this.api.getClientInfo(clientID);
		String nickname = ci.getNickname();
		String newChannel = this.api.getChannelInfo(ci.getChannelId()).getName();
		presenceState.put(nickname, new PresenceState(clientID, nickname, newChannel));
		if (!ignoreList.contains(nickname)) {
			bot.sendMessage(channel, "["+nickname+"] has "+Colors.GREEN+"JOINED"+Colors.NORMAL+" to Channel "+newChannel+".");
		}
	}

	public void clientMoved(int clientID) {
		ClientInfo ci = this.api.getClientInfo(clientID);
		String nickname = ci.getNickname();
		String newChannel = this.api.getChannelInfo(ci.getChannelId()).getName();
		if (presenceState.containsKey(nickname)) {
			String oldChannel = presenceState.get(nickname).channel;
			if (newChannel.equals(oldChannel)) {
				return;
			}
		}
		presenceState.put(nickname, new PresenceState(clientID, nickname, newChannel));
		if (!ignoreList.contains(nickname)) {
			bot.sendMessage(channel, "["+nickname+"] has "+Colors.CYAN+"MOVED"+Colors.NORMAL+" to Channel "+newChannel+".");
		}
	}

	public void clientLeft(int clientID) {
		String nickname = "";
		for (PresenceState ps : presenceState.values()) {
			if (ps.clientID == clientID) {
				nickname = ps.nickname;
			}
		}
		presenceState.remove(nickname);
		if (!ignoreList.contains(nickname)) {
			bot.sendMessage(channel, "["+nickname+"] has "+Colors.RED+"QUIT"+Colors.NORMAL+" Teamspeak.");
		}
	}

	public void connect() {
		String ts3User = props.getProperty("ts3_user");
		String ts3Pass = props.getProperty("ts3_pass");
		String ts3Server = props.getProperty("ts3_server");
		String botNick = props.getProperty("botnick");

		if (ts3User.equals("") || ts3Pass.equals("") || ts3Server.equals("")) {
			throw new IllegalArgumentException("Missing user, pass, or server property.");
		}

		TS3Config config = new TS3Config();
		config.setHost(ts3Server);
		config.setDebugLevel(Level.WARNING);
		config.setLoginCredentials(ts3User, ts3Pass);

		this.query = new TS3Query(config);
		this.query.connect();

		this.api = query.getApi();
		api.selectVirtualServerById(1);
		api.setNickname(botNick);

		//Populate initial state
		this.presenceState.clear();
		for (Client c : api.getClients()) {
			presenceState.put(c.getNickname(), new PresenceState(c.getId(),
			    c.getNickname(),
			    api.getChannelInfo(c.getChannelId()).getName()));
		}

		api.registerEvent(TS3EventType.CHANNEL, 0);
		api.addTS3Listeners(new TS3ListenerImpl(this));
	}

	public void disconnect() {
		this.api.quit();
		this.query.exit();
		this.api = null;
		this.query = null;
		this.presenceState.clear();
	}
}
