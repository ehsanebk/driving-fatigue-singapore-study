package tasks.driving;

/**
 * The class that defines the driver's particular behavioral parameters.
 * 
 * @author Dario Salvucci
 */
public class Driver {
	private String name;
	private int age;
	private double steeringFactor;
	private double stabilityFactor;

	Driver(String name, int age, double steeringFactor, double stabilityFactor) {
		this.name = name;
		this.age = age;
		this.steeringFactor = steeringFactor;
		this.stabilityFactor = stabilityFactor;
	}

	public String getName() {
		return name;
	}

	public int getAge() {
		return age;
	}

	public double getSteeringFactor() {
		return steeringFactor;
	}

	public double getStabilityFactor() {
		return stabilityFactor;
	}

	String writeString() {
		return new String("\"" + name + "\"\t" + age + "\t" + steeringFactor + "\t" + stabilityFactor + "\n");
	}

	@Override
	public String toString() {
		return name;
	}
}
