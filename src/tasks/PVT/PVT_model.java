package tasks.PVT;


import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import actr.model.Event;
import actr.model.Symbol;
import actr.task.*;

/**
 * Model of PVT test and Fatigue mechanism 
 * 
 * Singapore study
 * 
 * @author Ehsan Khosroshahi
 */

public class PVT_model extends Task {
	private static double SESSION_TOTAL_TIME = 300.0;
	
	private TaskLabel label;
	private double lastTime = 0;
	private String stimulus = "\u2588";
	private double interStimulusInterval = 0.0;
	private Boolean stimulusVisibility = false;
	private String response = null;
	private double responseTime = 0;
	// the following two variables are for handling sleep attacks
	private int sleepAttackIndex = 0;

	private double[] timesOfPVT = {
			//
			//time points
			//---1-----  -----2----- -----3-----  -----4----- -----5----- 
			12.0 + 24  , 13.0 + 24  , 14.0 +24  , 15.0 + 24  , 1.0 +24   // day1
			
	};
	
	int sessionNumber = 0; // starts from 0
	private Session currentSession;
	private Vector<Session> sessions = new Vector<Session>();

	@SuppressWarnings("unused")
	private PrintStream data;

	class Session {
		double startTime = 0;
		int falseStarts = 0;
		int alertRosponses = 0;
		int alertResponseSpread[] = new int[35]; // Alert responses (150-500ms,
													// 10ms
		// intervals )

		double totalSessionTime = 0;
		int lapses = 0;
		int sleepAttacks = 0;
		int stimulusIndex = 0;
		int responses = 0; // number of responses, this can be diff from the
							// stimulusIndex because of false resonces
		double responseTotalTime = 0;
	}

	public PVT_model() {
		super();
		label = new TaskLabel("", 200, 150, 40, 20);
		add(label);
		label.setVisible(false);
	}

	@Override
	public void start() {
		lastTime = 0;

		currentSession = new Session();
		stimulusVisibility = false;

		getModel().getFatigue().setFatigueHour(timesOfPVT[sessionNumber]);
		getModel().getFatigue().startFatigueSession();

		addUpdate(1.0);

		try {
			File dataFile = new File("./model/data.txt");
			if (!dataFile.exists())
				dataFile.createNewFile();
			data = new PrintStream(dataFile);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void update(double time) {
		currentSession.totalSessionTime = getModel().getTime() - currentSession.startTime;

		if (currentSession.totalSessionTime <= SESSION_TOTAL_TIME) {
			label.setText(stimulus);
			label.setVisible(true);
			processDisplay();
			stimulusVisibility = true;
			lastTime = getModel().getTime();
			// setting up the state to wait
			getModel().getDeclarative().get(Symbol.get("goal")).set(Symbol.get("state"), Symbol.get("stimulus"));

			// calling percentage reset after any new task presentation (audio
			// or visual)
			getModel().getFatigue().fatigueResetPercentages();

			// Handling the sleep attacks -- adding an event in 30 s to see if
			// the current stimulus is still on
			currentSession.stimulusIndex++;
			addEvent(new Event(getModel().getTime() + 30.0, "task", "update") {
				@Override
				public void action() {
					sleepAttackIndex++;
					if (sleepAttackIndex == currentSession.stimulusIndex && stimulusVisibility == true) {
						label.setVisible(false);
						processDisplay();
						stimulusVisibility = false;
						currentSession.sleepAttacks++;
						currentSession.responses++; // when sleep attack happens
													// we add to the number of
													// responses
						System.out
								.println("Sleep attack at time ==>" + (getModel().getTime() - currentSession.startTime)
										+ "model time :" + getModel().getTime());
						System.out.println(currentSession.stimulusIndex + " " + sleepAttackIndex);
						addUpdate(1.0);
						getModel().getDeclarative().get(Symbol.get("goal")).set(Symbol.get("state"),
								Symbol.get("wait"));
					}
					repaint();

				}
			});
		}

		// Starting a new Session
		else {
			sessionNumber++;
			getModel().getDeclarative().get(Symbol.get("goal")).set(Symbol.get("state"), Symbol.get("none"));
			// go to the next session or stop the model
			if (sessionNumber < timesOfPVT.length) {
				addEvent(new Event(getModel().getTime() + 60.0, "task", "update") {
					@Override
					public void action() {
						sessions.add(currentSession);
						currentSession = new Session();
						stimulusVisibility = false;
						sleepAttackIndex = 0;
						currentSession.startTime = getModel().getTime();
						getModel().getFatigue().setFatigueHour(timesOfPVT[sessionNumber]);
						// System.out.println(sessionNumber +" : "+
						// getModel().getFatigue().computeBioMathValueForHour());
						getModel().getFatigue().startFatigueSession();
						addUpdate(1.0);
						getModel().getDeclarative().get(Symbol.get("goal")).set(Symbol.get("state"),
								Symbol.get("wait"));
					}
				});

			} else {
				sessions.add(currentSession);
				getModel().stop();
			}

		}
	}

	@Override
	public void typeKey(char c) {

		if (stimulusVisibility == true) {
			response = c + "";
			responseTime = getModel().getTime() - lastTime;

			if (response != null) // && response.equals("spc"))
			{
				currentSession.responses++;
				currentSession.responseTotalTime += responseTime;
			}

			label.setVisible(false);
			processDisplay();

			Random random = new Random();
			interStimulusInterval = random.nextDouble() * 8 + 1; // A random
			addUpdate(interStimulusInterval);
			stimulusVisibility = false;

			if (responseTime < .150) {
				currentSession.falseStarts++;
			} else if (responseTime > .150 && responseTime <= .500) {
				currentSession.alertResponseSpread[(int) ((responseTime - .150) * 100)]++; // making
																							// the
																							// array
																							// for
																							// response
																							// time
				currentSession.alertRosponses++;
			} else if (responseTime > .500 && responseTime < 30.0) {
				currentSession.lapses++;
			}
			// setting up the state to wait
			getModel().getDeclarative().get(Symbol.get("goal")).set(Symbol.get("state"), Symbol.get("wait"));

		} else {
			currentSession.responses++;
			currentSession.falseStarts++;
		}

	}

	@Override
	public Result analyze(Task[] tasks, boolean output) {

		try {

			int numberOfSessions = timesOfPVT.length;
			Values[] totallLapsesValues = new Values[numberOfSessions];
			

			// allocating memory to the vectors
			for (int i = 0; i < numberOfSessions; i++) {
				totallLapsesValues[i] = new Values();
				
			}

			for (Task taskCast : tasks) {
				PVT_model task = (PVT_model) taskCast;
				for (int i = 0; i < numberOfSessions; i++) {
					
					totallLapsesValues[i].add(task.sessions.get(i).lapses);
					

					
				}
			}

			DecimalFormat df3 = new DecimalFormat("#.000");

			
			getModel().output("\nAverage Number of lapses in the time points \n");
			getModel().output("\t1 \t2 \t3 \t4 \t5");

			getModel().output("\t" + totallLapsesValues[0].mean() + "\t"
					+ totallLapsesValues[1].mean() + "\t" + totallLapsesValues[2].mean() + "\t"
					+ totallLapsesValues[3].mean()+ "\t" + totallLapsesValues[4].mean());

			
			getModel().output("\n*******************************************\n");

//			File dataFile = new File("./result/BioMathValuesNightA.txt");
//			if (!dataFile.exists())
//				dataFile.createNewFile();
//			PrintStream data = new PrintStream(dataFile);
//
//			for (int h = 0; h < timesOfPVT[timesOfPVT.length - 1]; h++) {
//				data.println(h + "\t" + df3.format(getModel().getFatigue().getBioMathModelValueforHour(h)));
//			}
//
//			data.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		Result result = new Result();
		return result;
	}

}
