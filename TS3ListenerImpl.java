import com.github.theholywaffle.teamspeak3.api.event.*;

public class TS3ListenerImpl implements TS3Listener {
	private TS3PresenceEngine parent;

	public TS3ListenerImpl(TS3PresenceEngine tpe) {
		this.parent = tpe;
	}

	public void onClientJoin(ClientJoinEvent e) {
		this.parent.clientJoin(e.getClientId());
	}

	public void onClientMoved(ClientMovedEvent e) {
		this.parent.clientMoved(e.getClientId());
	}

	public void onClientLeave(ClientLeaveEvent e) {
		this.parent.clientLeft(e.getClientId());
	}

	public void onTextMessage(TextMessageEvent e) {}
	public void onServerEdit(ServerEditedEvent e) {}
	public void onChannelEdit(ChannelEditedEvent e) {}
	public void onChannelDescriptionChanged(ChannelDescriptionEditedEvent e) {}
}
