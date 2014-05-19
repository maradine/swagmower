import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.User;
import java.util.Scanner;
import java.util.Set;
import java.util.ArrayList;
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
				} else {
					String aiString = "";
					ArrayList<Integer> aiPlayers = se.getAiPlayers();
					for (Integer i : aiPlayers) {
						aiString += i+" ";
					}
					event.respond("The following players are marked as AI: "+aiString);
					//report which ais
				}
				
			} else if (scanner.hasNext("somethingelse")) {
				//blah
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
						//trim path to remove trailing slash cuz dropbox can't dig
						gamePath = gamePath.substring(0,gamePath.length()-1);
						players = DropboxHelper.getPlayersOutstanding(appKey, appSecret, accessToken, gamePath, gamePrefix);
					} catch (DbxException de) {
						event.respond("Error connecting to Dropbox: "+de.getMessage());
						return;
					}
					year +=2400;
				 	String playerString = "";
					for (Integer i : players) {
						if (!se.getAiPlayers().contains(i)) {
							playerString += i+" ";
						}
					}
					int outstanding = players.size() - se.getAiPlayers().size();
					event.respond("The year is "+year+".  "+outstanding+" players outstanding: "+playerString);
				}

			}
		}
	}
}


