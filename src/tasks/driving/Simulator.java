package tasks.driving;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JPanel;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES1;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.util.FPSAnimator;

/**
 * The visual simulator as an OpenGL-based JPanel.
 * 
 * @author Dario Salvucci
 */
public class Simulator extends JPanel implements GLEventListener {
	private Simulation simulation;
	private GLCanvas canvas;
	private FPSAnimator animator;
	// private Overlay overlay;

	Simulator() {
		super();
		simulation = null;
		// overlay = null;

		setOpaque(true);
		setBackground(Color.black);

		GLProfile profile = GLProfile.getDefault();
		GLCapabilities caps = new GLCapabilities(profile);
		caps.setDoubleBuffered(true);
		caps.setHardwareAccelerated(true);
		canvas = new GLCanvas(caps);
		canvas.addGLEventListener(this);
		setLayout(new BorderLayout());
		add(canvas, BorderLayout.CENTER);

		animator = new FPSAnimator(canvas, 30);
		animator.start();
	}

	private void setViewport(GL2 gl) {
		int x = 0;
		int y = 0;
		int w = 1 * getWidth();
		int h = 1 * getHeight();
		if ((1.0 * w) / Environment.CANVAS_WIDTH < (1.0 * h) / Environment.CANVAS_HEIGHT) {
			int h2 = (w * Environment.CANVAS_HEIGHT) / Environment.CANVAS_WIDTH;
			y = (h - h2) / 2;
			h = h2;
		} else {
			int w2 = (h * Environment.CANVAS_WIDTH) / Environment.CANVAS_HEIGHT;
			x = (w - w2) / 2;
			w = w2;
		}
		gl.glLoadIdentity();
		gl.glViewport(x, y, w, h);
	}

	private static boolean texturesInitialized = false;

	@Override
	public void init(GLAutoDrawable drawable) {
		final GL2 gl = drawable.getGL().getGL2();
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthMask(true);
		gl.glEnable(GL.GL_LINE_SMOOTH);
		gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST); // GL2.GL_DONT_CARE);
		gl.glEnable(GL2ES1.GL_POINT_SMOOTH);
		gl.glHint(GL2ES1.GL_POINT_SMOOTH_HINT, GL.GL_NICEST); // GL2.GL_DONT_CARE);
		gl.glEnable(GL2GL3.GL_POLYGON_SMOOTH);
		gl.glHint(GL2GL3.GL_POLYGON_SMOOTH_HINT, GL.GL_NICEST);
		gl.glHint(GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST); // GL2.GL_DONT_CARE);
		gl.glEnable(GL2ES1.GL_ALPHA_TEST);
		gl.glAlphaFunc(GL.GL_GREATER, .1f);
		gl.glShadeModel(GLLightingFunc.GL_FLAT); // ??
		gl.glEnable(GLLightingFunc.GL_COLOR_MATERIAL);
		setViewport(gl);

		// overlay = new Overlay(drawable);

		if (!texturesInitialized) {
			Textures.init(gl);
			texturesInitialized = true;
		}
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		try {
			final GL2 gl = drawable.getGL().getGL2();
			gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
			if (simulation != null)
				simulation.draw(gl);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void useSimulation(Simulation simArg) {
		simulation = simArg;
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		final GL2 gl = drawable.getGL().getGL2();
		setViewport(gl);
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
	}

	void stop() {
		animator.stop();
	}
}
