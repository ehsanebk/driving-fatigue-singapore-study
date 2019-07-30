package tasks.driving;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.nio.ByteBuffer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

/**
 * A class that assists in loading and managing textures.
 * 
 * @author Dario Salvucci
 */
class Textures {
	private static GL2 textureGL = null;
	private static BufferedImage interfaceImage = null;

	static int priusDashboard;
	static int priusWheel;
	static int priusFront;
	static int priusBack;
	static int priusBackLit;
	static int priusSide;
	static int maximaDashboard;
	static int maximaWheel;
	static int maximaBack;
	static int maximaBackLit;
	static int maximaBackCrash;
	static int maximaNeedle;
	static int handSteering;
	static int handPointing;
	static int road;
	static int background;
	static int grass;
	static int[] trees;
	static int cone;
	static int interfaceScreen;

	static void init(GL2 gl) {
		textureGL = gl;

		priusDashboard = loadTexture(gl, "prius-dashboard.png");
		priusWheel = loadTexture(gl, "prius-wheel.png");
		priusFront = loadTexture(gl, "prius-front.png");
		priusBack = loadTexture(gl, "prius-back.png");
		priusSide = loadTexture(gl, "prius-side.png");
		priusBackLit = loadTexture(gl, "prius-back-lit.png");
		maximaDashboard = loadTexture(gl, "maxima-dashboard.png");
		maximaWheel = loadTexture(gl, "maxima-wheel.png");
		maximaBack = loadTexture(gl, "maxima-back.png");
		maximaBackLit = loadTexture(gl, "maxima-back-lit.png");
		maximaBackCrash = loadTexture(gl, "maxima-back-crash.png");
		maximaNeedle = loadTexture(gl, "maxima-needle.png");
		handSteering = loadTexture(gl, "handsteer.png");
		handPointing = loadTexture(gl, "handpoint.png");
		road = loadTexture(gl, "road.jpg");
		background = loadTexture(gl, "background.png");
		grass = loadTexture(gl, "grass.jpg");
		trees = new int[] { loadTexture(gl, "tree1.png"), loadTexture(gl, "tree2.png"), loadTexture(gl, "tree3.png") };
		cone = loadTexture(gl, "cone.png");

		if (interfaceImage == null)
			interfaceScreen = loadTexture(gl, "phone.png");
		else
			setInterfaceTexture(interfaceImage);
	}

	static void setInterfaceTexture(BufferedImage image) {
		if (textureGL == null || image == null) {
			interfaceImage = image;
			return;
		}

		GL2 gl = textureGL;

		gl.glBindTexture(GL.GL_TEXTURE_2D, interfaceScreen);
		Texture texture = new Texture(image, false);

		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST); // GL2.GL_LINEAR);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST); // GL2.GL_LINEAR);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);

		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, texture.getWidth(), texture.getHeight(), 0, GL.GL_RGB,
				GL.GL_UNSIGNED_BYTE, texture.getPixels());
	}

	static int loadTexture(GL2 gl, String filename) {
		final int[] tmp = new int[1];
		gl.glGenTextures(1, tmp, 0);
		int textureIndex = tmp[0];

		gl.glBindTexture(GL.GL_TEXTURE_2D, textureIndex);

		Image image = DrivingModel.getImage(filename);// Ehsan check the Driving file

		// wait until image is fully loaded!
		while (image.getWidth(null) == -1 || image.getHeight(null) == -1)
			;

		Texture texture = new Texture(image, true);

		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST); // GL2.GL_LINEAR);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST); // GL2.GL_LINEAR);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);

		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, texture.getWidth(), texture.getHeight(), 0, GL.GL_RGBA,
				GL.GL_UNSIGNED_BYTE, texture.getPixels());
		// gl.glTexImage2D (GL2.GL_TEXTURE_2D, 0, GL2.GL_RGB,
		// texture.getWidth(),
		// texture.getHeight(), 0, GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE,
		// texture.getPixels());

		return textureIndex;
	}

	static class Texture {
		private ByteBuffer pixels;
		private int width;
		private int height;

		Texture(Image img, boolean storeAlpha) {
			int[] packedPixels = new int[img.getWidth(null) * img.getHeight(null)];

			PixelGrabber pixelgrabber = new PixelGrabber(img, 0, 0, img.getWidth(null), img.getHeight(null),
					packedPixels, 0, img.getWidth(null));
			try {
				pixelgrabber.grabPixels();
			} catch (InterruptedException e) {
				throw new RuntimeException();
			}

			int bytesPerPixel = storeAlpha ? 4 : 3;
			ByteBuffer unpackedPixels = Buffers.newDirectByteBuffer(packedPixels.length * bytesPerPixel);

			for (int row = img.getHeight(null) - 1; row >= 0; row--) {
				for (int col = 0; col < img.getWidth(null); col++) {
					int packedPixel = packedPixels[row * img.getWidth(null) + col];
					unpackedPixels.put((byte) ((packedPixel >> 16) & 0xFF));
					unpackedPixels.put((byte) ((packedPixel >> 8) & 0xFF));
					unpackedPixels.put((byte) ((packedPixel >> 0) & 0xFF));

					if (storeAlpha) {
						unpackedPixels.put((byte) ((packedPixel >> 24) & 0xFF));
					}
				}
			}

			unpackedPixels.flip();

			pixels = unpackedPixels;
			width = img.getWidth(null);
			height = img.getHeight(null);
		}

		int getWidth() {
			return width;
		}

		int getHeight() {
			return height;
		}

		ByteBuffer getPixels() {
			return pixels;
		}
	}
}
