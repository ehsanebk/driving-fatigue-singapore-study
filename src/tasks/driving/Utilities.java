package tasks.driving;

import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

/**
 * A utility class with various useful methods.
 * 
 * @author Dario Salvucci
 */
public class Utilities {
	private static Map<Integer, DecimalFormat> dfCache = new HashMap<Integer, DecimalFormat>();

	public static String format(double d, int places) {
		DecimalFormat df = dfCache.get(places);
		if (df == null) {
			df = new DecimalFormat("#." + StringUtils.repeat("0", places));
			dfCache.put(places, df);
		}
		return df.format(d);
	}

	public static Random random = new Random(System.currentTimeMillis());

	public static int sign(double x) {
		return (x >= 0) ? 1 : -1;
	}

	public static double square(double x) {
		return (x * x);
	}

	public static double rotationAngle(double hx, double hz) {
		return (-180 * (Math.atan2(hz, hx)) / Math.PI);
	}

	public static double deg2rad(double x) {
		return x * (Math.PI / 180.0);
	}

	public static double rad2deg(double x) {
		return x * (180.0 / Math.PI);
	}

	public static double mps2mph(double x) {
		return x * 2.237;
	}

	public static double mph2mps(double x) {
		return x / 2.237;
	}

	public static double mph2kph(double x) {
		return x * 1.609;
	}

	public static double kph2mph(double x) {
		return x / 1.609;
	}

	public static int sec2ms(double x) {
		return (int) (Math.round(x * 1000));
	}

	public static String randomize(String s) {
		String s2 = "";
		while (!s.equals("")) {
			int r = random.nextInt(s.length());
			s2 += s.substring(r, r + 1);
			s = s.substring(0, r) + s.substring(r + 1, s.length());
		}
		return s2;
	}

	public static PrintStream uniqueOutputFile(String name) {
		int num = 1;
		File file;
		String filename;
		do {
			filename = name + num + ".txt";
			file = new File(filename);
			num++;
		} while (file.exists());
		PrintStream stream = null;
		try {
			stream = new PrintStream(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return stream;
	}

	public static void setFullScreen(Frame frame) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = ge.getDefaultScreenDevice();
		device.setFullScreenWindow(frame);
		frame.validate();
	}
}
