package tasks.driving;

import java.text.DecimalFormat;

/**
 * A class that defines useful measures for data collection.
 * 
 * @author Dario Salvucci
 */
public class Results {
	Driver driver;

	public double taskTime;
	public double taskLatDev;
	public double taskLatVel;
	public double headingError;
	public int laneViolations;
	public double taskSpeedDev;
	public double detectionError;
	public double brakeRT;
	public double STEX3; // percentage of samples with steering angel exceeding
							// 3˚
	public double taskSteeringDev;
	DecimalFormat df3 = new DecimalFormat("#.000");

	@Override
	public String toString() {
		return "taskTime     \t" + df3.format(taskTime) + "\n" + "taskLatDev  \t" + df3.format(taskLatDev) + "\n"
				+ "taskLatVel  \t" + df3.format(taskLatVel) + "\n" + "brakeRT     \t" + df3.format(brakeRT) + "\n"
				+ "headingError\t" + df3.format(headingError) + "\n" + "taskSpeedDev\t" + df3.format(taskSpeedDev)
				+ "\n" + "STEX3       \t" + df3.format(STEX3) + "\n" + "SteeringDev \t" + df3.format(taskSteeringDev)
				+ "\n";

	}

}
