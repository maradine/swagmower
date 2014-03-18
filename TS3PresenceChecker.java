import java.util.HashMap;
import java.util.logging.Level;
import java.util.Properties;
import java.io.IOException;
import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;


public class TS3PresenceChecker {

	public static HashMap<String,String> getPresence(int timeout, Properties props) throws IOException, IllegalArgumentException {
		
		HashMap<String,String> hm = new HashMap<String, String>();
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
	
		TS3Query query = new TS3Query(config);
		query.connect();
	
		TS3Api api = query.getApi();
		api.selectVirtualServerById(1);
		api.setNickname(botNick);

		for (Client c : api.getClients()) {
			hm.put(c.getNickname(), api.getChannelInfo(c.getChannelId()).getName());
		}
		api.quit();
		query.exit();
		return hm;
		
	}

}


