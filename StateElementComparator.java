import java.util.Comparator;
import org.pircbotx.User;
import java.util.HashMap;
import java.util.ArrayList;

class StateElementComparator implements Comparator<String> {
	
	private HashMap<String, ArrayList<User>> tonightState;

	public StateElementComparator (HashMap<String, ArrayList<User>> ts) {
		super();
		this.tonightState = ts;
	}

	public int compare(String s1, String s2) {
		int size1 = 0;
		int size2 = 0;
		ArrayList<User> al1 = tonightState.get(s1);
		ArrayList<User> al2 = tonightState.get(s2);
		
		if (al1 == null && al2 != null) {
			return 1;
		} else if (al1 != null && al2 == null) {
			return -1;
		} else if (al1 == null && al2 == null) {
			return 0;
		} else if (al1.size() < al2.size()) {
			return 1;
		} else if (al1.size() > al2.size()) {
			return -1;
		} else {
			return 0;
		}
	}

}


