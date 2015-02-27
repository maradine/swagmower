import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

public class Swagmower extends ListenerAdapter {
	
	
	private static String propsArg = null;
	private static FileInputStream fis = null;
	private static Properties props = null;


	public static void reloadProps() throws IOException {
		try {
			fis = new FileInputStream(propsArg);
		} catch (IOException ioe) {
			//something's happened to our file since we invoked.
			System.out.println("Attempted to relaod file input stream from argv file - ioexception");
			throw ioe;
		}
		props.load(fis);
	}
	
		
	public static void main(String[] args) throws Exception {
		
		//instantiate underlying bot
		PircBotX bot = new PircBotX();

		//load properties from disk
		props = new Properties();
		if (args.length > 0) {
			propsArg = args[0];
		} else {
			propsArg = "swagmower.properties";
		}
		try {
			fis = new FileInputStream(propsArg);
		} catch (IOException ioe) {
			System.out.println("Can't find swagmower.proprties in local directory.");
			System.out.println("Wrting out example file and terminating.");
			System.out.println("Modify this file and re-run.");
			
			try {
				props.setProperty("irc_server", "irc.slashnet.org");
				props.setProperty("irc_channel", "#planetside2");
				props.setProperty("botnick", "ps2bot");
				props.setProperty("nickpass", "");
				props.setProperty("ownernick", "");
				props.setProperty("channelpass", "");
				props.setProperty("ts3_user", "");
				props.setProperty("ts3_pass", "");
				props.setProperty("ts3_server", "");
		
				props.store(new FileOutputStream("swagmower.properties.example"), null);
			} catch (IOException ioe2) {
				System.out.println("There was an error writing to the filesystem.");
			}
			System.exit(1);

		} 			
		props.load(fis);
		if (!props.containsKey("irc_server") || !props.containsKey("irc_channel") || !props.containsKey("botnick")) {
			System.out.println("Config file is incomplete.  Delete it to receive a working template.");
			System.exit(1);
		}
		String ircServer = props.getProperty("irc_server");
		String ircChannel = props.getProperty("irc_channel");
		String botnick = props.getProperty("botnick");
		String ownernick = props.getProperty("ownernick");
		
		//seed the permissions manager
		PermissionsManager pm = PermissionsManager.initInstance(ownernick);
		
		//add misc listeners
		bot.getListenerManager().addListener(new BanterBox());
		bot.getListenerManager().addListener(new PermissionsHandler());

		//connect
		bot.setVerbose(true);
		bot.setName(botnick);
		bot.connect(ircServer);
		
		//identify with nickserv if so enabled
		String nickpass = props.getProperty("nickpass");
		if (nickpass != null) {
			if (!nickpass.isEmpty()) {
				bot.identify(nickpass);
			}
		}

		//join channel, passing key if needed
		String channelpass = props.getProperty("channelpass");
		if (channelpass==null || channelpass.equals("")) {
			bot.joinChannel(ircChannel);
		} else {
			bot.joinChannel(ircChannel, channelpass);
		}
		
		//pause to let channel join complete.  If we failed, exit.	
		Thread.sleep(5000);
		if (!bot.channelExists(ircChannel)) {
			System.out.println("*** Bot failed to connect to channel \""+ircChannel+"\".  Either the key is wrong, or the server is experiencing unusual load.");
			bot.shutdown(true);
		}

		
		//set up announcement engine
		AnnouncementEngine ae = new AnnouncementEngine(bot, ircChannel);
		Thread at = new Thread(ae, "at");
		at.start();

		//link announcement handler
		bot.getListenerManager().addListener(new AnnouncementHandler(ae, at));

		//set up Tonight engine
		TonightEngine te = new TonightEngine(bot, props);
		Thread tt = new Thread(te, "tt");
		tt.start();

		//link tonight handler
		bot.getListenerManager().addListener(new TonightHandler(bot, te, props));
		
		//set up stars engine
		StarsEngine se = new StarsEngine(bot, props);
		Thread st = new Thread(se, "st");
		st.start();

		//link stars handler
		bot.getListenerManager().addListener(new StarsHandler(se, props));
		
		//set up presence engine
		TS3PresenceEngine pe = new TS3PresenceEngine(bot, ircChannel, props);

		//link presence handler
		bot.getListenerManager().addListener(new TS3PresenceHandler(pe, props));

		//link general command handler
		bot.getListenerManager().addListener(new GeneralHandler(bot));
	
		//link lolback handler
		bot.getListenerManager().addListener(new LolbackHandler(bot, props));
		
		//link speech handler
		bot.getListenerManager().addListener(new SpeechHandler(bot,ircChannel));

		//link url shortener
		bot.getListenerManager().addListener(new BitlyHandler(bot, props));
	}
}

