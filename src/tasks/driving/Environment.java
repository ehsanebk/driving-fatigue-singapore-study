package tasks.driving;

import java.util.List;
import java.util.Vector;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;

import tasks.driving.Autocar.Type;

/**
 * The main driving environment that includes all other components of the
 * environment.
 * 
 * @author Dario Salvucci
 */
public class Environment {
	public static final double SAMPLE_TIME = .050;
	public static final int CANVAS_WIDTH = 640;
	public static final int CANVAS_HEIGHT = 360;

	private Road road;
	private Simcar simcar;
	private Autocar autocar;
	private List<Autocar> others;
	private double time;
	private boolean done;

	Environment(Driver driver, Scenario scenario) {
		road = new Road(scenario);
		road.startup();

		int lane = 4;
		simcar = new Simcar(scenario, driver, lane, this);
		road.vehicleReset(simcar, lane, 100);
		autocar = new Autocar(scenario, lane, true, Type.MAXIMA);
		road.vehicleReset(autocar, lane, 120);

		Autocar other = new Autocar(scenario, 1, false, Type.PRIUS);
		road.vehicleReset(other, lane, 300);
		others = new Vector<Autocar>();
		others.add(other);

		time = 0;
		done = false;
	}

	public Road getRoad() {
		return road;
	}

	public Simcar getSimcar() {
		return simcar;
	}

	public Autocar getAutocar() {
		return autocar;
	}

	public double getTime() {
		return time;
	}

	void setTime(double v) {
		time = v;
	}

	public boolean getDone() {
		return done;
	}

	void setDone(boolean v) {
		done = v;
	}

	void update() {
		simcar.update(this);
		autocar.update(this);
		for (Autocar other : others)
			other.update(this);
	}

	private final double simViewAH = .13;
	private final double simViewSD = -.37;
	private final double simViewHT = 1.15;
	private final double simFocalS = 450;
	private final double simOXR = 1.537 / (1.537 + 2.667);
	private final double simOYR = (2.57 - simViewHT) / 2.57;
	private final double simNear = 1.5;
	private final double simFar = 1800.00;

	Coordinate world2image(Position world) {
		double hx = simcar.getHX();
		double hz = simcar.getHZ();
		double px = simcar.getPX() + ((hx * simViewAH) - (hz * simViewSD));
		double py = simViewHT;
		double pz = simcar.getPZ() + ((hz * simViewAH) + (hx * simViewSD));
		double wx1 = world.getX() - px;
		double wy1 = world.getY();
		double wz1 = world.getZ() - pz;
		double wx = hx * wz1 - hz * wx1;
		double wy = py - wy1;
		double wz = hz * wz1 + hx * wx1;
		double ox = simOXR * CANVAS_WIDTH;
		double oy = simOYR * CANVAS_HEIGHT;
		if (wz > 0)
			return new Coordinate((int) Math.round(ox + ((simFocalS * wx) / wz)),
					(int) Math.round(oy + ((simFocalS * wy) / wz)), wz);
		else
			return null;
	}

	// void draw (Graphics g)
	// {
	// Coordinate im = world2image (new Position (0, simcar.roadIndex + 1000));
	// if (im != null)
	// g.drawImage (imageSky, im.x - envWidth, 0, 2*envWidth, (int)
	// (.516*envHeight), null, null);
	//
	// g.setColor (new Color (0, 125, 15));
	// g.fillRect (0, (int) (.516*envHeight), envWidth, envHeight);
	//
	// road.draw (g, this);
	// autocar.draw (g, this);
	// simcar.draw (g, this);
	// }

	void drawBackgroundPolygon(GL2 gl, double cx, double cz, double dx1, double dz1, double dx2, double dz2) {
		double skyDist = 500;
		double height = skyDist * (570.0 / 800.0);
		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glBindTexture(GL.GL_TEXTURE_2D, Textures.background);
		gl.glBegin(GL2.GL_POLYGON);
		gl.glTexCoord2f(0, 0);
		gl.glVertex3d(cx + skyDist * dx1, 0, cz + skyDist * dz1);
		gl.glTexCoord2f(0, 1);
		gl.glVertex3d(cx + skyDist * dx1, height, cz + skyDist * dz1);
		gl.glTexCoord2f(1, 1);
		gl.glVertex3d(cx + skyDist * dx2, height, cz + skyDist * dz2);
		gl.glTexCoord2f(1, 0);
		gl.glVertex3d(cx + skyDist * dx2, 0, cz + skyDist * dz2);
		gl.glEnd();
		gl.glDisable(GL.GL_TEXTURE_2D);
	}

	void draw(GL2 gl) {
		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		gl.glLoadIdentity();

		double w = 2.4;
		double h = (w * CANVAS_HEIGHT) / CANVAS_WIDTH;
		double dh = .2;

		gl.glFrustum(-(w / 2) - simViewSD, (w / 2) - simViewSD, dh - simViewHT, dh + h - simViewHT, simNear, simFar);

		double hx = simcar.getHX();
		double hz = simcar.getHZ();
		double px = simcar.getPX() + (hx * simViewAH) - (hz * simViewSD);
		double py = simViewHT;
		double pz = simcar.getPZ() + (hz * simViewAH) + (hx * simViewSD);
		GLU glu = new GLU();
		glu.gluLookAt(px, py, pz, px + hx, py, pz + hz, 0.0, 1.0, 0.0);

		drawBackgroundPolygon(gl, px, pz, 1, 1, 1, -1);
		drawBackgroundPolygon(gl, px, pz, -1, -1, 1, -1);
		drawBackgroundPolygon(gl, px, pz, -1, -1, -1, 1);
		drawBackgroundPolygon(gl, px, pz, 1, 1, -1, 1);

		road.draw(gl, this);
		simcar.draw(gl, this);
		autocar.draw(gl, this);
		for (Autocar other : others)
			other.draw(gl, this);
	}
}
