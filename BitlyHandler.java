import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.User;
import org.pircbotx.PircBotX;
import java.io.IOException;
import java.util.Scanner;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;


import com.rosaloves.bitlyj.Url;
import static com.rosaloves.bitlyj.Bitly.*;

import java.net.URL;
import java.net.MalformedURLException;

public class BitlyHandler extends ListenerAdapter {


	private Scanner scanner;
	private Properties props;
	private PermissionsManager pm;
	private PircBotX bot;
	private boolean onSwitch;
	private int cutoff;
	private String bitlyUser;
	private String bitlyKey;

	public BitlyHandler(PircBotX bot, Properties props) {
		super();
		this.bot = bot;
		this.props = props;
		this.pm = PermissionsManager.getInstance(); 
		System.out.println("PermissionsHandler initialized.");
		onSwitch = true;
		cutoff = 70;
		bitlyUser = props.getProperty("bitly_user");;
		bitlyKey = props.getProperty("bitly_key");
	}
			
	public void onMessage(MessageEvent event) {

		String command = event.getMessage();
		
		scanner = new Scanner(command);
		String token;
		while (scanner.hasNext()) {
			
			token = scanner.next();
			if (token.length() > cutoff) {
				try {
					URL candidateURL = new URL(token); //if the exception doesn't fire, it was a valid URL
					Url bitlyURL = as(bitlyUser, bitlyKey).call(shorten(token));
					event.respond("LET ME SHORTEN THAT FOR YOU: " + bitlyURL.getShortUrl());
				} catch (MalformedURLException e) {
				} catch (com.rosaloves.bitlyj.BitlyException e) {
					event.respond("Bitly thinks we're full of shit and won't shorten.  Someone yell at maradine to check the logs.");
				}
			}

		}

		String commandLower = command.toLowerCase();
		scanner = new Scanner(commandLower);
		token = scanner.next();
		
		if (token.equals("!bitly")) {
			if (!pm.isAllowed(command,event.getUser(),event.getChannel())) {
				event.respond("Sorry, you do not have permission to execute this command.");
				return;
			} else {
				if (scanner.hasNext("on") || scanner.hasNext("enable")) {		
					onSwitch = true;
					event.respond("Bitly URL shortening enabled at "+cutoff+" characters.");
				} else if (scanner.hasNext("off") || scanner.hasNext("disable")) {
					onSwitch = false;
					event.respond("Bitly URL shortening disabled.");
				} else if (scanner.hasNext("cutoff")) {
					scanner.next();
					if (scanner.hasNextInt()){
						cutoff = scanner.nextInt();
						event.respond("URL length cutoff set to "+cutoff+" characters.");
					} else {
						event.respond("command format: !bitly cutoff [INT]");
					}
				} else {
					String on;
					if (onSwitch == true) {
						on = "enabled";
					} else {
						on = "disabled";
					}
					event.respond("Bitly URL shortening is "+on+".  Cutoff is set to "+cutoff+" characters.  !bitly on|off|cutoff");
				}
			}
		}


	}

}
