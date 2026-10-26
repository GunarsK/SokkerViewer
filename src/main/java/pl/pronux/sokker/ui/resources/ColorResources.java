/*******************************************************************************
 * Copyright (c) 2004, 2006 Plum Canary Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Plum Canary Corporation - initial API and implementation
 * 
 * $Id$
 * $HeadURL$
 *******************************************************************************/
package pl.pronux.sokker.ui.resources;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;


/**
 * A color registry with ability to cache requested colors. The colors acquired
 * from this class are disposed on release of the default {@link Display} instance.
 * 
 * @author Alexey Afanasyev
 * @author Alexey Kharlamov
 */
public final class ColorResources {

	private static Display display = Display.getDefault();

	private static Map<Integer, Color> mapIdToColor = new HashMap<Integer, Color>();

	private static Map<RGB, Color> mapRgbToColor = new HashMap<RGB, Color>();

	static {
		display.addListener(SWT.Dispose, new Listener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {
				disposeColors(mapIdToColor.values());
				disposeColors(mapRgbToColor.values());
			}
		});
	}

	/**
	 * Returns the default color black.
	 * 
	 * @return the default color black
	 */
	public static Color getBlack() {
		return getSystemColor(SWT.COLOR_BLACK);
	}
	
	/**
	 * Returns a cached color for the given <code>red</code>,
	 * <code>green</code> and <code>blue</code> values.
	 * 
	 * @param red
	 *            the red component
	 * @param green
	 *            the green component
	 * @param blue
	 *            the blue component
	 * @return the desired color
	 */
	public static Color getColor(int red, int green, int blue) {
		return getColor(new RGB(red, green, blue));
	}

	/**
	 * Returns a cached color for the given <code>RGB</code> describing the
	 * desired red, green and blue values.
	 * 
	 * @param rgb
	 *            the RGB values of the desired color
	 * @return the desired color
	 */
	public static Color getColor(RGB rgb) {
		Color color = mapRgbToColor.get(rgb);
		if (color == null || color.isDisposed()) {
			color = new Color(display, rgb);
			mapRgbToColor.put(rgb, color);
		}
		return color;
	}

	/**
	 * Returns the system color used to paint list background areas.
	 * 
	 * @return the desired color
	 */
	public static Color getListBackground() {
		return getSystemColor(SWT.COLOR_LIST_BACKGROUND);
	}

	/**
	 * Returns a cached system color for the given <code>id</code>.
	 * 
	 * @param colorId
	 *            the color id
	 * @return the desired color
	 */
	public static Color getSystemColor(int colorId) {
		Integer id = colorId;
		Color color = mapIdToColor.get(id);
		if (color == null || color.isDisposed()) {
			color = display.getSystemColor(colorId);
			mapIdToColor.put(id, color);
		}
		return color;
	}

	/**
	 * Returns the default color white.
	 * 
	 * @return the default color white
	 */
	public static Color getWhite() {
		return getSystemColor(SWT.COLOR_WHITE);
	}

	/**
	 * Returns the system color used to paint background areas.
	 * 
	 * @return the desired color
	 */
	public static Color getWidgetBackground() {
		return getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
	}

	private static void disposeColors(Collection<Color> colors) {
		for (Color color : colors) {
			if (color != null && !color.isDisposed()) {
				color.dispose();
			}
		}
	}
	
	/**
	 * Private constructor to block instantiation.
	 */
	private ColorResources() {
	}

	public static String rgb2hex(Color color) {
		return ColorResources.rgb2hex(color.getRGB());
	}

	public static String rgb2hex(RGB rgb) {
		return String.format("#%02X%02X%02X", rgb.red, rgb.green, rgb.blue);
	}
}
