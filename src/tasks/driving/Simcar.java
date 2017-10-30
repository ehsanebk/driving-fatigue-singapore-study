package tasks.driving;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.awt.TextRenderer;

/**
 * The driver's own vehicle and its controls.
 * 
 * @author Dario Salvucci
 */
public class Simcar extends Vehicle {
	private Scenario scenario;
	private Driver driver;
	private int lane;
	private double steerAngle;
	private double accelerator;
	private double brake;
	private Position nearPoint;
	private Position farPoint;
	private Position carPoint;

	public Simcar(Scenario scenario, Driver driver, int lane, Environment env) {
		super();
		this.scenario = scenario;
		this.driver = driver;
		this.lane = lane;

		steerAngle = 0;
		accelerator = 0;
		brake = 0;
		setSpeed(0);
	}

	public Driver getDriver() {
		return driver;
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

	void setSteerAngle(double v) {
		steerAngle = v;
	}

	void setAccelerator(double v) {
		accelerator = v;
	}

	void setBrake(double v) {
		brake = v;
	}

	public Position getNearPoint() {
		return nearPoint;
	}

	public Position getFarPoint() {
		return farPoint;
	}

	public Position getCarPoint() {
		return carPoint;
	}

	private int order = 6;
	// private int max_order = 10;
	private double gravity = 9.8;
	private double air_drag_coeff = .25;
	private double engine_max_watts = 106000;
	private double brake_max_force = 8000;
	private double f_surface_friction = .2;
	private double lzz = 2618;
	private double ms = 1175;
	private double a = .946;
	private double b = 1.719;
	private double caf = 48000;
	private double car = 42000;
	private double[] y = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	private double[] dydx = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	private double[] yout = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	private double heading = -999;
	private double heading1 = -999;
	private double heading2 = -999;
	private double car_heading;
	private double car_accel_pedal;
	private double car_brake_pedal;
	private double car_deltaf;
	private double car_steer;
	private double car_speed;
	private double car_ke;

	private void derivs(double y[], double dydx[]) {
		double phi = y[1];
		double r = y[2];
		double beta = y[3];
		double ke = y[4];
		double u = (ke > 0) ? Math.sqrt(ke * 2 / ms) : 0;
		double deltar = 0;
		double deltaf = car_deltaf;
		dydx[1] = r;
		if (u > 5) {
			dydx[2] = (2.0 * a * caf * deltaf - 2.0 * b * car * deltar - 2.0 * (a * caf - b * car) * beta
					- (2.0 * (a * a * caf + b * b * car) * r / u)) / lzz;
			dydx[3] = (2.0 * caf * deltaf + 2.0 * car * deltar - 2.0 * (caf + car) * beta
					- (ms * u + (2.0 * (a * caf - b * car) / u)) * r) / (ms * u);
		} else {
			dydx[1] = 0.0;
			dydx[2] = 0.0;
			dydx[3] = 0.0;
		}
		double pengine = car_accel_pedal * engine_max_watts;
		double fbrake = car_brake_pedal * brake_max_force;
		double fdrag = (f_surface_friction * ms * gravity) + (air_drag_coeff * u * u);
		dydx[4] = pengine - fdrag * u - fbrake * u;
		dydx[5] = u * Math.cos(phi);
		dydx[6] = u * Math.sin(phi);
	}

	void rk4(int n, double x, double h) {
		double dym[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		double dyt[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		double yt[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		double hh = h * 5;
		double h6 = h / 6;
		int i;

		for (i = 1; i <= n; i++)
			yt[i] = y[i] + hh * dydx[i];
		derivs(yt, dyt);
		for (i = 1; i <= n; i++)
			yt[i] = y[i] + hh * dyt[i];
		derivs(yt, dym);
		for (i = 1; i <= n; i++) {
			yt[i] = y[i] + h * dym[i];
			dym[i] += dyt[i];
		}
		derivs(yt, dyt);
		for (i = 1; i <= n; i++)
			yout[i] = y[i] + h6 * (dydx[i] + dyt[i] + 2.0 * dym[i]);
	}

	void updateDynamics(Environment env) {
		Road road = env.getRoad();
		double time = env.getTime();
		double sampleTime = Environment.SAMPLE_TIME;

		if (heading2 == -999.0) {
			heading = heading1 = heading2 = Math.atan2(getHZ(), getHX());
			yout[1] = y[1] = car_heading = heading;
			yout[2] = y[2] = 0.0;
			yout[3] = y[3] = 0.0;
			yout[4] = y[4] = car_ke = 50000; // 0.0; // kinetic energy > 0,
												// otherwise unstable at start
			yout[5] = y[5] = getPX();
			yout[6] = y[6] = getPZ();
			if (car_ke > 0.0)
				car_speed = Math.sqrt(2.0 * car_ke / ms);
			else
				car_speed = 0.0;
		}

		car_steer = steerAngle;
		car_accel_pedal = accelerator;
		car_brake_pedal = brake;

		// original had lines below; changing to linear steering function
		// if (car_steer < 0.0) car_deltaf = -0.0423 * Math.pow(-1.0*car_steer,
		// 1.3);
		// else car_deltaf = 0.0423 * Math.pow(car_steer,1.3);
		car_deltaf = 0.0423 * car_steer;

		double forcing = 0.125 * (0.01 * Math.sin(2.0 * 3.14 * 0.13 * time + 1.137)
				+ 0.005 * Math.sin(2.0 * 3.14 * 0.47 * time + 0.875));
		car_deltaf += forcing;

		derivs(y, dydx);
		rk4(order, time, sampleTime);

		y[1] = car_heading = yout[1];
		y[2] = yout[2];
		y[3] = yout[3];
		y[4] = car_ke = yout[4];
		y[5] = yout[5];
		y[6] = yout[6];
		setPX(yout[5]);
		setPZ(yout[6]);

		if (car_ke > 0.0)
			car_speed = Math.sqrt(2.0 * car_ke / ms);
		else
			car_speed = 0.0;

		setHX(Math.cos(car_heading));
		setHZ(Math.sin(car_heading));

		heading2 = heading1;
		heading1 = heading;
		heading = car_heading;

		setSpeed(car_speed);

		if (scenario.isSimcarConstantSpeed()) {
			double fullspeed = Utilities.mph2mps(scenario.getSimcarMPH());
			if (getSpeed() < fullspeed)
				setSpeed(getSpeed() + .1);
			else
				setSpeed(fullspeed);
		} else {
			setSpeed(car_speed);
		}

		long i = Math.max(1, getRoadIndex());
		long newi = i;
		Position nearloc = (road.middle(i)).subtract(getP());
		double norm = (nearloc.getX() * nearloc.getX()) + (nearloc.getZ() * nearloc.getZ());
		double mindist = norm;
		boolean done = false;
		while (!done) {
			i += 1;
			nearloc = (road.middle(i)).subtract(getP());
			norm = (nearloc.getX() * nearloc.getX()) + (nearloc.getZ() * nearloc.getZ());
			if (norm < mindist) {
				mindist = norm;
				newi = i;
			} else
				done = true;
		}
		Position vec1 = (road.middle(newi)).subtract(getP());
		Position vec2 = (road.middle(newi)).subtract(road.middle(newi - 1));
		double dotprod = -((vec1.getX() * vec2.getX()) + (vec1.getZ() * vec2.getZ()));
		double fracdelta;
		if (dotprod < 0) {
			newi--;
			fracdelta = 1.0 + dotprod;
		} else
			fracdelta = dotprod;

		setIndex(newi + fracdelta);
	}

	void update(Environment env) {
		updateDynamics(env);
		nearPoint = env.getRoad().nearPoint(this, lane);
		farPoint = env.getRoad().farPoint(this, null, lane);
		carPoint = env.getAutocar().getP();
		carPoint.setY(.65);
	}

	// void draw (Graphics g, Env env)
	// {
	// g.setColor (Color.black);
	// double hx = h.getX();
	// double hz = h.getZ();
	// double ahead = 4.0;
	// double px1 = p.getX() + (hx*ahead - (-1*hz));
	// double pz1 = p.getZ() + (hz*ahead + (-1*hx));
	// Coordinate im1 = env.world2image (new Position (px1, pz1));
	// double px2 = p.getX() + (hx*ahead - (1*hz));
	// double pz2 = p.getZ() + (hz*ahead + (1*hx));
	// Coordinate im2 = env.world2image (new Position (px2, pz2));
	// g.fillRect (im1.getX(), im1.y, im2.getX()-im1.getX(),
	// Env.envHeight-im1.y);
	//
	// //simDriver.draw (g, env);
	// }

	double devscale = .0015;
	double devx = -.7;
	double devy = .5;

	double ifc2gl_x(double x ) {
		return devx + (devscale * -(x - DrivingNightA.centerX));
	}

	double ifc2gl_y(double y) {
		return devy + (devscale * -(y - DrivingNightA.centerY));
	}

	double gl2ifc_x(double x) {
		return DrivingNightA.centerX - ((x - devx) / devscale);
	}

	double gl2ifc_y(double y) {
		return DrivingNightA.centerY - ((y - devy) / devscale);
	}

	static TextRenderer textRenderer = null;

	void draw(GL2 gl, Environment env) {
		gl.glPushMatrix();
		gl.glTranslated(getPX(), 0, getPZ());
		gl.glRotated(90.0 - (57.30 * Math.atan2(getHZ(), getHX())), 0.0, 1.0, 0.0);

		// Dashboard

		gl.glPushMatrix();
		gl.glTranslated(.13, .2, 1.8);// .2, .2, 1.8);

		gl.glColor3d(1, 1, 1);
		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glBindTexture(GL.GL_TEXTURE_2D, Textures.maximaDashboard);

		double sx = 1.6;
		double sy = 0.8;

		gl.glBegin(GL2.GL_POLYGON);
		gl.glTexCoord2d(0.0f, 0.0f);
		gl.glVertex3d(sx, -sy, 0);
		gl.glTexCoord2d(1.0f, 0.0f);
		gl.glVertex3d(-sx, -sy, 0);
		gl.glTexCoord2d(1.0f, 1.0f);
		gl.glVertex3d(-sx, sy, 0);
		gl.glTexCoord2d(0.0f, 1.0f);
		gl.glVertex3d(sx, sy, 0);
		gl.glEnd();

		gl.glPopMatrix();

		// Steering Wheel

		gl.glPushMatrix();
		gl.glTranslated(.41, .05, 1.7); // (.5, .1, 1.7);
		gl.glRotated(Utilities.rad2deg(steerAngle), 0.0, 0.0, 1.0);

		sx = .75; // .6;
		sy = sx;

		gl.glBindTexture(GL.GL_TEXTURE_2D, Textures.maximaWheel);
		gl.glBegin(GL2.GL_POLYGON);
		gl.glTexCoord2d(0.0f, 0.0f);
		gl.glVertex3d(sx, -sy, 0);
		gl.glTexCoord2d(1.0f, 0.0f);
		gl.glVertex3d(-sx, -sy, 0);
		gl.glTexCoord2d(1.0f, 1.0f);
		gl.glVertex3d(-sx, sy, 0);
		gl.glTexCoord2d(0.0f, 1.0f);
		gl.glVertex3d(sx, sy, 0);
		gl.glEnd();

		gl.glPopMatrix();

		// Interface

		// gl.glPushMatrix();
		// gl.glTranslated(0, 0, 1.7);
		//
		// gl.glBindTexture(GL2.GL_TEXTURE_2D, Textures.interfaceScreen);
		// gl.glBegin(GL2.GL_POLYGON);
		// gl.glTexCoord2d(0.0f, 0.0f);
		// gl.glVertex3d(ifc2gl_x(Driving.minX), ifc2gl_y(Driving.maxY), 0);
		// gl.glTexCoord2d(1.0f, 0.0f);
		// gl.glVertex3d(ifc2gl_x(Driving.maxX), ifc2gl_y(Driving.maxY), 0);
		// gl.glTexCoord2d(1.0f, 1.0f);
		// gl.glVertex3d(ifc2gl_x(Driving.maxX), ifc2gl_y(Driving.minY), 0);
		// gl.glTexCoord2d(0.0f, 1.0f);
		// gl.glVertex3d(ifc2gl_x(Driving.minX), ifc2gl_y(Driving.minY), 0);
		// gl.glEnd();
		//
		// gl.glPopMatrix();

		// Left Hand

		double dr = .65; // .53;

		gl.glPushMatrix();
		gl.glTranslated(.41, .05, 1.69); // .5, .1, 1.69);
		gl.glRotated(-50.0 + Utilities.rad2deg(steerAngle), 0.0, 0.0, 1.0);
		gl.glTranslated(0, dr, 0);
		gl.glRotated(30.0, 0.0, 0.0, 1.0);

		sx = .2;
		sy = sx;

		gl.glBindTexture(GL.GL_TEXTURE_2D, Textures.handSteering);
		gl.glBegin(GL2.GL_POLYGON);
		gl.glTexCoord2d(0.0f, 0.0f);
		gl.glVertex3d(-sx, -sy, 0);
		gl.glTexCoord2d(1.0f, 0.0f);
		gl.glVertex3d(sx, -sy, 0);
		gl.glTexCoord2d(1.0f, 1.0f);
		gl.glVertex3d(sx, sy, 0);
		gl.glTexCoord2d(0.0f, 1.0f);
		gl.glVertex3d(-sx, sy, 0);
		gl.glEnd();

		gl.glPopMatrix();

		// Right Hand

		gl.glPushMatrix();

		// sx = .2;
		// sy = sx;

		// LocationChunk loc = simDriver.model.motor.handLocation;
		// if (loc==null || (loc.kind == Chunk.KIND_WHEEL &&
		// simDriver.model.motor.motorFree()))
		// {
		gl.glTranslated(.41, .05, 1.69); // handx, handy, 1.69);
		gl.glRotated(50.0 + Utilities.rad2deg(steerAngle), 0.0, 0.0, 1.0);
		gl.glTranslated(0, dr, 0);
		gl.glRotated(-30.0, 0.0, 0.0, 1.0);
		// }
		// else
		// {
		// handx = +sx + ifc2gl_x (driver.model.actr2ifc_x (loc.getX()));
		// handy = -sy + ifc2gl_y (driver.model.actr2ifc_y (loc.y));
		// gl.glTranslated (handx, handy, 1.69);
		// }
		//
		// if (loc==null || (loc.kind == Chunk.KIND_WHEEL &&
		// driver.model.motor.motorFree()))
		gl.glBindTexture(GL.GL_TEXTURE_2D, Textures.handSteering);
		// else
		// gl.glBindTexture (GL2.GL_TEXTURE_2D, Textures.HANDPOINT);

		gl.glBegin(GL2.GL_POLYGON);
		gl.glTexCoord2d(0.0f, 0.0f);
		gl.glVertex3d(sx, -sy, 0);
		gl.glTexCoord2d(1.0f, 0.0f);
		gl.glVertex3d(-sx, -sy, 0);
		gl.glTexCoord2d(1.0f, 1.0f);
		gl.glVertex3d(-sx, sy, 0);
		gl.glTexCoord2d(0.0f, 1.0f);
		gl.glVertex3d(sx, sy, 0);
		gl.glEnd();

		gl.glPopMatrix();

		if (Textures.maximaNeedle >= 0) {

			// Speedometer

			gl.glPushMatrix();
			gl.glTranslated(.41, .34, 1.79);
			gl.glRotated(77 + 206 * (Utilities.mps2mph(getSpeed()) / 160), 0.0, 0.0, 1.0);
			sx = .01;
			sy = .1;
			gl.glTranslated(0, -.8 * sy, 0);
			gl.glBindTexture(GL.GL_TEXTURE_2D, Textures.maximaNeedle);
			gl.glBegin(GL2.GL_POLYGON);
			gl.glTexCoord2d(0.0f, 0.0f);
			gl.glVertex3d(sx, -sy, 0);
			gl.glTexCoord2d(1.0f, 0.0f);
			gl.glVertex3d(-sx, -sy, 0);
			gl.glTexCoord2d(1.0f, 1.0f);
			gl.glVertex3d(-sx, sy, 0);
			gl.glTexCoord2d(0.0f, 1.0f);
			gl.glVertex3d(sx, sy, 0);
			gl.glEnd();
			gl.glPopMatrix();

			// Tachometer

			gl.glPushMatrix();
			gl.glTranslated(.70, .335, 1.79);
			double rpm = 1500 + 100 * (Utilities.mps2mph(getSpeed()) % 15);
			gl.glRotated(0 + 180 * (rpm / 7000), 0.0, 0.0, 1.0);
			sx = .01;
			sy = .09;
			gl.glTranslated(0, -.8 * sy, 0);
			gl.glBindTexture(GL.GL_TEXTURE_2D, Textures.maximaNeedle);
			gl.glBegin(GL2.GL_POLYGON);
			gl.glTexCoord2d(0.0f, 0.0f);
			gl.glVertex3d(sx, -sy, 0);
			gl.glTexCoord2d(1.0f, 0.0f);
			gl.glVertex3d(-sx, -sy, 0);
			gl.glTexCoord2d(1.0f, 1.0f);
			gl.glVertex3d(-sx, sy, 0);
			gl.glTexCoord2d(0.0f, 1.0f);
			gl.glVertex3d(sx, sy, 0);
			gl.glEnd();
			gl.glPopMatrix();

		}

		// Done

		gl.glPopMatrix();

		/*
		 * // Overlay
		 * 
		 * Overlay overlay = DistractR.resultsPanel.player.simulator.overlay;
		 * loc = simDriver.model.vision.eyeLocation; if (loc != null && overlay
		 * != null) { Graphics2D g = overlay.createGraphics(); g.setColor
		 * (Color.yellow);
		 * 
		 * Coordinate c = env.world2image (env.road.middle (env.simcar.fracIndex
		 * + 20)); g.fillRect (c.getX()-2, c.y-2, 5, 5); c = env.world2image
		 * (env.road.left (env.simcar.fracIndex + 20)); g.fillRect (c.getX()-2,
		 * c.y-2, 5, 5); c = env.world2image (env.road.right
		 * (env.simcar.fracIndex + 20)); g.fillRect (c.getX()-2, c.y-2, 5, 5);
		 * 
		 * c = env.world2image (env.road.middle (env.simcar.fracIndex + 500));
		 * g.fillRect (c.getX()-2, c.y-2, 5, 5);
		 * 
		 * //g.fillOval (loc.getX(), loc.y, 30, 30); g.fillOval (100, 100, 30,
		 * 30); g.dispose(); overlay.beginRendering(); overlay.drawAll();
		 * overlay.endRendering(); }
		 */

		// Speedometer

		/*
		 * if (textRenderer == null) textRenderer = new TextRenderer (new
		 * Font("Courier", Font.BOLD, 64));
		 * 
		 * textRenderer.beginRendering (Env.envWidth, Env.envHeight);
		 * textRenderer.setColor (0.2f, 1.0f, 0.2f, 0.8f); textRenderer.draw
		 * (Integer.toString((int) Utilities.mps2mph(env.simcar.speed)), 500,
		 * 340); textRenderer.endRendering();
		 */

		/*
		 * // Eye
		 * 
		 * loc = simDriver.model.vision.eyeLocation; if (loc != null) {
		 * gl.glPushMatrix ();
		 * 
		 * sx = .1; sy = sx;
		 * 
		 * if (loc.kind == Chunk.KIND_CAR || loc.kind == Chunk.KIND_FAR_CP) {
		 * double eyei = env.autocar.fracIndex - 5; Position eyep =
		 * env.road.middle (eyei); Position eyeh = env.road.heading (eyei);
		 * gl.glTranslated (eyep.getX(), 0.5, eyep.getZ()); gl.glRotated (45.0 -
		 * (57.30 * Math.atan2(eyeh.getZ(),eyeh.getX())), 0.0, 1.0, 0.0); } else
		 * { handx = ifc2gl_x (simDriver.model.actr2ifc_x (loc.getX())); handy =
		 * ifc2gl_y (simDriver.model.actr2ifc_y (loc.y)); gl.glTranslated
		 * (p.getX(), 0, p.getZ()); gl.glRotated (90.0 - (57.30 *
		 * Math.atan2(h.getZ(),h.getX())), 0.0, 1.0, 0.0); gl.glTranslated
		 * (handx, handy, 1.65); }
		 * 
		 * gl.glBindTexture (GL2.GL_TEXTURE_2D, Textures.EYE); gl.glBegin
		 * (GL2.GL_POLYGON); gl.glTexCoord2d (0.0f,0.0f); gl.glVertex3d (sx,
		 * -sy, 0); gl.glTexCoord2d (1.0f,0.0f); gl.glVertex3d (-sx, -sy, 0);
		 * gl.glTexCoord2d (1.0f,1.0f); gl.glVertex3d (-sx, sy, 0);
		 * gl.glTexCoord2d (0.0f,1.0f); gl.glVertex3d (sx, sy, 0); gl.glEnd ();
		 * 
		 * gl.glPopMatrix(); }
		 */
	}
}
