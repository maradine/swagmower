import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.User;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
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
					case "ignore": if (!scanner.hasNext()){
						event.respond("List, add, remove, purge, save.");
					} else {
						token = scanner.next().toLowerCase();
						if (token.equals("list")) {
							event.respond("Current ignored nicknames: "+pe.getIgnoreList());
						} else if (token.equals("add")) {
							System.out.println("ADD TOKEN ACCEPTED");
							if (!scanner.hasNext()) {
								System.out.println("OOPS, NOTHING AFTER ADD TOKEN");
								event.respond("Who am I adding?");
							} else {
								token = scanner.next().toLowerCase();
								System.out.println("GOT ADD TOKEN "+token);
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
					case "interval": if (!scanner.hasNextLong()){
						event.respond("Setting the interval requires a number, expressed in minutes.  Max 60.");
					} else {
						long rawlong = scanner.nextLong();
						if (rawlong > 60 || rawlong < 1) {
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
						if (rawint > 20 || rawint < 1) {
							event.respond("Setting the timeout requires a number, expressed in seconds.  Max 20.");
						} else {
							event.respond("Timeout set to "+rawint+" seconds.");
							pe.setTimeout(rawint*1000);
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
				HashMap<String,String> hm = pe.getPresenceState();
				event.respond("Hash dump: "+ hm); 
			} else {
				event.respond("Presence engine wasn't started, so I have no state to report.");
			}

		}
			
	}
}


