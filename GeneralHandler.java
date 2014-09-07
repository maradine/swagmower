import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.User;
import org.pircbotx.PircBotX;
import java.io.IOException;
import java.util.Scanner;
import java.util.Collections;
import java.util.LinkedList;

public class GeneralHandler extends ListenerAdapter {


	private Scanner scanner;
	private PermissionsManager pm;
	private PircBotX bot;
		
	public GeneralHandler(PircBotX bot) {
		super();
		this.bot = bot;
		this.pm = PermissionsManager.getInstance(); 
		System.out.println("PermissionsHandler initialized.");
	}
			
	public void onMessage(MessageEvent event) {

		String command = event.getMessage();
		String commandLower = command.toLowerCase();
		
		scanner = new Scanner(commandLower);
		String token = scanner.next();

		if (token.equals("!shutdown")) {

			if (!pm.isAllowed(command,event.getUser(),event.getChannel())) {
				event.respond("Sorry, you do not have permission to execute this command.");
				return;
			} else {
				if (scanner.hasNext()) {
					token = scanner.next();

					String myName = event.getBot().getNick().toLowerCase();
					if (token.equals(myName)) {
						event.respond("Nite nite.");
						try {
							bot.shutdown(true);
							Thread.sleep(5000L);
						} catch (Exception e) {
						}finally {
							System.exit(0);
						}
					}
				} else {
					event.respond("You need to call me by name.  It's crowded in here.");
				}
			}
		} else if (token.equals("!reload")) {
			if (!pm.isAllowed(command,event.getUser(),event.getChannel())) {
				event.respond("Sorry, you do not have permission to execute this command.");
				return;
			} else {
				try {
					Swagmower.reloadProps();
				} catch (IOException ioe) {
					event.respond("Reload failed: "+ioe.getMessage());
				}
				event.respond("Properties reloaded from file.  I hope you know what the fuck you are doing.");
			}
		}
					

	}
}
