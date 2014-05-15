import org.pircbotx.PircBotX;
import org.pircbotx.Colors;
import org.pircbotx.User;
import java.io.IOException;
import java.util.Properties;
import java.util.Calendar;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.Set;
import java.util.List;

public class StarsEngine implements Runnable {

	private PircBotX bot;
	private String channel;
	private boolean onSwitch;
	private Properties props;

	public StarsEngine(PircBotX bot, Properties props) {
		this.bot = bot;
		this.props = props;
		this.channel = props.getProperty("irc_channel");
		onSwitch = true;
	}
	
	
	public void turnOn() {
		onSwitch = true;
	}

	public void turnOff() {
		onSwitch = false;
	}

	public void run() {
		while (true) {
			try {
				//bot.sendMessage(channel, "Entering run() block.");
				Calendar now;
				
				//are we even turned on?
				if (onSwitch) {
					//what time is it?
					/*
					now = Calendar.getInstance();
					int nowHours = now.get(Calendar.HOUR_OF_DAY);
					int nowMinutes = now.get(Calendar.MINUTE);
					if (nowHours == flushHour && nowMinutes == 0) {
						//flushTonight();
						bot.sendMessage(channel, "A new day dawns!");
					}
					*/

				} else {
					//bot.sendMessage(channel, "Turned off, doing nothing.");
				}					
				
				//caluclate time to next minute
				now = Calendar.getInstance();
				int millis = 1000 - now.get(Calendar.MILLISECOND);
				int seconds = 59 - now.get(Calendar.SECOND);
				int sleepMillis = millis + (1000 * seconds);

				//sleep
				//bot.sendMessage(channel, "Sleeping for "+sleepMillis+" millis.");
				Thread.sleep(sleepMillis);


			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
				bot.sendMessage(channel, "Totally unhandled exception - check the live feed, sparky.");
			}
		}
	}
}
