import java.util.Comparator;
import org.pircbotx.User;
import java.util.Map;
import java.util.ArrayList;

class LolbackElementComparator implements Comparator<User> {
	
	private Map<User, Long> scoreTable;

	public LolbackElementComparator (Map<User, Long> st) {
		super();
		this.scoreTable = st;
	}

	public int compare(User u1, User u2) {
		Long score1 = scoreTable.get(u1);
		Long score2 = scoreTable.get(u2);
		
		if (score1 == null && score2 != null) {
			return 1;
		} else if (score1 != null && score2 == null) {
			return -1;
		} else if (score1 == null && score2 == null) {
			return 0;
		} else if (score1 < score2) {
			return 1;
		} else if (score1 > score2) {
			return -1;
		} else {
			return 0;
		}
	}

}


