import java.util.logging.Level;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

public class TSTest {

	public static void main(String[] args) throws Exception {
		final TS3Config config = new TS3Config();
		config.setHost("ts.shoat.org");
		config.setDebugLevel(Level.WARNING);
		config.setLoginCredentials("rosq", "LD5ssB9z");

		final TS3Query query = new TS3Query(config);
		query.connect();

		final TS3Api api = query.getApi();
		api.selectVirtualServerById(1);
		api.setNickname("SWAGMOWER");
		api.sendChannelMessage("PREDD IS A GIANT FUCKING DORK");

		while (true) {
			for (Client c : api.getClients()) {
				System.out.println(c.getNickname() + " in channel: "+ api.getChannelInfo(c.getChannelId()).getName());
			}
			Thread.sleep(60000);
		}
	}

}
