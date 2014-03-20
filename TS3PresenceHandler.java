import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.User;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Date;
import org.pircbotx.Colors;
import java.util.Properties;
import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import java.util.logging.Level;
import java.io.FileOutputStream;
import java.io.IOException;


public class TS3PresenceHandler extends ListenerAdapter {


	private PermissionsManager pm;
	private Scanner scanner;
	private TS3PresenceEngine pe;
	private Properties props;
	private Date lastRefresh;

	public TS3PresenceHandler(TS3PresenceEngine pe,Properties props) {
		super();
		this.pe = pe;
		this.pm = PermissionsManager.getInstance();
		this.props = props;
		this.lastRefresh = new Date();
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
					case "ignore": if (!scanner.hasNext()){
						event.respond("List, add, remove, purge, save.");
					} else {
						token = scanner.next().toLowerCase();
						if (token.equals("list")) {
							event.respond("Current ignored nicknames: "+pe.getIgnoreList());
						} else if (token.equals("add")) {
							if (!scanner.hasNext()) {
								event.respond("Who am I adding?");
							} else {
								token = scanner.next().toLowerCase();
								ArrayList<String> list = pe.getIgnoreList();
								if (list.contains(token)) {
									event.respond("Ignore list already contains "+ token +".");
								} else {
									list.add(token);
									event.respond("Added "+token+" to the ignore list.");
								}
							}
						} else if (token.equals("remove")) {
							if (!scanner.hasNext()) {
								event.respond("Who am I removing?");
							} else {
								token = scanner.next().toLowerCase();
								List<String> list = pe.getIgnoreList();
								if (!list.contains(token)) {
									event.respond("Ignore list does not contain "+ token +".");
								} else {
									list.remove(token);
									event.respond("Removed "+token+" from the ignore list.");
								}
							}
						} else if (token.equals("purge")) {
							pe.purgeIgnoreList();
							event.respond("Ignore list purged.");	
						} else if (token.equals("save")) {
							String saver = "";
							for (String s : pe.getIgnoreList()) {
								saver = saver + s + ",";
							}
							if (saver.length()>0) {
								saver = saver.substring(0, saver.length()-1);
							}
							props.setProperty("ignore_list", saver);
							try {
								props.store(new FileOutputStream("swagmower.properties"), null);
								event.respond("Ignore list saved.");
							} catch (IOException ioe) {
								event.respond("There was an error writing to the filesystem.");
							}
						} else {
							event.respond("List, add, remove, purge, save.");
						}
					}
					
					break;
					//
					//
					case "on": pe.turnOn();
					event.respond("Auto-presence turned ON.");
					break;
					//
					case "off": pe.turnOff();
					event.respond("Auto-presence turned OFF.");
					break;
					//
					default: event.respond("I'm not sure what you asked me.  Valid commands are \"on\", \"off\", \"ignore\", \"timeout\", and \"interval\".");
					break;

				}
			}
		}
		
		if (command.equals("!presence")) {
			if (pe.isOn()) {
				Date now = new Date();
				//Force a refresh up to every minute due to API thread lols
				if (now.getTime() - this.lastRefresh.getTime() > 60000) {
					pe.refreshClients();
					this.lastRefresh = now;
				}
				String toPrint = "";
				ArrayList<String> ignores = pe.getIgnoreList();
				for (PresenceState ps : pe.getPresenceState().values()) {
					if (!pe.shouldIgnore(ps.nickname)) {
						if (!toPrint.equals("")) {
							toPrint += ", ";
						}
						toPrint += ps.nickname + "=" + ps.channel;
					}
				}
				event.respond("Users: " + toPrint); 
			} else {
				event.respond("Presence engine wasn't started, so I have no state to report.");
			}

		}
			
	}
}


