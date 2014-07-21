import java.util.HashMap;

class StarsGameState {

	private int year;
	private HashMap<Integer,Boolean> state;



	public StarsGameState() {
		year = 0;
		state = new HashMap<Integer, Boolean>();
	}

	public StarsGameState(int y, HashMap<Integer, Boolean> s) {
		this.year = y;
		this.state = s;
	}

	public int getYear() {
		return this.year;
	}

	public void setYear(int y) {
		this.year = y;
	}

	public HashMap<Integer, Boolean> getState() {
		return this.state;
	}
}
	
