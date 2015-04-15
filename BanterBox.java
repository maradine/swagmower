import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import java.util.ArrayList;
import java.util.Random;

public class BanterBox extends ListenerAdapter {

	private ArrayList<String> triggerCommands;
	private ArrayList<String> outputSnark;
	private Random rand;

	public BanterBox() {
		
		triggerCommands = new ArrayList<String>();
		outputSnark = new ArrayList<String>();

		
		triggerCommands.add("!dicebag");
		triggerCommands.add("!dickbag");

		outputSnark.add("You think you're clever, don't you.");
		outputSnark.add("Does not compute.");
		outputSnark.add("Sod off.");
		outputSnark.add("I don't feel like that's a productive use of my time.");
		outputSnark.add("Come on.  Really?");
		outputSnark.add("Is that why you think I'm here?");
		outputSnark.add("I should kick you for that.");
		outputSnark.add("Go away.");
		outputSnark.add("(╯°□°）╯︵ ┻━┻");
		outputSnark.add("(ノಠ益ಠ)ノ彡┻━┻");

		rand = new Random();
	}


	public void onMessage(MessageEvent event) {
		
		String rawcommand = event.getMessage();
		String command = rawcommand.toLowerCase();
		
		if (command.startsWith("!presents ") || command.equals("!presents")) {
			event.respond("(ノಠ益ಠ)ノ彡┻━┻");

		} else if (command.startsWith("!preddance ") || command.equals("!preddance") || command.startsWith("!preddence ") || command.equals("!preddence")) {
			event.respond("THIS IS A POEM ABOUT PREDD:");
			event.respond("");
			event.respond("i never attend /");
			event.respond("your collective labors bore /");
			event.respond("autumn in the bronx");
		} else if (command.startsWith("!pokemon") || command.equals("!pokemon")) {
			event.respond("http://images.wikia.com/pokemontowerdefense/images/9/96/Pokemon-list.png");
		
		} else if (command.startsWith("!pokemans") || command.equals("!pokemams")) {
			event.respond("http://images.wikia.com/pokemontowerdefense/images/9/96/Pokemon-list.png");
		
		} else if (command.contains("nuestra") || command.contains("fiesta")){
			event.getBot().sendMessage(event.getChannel(), "ESTA ES NUESTRA FIESTA");
		
		} else if (command.contains("gabba")) {
			event.getBot().sendMessage(event.getChannel(), "GABBA GABBA GABBA HEY");
		
		} else if (command.contains("uno dos tres cuatro") || command.contains("un dos tres cuatro")) {
			event.getBot().sendMessage(event.getChannel(), "DEET DEET DEET DEET DEET DEET DEET");
			event.getBot().sendMessage(event.getChannel(), "DEET DEET DEET DEET DEET DEET DEET");
			event.getBot().sendMessage(event.getChannel(), "DEET DEET DEET DEET DEET DEET DEET");
			event.getBot().sendMessage(event.getChannel(), "DEET DEET DEET DEET DEET DEET DEET");
		
		} else if (command.contains("on fire")) {
			event.respond("SIR YOU ARE ON FIRE");
		
		} else {
			boolean returnFire = false;
			for (String triggerString : triggerCommands) {
				if (event.getMessage().startsWith(triggerString)) {
					returnFire = true;
				}
			}
			
			if (returnFire) {
	
				int size = outputSnark.size();
				int index = rand.nextInt(size);
				event.respond(outputSnark.get(index));
			}
		}
	}
}
