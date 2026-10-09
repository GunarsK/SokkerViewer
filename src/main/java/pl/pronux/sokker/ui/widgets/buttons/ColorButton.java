package pl.pronux.sokker.ui.widgets.buttons;


import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import pl.pronux.sokker.ui.resources.ImageResources;

public class ColorButton extends Button {

	private Color color;

	public ColorButton(Composite parent, int style) {
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
		Point size = new Point(0,0);
		if(this.getLayoutData() != null) {
			if(this.getLayoutData() instanceof FormData) {
				size.x = ((FormData)this.getLayoutData()).width;
				size.y = ((FormData)this.getLayoutData()).height;
			} else {
				size = getSize();
			}
		} else {
//			size = this.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			size = getSize();
		}

		this.setImage(ImageResources.swatch(color, size.x - 10, size.y - 10));
	}

}
