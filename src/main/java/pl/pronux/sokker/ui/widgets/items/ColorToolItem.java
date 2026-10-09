package pl.pronux.sokker.ui.widgets.items;


import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import pl.pronux.sokker.ui.resources.ImageResources;

public class ColorToolItem extends ToolItem {

	private Color color;

	public ColorToolItem(ToolBar parent, int style) {
		super(parent, style);
	}

	@Override
	protected void checkSubclass() {
		// super.checkSubclass();
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
		this.setImage(ImageResources.swatch(color, this.getWidth() - 10, this.getWidth() - 10));
	}

}
