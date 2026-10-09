package pl.pronux.sokker.ui.resources;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import pl.pronux.sokker.ui.handlers.DisplayHandler;

public final class ImageResources {

	private static final String IMAGE_PATH = "/images/"; 

	private static Map<String, Image> cache = new HashMap<String, Image>();

	static {
		DisplayHandler.getDisplay().disposeExec(new Runnable() {
			public void run() {
				if (!cache.isEmpty()) {
					for (Image image : cache.values()) {
						if(image != null) {
							if(!image.isDisposed()) {
								image.dispose();
							}
						}
					}
				}
			}
		});
	}
	
	private ImageResources() {
	}

	public static Image getImageResources(String filename) {
		Image image = cache.get(filename);
		if(image == null || image.isDisposed()) {
			image = loadImage(filename);
			cache.put(filename, image);
		}
		return image;
	}
	
	/** A colour swatch with a black border, cached until the display closes */
	public static Image swatch(Color color, int width, int height) {
		String key = "swatch " + color.getRGB() + " " + width + "x" + height;
		Image image = cache.get(key);
		if (image == null || image.isDisposed()) {
			Display display = DisplayHandler.getDisplay();
			image = new Image(display, width, height);
			GC gc = new GC(image);
			gc.setBackground(color);
			gc.fillRectangle(image.getBounds());
			gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
			gc.setLineWidth(2);
			gc.drawRectangle(image.getBounds());
			gc.dispose();
			cache.put(key, image);
		}
		return image;
	}

	public static Image getImageFile(String file) {
		Image image = cache.get(file);
		if(image == null || image.isDisposed()) {
			image = loadImageFile(file);
			cache.put(file, image);
		}
		return image;
		
	}
	
	private static Image loadImageFile(String file) {
		Image image = null;
		if(file!=null) {
			if(new File(file).exists()) {
				image = new Image(DisplayHandler.getDisplay(), file);	
			}
		}
		return image;
	}

	private static Image loadImage(String filename) {
		Image image = null;
		InputStream is = ImageResources.class.getResourceAsStream(IMAGE_PATH + filename);
		if (is != null) {
			image = new Image(DisplayHandler.getDisplay(), is);
		}
		return image;
	}

}
