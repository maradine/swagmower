import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.User;
import java.util.Scanner;
import java.util.Set;
import java.util.ArrayList;
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
			if (scanner.hasNext("in")) {  //signal inclusion into registered event or all events
				//event.respond(size+" in for "+title+": "+userString);
				//nothingPreventer = true;
			} else if (scanner.hasNext("somethingelse")) {
				//blah
			} else {
				//default case
				//State the game year and which players are outstanding
				String appKey = props.getProperty("dropbox_app_key");;
				String appSecret = props.getProperty("dropbox_app_secret");
				String accessToken = props.getProperty("dropbox_access_token");
				String gamePath = props.getProperty("stars_game_path");
				String gamePrefix = props.getProperty("stars_game_prefix");
				if (appKey == null || appSecret == null || accessToken == null || appKey.equals("") || appSecret.equals("") || accessToken.equals("")) {
					event.respond("Check config - app key, app secret, or access token missing or null.");
				} else if (gamePath == null || gamePrefix == null || gamePath.equals("") || gamePrefix.equals("")) {
					event.respond("Check config - game path or game prefix is missing or null.");
				} else {
					//actually do something
					int year = 0;
					int outstanding = 0;
					try {
						year = DropboxHelper.getGameDate(appKey, appSecret, accessToken, gamePath, gamePrefix);
					} catch (DbxException de) {
						event.respond("Error connection to Dropbox: "+de.getMessage());
						return;
					}
					try {
						//trim path to remove trailing slash cuz dropbox can't dig
						gamePath = gamePath.substring(0,gamePath.length()-1);
						outstanding = DropboxHelper.getTurnsOutstanding(appKey, appSecret, accessToken, gamePath, gamePrefix);
					} catch (DbxException de) {
						event.respond("Error connection to Dropbox: "+de.getMessage());
						return;
					}
					year +=2400;
					event.respond("The year is "+year+".  "+outstanding+" players outstanding.");
				}

			}
		}
	}
}


