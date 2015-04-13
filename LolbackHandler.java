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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Properties;
import java.util.Random;
import java.util.Calendar;
import java.util.Collections;
import java.util.Arrays;
import java.text.NumberFormat;

public class LolbackHandler extends ListenerAdapter {


	private Scanner scanner;
	private PermissionsManager pm;
	private PircBotX bot;
	private Map<String, List<String>> wordpile;
	private Properties props;
	private Random rand;

	private long inceptionTime;
	private long timeThreshhold;
	private long baseTimeThreshhold;
	private int messagesSince;
	private int messageThreshhold;
	private String activeCategory;
	private String magicWord;
	private String injector;
	private Boolean goSwitch;
	private Boolean debug;

	private String originalMessage;
	
	private float fishingThreshhold = .66f;

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
		this.baseTimeThreshhold = 900000L;
		this.timeThreshhold = randomTimeThreshhold();
		//this.timeThreshhold = 1000;
		this.messagesSince = 0;
		this.messageThreshhold = 20;
		//this.messageThreshhold = 5;
		this.activeCategory = null;
		this.magicWord = null;
		this.injector = null;
		this.debug = true;
		this.scoreTable = new ConcurrentHashMap<User, Long>(); 
		this.lockouts = new ConcurrentHashMap<User, Long>(); 

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

	private Long randomTimeThreshhold() {
		return Math.round( baseTimeThreshhold + Math.abs(baseTimeThreshhold * rand.nextGaussian()));

	}

	private Map<String,Integer> populateWordpile() {
		wordpile = new HashMap<String, List<String>>();
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

	private boolean isMessageFishing(String message, float threshhold) {
		message = message.toLowerCase();
		//carve it up
		String[] tokens = message.split("\\W+");
		//turn it into a list
		List<String> tokenList = Arrays.asList(tokens);
		int totalTokens = 0;
		int hitTokens = 0;
		for (String token : tokenList) {
			totalTokens++;
			foundit:
			for (List<String> list : wordpile.values()){
				for (String s : list) {
					if (token.equals(s)) {
						hitTokens++;
						break foundit;
					}
				}
			}
		}
		float percent = hitTokens/totalTokens;
		if (percent > threshhold) {
			return true;
		} else {
			return false;
		}
	}





	public void onMessage(MessageEvent event) {

		String channel = props.getProperty("irc_channel");
		String command = event.getMessage();
		String commandLower = command.toLowerCase();
		
		scanner = new Scanner(commandLower);
		String token = scanner.next();
		
		if (token.equals("!lolback")) {

			if (scanner.hasNext("score") || scanner.hasNext("scores")) {
					
					LolbackElementComparator lec = new LolbackElementComparator(scoreTable);
					ArrayList<User> alu = new ArrayList<User>(scoreTable.keySet());
					java.util.Collections.sort(alu, lec);
					
					NumberFormat myFormat = NumberFormat.getInstance();
					myFormat.setGroupingUsed(true);

					for (User u : alu) {
						Long s = scoreTable.get(u);
						event.respond("User "+u.getNick()+" has "+myFormat.format(s)+" points.");
					}
			
			} else if (scanner.hasNext("rules")) {
					event.respond("PM");
					bot.sendMessage(event.getUser(), "THESE ARE THE RULES.");
					bot.sendMessage(event.getUser(), "AFTER AN AMOUNT OF TIME PASSES I WILL WATCH FOR WORDS.");
					bot.sendMessage(event.getUser(), "WHEN I SEE A WORD I LIKE I WILL PICK IT. YOU WILL NOT KNOW I HAVE DONE SO.");
					bot.sendMessage(event.getUser(), "THE KINDS OF WORDS I LIKE ARE VISIBLE VIA !lolback cats.");
					bot.sendMessage(event.getUser(), "WHEN I HAVE PICKED A WORD I WILL WAIT FOR A BIT.  THEN I WILL LISTEN.");
					bot.sendMessage(event.getUser(), "IF I SEE A WORD IN THE SAME CATEGORY AS THE WORD I HAVE PICKED, I WILL SAY SOMETHING.");
					bot.sendMessage(event.getUser(), "IF YOU THINK YOU KNOW THE WORD I PICKED !lolback");
			
			} else if (scanner.hasNext("cats") || scanner.hasNext("list") || scanner.hasNext("categories")) {
					String out = "";
					for (String s : wordpile.keySet()) {
						out += s+" ";
					}
					event.respond("I currently scan for words in the following categories: "+out);
			
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
				
				//first exit - are we in a cooldown?
				long diff = Calendar.getInstance().getTimeInMillis() - inceptionTime;
				if (diff < timeThreshhold || messagesSince < messageThreshhold || magicWord == null) {
					Long now = Calendar.getInstance().getTimeInMillis();
					Long then = now + 3600000L;
					lockouts.put(event.getUser(), then);
					event.respond("Still in cooldown from last lolback.  ONE HOUR DUNGEON.");
					return;
				}


				// second exit - is user locked out?
				Long lockout = lockouts.get(event.getUser());
				if (lockout == null) {
					lockout = 0L;
				}
				if (Calendar.getInstance().getTimeInMillis() < lockout) {
					event.respond("STILL IN DUNGEON.");
					return;
				}
				
				token = scanner.next();
				if (token.equals(magicWord)) {
					diff = Calendar.getInstance().getTimeInMillis() - inceptionTime;
					Long score = diff / 1000;
					Long newScore = score;
					
					//iterate existing users in score table for one with an equal nick
					for (User u : scoreTable.keySet()) {
						if (u.getNick().equals(event.getUser().getNick())) {
							newScore += scoreTable.get(u);
							scoreTable.remove(u);
						}
					}
					scoreTable.put(event.getUser(), newScore);

					event.respond("DING. "+event.getUser().getNick()+" wins the lolback for "+score+" points, putting them at "+newScore+" points. The active category was \""+activeCategory+"\".");
					event.respond("Message as seeded by "+injector+": " + originalMessage);
					magicWord = null;
					activeCategory = null;
					inceptionTime = Calendar.getInstance().getTimeInMillis();
					timeThreshhold = randomTimeThreshhold();
					messagesSince = 0;
					lockouts = new ConcurrentHashMap<User, Long>(); 
				} else {
					Long now = Calendar.getInstance().getTimeInMillis();
					Long then = now + 1800000L;
					lockouts.put(event.getUser(), then);
					event.respond("NOPE.  THIRTY MINUTES DUNGEON.");
				}
			} else {
				event.respond("generic status message");
			}

		} else {
			//start processing messages
		    if (!goSwitch) {return;} //if not turned on, just exit

			//clock one message on the counter
			messagesSince++;

			//check for blatant fishing
			if (isMessageFishing(event.getMessage(), fishingThreshhold)) {
				event.respond("NICE TRY, BUB.  ONE HOUR DUNGEON.");
				Long now = Calendar.getInstance().getTimeInMillis();
				Long then = now + 3600000L;
				lockouts.put(event.getUser(), then);
				return;
			}

			//first: are we missing a magic word?
			if (magicWord == null) {
			
				//if so, is it time to pick one?
				long diff = Calendar.getInstance().getTimeInMillis() - inceptionTime;
				if (diff >= timeThreshhold && messagesSince >= messageThreshhold) { //we're out of cooldown and in the market to pick a magic word
					
					//get the canonical message from the event
					originalMessage = event.getMessage();
					String message = originalMessage.toLowerCase().trim();
					
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
									injector = event.getUser().getNick();
									done = true;
									inceptionTime = Calendar.getInstance().getTimeInMillis();
									timeThreshhold = randomTimeThreshhold();
									messagesSince = 0;
									//System.out.println(magicWord+" is now the magic word");
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
							//if (wordtoken.equals(word) && !wordtoken.equals(magicWord) && !done) { //we have a hit
							if (wordtoken.equals(word) && !done) { //we have a hit
								bot.sendMessage(channel, "YOU DO GO ON MR. "+event.getUser().getNick().toUpperCase()); 
								//event.respond("YOU DO GO ON MR. "+event.getUser().getNick().toUpperCase());
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
			
			} else if (scanner.hasNext("rules")) {
					event.respond("THESE ARE THE RULES.");
					event.respond("AFTER AN AMOUNT OF TIME PASSES I WILL WATCH FOR WORDS.");
					event.respond("WHEN I SEE A WORD I LIKE I WILL PICK IT. YOU WILL NOT KNOW I HAVE DONE SO.");
					event.respond("THE KINDS OF WORDS I LIKE ARE VISIBLE VIA !lolback cats.");
					event.respond("WHEN I HAVE PICKED A WORD I WILL WAIT FOR A BIT.  THEN I WILL LISTEN.");
					event.respond("IF I SEE A WORD IN THE SAME CATEGORY AS THE WORD I HAVE PICKED, I WILL SAY SOMETHING.");
					event.respond("IF YOU THINK YOU KNOW THE WORD I PICKED !lolback");
			
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
					String channel = props.getProperty("irc_channel");
					Long now = Calendar.getInstance().getTimeInMillis();
					Long then = now + 3600000000L;
					lockouts.put(event.getUser(), then);
					bot.sendMessage(channel, event.getUser().getNick() + " called a PM debug command and is locked out until the next lolback starts.");
					event.respond("Current magic word is \""+magicWord+"\".  active category is \""+activeCategory+"\".");
					event.respond("Message count at "+messagesSince+"/"+messageThreshhold+". Timer at "+Calendar.getInstance().getTimeInMillis()+"/"+(inceptionTime+timeThreshhold)+".");
				}

			}

		}





	}
}
