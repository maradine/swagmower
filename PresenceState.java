public class PresenceState {
	public int clientID;
	public String channel;
	public String nickname;

	public PresenceState(int clientID, String nickname, String channel) {
		this.clientID = clientID;
		this.nickname = nickname;
		this.channel = channel;
	}
}
