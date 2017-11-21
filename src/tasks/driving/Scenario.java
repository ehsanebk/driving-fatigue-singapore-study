package tasks.driving;

/**
 * A class that defines the particular scenario represented by the driving
 * environment.
 * 
 * @author Dario Salvucci
 */
public class Scenario {
	private boolean curvedRoad;
	private boolean simcarConstantSpeed;
	private int simcarMPH;
	private boolean leadCarConstantSpeed;
	private int leadCarMPH;
	private boolean leadCarBrakes;
	private int drivingMinutes;
	private int timeBetweenTrials;

	Scenario() {
		curvedRoad = false;
		simcarConstantSpeed = false;
		simcarMPH = 0;
		leadCarConstantSpeed = true;
		leadCarMPH = 30;
		leadCarBrakes = false;
		drivingMinutes = 0;
		timeBetweenTrials = 0;
	}

	public boolean isCurvedRoad() {
		return curvedRoad;
	}

	public boolean isSimcarConstantSpeed() {
		return simcarConstantSpeed;
	}

	public boolean isLeadConstantSpeed() {
		return leadCarConstantSpeed;
	}

	public int getSimcarMPH() {
		return simcarMPH;
	}

	public boolean getLeadCarConstantSpeed() {
		return leadCarConstantSpeed;
	}

	public int getLeadCarMPH() {
		return leadCarMPH;
	}

	public boolean getLeadCarBrakes() {
		return leadCarBrakes;
	}

	public int getDrivingMinutes() {
		return drivingMinutes;
	}

	public int getTimeBetweenTrials() {
		return timeBetweenTrials;
	}

	String writeString() {
		String s = new String("");
		s += ((curvedRoad) ? 1 : 0) + "\t";
		s += ((simcarConstantSpeed) ? 1 : 0) + "\t";
		s += Integer.toString(simcarMPH) + "\t";
		s += ((leadCarConstantSpeed) ? 1 : 0) + "\t";
		s += Integer.toString(leadCarMPH) + "\t";
		s += ((leadCarBrakes) ? 1 : 0) + "\t";
		s += drivingMinutes + "\t";
		s += timeBetweenTrials;
		return s;
	}

	// static Scenario readString (MyStringTokenizer st)
	// {
	// Scenario s = new Scenario();
	// s.curvedRoad = (st.nextInt() == 1);
	// s.simCarConstantSpeed = (st.nextInt() == 1);
	// s.simCarMPH = st.nextInt();
	// s.leadCarConstantSpeed = (st.nextInt() == 1);
	// s.leadCarMPH = st.nextInt();
	// s.leadCarBrakes = (st.nextInt() == 1);
	// s.drivingMinutes = st.nextInt();
	// s.timeBetweenTrials = st.nextInt();
	// return s;
	// }
}
