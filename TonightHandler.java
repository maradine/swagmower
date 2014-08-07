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
import org.pircbotx.PircBotX;


public class TonightHandler extends ListenerAdapter {


	private PermissionsManager pm;
	private Scanner scanner;
	private TonightEngine te;
	private Properties props;
	private PircBotX bot;

	public TonightHandler(PircBotX bot, TonightEngine te, Properties props) {
		super();
		this.te = te;
		this.pm = PermissionsManager.getInstance();
		this.props = props;
		this.bot = bot;
		System.out.println("TonightHandler Initialized.");
	}

	public void onMessage(MessageEvent event) {

		String command = event.getMessage();
		String commandLower = command.toLowerCase();
		
		scanner = new Scanner(commandLower);

		if (scanner.hasNext("!tonight")) {
			scanner.next();
			if (scanner.hasNext("in")) {  //signal inclusion into registered event or all events
				scanner.next();
				User u = event.getUser();
				if (scanner.hasNext()) {
					String inList = "";
					String dupList = "";
					String regList = "";
					if (scanner.hasNext("predd")) {event.respond("Gross."); return;}
					if (scanner.hasNext("yourmom")) {event.respond("Gonna get crowded in there."); return;}
					if (scanner.hasNext("yourmother")) {event.respond("Gonna get crowded in there."); return;}
					if (scanner.hasNext("your_mom")) {event.respond("Gonna get crowded in there."); return;}
					if (scanner.hasNext("sync")) {event.respond("IT AINT NO LIE"); bot.kick(event.getChannel(),event.getUser(),"BABY BYE BYE BYE");return;}
					if (scanner.hasNext("work")) {event.respond("Wanna increase your productivity at the office?"); bot.kick(event.getChannel(),event.getUser(),"GET OUT OF IRC");return;}
					if (scanner.hasNext("batteries")) {event.respond("PRIASE VANU."); return;}
					if (scanner.hasNext("thebutt")) {event.respond("What what?"); return;}
					if (scanner.hasNext("reuben")) {event.respond("Tonight's reuben contains: corned beef."); return;}
					if (scanner.hasNext("teamhorse")) {event.respond("TEAM HORSE RULES YOU"); return;}
					if (scanner.hasNext("team_horse")) {event.respond("TEAM HORSE RULES YOU"); return;}
					if (scanner.hasNext("horse")) {event.respond("TEAM HORSE RULES YOU"); return;}
					
					while (scanner.hasNext()) {
						String title = scanner.next();
						//try to parse game name
						if (te.isTitleRegistered(title)) {
							if (te.addUserToTitle(u, title)) {
								//event.respond("You are in for "+title+".");
								inList += title+" ";
							} else {
								//event.respond("You were already in.");
								dupList += title+" ";
							}
	
						} else {
							//event.respond("That's not a registered title.  Have an op fix you up.");
							regList += title+" ";
						}
					}
					String output = "";
					if (!inList.equals("")) {
						output += "You are in for: "+inList+"  ";
					}
					if (!dupList.equals("")) {
						output += "You were already in for: "+dupList+"  ";
					}
					if (!regList.equals("")) {
						output += "Not a registered title: "+regList+"  ";
					}
					event.respond(output);

				} else {
					//add user to everything
					te.addUserToAllPopulated(u);
					event.respond("You are in for everything someone else has already called in on.");
				}

			} else if (scanner.hasNext("register")) { //add a registered event
				if (!pm.isAllowed("!tonight register",event.getUser(),event.getChannel())) {
					event.respond("Sorry, you are not in the access list for title registration.");
					return;
				}
				scanner.next();
				if (scanner.hasNext()) {
					String title = scanner.next();
					if (te.registerTitle(title)){
						event.respond("Title \""+title+"\" has been registered.");
					} else {
						event.respond("Title \""+title+"\" was already registered; I shan't do it again.");
					}
				} else {
					event.respond("I can't register nothing.  Well, I can, but that's not a fun data structure.");
				}
			
			} else if (scanner.hasNext("deregister")) { //remove a registered event
				if (!pm.isAllowed("!tonight deregister",event.getUser(),event.getChannel())) {
					event.respond("Sorry, you are not in the access list for title registration.");
					return;
				}
				scanner.next();
				if (scanner.hasNext()) {
					String title = scanner.next();
					if(te.deregisterTitle(title)) {
						event.respond("\""+title+"\" has been deregistered.  It probably sucked anyway.");
					} else {
						event.respond("\""+title+"\" wasn't registered in the first place.");
					}
				} else {
					event.respond("I can't deregister nothing.  Oh, I mean, wait - done.");
				}

			} else if (scanner.hasNext("list")) { //show registered events in some order
				ArrayList<String> titles = te.getRegisteredTitles();
				String writer = "Registered titles: ";
				for (String s : titles) {
					writer += s+" ";
				}
				event.respond(writer);
			
			} else if (scanner.hasNext("out")) { //back out of an event or all events
				scanner.next();
				User u = event.getUser();
				if (scanner.hasNext()) {
					String title = scanner.next();
					if (te.removeUserFromTitle(u, title)) {
						event.respond("You are out for "+title+".");
					} else {
						event.respond("Either you weren't in to begin with, or that's not a registered title.");
					}
				} else {
					te.removeUserFromAll(u);
					event.respond("You are out of every thing.");
				}
			
			} else if (scanner.hasNext("save")) { //store registered events in properties for persistence
				//save state to props
				if (!pm.isAllowed("!tonight save",event.getUser(),event.getChannel())) {
					event.respond("Sorry, you are not in the access list for !tonight management.");
					return;
				}
				String saver = "";
				for (String s : te.getRegisteredTitles()) {
					saver = saver + s + ",";
				}
				if (saver.length()>0) {
					saver = saver.substring(0, saver.length()-1);
				}
				props.setProperty("title_list", saver);
				try {
					props.store(new FileOutputStream("swagmower.properties"), null);
					event.respond("Title list saved.");
				} catch (IOException ioe) {
					event.respond("There was an error writing to the filesystem.");
				}

			} else if (scanner.hasNext("flush")) { //store registered events in properties for persistence
				if (!pm.isAllowed("!tonight flush",event.getUser(),event.getChannel())) {
					event.respond("Sorry, you are not in the access list for !tonight management.");
					return;
				}
				scanner.next();
				if (scanner.hasNextInt()) {
					int set = scanner.nextInt();
					if (set >=0 || set <=23) {
						te.setFlushHour(set);
						event.respond("Now flushing titles at "+set+":00 system time.");
					} else {
						event.respond("Valid hours are 0 - 23.");
					}
				} else {
					te.flushTonight();
					event.respond("State manually flushed.  Titles remain.");
				}

			} else if (scanner.hasNext("purge")) { //store registered events in properties for persistence
				if (!pm.isAllowed("!tonight purge",event.getUser(),event.getChannel())) {
					event.respond("Sorry, you are not in the access list for !tonight management.");
					return;
				}
				te.purgeTonight();
				event.respond("State destroyed.");
			
			
			} else if (scanner.hasNext("all")) { 
				event.respond("TONIGHT ON #FKPK: ");
				ArrayList<String> titles = te.getRegisteredTitles();
				Boolean nothingPreventer = false;
				if (titles.size() > 0) {
					for (String title : titles) {
						ArrayList<User> users = te.getInUsers(title);
						int size = users.size();
						String userString="";
						for (User u : users) {
							userString += u.getNick()+", ";
						}
						if (size > 0) {
							userString = userString.substring(0,userString.length()-2);
							event.respond(size+" in for "+title+": "+userString);
							nothingPreventer = true;
						}
					}
				}
				if (!nothingPreventer) {
					event.respond("NOOOOOOTHIIIIIIIIIIING.");
				}
			
			
			
			} else if (scanner.hasNext()) { //unrecognized verb
				event.respond("Valid !tonight verbs include 'in' and 'out'.  ex. \"!tonight in ps2\"");
			
			} else {
				//default output, show what's up tonight
				ArrayList<String> titles = te.getRegisteredTitles();
				Boolean nothingPreventer = false;
				if (titles.size() > 0) {
					ArrayList<User> users = te.getInUsers(titles.get(0));
					int size = users.size();
					String userString="";
					for (User u : users) {
						userString += u.getNick()+", ";
					}
					if (size > 0) {
						userString = userString.substring(0,userString.length()-2);
						event.respond("TONIGHT ON #FKPK: "+size+" in for "+titles.get(0)+": "+userString);
						nothingPreventer = true;
					}
					
				}
				if (!nothingPreventer) {
					event.respond("NOOOOOOTHIIIIIIIIIIING.");
				}

			}
		}
	}
}


