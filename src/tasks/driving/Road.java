package tasks.driving;

import java.util.Random;
import java.util.Vector;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES3;

/**
 * The primary class that defines a road.
 * 
 * @author Dario Salvucci
 */
public class Road {
	public static final int N_LANES = 4;

	private Scenario scenario;

	Road(Scenario scenario) {
		this.scenario = scenario;
	}

	private static Vector<Segment> segments = null;
	private static boolean savedCurvedRoad = false;

	class Segment {
		Position left, middle, right;
		Position h;
		Position l_left, r_right;
		Position ll_left, rr_right;
		Position lll_left, rrr_right;
		Position ll_mid, lr_mid, rl_mid, rr_mid;
		Position l_lmid, r_lmid, l_rmid, r_rmid;

		int treeType;
		double treeWidth, treeHeight;
		Position l_tree, r_tree;

		Segment(double a1, double a2, double a3, double a4, double a5, double a6) {
			left = new Position(a1, a2);
			middle = new Position(a3, a4);
			right = new Position(a5, a6);

			h = new Position(right.getX() - left.getX(), right.getZ() - left.getZ());
			h = h.normalize();

			double HALF_STRIPW = 0.08;
			double STRIPW = (2 * HALF_STRIPW);
			double SHOULDER = 1.5;
			double WALL = 40;

			double dx = .05 * h.getX();
			double dz = .05 * h.getZ();

			l_left = new Position(left.getX() - 2 * STRIPW * h.getX() - dx, left.getZ() - 2 * STRIPW * h.getZ() - dz);
			r_right = new Position(right.getX() + 2 * STRIPW * h.getX() + dx,
					right.getZ() + 2 * STRIPW * h.getZ() + dz);

			ll_left = new Position(left.getX() - SHOULDER * h.getX() - dx, left.getZ() - SHOULDER * h.getZ() - dz);
			rr_right = new Position(right.getX() + SHOULDER * h.getX() + dx, right.getZ() + SHOULDER * h.getZ() + dz);

			lll_left = new Position(left.getX() - WALL * h.getX(), left.getZ() - WALL * h.getZ());
			rrr_right = new Position(right.getX() + WALL * h.getX(), right.getZ() + WALL * h.getZ());

			ll_mid = new Position(middle.getX() - 3 * HALF_STRIPW * h.getX(),
					middle.getZ() - 3 * HALF_STRIPW * h.getZ());
			lr_mid = new Position(middle.getX() - HALF_STRIPW * h.getX(), middle.getZ() - HALF_STRIPW * h.getZ());
			rl_mid = new Position(middle.getX() + HALF_STRIPW * h.getX(), middle.getZ() + HALF_STRIPW * h.getZ());
			rr_mid = new Position(middle.getX() + 3 * HALF_STRIPW * h.getX(),
					middle.getZ() + 3 * HALF_STRIPW * h.getZ());

			// if (N_LANES == 4) {
			l_lmid = new Position((0.5 * ll_mid.getX() + 0.5 * left.getX()) - HALF_STRIPW * h.getX(),
					(0.5 * ll_mid.getZ() + 0.5 * left.getZ()) - HALF_STRIPW * h.getZ());
			r_lmid = new Position((0.5 * ll_mid.getX() + 0.5 * left.getX()) + HALF_STRIPW * h.getX(),
					(0.5 * ll_mid.getZ() + 0.5 * left.getZ()) + HALF_STRIPW * h.getZ());

			l_rmid = new Position((0.5 * rr_mid.getX() + 0.5 * right.getX()) - HALF_STRIPW * h.getX(),
					(0.5 * rr_mid.getZ() + 0.5 * right.getZ()) - HALF_STRIPW * h.getZ());
			r_rmid = new Position((0.5 * rr_mid.getX() + 0.5 * right.getX()) + HALF_STRIPW * h.getX(),
					(0.5 * rr_mid.getZ() + 0.5 * right.getZ()) + HALF_STRIPW * h.getZ());
			// } else if (N_LANES == 3) {
			// l_lmid = new Position((0.666 * middle.getX() + 0.334 *
			// left.getX()) - HALF_STRIPW * h.getX(),
			// (0.666 * middle.getZ() + 0.334 * left.getZ()) - HALF_STRIPW *
			// h.getZ());
			// r_lmid = new Position((0.666 * middle.getX() + 0.334 *
			// left.getX()) + HALF_STRIPW * h.getX(),
			// (0.666 * middle.getZ() + 0.334 * left.getZ()) + HALF_STRIPW *
			// h.getZ());
			//
			// l_rmid = new Position((0.666 * middle.getX() + 0.334 *
			// right.getX()) - HALF_STRIPW * h.getX(),
			// (0.666 * middle.getZ() + 0.334 * right.getZ()) - HALF_STRIPW *
			// h.getZ());
			// r_rmid = new Position((0.666 * middle.getX() + 0.334 *
			// right.getX()) + HALF_STRIPW * h.getX(),
			// (0.666 * middle.getZ() + 0.334 * right.getZ()) + HALF_STRIPW *
			// h.getZ());
			// }

			Random r = new Random();

			treeType = r.nextInt(3);
			treeWidth = 5 + 5 * r.nextDouble();
			treeHeight = 1.5 * treeWidth;

			double rr = .25 + .25 * r.nextDouble();
			l_tree = new Position(left.getX() - rr * WALL * h.getX(), left.getZ() - rr * WALL * h.getZ());
			r_tree = new Position(right.getX() + rr * WALL * h.getX(), right.getZ() + rr * WALL * h.getZ());
		}
	}

	void startup() {
		boolean curved = scenario.isCurvedRoad();
		if (segments != null && savedCurvedRoad == curved)
			return;

		segments = new Vector<Segment>();
		savedCurvedRoad = curved;

		Position p = new Position(0.0, 0.0);
		Position h = new Position(1.0, 0.0);
		h = h.normalize();

		int seglen = 200;
		int segcount = 0;
		boolean curving = false;
		double da = 0;
		double dascale = .02;
		double d = N_LANES * 3.66 / 2.0;

		for (int i = 1; i <= 100000; i++) {
			if (segcount >= seglen) {
				segcount = 0;
				seglen = 100;
				if (curved) {
					curving = !curving;
					if (curving)
						da = ((da > 0) ? -1 : +1) * dascale * 17; // (i % 17);
				}
			}
			if (curving)
				h = h.rotate(da);
			p = p.add(h);
			Segment s = new Segment(p.getX() + d * h.getZ(), p.getZ() - d * h.getX(), p.getX(), p.getZ(),
					p.getX() - d * h.getZ(), p.getZ() + d * h.getX());
			segments.addElement(s);
			segcount++;
		}
	}

	public Segment getSegment(int i) {
		return (segments.elementAt(i));
	}

	public Position location(double index, double lanePos) {
		int i = (int) (Math.floor(index));
		double r = index - i;
		double laner = (lanePos - 1) / N_LANES;
		if (i == index) {
			Position locL = getSegment(i).left;
			Position locR = getSegment(i).right;
			return locL.average(locR, laner);
		} else {
			Position loc1L = getSegment(i).left;
			Position loc1R = getSegment(i).right;
			Position loc1 = loc1L.average(loc1R, laner);
			Position loc2L = getSegment(i + 1).left;
			Position loc2R = getSegment(i + 1).right;
			Position loc2 = loc2L.average(loc2R, laner);
			return loc1.average(loc2, r);
		}
	}

	public Position left(double index) {
		return location(index, N_LANES + 1);
	}

	public Position left(double index, int lane) {
		return location(index, lane + 1);
	}

	public Position middle(double index) {
		return location(index, 0.5 * N_LANES);
	}

	public Position middle(double index, int lane) {
		return location(index, lane + .5);
	}

	public Position right(double index) {
		return location(index, 1);
	}

	public Position right(double index, int lane) {
		return location(index, lane);
	}

	public Position heading(double index) {
		Position locdiff = (middle(index + 1)).subtract(middle(index - 1));
		return locdiff.normalize();
	}

	void vehicleReset(Vehicle v, int lane, double index) {
		Position p = middle(index, lane);
		Position h = heading(index);
		if (/* N_LANES == 4 && */ lane <= 2)
			h = h.negate();
		v.setPX(p.getX());
		v.setPZ(p.getZ());
		v.setHX(h.getX());
		v.setHZ(h.getZ());
		v.setIndex(index);
	}

	public double vehicleLanePosition(Vehicle v) {
		double i = v.getIndex();
		Position lloc = left(i);
		Position rloc = right(i);
		Position head = heading(i);
		double ldx = head.getX() * (v.getPZ() - rloc.getZ());
		double ldz = head.getZ() * (v.getPX() - rloc.getX());
		double wx = head.getX() * (lloc.getZ() - rloc.getZ());
		double wz = head.getZ() * (lloc.getX() - rloc.getX());
		double ldist = Math.abs(ldx) + Math.abs(ldz);
		double width = Math.abs(wx) + Math.abs(wz);
		double lanepos = (ldist / width) * N_LANES;
		if (((Math.abs(wx) > Math.abs(wz)) && (Utilities.sign(ldx) != Utilities.sign(wx)))
				|| ((Math.abs(wz) > Math.abs(wx)) && (Utilities.sign(ldz) != Utilities.sign(wz))))
			lanepos = -lanepos;
		lanepos += 1;
		return lanepos;
	}

	public int vehicleLane(Vehicle v) {
		return (int) Math.floor(vehicleLanePosition(v));
	}

	private double nearDistance = 10.0;
	private double farTime = 4.0;

	Position nearPoint(Simcar simcar) {
		return middle(simcar.getIndex() + nearDistance);
	}

	Position nearPoint(Simcar simcar, int lane) {
		return middle(simcar.getIndex() + nearDistance, lane);
	}

	Position farPoint(Simcar simcar, Vector<Autocar> autocars, int lane) {
		double fracNearestRP = simcar.getIndex();
		long nearestRP = (int) Math.floor(fracNearestRP);
		long j = nearestRP + 1;
		Position simcarLoc = new Position(simcar.getPX(), simcar.getPZ());
		int turn = 0; // left=1, right=2
		double aheadMin = nearDistance + 10;
		double aheadMax = Math.max(aheadMin, simcar.getSpeed() * farTime);

		int rln = (lane != 0) ? lane : 1;
		int lln = (lane != 0) ? lane : 2;

		Position h_l = (left(j, lln)).subtract(simcarLoc);
		Position hrd_l = (left(j, lln)).subtract(left(j - 1, lln));
		Position h_r = (right(j, rln)).subtract(simcarLoc);
		Position hrd_r = (right(j, rln)).subtract(right(j - 1, rln));

		double lxprod1 = (h_l.getX() * hrd_l.getZ()) - (h_l.getZ() * hrd_l.getX());
		double norm_lxp1 = Math.abs(lxprod1 / (Math.sqrt(Utilities.square(h_l.getX()) + Utilities.square(h_l.getZ()))
				+ Math.sqrt(Utilities.square(hrd_l.getX()) + Utilities.square(hrd_l.getZ()))));
		double rxprod1 = (h_r.getX() * hrd_r.getZ()) - (h_r.getZ() * hrd_r.getX());
		// note: below, lisp code has lxprod1 instead!!
		double norm_rxp1 = Math.abs(rxprod1 / (Math.sqrt(Utilities.square(h_r.getX()) + Utilities.square(h_r.getZ()))
				+ Math.sqrt(Utilities.square(hrd_r.getX()) + Utilities.square(hrd_r.getZ()))));

		boolean go_on = true;

		while (go_on) {
			j += 1;

			h_l = (left(j, lln)).subtract(simcarLoc);
			hrd_l = (left(j, lln)).subtract(left(j - 1, lln));
			h_r = (right(j, rln)).subtract(simcarLoc);
			hrd_r = (right(j, rln)).subtract(right(j - 1, rln));

			double lxprod2 = (h_l.getX() * hrd_l.getZ()) - (h_l.getZ() * hrd_l.getX());
			double norm_lxp2 = Math
					.abs(lxprod1 / (Math.sqrt(Utilities.square(h_l.getX()) + Utilities.square(h_l.getZ()))
							+ Math.sqrt(Utilities.square(hrd_l.getX()) + Utilities.square(hrd_l.getZ()))));
			double rxprod2 = (h_r.getX() * hrd_r.getZ()) - (h_r.getZ() * hrd_r.getX());
			double norm_rxp2 = Math
					.abs(rxprod1 / (Math.sqrt(Utilities.square(h_r.getX()) + Utilities.square(h_r.getZ()))
							+ Math.sqrt(Utilities.square(hrd_r.getX()) + Utilities.square(hrd_r.getZ()))));

			if (Utilities.sign(lxprod1) != Utilities.sign(lxprod2)) {
				turn = 1;
				go_on = false;
			}
			if (Utilities.sign(rxprod1) != Utilities.sign(rxprod2)) {
				turn = 2;
				go_on = false;
			}

			lxprod1 = lxprod2;
			norm_lxp1 = norm_lxp2;
			rxprod1 = rxprod2;
			norm_rxp1 = norm_rxp2;

			if (j >= (fracNearestRP + aheadMax)) {
				turn = 0;
				go_on = false;
			}
			if (j <= (fracNearestRP + aheadMin)) {
				j = (long) (fracNearestRP + aheadMin);
			}

			if (lane != 0) {
				if (turn == 1) // left
				{
					double fi = ((norm_lxp1 * (j - 1)) + (norm_lxp2 * (j - 2))) / (norm_lxp1 + norm_lxp2);
					// fpText = "ltp";
					// fpTPindex = fi;
					return left(fi, lane);
				} else if (turn == 2) // right
				{
					double fi = ((norm_rxp1 * (j - 1)) + (norm_rxp2 * (j - 2))) / (norm_rxp1 + norm_rxp2);
					// fpText = "rtp";
					// fpTPindex = fi;
					return right(fi, lane);
				} else {
					double fi = fracNearestRP + aheadMax;
					// fpText = "vp";
					// fpTPindex = 0;
					return middle(fi, lane);
				}
			} else {
				// not implemented -- only for lane changes
			}
		}
		return null;
	}

	// private float distAhead = 400;
	//
	// void draw (Graphics g, Env env)
	// {
	// long ri = env.simcar.roadIndex;
	//
	// g.setColor (Color.darkGray);
	// Polygon p = new Polygon ();
	// Coordinate newLoc = env.world2image (location (ri+3, 1));
	// p.addPoint (newLoc.getX(), newLoc.getY());
	// newLoc = env.world2image (location (ri+distAhead, 1));
	// p.addPoint (newLoc.getX(), newLoc.getY());
	// newLoc = env.world2image (location (ri+distAhead, 4));
	// p.addPoint (newLoc.getX(), newLoc.getY());
	// newLoc = env.world2image (location (ri+3, 4));
	// p.addPoint (newLoc.getX(), newLoc.getY());
	// g.fillPolygon (p);
	//
	// long di = 3;
	// int[] lps = {1,2,3,4};
	// Coordinate[] oldLocs = {null, null, null, null};
	// while (di <= distAhead)
	// {
	// g.setColor (Color.white);
	// for (int i=0 ; i<4 ; i++)
	// {
	// double lp = lps[i];
	// Coordinate oldLoc = oldLocs[i];
	// newLoc = env.world2image (location (ri+di, lp));
	// if (oldLoc!=null && newLoc!=null
	// && (lp==1 || lp==4 || ((ri+di) % 5 < 2))
	// )
	// {
	// g.drawLine (oldLoc.getX(), oldLoc.getY(), newLoc.getX(), newLoc.getY());
	// }
	// oldLocs[i] = newLoc;
	// }
	//
	// if (di < 50) di += 1;
	// else if (di < 100) di += 3;
	// else di += 25;
	// }
	// }

	void drawCone(GL2 gl, Position p) {
		double cx = p.getX(), cz = p.getZ();
		double h = .71, d = .14, d2 = .025, d3 = .20;

		gl.glColor3d(.90, .24, .00);

		gl.glBegin(GL2.GL_POLYGON);
		gl.glTexCoord2d(0, 0);
		gl.glVertex3d(cx + d, 0, cz + d);
		gl.glTexCoord2d(1, 0);
		gl.glVertex3d(cx - d, 0, cz + d);
		gl.glTexCoord2d(1, 1);
		gl.glVertex3d(cx - d2, h, cz + d2);
		gl.glTexCoord2d(0, 1);
		gl.glVertex3d(cx + d2, h, cz + d2);
		gl.glEnd();

		gl.glBegin(GL2.GL_POLYGON);
		gl.glTexCoord2d(0, 0);
		gl.glVertex3d(cx + d, 0, cz - d);
		gl.glTexCoord2d(1, 0);
		gl.glVertex3d(cx - d, 0, cz - d);
		gl.glTexCoord2d(1, 1);
		gl.glVertex3d(cx - d2, h, cz - d2);
		gl.glTexCoord2d(0, 1);
		gl.glVertex3d(cx + d2, h, cz - d2);
		gl.glEnd();

		gl.glBegin(GL2.GL_POLYGON);
		gl.glTexCoord2d(0, 0);
		gl.glVertex3d(cx + d, 0, cz + d);
		gl.glTexCoord2d(1, 0);
		gl.glVertex3d(cx + d, 0, cz - d);
		gl.glTexCoord2d(1, 1);
		gl.glVertex3d(cx + d2, h, cz - d2);
		gl.glTexCoord2d(0, 1);
		gl.glVertex3d(cx + d2, h, cz + d2);
		gl.glEnd();

		gl.glBegin(GL2.GL_POLYGON);
		gl.glTexCoord2d(0, 0);
		gl.glVertex3d(cx - d, 0, cz + d);
		gl.glTexCoord2d(1, 0);
		gl.glVertex3d(cx - d, 0, cz - d);
		gl.glTexCoord2d(1, 1);
		gl.glVertex3d(cx - d2, h, cz - d2);
		gl.glTexCoord2d(0, 1);
		gl.glVertex3d(cx - d2, h, cz + d2);
		gl.glEnd();

		gl.glBegin(GL2.GL_POLYGON);
		gl.glTexCoord2d(0, 0);
		gl.glVertex3d(cx + d3, .02, cz + d3);
		gl.glTexCoord2d(1, 0);
		gl.glVertex3d(cx - d3, .02, cz + d3);
		gl.glTexCoord2d(1, 1);
		gl.glVertex3d(cx - d3, .02, cz - d3);
		gl.glTexCoord2d(0, 1);
		gl.glVertex3d(cx + d3, .02, cz - d3);
		gl.glEnd();
	}

	boolean isLineSegment(int i) {
		return ((i % 12) < 4);
	}

	@SuppressWarnings("unused")
	void draw(GL2 gl, Environment env) {
		int distAhead = 400;
		int start_idx = (int) (Math.floor(env.getSimcar().getRoadIndex()));
		int end_idx = start_idx + distAhead;

		int i;
		int JUMP_ROAD = 20;
		int JUMP_LINE = 2;
		int JUMP_TREES = 10;
		int JUMP_CONE = 30;

		// Textured Road

		gl.glEnable(GL.GL_TEXTURE_2D);

		for (i = end_idx + JUMP_ROAD + 1; (i >= start_idx) && (i >= JUMP_ROAD); i--)
			if (i % JUMP_ROAD == 0) {
				Segment s1 = getSegment(i);
				Segment s2 = getSegment(i - JUMP_ROAD);
				// gl.glTexEnvf (GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE,
				// GL2.GL_DECAL);

				gl.glBindTexture(GL.GL_TEXTURE_2D, Textures.road);
				gl.glBegin(GL2ES3.GL_QUADS);
				gl.glTexCoord2f(0.0f, 0.0f);
				gl.glVertex3d(s1.ll_left.getX(), 0.0, s1.ll_left.getZ());
				gl.glTexCoord2f(1.0f, 0.0f);
				gl.glVertex3d(s1.rr_right.getX(), 0.0, s1.rr_right.getZ());
				gl.glTexCoord2f(1.0f, 1.0f);
				gl.glVertex3d(s2.rr_right.getX(), 0.0, s2.rr_right.getZ());
				gl.glTexCoord2f(0.0f, 1.0f);
				gl.glVertex3d(s2.ll_left.getX(), 0.0, s2.ll_left.getZ());
				gl.glEnd();

				gl.glBindTexture(GL.GL_TEXTURE_2D, Textures.grass);
				gl.glBegin(GL2ES3.GL_QUADS);
				gl.glTexCoord2f(0.0f, 0.0f);
				gl.glVertex3d(s1.lll_left.getX(), 0.0, s1.lll_left.getZ());
				gl.glTexCoord2f(1.0f, 0.0f);
				gl.glVertex3d(s1.ll_left.getX(), 0.0, s1.ll_left.getZ());
				gl.glTexCoord2f(1.0f, 1.0f);
				gl.glVertex3d(s2.ll_left.getX(), 0.0, s2.ll_left.getZ());
				gl.glTexCoord2f(0.0f, 1.0f);
				gl.glVertex3d(s2.lll_left.getX(), 0.0, s2.lll_left.getZ());
				gl.glEnd();

				gl.glBindTexture(GL.GL_TEXTURE_2D, Textures.grass);
				gl.glBegin(GL2ES3.GL_QUADS);
				gl.glTexCoord2f(0.0f, 0.0f);
				gl.glVertex3d(s1.rrr_right.getX(), 0.0, s1.rrr_right.getZ());
				gl.glTexCoord2f(1.0f, 0.0f);
				gl.glVertex3d(s1.rr_right.getX(), 0.0, s1.rr_right.getZ());
				gl.glTexCoord2f(1.0f, 1.0f);
				gl.glVertex3d(s2.rr_right.getX(), 0.0, s2.rr_right.getZ());
				gl.glTexCoord2f(0.0f, 1.0f);
				gl.glVertex3d(s2.rrr_right.getX(), 0.0, s2.rrr_right.getZ());
				gl.glEnd();
			}

		gl.glDisable(GL.GL_TEXTURE_2D);

		// Side lines

		gl.glColor3d(.8, .8, .8);

		gl.glBegin(GL2.GL_QUAD_STRIP);
		for (i = end_idx + JUMP_LINE; i >= start_idx; i--)
			if (i % JUMP_LINE == 0) {
				Segment s = getSegment(i);
				gl.glVertex3d(s.l_left.getX(), .02, s.l_left.getZ());
				gl.glVertex3d(s.left.getX(), 0.02, s.left.getZ());
			}
		gl.glEnd();
		gl.glBegin(GL.GL_LINE_STRIP);
		for (i = end_idx + JUMP_LINE; i >= start_idx; i--)
			if (i % JUMP_LINE == 0) {
				Segment s = getSegment(i);
				gl.glVertex3d(s.left.getX(), .02, s.left.getZ());
			}
		gl.glEnd();

		gl.glBegin(GL2.GL_QUAD_STRIP);
		for (i = end_idx + JUMP_LINE; i >= start_idx; i--)
			if (i % JUMP_LINE == 0) {
				Segment s = getSegment(i);
				gl.glVertex3d(s.r_right.getX(), .02, s.r_right.getZ());
				gl.glVertex3d(s.right.getX(), .02, s.right.getZ());
			}
		gl.glEnd();
		gl.glBegin(GL.GL_LINE_STRIP);
		for (i = end_idx + JUMP_LINE; i >= start_idx; i--)
			if (i % JUMP_LINE == 0) {
				Segment s = getSegment(i);
				gl.glVertex3d(s.right.getX(), .02, s.right.getZ());
			}
		gl.glEnd();

		gl.glColor3d(1, 1, 1);

		// Middle lines

		// if (N_LANES == 4) {
		gl.glColor3d(.8, .8, .2);

		gl.glBegin(GL2.GL_QUAD_STRIP);
		for (i = end_idx + JUMP_LINE; i >= start_idx; i--)
			if (i % JUMP_LINE == 0) {
				Segment s = getSegment(i);
				gl.glVertex3d(s.ll_mid.getX(), .02, s.ll_mid.getZ());
				gl.glVertex3d(s.lr_mid.getX(), 0.02, s.lr_mid.getZ());
			}
		gl.glEnd();
		gl.glBegin(GL.GL_LINE_STRIP);
		for (i = end_idx + JUMP_LINE; i >= start_idx; i--)
			if (i % JUMP_LINE == 0) {
				Segment s = getSegment(i);
				gl.glVertex3d(s.ll_mid.getX(), .02, s.ll_mid.getZ());
			}
		gl.glEnd();

		gl.glBegin(GL2.GL_QUAD_STRIP);
		for (i = end_idx + JUMP_LINE; i >= start_idx; i--)
			if (i % JUMP_LINE == 0) {
				Segment s = getSegment(i);
				gl.glVertex3d(s.rl_mid.getX(), .02, s.rl_mid.getZ());
				gl.glVertex3d(s.rr_mid.getX(), .02, s.rr_mid.getZ());
			}
		gl.glEnd();
		gl.glBegin(GL.GL_LINE_STRIP);
		for (i = end_idx + JUMP_LINE; i >= start_idx; i--)
			if (i % JUMP_LINE == 0) {
				Segment s = getSegment(i);
				gl.glVertex3d(s.rr_mid.getX(), .02, s.rr_mid.getZ());
			}
		gl.glEnd();

		gl.glColor3d(1, 1, 1);
		// }

		// Left-middle line

		gl.glColor3d(.8, .8, .8);

		gl.glBegin(GL2.GL_QUAD_STRIP);
		for (i = end_idx + JUMP_LINE; i >= start_idx; i--)
			if (i % JUMP_LINE != 0) {
				Segment s = getSegment(i);
				if (isLineSegment(i))
					gl.glColor4d(.8, .8, .8, 1.0);
				else
					gl.glColor4d(0.0, 0.0, 0.0, 0.0);
				gl.glVertex3d(s.r_lmid.getX(), .02, s.r_lmid.getZ());
				gl.glVertex3d(s.l_lmid.getX(), .02, s.l_lmid.getZ());
			}
		gl.glEnd();

		// Right-middle line
		gl.glBegin(GL2.GL_QUAD_STRIP);
		for (i = end_idx + JUMP_LINE; i >= start_idx; i--)
			if (i % JUMP_LINE == 0) {
				Segment s = getSegment(i);
				if (isLineSegment(i))
					gl.glColor4d(.8, .8, .8, 1.0);
				else
					gl.glColor4d(0.0, 0.0, 0.0, 0.0);
				gl.glVertex3d(s.r_rmid.getX(), .02, s.r_rmid.getZ());
				gl.glVertex3d(s.l_rmid.getX(), .02, s.l_rmid.getZ());
			}
		gl.glEnd();

		gl.glColor3d(1, 1, 1);

		// Trees

		gl.glEnable(GL.GL_TEXTURE_2D);
		for (i = end_idx + JUMP_TREES + 1; (i >= start_idx) && (i >= JUMP_TREES); i--)
			if (i % JUMP_TREES == 0) {
				Segment s = getSegment(i);

				gl.glBindTexture(GL.GL_TEXTURE_2D, Textures.trees[s.treeType]);

				gl.glBegin(GL2.GL_POLYGON);
				gl.glTexCoord2d(0.0f, 1.0f);
				gl.glVertex3d(s.l_tree.getX() - s.treeWidth * s.h.getX(), s.treeHeight,
						s.l_tree.getZ() - s.treeWidth * s.h.getZ());
				gl.glTexCoord2d(1.0f, 1.0f);
				gl.glVertex3d(s.l_tree.getX() + s.treeWidth * s.h.getX(), s.treeHeight,
						s.l_tree.getZ() + s.treeWidth * s.h.getZ());
				gl.glTexCoord2d(1.0f, 0.0f);
				gl.glVertex3d(s.l_tree.getX() + s.treeWidth * s.h.getX(), 0,
						s.l_tree.getZ() + s.treeWidth * s.h.getZ());
				gl.glTexCoord2d(0.0f, 0.0f);
				gl.glVertex3d(s.l_tree.getX() - s.treeWidth * s.h.getX(), 0,
						s.l_tree.getZ() - s.treeWidth * s.h.getZ());
				gl.glEnd();

				gl.glBegin(GL2.GL_POLYGON);
				gl.glTexCoord2d(0.0f, 1.0f);
				gl.glVertex3d(s.r_tree.getX() - s.treeWidth * s.h.getX(), s.treeHeight,
						s.r_tree.getZ() - s.treeWidth * s.h.getZ());
				gl.glTexCoord2d(1.0f, 1.0f);
				gl.glVertex3d(s.r_tree.getX() + s.treeWidth * s.h.getX(), s.treeHeight,
						s.r_tree.getZ() + s.treeWidth * s.h.getZ());
				gl.glTexCoord2d(1.0f, 0.0f);
				gl.glVertex3d(s.r_tree.getX() + s.treeWidth * s.h.getX(), 0,
						s.r_tree.getZ() + s.treeWidth * s.h.getZ());
				gl.glTexCoord2d(0.0f, 0.0f);
				gl.glVertex3d(s.r_tree.getX() - s.treeWidth * s.h.getX(), 0,
						s.r_tree.getZ() - s.treeWidth * s.h.getZ());
				gl.glEnd();
			}
		gl.glDisable(GL.GL_TEXTURE_2D);

		// Road signs

		// if (lanes == 4)
		// {
		// gl.glEnable (GL2.GL_TEXTURE_2D);
		// for ( i=end_idx+JUMP_ROAD ; (i>=start_idx) && (i>=JUMP_ROAD) ; i-- )
		// if (i % 501 == 0)
		// {
		// int type = Textures.SIGN_ROAD + (i % 2);
		// gl.glBindTexture (GL2.GL_TEXTURE_2D, type);
		//
		// Segment s = getSegment (i);
		// double halfw = 0.5;
		// double height = 2.0;
		//
		// gl.glBegin (GL2.GL_POLYGON);
		// gl.glTexCoord2d (1.0f,1.0f);
		// gl.glVertex3d (s.ll_left.getX() - halfw * s.h.getX(), height,
		// s.ll_left.getZ() -
		// halfw * s.h.getZ());
		// gl.glTexCoord2d (0.0f,1.0f);
		// gl.glVertex3d (s.ll_left.getX() + halfw * s.h.getX(), height,
		// s.ll_left.getZ() +
		// halfw * s.h.getZ());
		// gl.glTexCoord2d (0.0f,0.0f);
		// gl.glVertex3d (s.ll_left.getX() + halfw * s.h.getX(), 0,
		// s.ll_left.getZ() + halfw *
		// s.h.getZ());
		// gl.glTexCoord2d (1.0f,0.0f);
		// gl.glVertex3d (s.ll_left.getX() - halfw * s.h.getX(), 0,
		// s.ll_left.getZ() - halfw *
		// s.h.getZ());
		// gl.glEnd ();
		// }
		// gl.glDisable (GL2.GL_TEXTURE_2D);
		// }

		// Construction cones

		if (N_LANES == 3) {
			gl.glEnable(GL.GL_TEXTURE_2D);
			gl.glBindTexture(GL.GL_TEXTURE_2D, Textures.cone);
			for (i = end_idx + JUMP_CONE; i >= start_idx; i--)
				if (i % JUMP_CONE == 0) {
					Segment s = getSegment(i);
					double halfw = .20;
					double height = .71;

					Position p = location(i, 1.85);

					gl.glBegin(GL2.GL_POLYGON);
					gl.glTexCoord2d(1.0f, 1.0f);
					gl.glVertex3d(p.getX() - halfw * s.h.getX(), height, p.getZ() - halfw * s.h.getZ());
					gl.glTexCoord2d(0.0f, 1.0f);
					gl.glVertex3d(p.getX() + halfw * s.h.getX(), height, p.getZ() + halfw * s.h.getZ());
					gl.glTexCoord2d(0.0f, 0.0f);
					gl.glVertex3d(p.getX() + halfw * s.h.getX(), 0, p.getZ() + halfw * s.h.getZ());
					gl.glTexCoord2d(1.0f, 0.0f);
					gl.glVertex3d(p.getX() - halfw * s.h.getX(), 0, p.getZ() - halfw * s.h.getZ());
					gl.glEnd();

					p = location(i, 3.15);

					gl.glBegin(GL2.GL_POLYGON);
					gl.glTexCoord2d(1.0f, 1.0f);
					gl.glVertex3d(p.getX() - halfw * s.h.getX(), height, p.getZ() - halfw * s.h.getZ());
					gl.glTexCoord2d(0.0f, 1.0f);
					gl.glVertex3d(p.getX() + halfw * s.h.getX(), height, p.getZ() + halfw * s.h.getZ());
					gl.glTexCoord2d(0.0f, 0.0f);
					gl.glVertex3d(p.getX() + halfw * s.h.getX(), 0, p.getZ() + halfw * s.h.getZ());
					gl.glTexCoord2d(1.0f, 0.0f);
					gl.glVertex3d(p.getX() - halfw * s.h.getX(), 0, p.getZ() - halfw * s.h.getZ());
					gl.glEnd();
				}
			gl.glDisable(GL.GL_TEXTURE_2D);

			/*
			 * gl.glColor3d (.3,.3,.7); for ( i=end_idx+JUMP_CONE ; i>=start_idx
			 * ; i-- ) if (i % JUMP_CONE == 0) { drawCone (gl, location (i,
			 * 1.85)); drawCone (gl, location (i, 3.15)); } gl.glColor3d (1, 1,
			 * 1);
			 */
		}

		// Overhead signs

		// if (lanes == 4)
		// {
		// gl.glEnable (GL2.GL_TEXTURE_2D);
		// for ( i=end_idx+JUMP_ROAD ; (i>=start_idx) && (i>=JUMP_ROAD) ; i-- )
		// if (i % 1301 == 0)
		// {
		// Segment s = getSegment (i);
		// double w = 0.15;
		// double h = 7.0;
		//
		// gl.glBindTexture (GL2.GL_TEXTURE_2D, Textures.POLE);
		//
		// gl.glBegin (GL2.GL_POLYGON);
		// gl.glTexCoord2d (1.0f,1.0f);
		// gl.glVertex3d (s.ll_left.getX() - w * s.h.getX(), h+w,
		// s.ll_left.getZ() - w *
		// s.h.getZ());
		// gl.glTexCoord2d (0.0f,1.0f);
		// gl.glVertex3d (s.ll_left.getX() + w * s.h.getX(), h+w,
		// s.ll_left.getZ() + w *
		// s.h.getZ());
		// gl.glTexCoord2d (0.0f,0.0f);
		// gl.glVertex3d (s.ll_left.getX() + w * s.h.getX(), 0, s.ll_left.getZ()
		// + w * s.h.getZ());
		// gl.glTexCoord2d (1.0f,0.0f);
		// gl.glVertex3d (s.ll_left.getX() - w * s.h.getX(), 0, s.ll_left.getZ()
		// - w * s.h.getZ());
		// gl.glEnd ();
		//
		// gl.glBegin (GL2.GL_POLYGON);
		// gl.glTexCoord2d (1.0f,1.0f);
		// gl.glVertex3d (s.rr_right.getX() - w * s.h.getX(), h+w,
		// s.rr_right.getZ() - w *
		// s.h.getZ());
		// gl.glTexCoord2d (0.0f,1.0f);
		// gl.glVertex3d (s.rr_right.getX() + w * s.h.getX(), h+w,
		// s.rr_right.getZ() + w *
		// s.h.getZ());
		// gl.glTexCoord2d (0.0f,0.0f);
		// gl.glVertex3d (s.rr_right.getX() + w * s.h.getX(), 0,
		// s.rr_right.getZ() + w *
		// s.h.getZ());
		// gl.glTexCoord2d (1.0f,0.0f);
		// gl.glVertex3d (s.rr_right.getX() - w * s.h.getX(), 0,
		// s.rr_right.getZ() - w *
		// s.h.getZ());
		// gl.glEnd ();
		//
		// gl.glBegin (GL2.GL_POLYGON);
		// gl.glTexCoord2d (1.0f,1.0f);
		// gl.glVertex3d (s.ll_left.getX() + w * s.h.getX(), h-w,
		// s.ll_left.getZ() + w *
		// s.h.getZ());
		// gl.glTexCoord2d (0.0f,1.0f);
		// gl.glVertex3d (s.ll_left.getX() + w * s.h.getX(), h+w,
		// s.ll_left.getZ() + w *
		// s.h.getZ());
		// gl.glTexCoord2d (0.0f,0.0f);
		// gl.glVertex3d (s.rr_right.getX() - w * s.h.getX(), h+w,
		// s.rr_right.getZ() - w *
		// s.h.getZ());
		// gl.glTexCoord2d (1.0f,0.0f);
		// gl.glVertex3d (s.rr_right.getX() - w * s.h.getX(), h-w,
		// s.rr_right.getZ() - w *
		// s.h.getZ());
		// gl.glEnd ();
		//
		// gl.glBegin (GL2.GL_POLYGON);
		// gl.glTexCoord2d (1.0f,1.0f);
		// gl.glVertex3d (s.ll_left.getX() + w * s.h.getX(), h-w-2,
		// s.ll_left.getZ() + w *
		// s.h.getZ());
		// gl.glTexCoord2d (0.0f,1.0f);
		// gl.glVertex3d (s.ll_left.getX() + w * s.h.getX(), h+w-2,
		// s.ll_left.getZ() + w *
		// s.h.getZ());
		// gl.glTexCoord2d (0.0f,0.0f);
		// gl.glVertex3d (s.rr_right.getX() - w * s.h.getX(), h+w-2,
		// s.rr_right.getZ() - w *
		// s.h.getZ());
		// gl.glTexCoord2d (1.0f,0.0f);
		// gl.glVertex3d (s.rr_right.getX() - w * s.h.getX(), h-w-2,
		// s.rr_right.getZ() - w *
		// s.h.getZ());
		// gl.glEnd ();
		//
		// gl.glBindTexture (GL2.GL_TEXTURE_2D, Textures.SIGN_OVERHEAD);
		//
		// gl.glBegin (GL2.GL_POLYGON);
		// gl.glTexCoord2d (0.0f,1.0f);
		// gl.glVertex3d (s.middle.getX() + w * s.h.getZ(), h+.5,
		// s.middle.getZ() - w * s.h.getX());
		// gl.glTexCoord2d (1.0f,1.0f);
		// gl.glVertex3d (s.left.getX() + w * s.h.getZ(), h+.5, s.left.getZ() -
		// w * s.h.getX());
		// gl.glTexCoord2d (1.0f,0.0f);
		// gl.glVertex3d (s.left.getX() + w * s.h.getZ(), h-2.5, s.left.getZ() -
		// w * s.h.getX());
		// gl.glTexCoord2d (0.0f,0.0f);
		// gl.glVertex3d (s.middle.getX() + w * s.h.getZ(), h-2.5,
		// s.middle.getZ() - w *
		// s.h.getX());
		// gl.glEnd ();
		// }
		// gl.glDisable (GL2.GL_TEXTURE_2D);
		// }
	}
}
