package tasks.driving;

/**
 * The class that defines a collected data sample at a given point in time.
 * 
 * @author Dario Salvucci
 */
public class Sample {
	public double time;
	private Position simcarP;
	private Position simcarH;
	private double simcarLanePos;
	private double simcarIndex;
	private double simcarSpeed;
	private double steerAngle;
	private double accelerator;
	private double brake;
	private Position autocarPos;
	private Position autocarHeading;
	private double autocarSpeed;
	private boolean autocarBraking;
	// private LocationChunk eyeLocation;
	// private boolean lookingAwayFromRoad;
	// private LocationChunk handLocation;
	// private boolean handMoving;
	// private boolean listening;
	// private boolean inDriveGoal;
	// private int event;

	Sample(Environment env) {
		Simcar simcar = env.getSimcar();
		Autocar autocar = env.getAutocar();

		time = env.getTime();
		simcarP = simcar.getP().myclone();
		simcarH = simcar.getH().myclone();
		simcarLanePos = env.getRoad().vehicleLanePosition(env.getSimcar()); // ==
																			// X.5
																			// for
																			// the
																			// center
																			// of
																			// lane;
		// .0 means exactly over the lane line
		// lane is 12 feet wide

		simcarIndex = simcar.getIndex();
		simcarSpeed = simcar.getSpeed();
		steerAngle = simcar.getSteerAngle(); // steering angle, in radians (I
												// think)
		accelerator = simcar.getAccelerator();
		brake = simcar.getBrake();
		autocarPos = autocar.getP().myclone();
		autocarHeading = autocar.getH().myclone();
		autocarSpeed = autocar.getSpeed();
		autocarBraking = autocar.isBraking();

		// eyeLocation = model.getVision().getEyeLocation();
		// if (eyeLocation != null)
		// eyeLocation = eyeLocation.myclone();
		// lookingAwayFromRoad = !(eyeLocation != null && eyeLocation.getX() <
		// 350);
		//
		// handLocation = model.getMotor().getHandLocation();
		// if (handLocation != null)
		// handLocation = handLocation.myclone();
		// handMoving = (model.getMotor().getHandLocationNext() != null);
		// listening = !model.getBuffers().

		// if (model.getGoals().size() > 0)
		// inDriveGoal = (model.getGoals().elementAt(0) instanceof DriveGoal);

		// event = model.getEvent();
	}

	public double getTime() {
		return time;
	}

	public Position getSimcarPosition() {
		return simcarP;
	}

	public Position getSimcarHeading() {
		return simcarH;
	}

	public double getSimcarLanePosition() {
		return simcarLanePos;
	}

	public double getSimcarIndex() {
		return simcarIndex;
	}

	public long getSimcarRoadIndex() {
		return (long) Math.floor(simcarIndex);
	}

	public double getSimcarSpeed() {
		return simcarSpeed;
	}

	public double getSteerAngle() {
		return steerAngle;
	}

	public double getAccelerator() {
		return accelerator;
	}

	public double getBrake() {
		return brake;
	}

	public Position getAutocarPos() {
		return autocarPos;
	}

	public Position getAutocarHeading() {
		return autocarHeading;
	}

	public double getAutocarSpeed() {
		return autocarSpeed;
	}

	public boolean getAutocarBraking() {
		return autocarBraking;
	}

	// public LocationChunk getEyeLocation() {
	// return eyeLocation;
	// }
	//
	// public boolean getLookingAwayFromRoad() {
	// return lookingAwayFromRoad;
	// }
	//
	// public LocationChunk getHandLocation() {
	// return handLocation;
	// }
	//
	// public boolean getHandMoving() {
	// return handMoving;
	// }
	//
	// public boolean getListening() {
	// return listening;
	// }
	//
	// public boolean getInDriveGoal() {
	// return inDriveGoal;
	// }
	//
	// public int getEvent() {
	// return event;
	// }

	public static String headerString(String separator) {
		String s = "";
		s += "time";
		s += separator + "simcarX";
		s += separator + "simcarZ";
		s += separator + "simcarHeading";
		s += separator + "simcarLanePos";
		s += separator + "simcarIndex";
		s += separator + "simcarSpeed";
		s += separator + "steerAngle";
		s += separator + "accelerator";
		s += separator + "brake";
		s += separator + "autocarX";
		s += separator + "autocarSpeed";
		s += separator + "autocarBraking";
		// s += separator + LocationChunk.headerString("eyeLocation",
		// separator);
		// s += separator + "lookingAwayFromRoad";
		// s += separator + LocationChunk.headerString("handLocation",
		// separator);
		// s += separator + "handMoving";
		// s += separator + "listening";
		// s += separator + "inDriveGoal";
		// s += separator + "event";
		return s;
	}

	public String toString(String separator) {
		String s = "";
		s += Utilities.format(time, 3);
		s += separator + Utilities.format(simcarP.getX(), 3);
		s += separator + Utilities.format(simcarP.getZ(), 3);
		s += separator + Utilities.format(Utilities.rotationAngle(simcarH.getX(), simcarH.getZ()), 5);
		s += separator + Utilities.format(simcarLanePos, 3);
		s += separator + Utilities.format(simcarIndex, 3);
		s += separator + Utilities.format(simcarSpeed, 3);
		s += separator + Utilities.format(steerAngle, 5);
		s += separator + Utilities.format(accelerator, 3);
		s += separator + Utilities.format(brake, 3);
		s += separator + Utilities.format(autocarPos.getX(), 3);
		s += separator + Utilities.format(autocarSpeed, 3);
		s += separator + (autocarBraking ? 1 : 0);
		// s += separator + (eyeLocation != null ?
		// eyeLocation.toString(separator) :
		// LocationChunk.nullString(separator));
		// s += separator + (lookingAwayFromRoad ? 1 : 0);
		// s += separator
		// + (handLocation != null ? handLocation.toString(separator) :
		// LocationChunk.nullString(separator));
		// s += separator + (handMoving ? 1 : 0);
		// s += separator + (listening ? 1 : 0);
		// s += separator + (inDriveGoal ? 1 : 0);
		// s += separator + event;
		return s;
	}

	public static String detailedHeaderString(String separator) {
		String s = "";
		s += "time";
		s += separator + Position.headerString("simcarPos", separator);
		s += separator + Position.headerString("simcarHeading", separator);
		s += separator + "simcarRoadIndex";
		s += separator + "simcarSpeed";
		s += separator + "steerAngle";
		s += separator + "accelerator";
		s += separator + "brake";
		s += separator + Position.headerString("autocarPos", separator);
		s += separator + Position.headerString("autocarHeading", separator);
		s += separator + "autocarRoadIndex";
		s += separator + "autocarSpeed";
		s += separator + "autocarBraking";
		// s += separator + LocationChunk.headerString("eyeLocation",
		// separator);
		// s += separator + "lookingAwayFromRoad";
		// s += separator + LocationChunk.headerString("handLocation",
		// separator);
		// s += separator + "handMoving";
		// s += separator + "listening";
		// s += separator + "inDriveGoal";
		// s += separator + "event";
		s += separator + "lanepos";
		return s;
	}

	public String toDetailedString(String separator) {
		String s = "";
		s += time;
		s += separator + simcarP.toString(separator);
		s += separator + simcarH.toString(separator);
		s += separator + simcarSpeed;
		s += separator + steerAngle;
		s += separator + accelerator;
		s += separator + brake;
		s += separator + autocarPos.toString(separator);
		s += separator + autocarHeading.toString(separator);
		s += separator + autocarSpeed;
		s += separator + (autocarBraking ? 1 : 0);
		// s += separator + (eyeLocation != null ?
		// eyeLocation.toString(separator) :
		// LocationChunk.nullString(separator));
		// s += separator + (lookingAwayFromRoad ? 1 : 0);
		// s += separator
		// + (handLocation != null ? handLocation.toString(separator) :
		// LocationChunk.nullString(separator));
		// s += separator + (handMoving ? 1 : 0);
		// s += separator + (listening ? 1 : 0);
		// s += separator + (inDriveGoal ? 1 : 0);
		// s += separator + event;
		s += separator + simcarLanePos;
		return s;
	}
}
