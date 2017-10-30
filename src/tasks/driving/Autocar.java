package tasks.driving;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

/**
 * An automated car that drives itself down the road.
 * 
 * @author Dario Salvucci
 */
public class Autocar extends Vehicle {
	public enum Type {
		PRIUS, MAXIMA
	};

	private final double MAX_DISTANCE = 500;
	private final double BRAKE_TIME = 1.0;
	private final double BRAKE_DECELERATION_PER_UPDATE = .65;

	private Scenario scenario;
	private int lane;
	private boolean leadCar;
	private Type type;
	private Double timeBetweenBrake;
	private double nextBrakeTime;
	private boolean braking;
	private boolean accelerating;
	private boolean crashed;

	Autocar(Scenario scenario, int lane, boolean leadCar, Type type) {
		super();
		this.scenario = scenario;
		this.lane = lane;
		this.leadCar = leadCar;
		this.type = type;
		if (leadCar) {
			timeBetweenBrake = 0.0;
			nextBrakeTime = timeBetweenBrake;
			braking = false;
			setSpeed(0);
		} else {
			timeBetweenBrake = null;
			braking = false;
			setSpeed(-15);
		}
		accelerating = true;
		crashed = false;
	}

	public boolean isBraking() {
		return braking;
	}

	public boolean hasCrashed() {
		return crashed;
	}

	void update(Environment env) {
		if (leadCar) {
			if (timeBetweenBrake > 0.0) {
				if (!braking && env.getTime() > nextBrakeTime)
					braking = true;
				else if (braking && env.getTime() > nextBrakeTime + BRAKE_TIME) {
					braking = false;
					nextBrakeTime = env.getTime() + timeBetweenBrake;
				}
			}

			if (env.getSimcar().getSpeed() >= 15.0)
				accelerating = false;

			if (accelerating) {
				setSpeed(env.getSimcar().getSpeed() + 1.0);
				setIndex(env.getSimcar().getIndex() + 20.0);
			} else if (braking) {
				setSpeed(getSpeed() - BRAKE_DECELERATION_PER_UPDATE);
				setIndex(getIndex() + getSpeed() * Environment.SAMPLE_TIME);
			} else {
				if (scenario.getLeadCarConstantSpeed()) {
					double fullspeed = Utilities.mph2mps(scenario.getLeadCarMPH()); // scenario.getLeadCarMPH());
					if (getSpeed() < fullspeed)
						setSpeed(getSpeed() + .1);
					else
						setSpeed(fullspeed);
				} else {
					// from CSR 2002
					setSpeed(20 + 5 * Math.sin(getIndex() / 100.0) + 5 * Math.sin(13.0 + getIndex() / 53.0)
							+ 5 * Math.sin(37.0 + getIndex() / 141.0));
				}
				setIndex(getIndex() + getSpeed() * Environment.SAMPLE_TIME);
			}
		} else {
			setIndex(getIndex() + getSpeed() * Environment.SAMPLE_TIME);
			if (getIndex() < env.getSimcar().getIndex() - MAX_DISTANCE)
				setIndex(env.getSimcar().getIndex() + MAX_DISTANCE);
			else if (getIndex() > env.getSimcar().getIndex() + MAX_DISTANCE)
				setIndex(env.getSimcar().getIndex() - MAX_DISTANCE);
		}

		if (getIndex() < env.getSimcar().getIndex() + 5) {
			crashed = true;
			setIndex(env.getSimcar().getIndex() + 5);
		}

		setP(env.getRoad().location(getIndex(), lane + .5));

		Position h = env.getRoad().heading(getIndex());
		setH(leadCar ? h : h.negate());
	}

	void draw(GL2 gl, Environment env) {
		if (type == Type.MAXIMA)
			drawMaxima(gl, env);
		else
			drawPrius(gl, env);
		return;
	}

	void drawPrius(GL2 gl, Environment env) {
		double[] cs = { 0.9, 1.55, 2.5 };

		double[] rb = { -0.9, 0.35, -2.48 };
		double[] rm = { -0.9, 1.18, -2.25 };
		double[] crm = { -0.7, 1.45, -1.30 };
		double[] crh = { -0.7, 1.55, -0.95 };
		double[] ch = { -0.7, 1.55, 0.00 };
		double[] cfh = { -0.7, 1.50, 0.30 };
		double[] cfm = { -0.9, 0.90, 1.90 };
		double[] fm = { -0.9, 0.75, 2.33 };
		double[] fb = { -0.9, 0.16, 2.38 };

		gl.glPushMatrix();
		gl.glTranslated(getPX(), 0, getPZ());
		gl.glRotated(90.0 - (57.30 * Math.atan2(getHZ(), getHZ())), 0.0, 1.0, 0.0);

		double tl[] = { 0, 0 };
		double tr[] = { 1, 1 };

		// Front of car

		gl.glColor3d(1, 1, 1);

		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glBindTexture(GL.GL_TEXTURE_2D, Textures.priusFront);

		gl.glBegin(GL2.GL_POLYGON);
		tl[1] = tr[1] = .00;
		gl.glTexCoord2d(tl[0], tl[1]);
		gl.glVertex3d(-fb[0], fb[1], fb[2]);
		gl.glTexCoord2d(tr[0], tr[1]);
		gl.glVertex3d(fb[0], fb[1], fb[2]);
		tl[1] = tr[1] = .50;
		gl.glTexCoord2d(tr[0], tr[1]);
		gl.glVertex3d(fm[0], fm[1], fm[2]);
		gl.glTexCoord2d(tl[0], tl[1]);
		gl.glVertex3d(-fm[0], fm[1], fm[2]);
		gl.glEnd();

		gl.glBegin(GL2.GL_POLYGON);
		gl.glTexCoord2d(tl[0], tl[1]);
		gl.glVertex3d(-fm[0], fm[1], fm[2]);
		gl.glTexCoord2d(tr[0], tr[1]);
		gl.glVertex3d(fm[0], fm[1], fm[2]);
		tl[1] = tr[1] = .56;
		gl.glTexCoord2d(tr[0], tr[1]);
		gl.glVertex3d(cfm[0], cfm[1], cfm[2]);
		gl.glTexCoord2d(tl[0], tl[1]);
		gl.glVertex3d(-cfm[0], cfm[1], cfm[2]);
		gl.glEnd();

		gl.glBegin(GL2.GL_POLYGON);
		gl.glTexCoord2d(tl[0], tl[1]);
		gl.glVertex3d(-cfm[0], cfm[1], cfm[2]);
		gl.glTexCoord2d(tr[0], tr[1]);
		gl.glVertex3d(cfm[0], cfm[1], cfm[2]);
		tl[1] = tr[1] = .98;
		gl.glTexCoord2d(tr[0], tr[1]);
		gl.glVertex3d(cfh[0], cfh[1], cfh[2]);
		gl.glTexCoord2d(tl[0], tl[1]);
		gl.glVertex3d(-cfh[0], cfh[1], cfh[2]);
		gl.glEnd();

		gl.glBegin(GL2.GL_POLYGON);
		gl.glTexCoord2d(tr[0], tr[1]);
		gl.glVertex3d(ch[0], ch[1] + .01, ch[2]);
		gl.glTexCoord2d(tl[0], tl[1]);
		gl.glVertex3d(-ch[0], ch[1] + .01, ch[2]);
		tl[1] = tr[1] = 1.0;
		gl.glTexCoord2d(tl[0], tl[1]);
		gl.glVertex3d(-cfh[0], cfh[1], cfh[2]);
		gl.glTexCoord2d(tr[0], tr[1]);
		gl.glVertex3d(cfh[0], cfh[1], cfh[2]);
		gl.glEnd();

		gl.glDisable(GL.GL_TEXTURE_2D);

		// Tires

		double r = .35;
		double ang;
		double ang1 = Math.PI / 2.0;
		double ang2 = 3.0 * Math.PI / 2.0;
		double dang = 2.0 * Math.PI / 18.0;

		gl.glColor3d(.11, .11, .11);

		gl.glBegin(GL.GL_TRIANGLE_STRIP);
		for (ang = ang1; ang <= ang2; ang += dang) {
			gl.glVertex3d(fb[0] + .01, r + r * Math.cos(ang) - .05, 1.6 + r * Math.sin(ang));
			gl.glVertex3d(fb[0] + .3, r + r * Math.cos(ang) - .05, 1.6 + r * Math.sin(ang));
		}
		gl.glEnd();
		gl.glBegin(GL2.GL_POLYGON);
		for (ang = ang1; ang <= ang2; ang += dang)
			gl.glVertex3d(fb[0] + .3, r + r * Math.cos(ang) - .05, 1.6 + r * Math.sin(ang));
		gl.glEnd();

		gl.glBegin(GL.GL_TRIANGLE_STRIP);
		for (ang = ang1; ang <= ang2; ang += dang) {
			gl.glVertex3d(-fb[0] - .01, r + r * Math.cos(ang) - .05, 1.6 + r * Math.sin(ang));
			gl.glVertex3d(-fb[0] - .3, r + r * Math.cos(ang) - .05, 1.6 + r * Math.sin(ang));
		}
		gl.glEnd();
		gl.glBegin(GL2.GL_POLYGON);
		for (ang = ang1; ang <= ang2; ang += dang)
			gl.glVertex3d(-fb[0] - .3, r + r * Math.cos(ang) - .05, 1.6 + r * Math.sin(ang));
		gl.glEnd();

		gl.glBegin(GL.GL_TRIANGLE_STRIP);
		for (ang = ang1; ang <= ang2; ang += dang) {
			gl.glVertex3d(rb[0] + .01, r + r * Math.cos(ang) - .05, -1.4 + r * Math.sin(ang));
			gl.glVertex3d(rb[0] + .3, r + r * Math.cos(ang) - .05, -1.4 + r * Math.sin(ang));
		}
		gl.glEnd();
		gl.glBegin(GL2.GL_POLYGON);
		for (ang = ang1; ang <= ang2; ang += dang)
			gl.glVertex3d(rb[0] + .3, r + r * Math.cos(ang) - .05, -1.4 + r * Math.sin(ang));
		gl.glEnd();

		gl.glBegin(GL.GL_TRIANGLE_STRIP);
		for (ang = ang1; ang <= ang2; ang += dang) {
			gl.glVertex3d(-rb[0] - .01, r + r * Math.cos(ang) - .05, -1.4 + r * Math.sin(ang));
			gl.glVertex3d(-rb[0] - .3, r + r * Math.cos(ang) - .05, -1.4 + r * Math.sin(ang));
		}
		gl.glEnd();
		gl.glBegin(GL2.GL_POLYGON);
		for (ang = ang1; ang <= ang2; ang += dang)
			gl.glVertex3d(-rb[0] - .3, r + r * Math.cos(ang) - .05, -1.4 + r * Math.sin(ang));
		gl.glEnd();

		gl.glColor3d(1, 1, 1);

		// Sides of car

		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glBindTexture(GL.GL_TEXTURE_2D, Textures.priusSide);

		gl.glBegin(GL2.GL_POLYGON);
		tl[1] = tr[1] = 1;
		gl.glTexCoord2d(tl[0], tl[1]);
		gl.glVertex3d(cs[0] - .20, cs[1], -cs[2]);
		gl.glTexCoord2d(tr[0], tr[1]);
		gl.glVertex3d(cs[0] - .20, cs[1], cs[2]);
		tl[1] = tr[1] = .7;
		gl.glTexCoord2d(tr[0], tr[1]);
		gl.glVertex3d(cs[0], 1.1, cs[2]);
		gl.glTexCoord2d(tl[0], tl[1]);
		gl.glVertex3d(cs[0], 1.1, -cs[2]);
		gl.glEnd();

		gl.glBegin(GL2.GL_POLYGON);
		gl.glTexCoord2d(tl[0], tl[1]);
		gl.glVertex3d(cs[0], 1.1, -cs[2]);
		gl.glTexCoord2d(tr[0], tr[1]);
		tl[1] = tr[1] = 0;
		gl.glVertex3d(cs[0], 1.1, cs[2]);
		gl.glTexCoord2d(tr[0], tr[1]);
		gl.glVertex3d(cs[0], 0.0, cs[2]);
		gl.glTexCoord2d(tl[0], tl[1]);
		gl.glVertex3d(cs[0], 0.0, -cs[2]);
		gl.glEnd();

		gl.glBegin(GL2.GL_POLYGON);
		tl[1] = tr[1] = 1;
		gl.glTexCoord2d(tl[0], tl[1]);
		gl.glVertex3d(-cs[0] + .20, cs[1], -cs[2]);
		gl.glTexCoord2d(tr[0], tr[1]);
		tl[1] = tr[1] = .7;
		gl.glVertex3d(-cs[0] + .20, cs[1], cs[2]);
		gl.glTexCoord2d(tr[0], tr[1]);
		gl.glVertex3d(-cs[0], 1.1, cs[2]);
		gl.glTexCoord2d(tl[0], tl[1]);
		gl.glVertex3d(-cs[0], 1.1, -cs[2]);
		gl.glEnd();

		gl.glBegin(GL2.GL_POLYGON);
		gl.glTexCoord2d(tl[0], tl[1]);
		gl.glVertex3d(-cs[0], 1.1, -cs[2]);
		gl.glTexCoord2d(tr[0], tr[1]);
		tl[1] = tr[1] = 0;
		gl.glVertex3d(-cs[0], 1.1, cs[2]);
		gl.glTexCoord2d(tr[0], tr[1]);
		gl.glVertex3d(-cs[0], 0.0, cs[2]);
		gl.glTexCoord2d(tl[0], tl[1]);
		gl.glVertex3d(-cs[0], 0.0, -cs[2]);
		gl.glEnd();

		gl.glDisable(GL.GL_TEXTURE_2D);

		// Back of car

		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glBindTexture(GL.GL_TEXTURE_2D, !braking ? Textures.priusBack : Textures.priusBackLit);

		gl.glBegin(GL2.GL_POLYGON);
		tl[1] = tr[1] = 0;
		gl.glTexCoord2d(tl[0], tl[1]);
		gl.glVertex3d(-rb[0], rb[1], rb[2]);
		gl.glTexCoord2d(tr[0], tr[1]);
		gl.glVertex3d(rb[0], rb[1], rb[2]);
		tl[1] = tr[1] = .70;
		gl.glTexCoord2d(tr[0], tr[1]);
		gl.glVertex3d(rm[0], rm[1], rm[2]);
		gl.glTexCoord2d(tl[0], tr[1]);
		gl.glVertex3d(-rm[0], rm[1], rm[2]);
		gl.glEnd();

		gl.glBegin(GL2.GL_POLYGON);
		gl.glTexCoord2d(tl[0], tl[1]);
		gl.glVertex3d(-rm[0], rm[1], rm[2]);
		gl.glTexCoord2d(tr[0], tr[1]);
		gl.glVertex3d(rm[0], rm[1], rm[2]);
		tl[1] = tr[1] = .85;
		gl.glTexCoord2d(tr[0], tr[1]);
		gl.glVertex3d(crm[0], crm[1], crm[2]);
		gl.glTexCoord2d(tl[0], tl[1]);
		gl.glVertex3d(-crm[0], crm[1], crm[2]);
		gl.glEnd();

		gl.glBegin(GL2.GL_POLYGON);
		gl.glTexCoord2d(tl[0], tl[1]);
		gl.glVertex3d(-crm[0], crm[1], crm[2]);
		gl.glTexCoord2d(tr[0], tr[1]);
		gl.glVertex3d(crm[0], crm[1], crm[2]);
		tl[1] = tr[1] = .95;
		gl.glTexCoord2d(tr[0], tr[1]);
		gl.glVertex3d(crh[0], crh[1], crh[2]);
		gl.glTexCoord2d(tl[0], tl[1]);
		gl.glVertex3d(-crh[0], crh[1], crh[2]);
		gl.glEnd();

		gl.glBegin(GL2.GL_POLYGON);
		gl.glTexCoord2d(tl[0], tl[1]);
		gl.glVertex3d(-crh[0], crh[1], crh[2]);
		gl.glTexCoord2d(tr[0], tr[1]);
		gl.glVertex3d(crh[0], crh[1], crh[2]);
		tl[1] = tr[1] = 1;
		gl.glTexCoord2d(tr[0], tr[1]);
		gl.glVertex3d(ch[0], ch[1], ch[2]);
		gl.glTexCoord2d(tl[0], tl[1]);
		gl.glVertex3d(-ch[0], ch[1], ch[2]);
		gl.glEnd();

		gl.glDisable(GL.GL_TEXTURE_2D);

		gl.glColor3d(1, 1, 1);

		gl.glPopMatrix();
	}

	void drawMaxima(GL2 gl, Environment env) {
		double[] rb = { -0.9, 0, -2.48 };
		double[] rm = { -0.9, 1.4, -2.25 };

		gl.glPushMatrix();
		gl.glTranslated(getPX(), 0, getPZ());
		gl.glRotated(90.0 - (57.30 * Math.atan2(getHZ(), getHZ())), 0.0, 1.0, 0.0);

		double tl[] = { 0, 0 };
		double tr[] = { 1, 1 };

		// Back of car

		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glBindTexture(GL.GL_TEXTURE_2D,
				crashed ? Textures.maximaBackCrash : (braking ? Textures.maximaBackLit : Textures.maximaBack));

		gl.glBegin(GL2.GL_POLYGON);
		tl[1] = tr[1] = 0.0;
		gl.glTexCoord2d(tl[0], tl[1]);
		gl.glVertex3d(-rb[0], rb[1], rb[2]);
		gl.glTexCoord2d(tr[0], tr[1]);
		gl.glVertex3d(rb[0], rb[1], rb[2]);
		tl[1] = tr[1] = 1.0;
		gl.glTexCoord2d(tr[0], tr[1]);
		gl.glVertex3d(rm[0], rm[1], rm[2]);
		gl.glTexCoord2d(tl[0], tr[1]);
		gl.glVertex3d(-rm[0], rm[1], rm[2]);
		gl.glEnd();

		gl.glDisable(GL.GL_TEXTURE_2D);

		gl.glColor3d(1, 1, 1);

		gl.glPopMatrix();
	}
}
