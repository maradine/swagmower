import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.User;
import org.pircbotx.PircBotX;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.BufferedReader;
import java.util.StringTokenizer;
import java.io.FileReader;
import java.util.Scanner;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.Calendar;
import java.util.Collections;
import java.util.Arrays;

public class LolbackHandler extends ListenerAdapter {


	private Scanner scanner;
	private PermissionsManager pm;
	private PircBotX bot;
	private Map<String, List<String>> wordpile;
	private Properties props;
	private Random rand;

	private long inceptionTime;
	private long timeThreshhold;
	private int messagesSince;
	private int messageThreshhold;
	private String activeCategory;
	private String magicWord;
	private Boolean goSwitch;
	private Boolean debug;

	private Map<User,Long> scoreTable;
	private Map<User,Long> lockouts;

	public LolbackHandler(PircBotX bot, Properties props) {
		super();
		this.bot = bot;
		this.props = props;
		this.pm = PermissionsManager.getInstance();
		this.wordpile = new HashMap<String, List<String>>();
		this.rand = new Random();
		this.inceptionTime = 0L;
		this.timeThreshhold = 1800000;
		//this.timeThreshhold = 1000;
		this.messagesSince = 0;
		this.messageThreshhold = 20;
		//this.messageThreshhold = 5;
		this.activeCategory = null;
		this.magicWord = null;
		this.debug = true;
		this.scoreTable = new HashMap<User, Long>(); 
		this.lockouts = new HashMap<User, Long>(); 

		goSwitch = initLolback();
	}
	
	private Boolean initLolback(){

	    Map<String,Integer> output = populateWordpile();
	    int wordCount = output.get("wordcount");
	    int catCount = output.get("catcount");
	    int exceptionCount = output.get("exceptioncount");
	    
		String channel = props.getProperty("irc_channel");
	    if (wordCount < 5 || catCount < 1) {
			bot.sendMessage(channel, "Word count abnormally low - not activating !lolback.");
			return false;
	    }
	    if (exceptionCount > 0) {
			bot.sendMessage(channel, "Exceptions in !lolback initialzation, but enough wordpile to play with.");
			inceptionTime = Calendar.getInstance().getTimeInMillis();
			messagesSince = 0;
			return true;
	    }
		inceptionTime = Calendar.getInstance().getTimeInMillis();
		messagesSince = 0;
	    return true;
	}

	private Map<String,Integer> populateWordpile() {
		int catCount = 0;
		int wordCount = 0;
		int exceptionCount = 0;
		File dir = new File(".");
		File[] filesList = dir.listFiles();
		for (File file : filesList) {
		    if (file.isFile()) {
			String filename = file.getName();
			if (filename.endsWith(".lolback.category")){
			    String category = filename.substring(0,filename.length()-17);
			    try {
			    	BufferedReader br = new BufferedReader(new FileReader(file));
			    	List<String> words = new LinkedList<String>();
			    	String line;
			    	while ((line = br.readLine()) != null) {
					if (line.trim().length() == 0) {
				    	continue;
					}
					StringTokenizer st = new StringTokenizer(line);
					words.add(st.nextToken());
					wordCount++;
			    	}
			    	wordpile.put(category, words);
			    	catCount++;
			    } catch (FileNotFoundException fnfe) {
				System.out.println("FileNotFoundException: "+fnfe);
				exceptionCount++;
			    } catch (IOException ioe) {
				System.out.println("IOException: "+ioe);
				exceptionCount++;
			    }
			}
		    }
		}
		Map<String,Integer> output = new HashMap<String, Integer>();
		output.put("catcount", catCount);
		output.put("wordcount", wordCount);
		output.put("exceptioncount", exceptionCount);
		return output;
	}


	public void onMessage(MessageEvent event) {

		String command = event.getMessage();
		String commandLower = command.toLowerCase();
		
		scanner = new Scanner(commandLower);
		String token = scanner.next();
		
		if (token.equals("!lolback")) {

			if (scanner.hasNext("score")) {
					Long currentScore = scoreTable.get(event.getUser());
					if (currentScore == null) {
						currentScore = 0L;
					}
					event.respond("You have "+currentScore+" points.");
			
			} else if (scanner.hasNext("reload")) {
				if (!pm.isAllowed(command,event.getUser(),event.getChannel())) {
					event.respond("Sorry, you do not have permission to execute this command.");
					return;
				} else {
					//things
					Map<String,Integer> output = populateWordpile();
					event.respond("Read "+output.get("wordcount")+" words from "+output.get("catcount")+" categories.");
					event.respond(output.get("exceptioncount")+" exceptions reported during file handling.");
				}
			} else if (scanner.hasNext()) {
				
				//first exit - are we in cooldown?
				if (magicWord == null) {
					event.respond("Still in cooldown from last lolback.");
					return;
				}
				
				// second exit - is user locked out?
				Long lockout = lockouts.get(event.getUser());
				if (lockout == null) {
					lockout = 0L;
				}
				if (Calendar.getInstance().getTimeInMillis() < lockout) {
					event.respond("Son, you're still locked out.");
					return;
				}
				
				token = scanner.next();
				if (token.equals(magicWord)) {
					Long diff = Calendar.getInstance().getTimeInMillis() - inceptionTime;
					Long score = diff / 1000;
					Long currentScore = scoreTable.get(event.getUser());
					Long newScore;
					if (currentScore !=null) {
						newScore = score + currentScore;
					} else {
						newScore = score;
					}
					scoreTable.put(event.getUser(), newScore);
					event.respond("DING. "+event.getUser().getNick()+" wins the lolback for "+score+" points, putting them at "+newScore+" points. The active category was \""+activeCategory+"\".");
					magicWord = null;
					activeCategory = null;
					inceptionTime = Calendar.getInstance().getTimeInMillis();
					messagesSince = 0;
				} else {
					Long now = Calendar.getInstance().getTimeInMillis();
					Long then = now + 3600000L;
					lockouts.put(event.getUser(), then);
					event.respond("NOPE.  Locked out for an hour.");
				}
			} else {
				event.respond("generic status message");
			}

		} else {
			//start processing messages
		    if (!goSwitch) {return;} //if not turned on, just exit

			//clock one message on the counter
			messagesSince++;

			//first: are we missing a magic word?
			if (magicWord == null) {
			
				//if so, is it time to pick one?
				long diff = Calendar.getInstance().getTimeInMillis() - inceptionTime;
				if (diff >= timeThreshhold && messagesSince >= messageThreshhold) { //we're out of cooldown and in the market to pick a magic word
					
					//get the canonical message from the event
					String message = event.getMessage().toLowerCase().trim();
					
					//carve it up
					String[] tokens = message.split("\\W+");

					//turn it into a list
					List<String> tokenList = Arrays.asList(tokens);
					
					//randomize order of list such that we don't always select the same word from the same sentence
					//event.respond("Original canonical string: "+tokenList);
					Collections.shuffle(tokenList, rand);
					//event.respond("Shuffled canonical string: "+tokenList);

					//get the set of categories
					Set<String> catset = wordpile.keySet();
					
					//convert to list to impose order.  only doing this for the case where the same word might be in multiple cats
					//remember kids, 'undefined' does not equal 'random'
					LinkedList<String> catlist = new LinkedList<String>(catset);

					//shuffle it.
					Collections.shuffle(catlist, rand);
					
					Boolean done = false;

					//start walking the tokens and look for a category hit
					for (String wordtoken : tokenList) {
						//for every category, see if we have a word that matches
						for (String cat : catlist) {

							for (String word : wordpile.get(cat)) {
								if (wordtoken.equals(word) && !done) { //we have a hit
									magicWord = wordtoken;
									activeCategory = cat;
									done = true;
									inceptionTime = Calendar.getInstance().getTimeInMillis();
									messagesSince = 0;
									System.out.println(magicWord+" is now the magic word");
								}
							}
						}
					}
					//by here we should have a magic word, if there's one to be had
				}
			} else { //we have our magic word, let's see if it's time to drop a hint.
				
				long diff = Calendar.getInstance().getTimeInMillis() - inceptionTime;
				if (diff >= timeThreshhold && messagesSince >= messageThreshhold) { //we're out of cooldown and now we should check if we need to drop a hint

					//get the canonical message from the event
					String message = event.getMessage().toLowerCase().trim();
					
					//carve it up
					String[] tokens = message.split("\\W+");

					//turn it into a list
					List<String> tokenList = Arrays.asList(tokens);
					
					//randomize order of list such that we don't always select the same word from the same sentence
					//event.respond("Original canonical string: "+tokenList);
					Collections.shuffle(tokenList, rand);
					//event.respond("Shuffled canonical string: "+tokenList);

					Boolean done = false;

					//start walking the tokens and look for a category hit
					for (String wordtoken : tokenList) {

						for (String word : wordpile.get(activeCategory)) {
							if (wordtoken.equals(word) && !wordtoken.equals(magicWord) && !done) { //we have a hit
								event.respond("YOU DO GO ON MR. "+event.getUser().getNick().toUpperCase());
								done = true;
							}
						}
					}
					//by here we should have a magic word, if there's one to be had
				}

			}
		}
	}
	public void onPrivateMessage(PrivateMessageEvent event) {
		String command = event.getMessage();
		String commandLower = command.toLowerCase();
		
		scanner = new Scanner(commandLower);
		String token = scanner.next();
		if (token.equals("!lolback")) {

			if (scanner.hasNext("score")) {
					Long currentScore = scoreTable.get(event.getUser());
					if (currentScore == null) {
						currentScore = 0L;
					}
					event.respond("You have "+currentScore+" points.");
			
			} else if (scanner.hasNext("reload")) {
				if (!pm.isAllowed("!lolback reload",event.getUser(),props)) {
					event.respond("Sorry, you do not have permission to execute this command.");
					return;
				} else {
					//things
					Map<String,Integer> output = populateWordpile();
					event.respond("Read "+output.get("wordcount")+" words from "+output.get("catcount")+" categories.");
					event.respond(output.get("exceptioncount")+" exceptions reported during file handling.");
				}
			} else if (scanner.hasNext("debug")) {
				if (!pm.isAllowed("!lolback debug",event.getUser(),props)) {
					event.respond("Sorry, you do not have permission to execute this command.");
					return;
				} else {
					event.respond("Current magic word is \""+magicWord+"\".  active category is \""+activeCategory+"\".");
					event.respond("Message count at "+messagesSince+"/"+messageThreshhold+". Timer at "+Calendar.getInstance().getTimeInMillis()+"/"+(inceptionTime+timeThreshhold)+".");
				}

			}

		}





	}
}
