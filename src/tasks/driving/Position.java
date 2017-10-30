package tasks.driving;

/**
 * A simple class representing position in space in the simulation.
 * 
 * @author Dario Salvucci
 */
public class Position {
	private double x;
	private double y;
	private double z;

	Position(double x, double z) {
		this.x = x;
		this.z = z;
		this.y = 0;
	}

	Position(double x, double z, double y) {
		this.x = x;
		this.z = z;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	void setX(double v) {
		x = v;
	}

	void setY(double v) {
		y = v;
	}

	void setZ(double v) {
		z = v;
	}

	Position add(Position l2) {
		return new Position(x + l2.x, z + l2.z);
	}

	Position subtract(Position l2) {
		return new Position(x - l2.x, z - l2.z);
	}

	Position scale(double s) {
		return new Position(s * x, s * y);
	}

	Position average(Position l2, double weight) {
		return new Position(((1.0 - weight) * x) + (weight * l2.x), ((1.0 - weight) * z) + (weight * l2.z));
	}

	Position normalize() {
		double m = Math.sqrt((x * x) + (z * z));
		return new Position(x / m, z / m);
	}

	Position rotate(double degrees) {
		double angle = (-180 * (Math.atan2(z, x)) / Math.PI);
		angle += degrees;
		double rad = -angle * Math.PI / 180;
		return new Position(Math.cos(rad), Math.sin(rad));
	}

	Position negate() {
		return new Position(-x, -z, y);
	}

	Position myclone() {
		return new Position(x, z, y);
	}

	public static String headerString(String prefix, String separator) {
		return prefix + "X" + separator + prefix + "Y" + separator + prefix + "Z";
	}

	public String toString(String separator) {
		return Utilities.format(x, 3) + separator + Utilities.format(y, 3) + separator + Utilities.format(z, 3);
	}

	@Override
	public String toString() {
		return toString(",");
	}
}
