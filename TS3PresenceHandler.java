import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.User;
import java.util.HashMap;
import java.util.Scanner;
import org.pircbotx.Colors;
import java.util.Properties;
import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import java.util.logging.Level;


public class TS3PresenceHandler extends ListenerAdapter {


	private PermissionsManager pm;
	private Scanner scanner;
	private TS3PresenceEngine pe;
	private Thread peThread;
	private Properties props;

	public TS3PresenceHandler(TS3PresenceEngine pe, Thread peThread, Properties props) {
		super();
		this.pe = pe;
		this.peThread = peThread;
		this.pm = PermissionsManager.getInstance();
		this.props = props;
		System.out.println("PresenceHandler Initialized.");
	}

	public void onMessage(MessageEvent event) {
		String rawcommand = event.getMessage();
		String command = rawcommand.toLowerCase();
		
		if (command.startsWith("!presence ")) {
			User user = event.getUser();
			if (!pm.isAllowed("!presence",event.getUser(),event.getChannel())) {
				event.respond("Sorry, you are not in the access list for presence checking.");
				return;
			}
			scanner = new Scanner(command);
			String token = scanner.next();
			
			if (scanner.hasNext()){
				token = scanner.next().toLowerCase();
				switch (token) {
					//
					case "interval": if (!scanner.hasNextLong()){
						event.respond("Setting the interval requires a number, expressed in minutes.  Max 60.");
					} else {
						long rawlong = scanner.nextLong();
						if (rawlong > 60) {
							event.respond("Setting the interval requires a number, expressed in minutes.  Max 60.");
						} else {
							event.respond("Interval set to "+rawlong+" minutes.");
							pe.setInterval(rawlong*1000*60);
							peThread.interrupt();
						}
					}
					break;
					//
					//
					case "timeout": if (!scanner.hasNextInt()){
						event.respond("Setting the timeout requires a number, expressed in seconds.  Max 20.");
					} else {
						int rawint = scanner.nextInt();
						if (rawint > 20) {
							event.respond("Setting the timeout requires a number, expressed in seconds.  Max 20.");
						} else {
							event.respond("Timeout set to "+rawint+" seconds.");
							pe.setTimeout(rawint*1000);
						}
					}
					break;
					//
					//
					case "squelch": if (!scanner.hasNext()){
						event.respond("On or off?");
					} else {
						String onoff = scanner.next();
						if (!onoff.equals("on") && !onoff.equals("off")) {
							event.respond("On or off?");
						} else if (onoff.equals("on")){
							event.respond("Turning on squelch.");
							pe.squelchOn();
						} else if (onoff.equals("off")){
							event.respond("Turning off squelch.");
							pe.squelchOff();
						}
					}
					break;
					//
					case "on": pe.turnOn();
					event.respond("Auto-presence turned ON.");
					break;
					//
					case "off": pe.turnOff();
					event.respond("Auto-presence turned OFF.");
					break;
					//
					default: event.respond("I'm not sure what you asked me.  Valid commands are \"on\", \"off\", \"timeout\", and \"interval\".");
					break;

				}
			}
		}
		
		if (command.equals("!presence")) {

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
			config.setDebugLevel(Level.ALL);
			config.setLoginCredentials(ts3User, ts3Pass);
	
			TS3Query query = new TS3Query(config);
			query.connect();
	
			TS3Api api = query.getApi();
			api.selectVirtualServerById(1);
			api.setNickname(botNick);

			for (Client c : api.getClients()) {
				hm.put(c.getNickname(), api.getChannelInfo(c.getChannelId()).getName());
			}
			event.respond("Hash dump: "+ hm); 
			api.quit();
			query.exit();
			System.out.println("I AM LEAVING THIS FUCKING BLOCK");

		}
			
	}
}


