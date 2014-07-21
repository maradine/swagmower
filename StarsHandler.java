import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.User;
import java.util.Scanner;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import org.pircbotx.Colors;
import java.util.Properties;
import java.io.FileOutputStream;
import java.io.IOException;
import com.dropbox.core.*;


public class StarsHandler extends ListenerAdapter {


	private PermissionsManager pm;
	private Scanner scanner;
	private StarsEngine se;
	private Properties props;

	public StarsHandler(StarsEngine se,Properties props) {
		super();
		this.se = se;
		this.pm = PermissionsManager.getInstance();
		this.props = props;
		System.out.println("StarsHandler Initialized.");
	}

	public void onMessage(MessageEvent event) {
		String command = event.getMessage();
		String commandLower = command.toLowerCase();
		
		scanner = new Scanner(commandLower);

		if (scanner.hasNext("!stars")) {
			scanner.next();
			if (scanner.hasNext("ai")) {
				scanner.next();
				if (scanner.hasNext("add")) {
					scanner.next();
					if (scanner.hasNextInt()) {
						int p = scanner.nextInt();
						se.addAiPlayer(p);
						event.respond("Player "+p+" has been marked as AI.");
					} else {
						event.respond("Which number am I adding?");
					}
				} else if (scanner.hasNext("remove")) {
					scanner.next();
					if (scanner.hasNextInt()) {
						int p = scanner.nextInt();
						if (se.removeAiPlayer(p)){
							event.respond("Player "+p+" has been removed from the AI roster.");
						} else {
							event.respond("That player wasn't marked as an AI.");
						}
					} else {
						event.respond("Which number am I removing?");
					}

				} else if (scanner.hasNext("purge")) {
					se.purgeAiPlayers();
					event.respond("AI player manifest purged.");
				} else if (scanner.hasNext("save")) { //store ai elections in properties for persistence
					//save state to props
					if (!pm.isAllowed("!stars save",event.getUser(),event.getChannel())) {
						event.respond("Sorry, you are not in the access list for !stars management.");
						return;
					}
					String saver = "";
					for (Integer i : se.getAiPlayers()) {
						saver = saver + i.toString() + ",";
					}
					if (saver.length()>0) {
						saver = saver.substring(0, saver.length()-1);
					}
					props.setProperty("stars_ai_list", saver);
					try {
						props.store(new FileOutputStream("swagmower.properties"), null);
						event.respond("AI list saved.");
					} catch (IOException ioe) {
						event.respond("There was an error writing to the filesystem.");
					}
				} else {
					String aiString = "";
					ArrayList<Integer> aiPlayers = se.getAiPlayers();
					for (Integer i : aiPlayers) {
						aiString += i+" ";
					}
					event.respond("The following players are marked as AI: "+aiString);
					//report which ais
				}
				
			} else if (scanner.hasNext("map")) {
				scanner.next();
				if (scanner.hasNextInt()) {
					int p = scanner.nextInt();
					se.mapPlayer(p, event.getUser());
					event.respond(event.getUser().getNick()+" has been mapped to player "+p+".");
				} else if (scanner.hasNext("purge")) {
					se.purgePlayerMap();
					event.respond("You have unmapped all playeers.");
				} else 	{
					event.respond("Give me a number and I'll map you to a player slot.  You can't map someone else.  Maybe I'll do a list here later.");
				}
			} else if (scanner.hasNext("unmap")) {
				scanner.next();
				if (scanner.hasNextInt()) {
					int p = scanner.nextInt();
					se.unmapPlayer(p);
					event.respond("Player "+p+"  has been unmapped.");
				} else {
					event.respond("Give me a number and I'll unmap a player slot. Don't abuse this.");
				}
			} else if (scanner.hasNext("init")) {
				if (se.init()) {
					event.respond("I rebuilt the stars engine from props.  It's fine.  Everything that can be fine, is fine.");
					se.turnOn();
				} else {
					event.respond("I tried, but something went wrong, and I'm not feeling particularly communicative.");
				}
			} else if (scanner.hasNext("off")) {
				se.turnOff();
				event.respond("Stars engine shut down.  No updates will be forthcoming.");
			} else {
				//default case
				//State the game year and which players are outstanding
				String appKey = se.getAppKey();
				String appSecret = se.getAppSecret();
				String accessToken = se.getAccessToken();
				String gamePath = se.getGamePath();
				String gamePrefix = se.getGamePrefix();
				if (appKey == null || appSecret == null || accessToken == null || appKey.equals("") || appSecret.equals("") || accessToken.equals("")) {
					event.respond("Check config - app key, app secret, or access token missing or null.");
				} else if (gamePath == null || gamePrefix == null || gamePath.equals("") || gamePrefix.equals("")) {
					event.respond("Check config - game path or game prefix is missing or null.");
				} else {
					//actually do something
					int year = 0;
					PriorityQueue<Integer> players;
					try {
						year = DropboxHelper.getGameDate(appKey, appSecret, accessToken, gamePath, gamePrefix);
						players = DropboxHelper.getPlayersOutstanding(appKey, appSecret, accessToken, gamePath, gamePrefix);
					} catch (DbxException de) {
						event.respond("Error connecting to Dropbox: "+de.getMessage());
						return;
					}
					year +=2400;
				 	String playerString = "";
					HashMap<Integer, User> playerMap = se.getPlayerMap();
					for (Integer i : players) {
						if (!se.getAiPlayers().contains(i)) {
							if (playerMap.containsKey(i)) {
								playerString += playerMap.get(i).getNick() + " ";
							} else {
								playerString += i+" ";
							}
						}
					}
					//int outstanding = players.size() - se.getAiPlayers().size();
					//event.respond("The year is "+year+".  "+outstanding+" players outstanding: "+playerString);
					
					event.respond("The year is "+year+".  "+players+" players outstanding: "+playerString);
				}

			}
		}
	}
}


