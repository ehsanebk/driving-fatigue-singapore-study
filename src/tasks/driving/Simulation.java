package tasks.driving;

import java.util.Arrays;
import java.util.Vector;

import com.jogamp.opengl.GL2;

/**
 * A class that defines the entire simulation include driver, scenario, and
 * samples.
 * 
 * @author Dario Salvucci
 */
public class Simulation {
	private final double LANE_CENTER = 4.5;

	private Scenario scenario;
	private Driver driver;
	private Environment env = null;
	Vector<Sample> samples = new Vector<Sample>();
	private Results results = null;

	Simulation() {
		scenario = new Scenario();
		driver = new Driver("Driver", 25, 1.0f, 1.0f);
		env = new Environment(driver, scenario);
		samples.add(new Sample(env));
	}

	synchronized void update() {
		env.update();
		samples.add(new Sample(env));
	}

	public Scenario getScenario() {
		return scenario;
	}

	public Driver getDriver() {
		return driver;
	}

	public Environment getEnvironment() {
		return env;
	}

	Results getResults() {
		try {
			results = analyze();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}

	int numSamples() {
		return samples.size();
	}

	synchronized void draw(GL2 gl) {
		if (env != null)
			env.draw(gl);
	}

	double rotAngle(double hx, double hz) {
		return (-180 * (Math.atan2(hz, hx)) / Math.PI);
	}

	double headingError(Sample s) {
		Road.Segment s2 = env.getRoad().getSegment((int) s.getSimcarRoadIndex());
		Road.Segment s1 = env.getRoad().getSegment((int) s.getSimcarRoadIndex() - 1);
		Position rh = s2.middle.subtract(s1.middle);
		rh.normalize();
		return Math.abs(
				rotAngle(rh.getX(), rh.getZ()) - rotAngle(s.getSimcarHeading().getX(), s.getSimcarHeading().getZ()));
	}

	boolean lookingAhead(Sample s) {
		return true;
		// return (s != null && s.eyeLocation != null && s.eyeLocation.x < 350);
	}

	private Results analyze() throws Exception {

		// return null;
		// double startTime = 0;
		// double stopTime = -1000;

		@SuppressWarnings("unused")
		int numTasks = 0;
		int numTaskSamples = 0;
		@SuppressWarnings("unused")
		double sumTime = 0;
		double sumLatDev = 0;
		double sumSteeringDev = 0;
		double sumLatVel = 0;
		double sumSpeedDev = 0;

		double numSTEX3 = 0; // number of samples with steering angel exceeding
								// 3˚
		double numTaskDetects = 0, numTaskDetectsCount = 0;
		double sumBrakeRTs = 0, numBrakeRTs = 0, lastBrakeTime = 0;
		boolean brakeEvent = false;
		double[] headingErrors = new double[samples.size()];
		int laneViolations = 0;
		boolean lvDetected = false;

		double meanSteering = 0;
		for (int i = 0; i < samples.size(); i++) {
			meanSteering += Utilities.rad2deg(samples.elementAt(i).getSteerAngle());
		}
		meanSteering = meanSteering / samples.size();

		for (int i = 1; i < samples.size(); i++) {
			Sample s = samples.elementAt(i);
			Sample sprev = samples.elementAt(i - 1);

			// if ((s.event > 0) || (s.getTime() < stopTime + 5.0)) {

			numTaskSamples++;
			if (Utilities.rad2deg(s.getSteerAngle()) > 3.0)
				numSTEX3++;

			double latdev = 3.66 * (s.getSimcarLanePosition() - LANE_CENTER);
			sumLatDev += (latdev * latdev);

			sumSteeringDev = Math.abs(Math.pow((Utilities.rad2deg(s.getSteerAngle()) - meanSteering), 2));

			sumLatVel += Math.abs(
					(3.66 * (s.getSimcarLanePosition() - sprev.getSimcarLanePosition())) / Environment.SAMPLE_TIME);

			sumSpeedDev += (s.getSimcarSpeed() - s.getAutocarSpeed()) * (s.getSimcarSpeed() - s.getAutocarSpeed());

			// if ((s.event > 0) || (s.time < stopTime)) {
			//
			// numTaskDetectsCount++;
			// if (lookingAhead(s)) {
			// numTaskDetects += 1;
			// // if (s.listening) numTaskDetects -= .1;
			// }
			// // if (s.inDriveGoal) numTaskDetects ++;
			// }

			// if ((s.event > 0) || (s.time < stopTime))
			// {
			// if (((s.event > 0) || (s.time < stopTime)) && !brakeEvent
			if (!brakeEvent && (s.getAutocarBraking() && !sprev.getAutocarBraking())) {
				brakeEvent = true;
				lastBrakeTime = s.time;
			}
			if (brakeEvent && !s.getAutocarBraking()) {
				brakeEvent = false;
			}
			if (brakeEvent && (s.getBrake() > 0)) {
				// System.out.println ("BrakeRT: " + (s.time -
				// lastBrakeTime));
				sumBrakeRTs += (s.time - lastBrakeTime);
				numBrakeRTs++;
				brakeEvent = false;
			}
			// }

			headingErrors[numTaskSamples - 1] = headingError(s);
			if (!lvDetected && (Math.abs(latdev) > (1.83 - 1.0))) {
				laneViolations++;
				lvDetected = true;
			}
		}

		// if ((s.event == 1) && (sprev.event == 0)) {
		// startTime = s.time;
		// lvDetected = false;
		// brakeEvent = false;
		// } else if ((s.event == 0) && (sprev.event == 1)) {
		// numTasks++;
		// stopTime = s.time;
		// sumTime += (stopTime - startTime);
		// }

		// }

		Results r = new Results();
		// r.ifc = ifc;
		// r.task = task;
		r.driver = driver;
		// if (r.task.numActions() > 0) r.taskTime = sumTime / numTasks;
		// else r.taskTime = 0;
		r.taskLatDev = Math.sqrt(sumLatDev / numTaskSamples);
		r.taskLatVel = sumLatVel / numTaskSamples;
		r.taskSpeedDev = Math.sqrt(sumSpeedDev / numTaskSamples);

		r.detectionError = (numTaskSamples == 0) ? 0 : (1.0 - (1.0 * numTaskDetects / numTaskDetectsCount));

		r.brakeRT = (numBrakeRTs == 0) ? 0 : (sumBrakeRTs / numBrakeRTs);

		Arrays.sort(headingErrors, 0, numTaskSamples - 1);
		int heIndex = (int) (0.9 * numTaskSamples);
		r.headingError = headingErrors[heIndex];

		r.laneViolations = laneViolations;
		r.STEX3 = numSTEX3 / numTaskSamples * 100;
		r.taskSteeringDev = Math.sqrt(sumSteeringDev / numTaskSamples);

		return r;
	}
}
