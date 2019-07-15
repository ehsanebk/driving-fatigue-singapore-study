package tasks.driving;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.JLabel;
import actr.task.Result;
import actr.task.Task;

/**
 * The main Driving task class that sets up the simulation and starts periodic
 * updates.
 * 
 * @author Dario Salvucci, Ehsan Khosroshahi
 * 
 * LPSD  : Standard deviation of lane position from the center of the lane in ft 
 * SWASD : Standard deviation of steering wheel angle 
 * SSD   : Standard deviation of speed in miles per hour (MPH)
 * 
 */
public class Driving_B extends Task {
	// --- Task Code ---//
	
	private Simulation currentSimulation;
	private JLabel nearLabel, carLabel, keypad;

	private final double scale = .40;
	private final double steerFactor_dfa = (16 * scale);
	private final double steerFactor_dna = (4 * scale);
	private final double steerFactor_na = (3 * scale); // 3 orig
	private final double accelFactor_thw = (1 * .40); // 1 .40 orig, 3?
	private final double accelFactor_dthw = (3 * .40); // 3 .40 orig, 5?
	private final double steerNaMax = .07; //.07 orig
	private final double thwFollow = 1.0; // 1.0 orig

	private double simulationDurarion = 0; // the driving sessions are 30 min (30 * 60sec)
	private ArrayList<Double> timesOfSimulation;
	private final double simulationDistance = 45061.6;  // equal to 28 miles

	private double accelBrake = 0, speed = 0;

	private static final int minX = 174, maxX = (238 + 24), minY = 94, maxY = (262 + 32);
	static final int centerX = (minX + maxX) / 2, centerY = (minY + maxY) / 2;

	private static Simulator simulator = null;

	int simulationNumber = 0;
	double simulationStartTime = 0;
	private Vector<Results> results = new Vector<Results>();
	private boolean completed;
	private Vector<int []> microLapses = new Vector<int []>(); // [ number of micro lapses , number of total productions ] 
	private int currentSimulation_NumberOf_MicroLapses=0;
	private int currentSimulation_NumberOf_Productions=0;
	

	
	int c=0;
	
	public Driving_B() {
		super();
		nearLabel = new JLabel(".");
		carLabel = new JLabel("X");
		keypad = new JLabel("*");	
	}

	@Override
	public void start() {

		
		completed = true;
		currentSimulation = new Simulation();

		simulationDurarion = getModel().getFatigue().getTaskDuration(); // the driving sessions are 30 min (30 * 60sec)
		timesOfSimulation = getModel().getFatigue().getTaskSchdule();
		
		getModel().getFatigue().setFatigueStartTime(timesOfSimulation.get(simulationNumber));
		getModel().getFatigue().startFatigueSession();

		if (getModel().getRealTime()) {
			if (simulator == null)
				simulator = new Simulator();
			simulator.useSimulation(currentSimulation);
			setLayout(new BorderLayout());
			add(simulator, BorderLayout.CENTER);
			setVisible(false); // trigger OpenGL init
			setVisible(true);
		} else {

			add(nearLabel);
			nearLabel.setSize(20, 20);
			nearLabel.setLocation(250, 250);
			add(carLabel);
			carLabel.setSize(20, 20);
			carLabel.setLocation(250, 250);
			add(keypad);
			keypad.setSize(20, 20);
			int keypadX = 250 + (int) (actr.model.Utilities.angle2pixels(10.0));
			keypad.setLocation(keypadX, 250);

		}

		getModel().runCommand("(set-visual-frequency near .1)");
		getModel().runCommand("(set-visual-frequency far .1)");
		getModel().runCommand("(set-visual-frequency car .1)");

		accelBrake = 0;
		speed = 0;

		getModel().getVision().addVisual("near", "near", "near", nearLabel.getX(), nearLabel.getY(), 1, 1, 10);
		getModel().getVision().addVisual("car", "car", "car", carLabel.getX(), carLabel.getY(), 1, 1, 100);
		getModel().getVision().addVisual("keypad", "keypad", "keypad", keypad.getX(), keypad.getY(), 1, 1);

		addPeriodicUpdate(Environment.SAMPLE_TIME);
	}

	@Override
	public void update(double time) {
		try {
			if (currentSimulation.getEnvironment().getSimcar().getIndex() <= simulationDistance) {
				currentSimulation.getEnvironment().setTime(time - simulationStartTime);
				currentSimulation.update();
				updateVisuals();
				c++;
				if (getModel().getProcedural().isMicroLapse())
					currentSimulation_NumberOf_MicroLapses++;
				currentSimulation_NumberOf_Productions++;
				
				// in case the car position is out of lane
				if (currentSimulation.samples.lastElement().getSimcarLanePosition()<3
						|| currentSimulation.samples.lastElement().getSimcarLanePosition()>6)
				{
					System.out.println("car out of lane !!!");
					completed = false;
					results.add(currentSimulation.getResults());
					int [] l = {currentSimulation_NumberOf_MicroLapses,currentSimulation_NumberOf_Productions} ;
					currentSimulation_NumberOf_MicroLapses = 0;
					currentSimulation_NumberOf_Productions = 0;
					microLapses.add(l);
					getModel().stop();
				}

				if (simulator != null)
					simulator.repaint();

			} else {
				System.out.println(simulationNumber);
				results.add(currentSimulation.getResults());
				int [] l = {currentSimulation_NumberOf_MicroLapses,currentSimulation_NumberOf_Productions} ;
				currentSimulation_NumberOf_MicroLapses = 0;
				currentSimulation_NumberOf_Productions = 0;
				microLapses.add(l);
				simulationNumber++;
				// go to the next simulation or stop the model
				if (simulationNumber < timesOfSimulation.size()) {
					currentSimulation = new Simulation();
					simulationStartTime = time;
					getModel().getFatigue().setFatigueStartTime(timesOfSimulation.get(simulationNumber));
					getModel().getFatigue().startFatigueSession();

					removeAll();

					add(nearLabel);
					nearLabel.setSize(20, 20);
					nearLabel.setLocation(250, 250);
					add(carLabel);
					carLabel.setSize(20, 20);
					carLabel.setLocation(250, 250);
					add(keypad);
					keypad.setSize(20, 20);
					int keypadX = 250 + (int) (actr.model.Utilities.angle2pixels(10.0));
					keypad.setLocation(keypadX, 250);

					accelBrake = 0;
					speed = 0;

					getModel().getVision().addVisual("near", "near", "near", nearLabel.getX(), nearLabel.getY(), 1, 1,
							10);
					getModel().getVision().addVisual("car", "car", "car", carLabel.getX(), carLabel.getY(), 1, 1, 100);
					getModel().getVision().addVisual("keypad", "keypad", "keypad", keypad.getX(), keypad.getY(), 1, 1);

				} else {
					getModel().stop();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void updateVisuals() {
		Environment env = currentSimulation.getEnvironment();
		if (env.getSimcar().getNearPoint() != null) {
			Coordinate cn = env.world2image(env.getSimcar().getNearPoint());
			Coordinate cc = env.world2image(env.getSimcar().getCarPoint());
			if (cn == null || cc == null)
				env.setDone(true);
			else {
				nearLabel.setLocation(cn.getX(), cn.getY());
				carLabel.setLocation(cc.getX(), cc.getY());
				getModel().getVision().moveVisual("near", cn.getX() + 5, cn.getY() + 10, cn.getD());
				getModel().getVision().moveVisual("car", cc.getX() + 5, cc.getY() + 10, cc.getD());
				speed = env.getSimcar().getSpeed();
			}
		}
	}

	double minSigned(double x, double y) {
		return (x >= 0) ? Math.min(x, y) : Math.max(x, -y);
	}

	boolean isCarStable(double na, double nva, double fva) {
		double f = 0.5; //2.5;
		return (Math.abs(na) < .025 * f) && (Math.abs(nva) < .0125 * f) && (Math.abs(fva) < .0125 * f);
	}

	double image2angle(double x, double d) {
		Environment env = currentSimulation.getEnvironment();
		double px = env.getSimcar().getPX() + (env.getSimcar().getHX() * d);
		double pz = env.getSimcar().getPZ() + (env.getSimcar().getHZ() * d);
		Coordinate im = env.world2image(new Position(px, pz));
		try {
			return Math.atan2(.5 * (x - im.getX()), 450);
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public void eval(Iterator<String> it) {
		it.next(); // (
		String cmd = it.next();
		if (cmd.equals("do-steer")) {
			double na = Double.valueOf(it.next());
			double dna = Double.valueOf(it.next());
			double dfa = Double.valueOf(it.next());
			double dt = Double.valueOf(it.next());
			doSteer(na, dna, dfa, dt);
		} else if (cmd.equals("do-accelerate")) {
			double fthw = Double.valueOf(it.next());
			double dthw = Double.valueOf(it.next());
			double dt = Double.valueOf(it.next());
			doAccelerate(fthw, dthw, dt);
		} else if (cmd.equals("fatigue-reset-percentage")) {
			fatigueResetPercentage();
		}
	}
	
	// calling percentage reset after any new task presentation (audio or visual)
	void fatigueResetPercentage() {
		getModel().getFatigue().fatigueResetPercentages();
		if (getModel().isVerbose())
			getModel().output("!!!! Fatigue Percentage Reset !!!!");
	}

	@Override
	public boolean evalCondition(Iterator<String> it) {
		it.next(); // (
		String cmd = it.next();
		if (cmd.equals("is-car-stable") || cmd.equals("is-car-not-stable")) {
			double na = Double.valueOf(it.next());
			double nva = Double.valueOf(it.next());
			double fva = Double.valueOf(it.next());
			boolean b = isCarStable(na, nva, fva);
			return cmd.equals("is-car-stable") ? b : !b;
		} else
			return false;
	}

	@Override
	public double bind(Iterator<String> it) {
		try {
			it.next(); // (
			String cmd = it.next();
			if (cmd.equals("image->angle")) {
				double x = Double.valueOf(it.next());
				double d = Double.valueOf(it.next());
				return image2angle(x, d);
			} else if (cmd.equals("mp-time"))
				return currentSimulation.getEnvironment().getTime();
			else if (cmd.equals("get-thw")) {
				double fd = Double.valueOf(it.next());
				double v = Double.valueOf(it.next());
				double thw = (v == 0) ? 4.0 : fd / v;
				return Math.min(thw, 4.0);
			} else if (cmd.equals("get-velocity"))
				return speed;
			else
				return 0;

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
			return 0;
		}
	}

	void doSteer(double na, double dna, double dfa, double dt) {
		Simcar simcar = currentSimulation.getEnvironment().getSimcar();
		if (simcar.getSpeed() >= 10.0) {
			double dsteer = (dna * steerFactor_dna) + (dfa * steerFactor_dfa)
					+ (minSigned(na, steerNaMax) * steerFactor_na * dt);
			dsteer *= currentSimulation.getDriver().getSteeringFactor();
			simcar.setSteerAngle(simcar.getSteerAngle() + dsteer);
		} else
			simcar.setSteerAngle(0);
	}

	void doAccelerate(double fthw, double dthw, double dt) {
		Simcar simcar = currentSimulation.getEnvironment().getSimcar();
		if (simcar.getSpeed() >= 10.0) {
			double dacc = (dthw * accelFactor_dthw) + (dt * (fthw - thwFollow) * accelFactor_thw);
			accelBrake += dacc;
			accelBrake = minSigned(accelBrake, 1.0);
		} else {
			accelBrake = .65 * (currentSimulation.getEnvironment().getTime() / 3.0);
			accelBrake = minSigned(accelBrake, .65);
		}
		simcar.setAccelerator((accelBrake >= 0) ? accelBrake : 0);
		simcar.setBrake((accelBrake < 0) ? -accelBrake : 0);
	}

	// @Override
	// public void finish() {
	// simulator.stop();
	// }

	// number of participants in the experiment is 13 for night condition
	@Override
	public int analysisIterations() {
 		return 13;
	}

	public static Image getImage(final String name) {
		URL url = Driving_B.class.getResource("images/" + name);
		return Toolkit.getDefaultToolkit().getImage(url);
	}

	/**
	 * 
	 * LPSD  : Standard deviation of lane position from the center of the lane in ft 
	 * SWASD : Standard deviation of steering wheel angle 
	 * SSD   : Standard deviation of speed in miles per hour (MPH)
	 */
	@Override
	public Result analyze(Task[] tasks, boolean output) {
		
		Result result = new Result();
		///////////////////////////////////////////////////////////////////////////////////////////////////		
		// Writing the output to csv files in the specified directory (outputDIR)
		String DIR = getModel().getFatigue().getOutputDIR();

		if (DIR == null)
			return result;
		
		PrintWriter outputCSV = null;
		
		
		File out = new File(DIR + "/"  +"Results_Model_TimePoints_Cumulative.csv");

		if (!new File(out.getParent()).exists()){
			getModel().output("The output file path is not valid!!");
			getModel().stop();
		}
		
		// for output the data
		try {
			outputCSV = new PrintWriter(out);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		int numberOfSimulations = ((Driving_B)tasks[0]).results.size();
		
		Values[] totalLatDev = new Values[numberOfSimulations];
		Values[] totalSteeringDev = new Values[numberOfSimulations];
		Values[] totalSpeedDev = new Values[numberOfSimulations];
		Values[][] totalLatDev_10seg = new Values[numberOfSimulations][10];
		Values[][] totalSteeringDev_10seg = new Values[numberOfSimulations][10];
		Values[][] totalSpeedDev_10seg = new Values[numberOfSimulations][10];
		for (int i = 0; i < numberOfSimulations; i++){
			totalLatDev[i] = new Values();
			totalSteeringDev[i] = new Values();
			totalSpeedDev[i] = new Values();
			for (int j = 0; j < 10; j++) {
				totalLatDev_10seg[i][j] = new Values();
				totalSteeringDev_10seg[i][j] = new Values();
				totalSpeedDev_10seg[i][j] = new Values();
			}
		}
		
		for (Task taskCast : tasks) {
			Driving_B task = (Driving_B) taskCast;
			if (!task.completed)
				continue;
			for (int i = 0; i < numberOfSimulations; i++) {
				Results results = task.results.elementAt(i);
				totalLatDev[i].add(Utilities.arrayAverage(results.taskLatDev_10Segments,2,10));
				totalSteeringDev[i].add(Utilities.arrayAverage(results.taskSteeringDev_10Segments,2,10));
				totalSpeedDev[i].add(Utilities.arrayAverage(results.taskSpeedDev_10Segments,2,10));
				for (int j = 0; j < 10; j++) {
					totalLatDev_10seg[i][j].add(results.taskLatDev_10Segments[j]);
					totalSteeringDev_10seg[i][j].add(results.taskSteeringDev_10Segments[j]);
					totalSpeedDev_10seg[i][j].add(results.taskSpeedDev_10Segments[j]);
				}
			}
		}
		
		DecimalFormat df = new DecimalFormat("#.0000");
		
		//session #
		getModel().outputInLine("Session #\t");
		outputCSV.print("Session #,");			
		for (int i = 0; i < numberOfSimulations; i++) {
			getModel().outputInLine((i+1) +"\t");
			outputCSV.print((i+1) +",");
		}
		getModel().outputInLine("\n");
		outputCSV.print("\n");
		
		//LP_STD 
		getModel().outputInLine("LPSD\t");
		outputCSV.print("LPSD,");			
		for (int i = 0; i < numberOfSimulations; i++) {
			getModel().outputInLine(df.format( totalLatDev[i].average() ) +"\t");
			outputCSV.print(df.format(totalLatDev[i].average())+",");
		}
		getModel().outputInLine("\n");
		outputCSV.print("\n");
		
		//Steering_STD 
		getModel().outputInLine("SWASD\t");
		outputCSV.print("SWASD,");			
		for (int i = 0; i < numberOfSimulations; i++) {
			getModel().outputInLine(df.format(totalSteeringDev[i].average()) +"\t");
			outputCSV.print(df.format(totalSteeringDev[i].average())+",");
		}
		getModel().outputInLine("\n");
		outputCSV.print("\n");
		
		//MPH_STD 
		getModel().outputInLine("SSD\t");
		outputCSV.print("SSD,");			
		for (int i = 0; i < numberOfSimulations; i++) {
			getModel().outputInLine(df.format(totalSpeedDev[i].average()) +"\t");
			outputCSV.print(df.format(totalSpeedDev[i].average()) +",");
		}
		getModel().outputInLine("\n");
		outputCSV.print("\n");
		outputCSV.flush();
		
		getModel().outputInLine("\n");
		outputCSV.print(",\n");
		outputCSV.flush();

		//LP_STD SD
		getModel().outputInLine("LPSD SD\t");
		outputCSV.print("LPSD SD,");			
		for (int i = 0; i < numberOfSimulations; i++) {
			getModel().outputInLine(df.format( totalLatDev[i].stddev() ) +"\t");
			outputCSV.print(df.format(totalLatDev[i].stddev())+",");
		}
		getModel().outputInLine("\n");
		outputCSV.print("\n");

		//Steering_STD SD 
		getModel().outputInLine("SWASD SD\t");
		outputCSV.print("SWASD SD,");			
		for (int i = 0; i < numberOfSimulations; i++) {
			getModel().outputInLine(df.format(totalSteeringDev[i].stddev()) +"\t");
			outputCSV.print(df.format(totalSteeringDev[i].stddev())+",");
		}
		getModel().outputInLine("\n");
		outputCSV.print("\n");

		//MPH_STD SD
		getModel().outputInLine("SSD SD\t");
		outputCSV.print("SSD SD,");			
		for (int i = 0; i < numberOfSimulations; i++) {
			getModel().outputInLine(df.format(totalSpeedDev[i].stddev()) +"\t");
			outputCSV.print(df.format(totalSpeedDev[i].stddev()) +",");
		}
		getModel().outputInLine("\n");
		outputCSV.print("\n");
		outputCSV.flush();

		getModel().outputInLine("\n");
		outputCSV.print(",\n");
		outputCSV.flush();

		outputCSV.println(",");
		outputCSV.println("******* Average for Segments **********");
		for (int i = 2; i < 10; i++) {
			outputCSV.print("LPSD Seg " + i);
			for (int j = 0; j < numberOfSimulations; j++) {
				outputCSV.print("," + totalLatDev_10seg[j][i].mean());
			}
			outputCSV.print("\n");
		}
		outputCSV.flush();

		for (int i = 2; i < 10; i++) {
			outputCSV.print("SWASD Seg " + i);
			for (int j = 0; j < numberOfSimulations; j++) {
				outputCSV.print("," + totalSteeringDev_10seg[j][i].mean());
			}
			outputCSV.print("\n");
		}
		outputCSV.flush();
		

		for (int i = 2; i < 10; i++) {
			outputCSV.print("SSD Seg " + i);
			for (int j = 0; j < numberOfSimulations; j++) {
				outputCSV.print("," + totalSpeedDev_10seg[j][i].mean());
			}
			outputCSV.print("\n");
		}
		outputCSV.flush();
		
		outputCSV.print(", \n");
		outputCSV.flush();


		for (int i = 2; i < 10; i++) {
			outputCSV.print("LPSD Seg SD" + i);
			for (int j = 0; j < numberOfSimulations; j++) {
				outputCSV.print("," + totalLatDev_10seg[j][i].stddev());
			}
			outputCSV.print("\n");
		}
		outputCSV.flush();

		for (int i = 2; i < 10; i++) {
			outputCSV.print("SWASD Seg SD" + i);
			for (int j = 0; j < numberOfSimulations; j++) {
				outputCSV.print("," + totalSteeringDev_10seg[j][i].stddev());
			}
			outputCSV.print("\n");
		}
		outputCSV.flush();
		

		for (int i = 2; i < 10; i++) {
			outputCSV.print("SSD Seg SD" + i);
			for (int j = 0; j < numberOfSimulations; j++) {
				outputCSV.print("," + totalSpeedDev_10seg[j][i].stddev());
			}
			outputCSV.print("\n");
		}
		outputCSV.flush();
		
		outputCSV.print(", \n");
		outputCSV.flush();
		
		
		
		getModel().outputInLine("\n ****************** \n");
		try {
			int count = 1;
			for (Task taskCast : tasks) {
				Driving_B task = (Driving_B) taskCast;
				numberOfSimulations = task.results.size(); // some task might not be complete
				if (!task.completed){
					getModel().outputInLine("* Night " + count + "\n");
					outputCSV.print("* Night " + count + "\n");
				}
				else{
					getModel().outputInLine("Night " + count + "\n");
					outputCSV.print("Night " + count + "\n");
				}
				
				//session #
				getModel().outputInLine("Session #\t");
				outputCSV.print("Session #,");			
				for (int i = 0; i < numberOfSimulations; i++) {
					getModel().outputInLine((i+4) +"\t");
					outputCSV.print((i+4) +",");
				}
				getModel().outputInLine("\n");
				outputCSV.print("\n");
				
				//LP_STD 
				getModel().outputInLine("LPSD\t");
				outputCSV.print("LPSD,");			
				for (int i = 0; i < numberOfSimulations; i++) {
					Results results = task.results.elementAt(i);
					
					getModel().outputInLine(String.valueOf(df.format(Utilities.arrayAverage(results.taskLatDev_10Segments)) +"\t"));
					outputCSV.print(String.valueOf(df.format(Utilities.arrayAverage(results.taskLatDev_10Segments)) +","));
				}
				getModel().outputInLine("\n");
				outputCSV.print("\n");
				
				//Steering_STD 
				getModel().outputInLine("SWASD\t");
				outputCSV.print("SWATD,");			
				for (int i = 0; i < numberOfSimulations; i++) {
					Results results = task.results.elementAt(i);
					
					getModel().outputInLine(String.valueOf(df.format(Utilities.arrayAverage(results.taskSteeringDev_10Segments)) +"\t"));
					outputCSV.print(String.valueOf(df.format(Utilities.arrayAverage(results.taskSteeringDev_10Segments)) +","));
				}
				getModel().outputInLine("\n");
				outputCSV.print("\n");
				
				//MPH_STD 
				getModel().outputInLine("SSD\t");
				outputCSV.print("SSD,");			
				for (int i = 0; i < numberOfSimulations; i++) {
					Results results = task.results.elementAt(i);
					
					getModel().outputInLine(String.valueOf(df.format(Utilities.arrayAverage(results.taskSpeedDev_10Segments)) +"\t"));
					outputCSV.print(String.valueOf(df.format(Utilities.arrayAverage(results.taskSpeedDev_10Segments)) +","));
				}
				getModel().outputInLine("\n");
				outputCSV.print("\n");
				outputCSV.flush();
				
				count++;
			}
			outputCSV.flush();
			
			getModel().outputInLine("\n");
			outputCSV.print("\n");
			
			getModel().output("\n ******* Task Time ********** \n");
			outputCSV.print("\n ******* Task Time ********** \n");
			for (Task taskCast : tasks) {
				Driving_B task = (Driving_B) taskCast;
				numberOfSimulations = task.results.size(); // some task might not be complete
				if (!task.completed){
					getModel().outputInLine("*\t");
					outputCSV.print("*,");
				}
				else{
					getModel().outputInLine("\t");
					outputCSV.print(",");
				}
				
				for (int i = 0; i < numberOfSimulations; i++) {
					Results results = task.results.elementAt(i);
					getModel().outputInLine(String.valueOf(df.format(results.taskTime) +"\t"));
					getModel().outputInLine("\t");
					outputCSV.print(String.valueOf(df.format(results.taskTime) +","));
					outputCSV.print(",");
				}
				getModel().outputInLine("\n\n");
				outputCSV.print("\n\n");
			}
			outputCSV.flush();
			
			
			getModel().output("\n******* taskLPSD_10Segments ********** \n");
			outputCSV.print("\n******* taskLPSD_10Segments ********** \n");
			for (Task taskCast : tasks) {
				Driving_B task = (Driving_B) taskCast;
				numberOfSimulations = task.results.size(); // some task might not be complete
				if (!task.completed){
					getModel().outputInLine("*\t");
					outputCSV.print("*,");
				}
				else{
					getModel().outputInLine("\t");
					outputCSV.print(",");
				}
				
				for (int i = 0; i < numberOfSimulations; i++) {
					Results results = task.results.elementAt(i);
					for (int j = 0; j < 10; j++) {
						getModel().outputInLine(df.format(results.taskLatDev_10Segments[j]) +" ");
						outputCSV.print(df.format(results.taskLatDev_10Segments[j]) +",");
					}
					getModel().outputInLine("\t");
					outputCSV.print(",,");
				}
				getModel().outputInLine("\n\n");
				outputCSV.print("\n\n");
			}
			outputCSV.flush();

			getModel().output("\n******* STEX3_10Segments ********** \n");
			outputCSV.print("\n******* STEX3_10Segments ********** \n");
			for (Task taskCast : tasks) {
				Driving_B task = (Driving_B) taskCast;
				numberOfSimulations = task.results.size(); // some task might not be complete
				if (!task.completed){
					getModel().outputInLine("*\t");
					outputCSV.print("*,");
				}
				else{
					getModel().outputInLine("\t");
					outputCSV.print(",");
				}
				
				for (int i = 0; i < numberOfSimulations; i++) {
					Results results = task.results.elementAt(i);
					for (int j = 0; j < 10; j++) {
						getModel().outputInLine(df.format(results.STEX3_10Segments[j]) +" ");
						outputCSV.print(df.format(results.STEX3_10Segments[j]) +",");
					}
					getModel().outputInLine("\t");
					outputCSV.print(",,");
				}
				getModel().outputInLine("\n\n");
				outputCSV.print("\n\n");
			}
			outputCSV.flush();
			
			getModel().output("\n******* taskSWASD_10Segments ********** \n");
			outputCSV.print("\n******* taskSWASD_10Segments ********** \n");
			for (Task taskCast : tasks) {
				Driving_B task = (Driving_B) taskCast;
				numberOfSimulations = task.results.size(); // some task might not be complete
				if (!task.completed){
					getModel().outputInLine("*\t");
					outputCSV.print("*,");
				}
				else{
					getModel().outputInLine("\t");
					outputCSV.print(",");
				}
				
				for (int i = 0; i < numberOfSimulations; i++) {
					Results results = task.results.elementAt(i);
					for (int j = 0; j < 10; j++) {
						getModel().outputInLine(df.format(results.taskSteeringDev_10Segments[j]) +" ");
						outputCSV.print(df.format(results.taskSteeringDev_10Segments[j]) +",");
					}
					getModel().outputInLine("\t");
					outputCSV.print(",,");
				}
				getModel().outputInLine("\n\n");
				outputCSV.print("\n\n");
			}
			outputCSV.flush();
			
			getModel().output("\n******* taskSSD_10Segments ********** \n");
			outputCSV.print("\n******* taskSSD_10Segments ********** \n");
			for (Task taskCast : tasks) {
				Driving_B task = (Driving_B) taskCast;
				numberOfSimulations = task.results.size(); // some task might not be complete
				if (!task.completed){
					getModel().outputInLine("*\t");
					outputCSV.print("*,");
				}
				else{
					getModel().outputInLine("\t");
					outputCSV.print(",");
				}
				
				for (int i = 0; i < numberOfSimulations; i++) {
					Results results = task.results.elementAt(i);
					for (int j = 0; j < 10; j++) {
						getModel().outputInLine(df.format(results.taskSpeedDev_10Segments[j]) +" ");
						outputCSV.print(df.format(results.taskSpeedDev_10Segments[j]) +",");
					}
					getModel().outputInLine("\t");
					outputCSV.print(",,");
				}
				getModel().outputInLine("\n\n");
				outputCSV.print("\n\n");
			}
			outputCSV.flush();
			outputCSV.close();
			

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
//		if (simulationNumber == timesOfPVT.length-1)
//			outputPara.println(currentSimulation.getEnvironment().getTime()+","
//					+ getModel().getProcedural().getFatigueUtility() + ","
//					+ getModel().getProcedural().getFinalInstUtility() + "," 
//					+ getModel().getFatigue().getFatigueUT());
//		outputPara.flush();
		
		
		return result;
	}

	public static String toString(double a[]) {
		String s = "";
		for (int i = 0; i < a.length; i++)
			s += String.format("%.2f", a[i]) + (i < a.length - 1 ? "\t" : "");
		return s;
	}	
}
