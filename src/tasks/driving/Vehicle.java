package tasks.driving;

/**
 * A general vehicle class (subclassed by other classes).
 * 
 * @author Dario Salvucci
 */
public class Vehicle {
	private Position p;
	private Position h;
	private double index;
	private double speed;

	Vehicle() {
		p = new Position(0, 0);
		h = new Position(1, 0);
		index = 0;
		speed = 0;
	}

	public Position getP() {
		return p;
	}

	public Position getH() {
		return h;
	}

	public double getIndex() {
		return index;
	}

	public long getRoadIndex() {
		return (long) Math.floor(index);
	}

	public double getSpeed() {
		return speed;
	}

	public double getPX() {
		return p.getX();
	}

	public double getPZ() {
		return p.getZ();
	}

	public double getHX() {
		return h.getX();
	}

	public double getHZ() {
		return h.getZ();
	}

	void setP(Position v) {
		p = v;
	}

	void setH(Position v) {
		h = v;
	}

	void setPX(double v) {
		p.setX(v);
	}

	void setPZ(double v) {
		p.setZ(v);
	}

	void setHX(double v) {
		h.setX(v);
	}

	void setHZ(double v) {
		h.setZ(v);
	}

	void setIndex(double v) {
		index = v;
	}

	void setSpeed(double v) {
		speed = v;
	}
}
