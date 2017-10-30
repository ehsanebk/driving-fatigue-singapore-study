package tasks.driving;

/**
 * A simple class for x,y coordinates plus depth.
 * 
 * @author Dario Salvucci
 */
public class Coordinate {
	private int x;
	private int y;
	private double d;

	Coordinate(int x, int y) {
		this.x = x;
		this.y = y;
		d = 0;
	}

	Coordinate(int x, int y, double d) {
		this.x = x;
		this.y = y;
		this.d = d;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public double getD() {
		return d;
	}
}
