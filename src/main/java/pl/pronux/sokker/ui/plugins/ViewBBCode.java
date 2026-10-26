package pl.pronux.sokker.ui.plugins;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TreeItem;

import pl.pronux.sokker.bean.SvBean;
import pl.pronux.sokker.model.SokkerViewerSettings;
import pl.pronux.sokker.resources.Messages;
import pl.pronux.sokker.ui.beans.Colors;
import pl.pronux.sokker.ui.interfaces.IPlugin;
import pl.pronux.sokker.ui.interfaces.IViewConfigure;
import pl.pronux.sokker.ui.resources.ColorResources;
import pl.pronux.sokker.ui.resources.ImageResources;
import pl.pronux.sokker.ui.resources.Theme;
import pl.pronux.sokker.ui.widgets.items.ColorToolItem;

public class ViewBBCode implements IPlugin {

	private static final int BOLD = 1;

	private static final int ITALIC = 2;

	private static final int UNDERLINE = 3;

	private static final int COLOR = 4;

	/** The post being written, kept when the main window is rebuilt */
	private static String draft = "";

	private static StyleRange[] draftStyles = new StyleRange[0];

	private Composite composite;

	private TreeItem treeItem;

	private StyledText styledText;

	private ColorToolItem colorItem;

	private StyledText formattedText;

	public void clear() {
	}

	public Composite getComposite() {
		return composite;
	}

	public IViewConfigure getConfigureComposite() {
		return null;
	}

	public String getInfo() {
		return this.getClass().getSimpleName();
	}

	public String getStatusInfo() {
		return Messages.getString("progressBar.info.setInfoBBCode");
	}

	public TreeItem getTreeItem() {
		return treeItem;
	}

	public void init(Composite composite) {
		this.composite = composite;
		composite.setLayout(new FormLayout());
		initComposite(composite);
		composite.layout();
	}

	private void initComposite(Composite composite2) {
		ToolItem item;

		FormData formData = new FormData();
		formData.left = new FormAttachment(0, 5);
		formData.right = new FormAttachment(100, 0);
		formData.top = new FormAttachment(0, 5);
		formData.height = 20;

		final ToolBar toolBar = new ToolBar(composite2, SWT.HORIZONTAL | SWT.FLAT);
		toolBar.setLayoutData(formData);
		// the B, I and U images are black, so in the dark theme the toolbar takes the editors' paper
		if (Theme.isDark()) {
			toolBar.setBackground(Colors.getForumPaper());
		}

		item = new ToolItem(toolBar, SWT.PUSH);
		item.setImage(ImageResources.getImageResources("bold_mini.png"));
		item.setWidth(30);

		item.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event arg0) {
				checkRanges(styledText, styledText.getSelectionRange().x, styledText.getSelectionCount(), BOLD);
				text2BBCode(styledText);
			}

		});

		new ToolItem(toolBar, SWT.SEPARATOR);
		item = new ToolItem(toolBar, SWT.PUSH);
		item.setImage(ImageResources.getImageResources("italic_mini.png"));
		item.setWidth(20);

		item.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event arg0) {
				checkRanges(styledText, styledText.getSelectionRange().x, styledText.getSelectionCount(), ITALIC);
				text2BBCode(styledText);
			}

		});
		new ToolItem(toolBar, SWT.SEPARATOR);
		item = new ToolItem(toolBar, SWT.PUSH);
		item.setImage(ImageResources.getImageResources("underline_mini.png"));
		item.setWidth(20);

		item.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event arg0) {
				checkRanges(styledText, styledText.getSelectionRange().x, styledText.getSelectionCount(), UNDERLINE);
				text2BBCode(styledText);

			}

		});

		new ToolItem(toolBar, SWT.SEPARATOR);
		colorItem = new ColorToolItem(toolBar, SWT.DROP_DOWN);
		colorItem.setWidth(20);
		colorItem.setColor(ColorResources.getBlack());
		colorItem.setData("color", ColorResources.getBlack());

		final Menu menu = new Menu(composite2.getShell(), SWT.POP_UP);
		MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText(Messages.getString("bbcode.color.change"));
		menuItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				ColorDialog dialog = new ColorDialog(composite.getShell(), SWT.NONE);
				dialog.setRGB(((Color) colorItem.getData("color")).getRGB());
				RGB color = dialog.open();
				if (color != null) {
					Color newColor = ColorResources.getColor(color);
					colorItem.setColor(newColor);
					colorItem.setData("color", newColor);
				}
			}
		});

		colorItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				ToolItem item = (ToolItem) event.widget;
				if (event.detail == SWT.ARROW) {
					Rectangle rect = item.getBounds();
					Point pt = new Point(rect.x, rect.y + rect.height);
					pt = toolBar.toDisplay(pt);
					menu.setLocation(pt.x + 10, pt.y - 10);
					menu.setVisible(true);
				} else {
					checkRanges(styledText, styledText.getSelectionRange().x, styledText.getSelectionCount(), (Color) colorItem.getData("color"));
					text2BBCode(styledText);
				}
			}
		});

		formData = new FormData();
		formData.left = new FormAttachment(0, 0);
		formData.right = new FormAttachment(100, 0);
		formData.top = new FormAttachment(toolBar, 5);
		formData.bottom = new FormAttachment(50, 5);

		styledText = new StyledText(composite2, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		styledText.setLayoutData(formData);
		// forum paper in every theme; black text keeps [color] out of the output
		styledText.setBackground(Colors.getForumPaper());
		styledText.setForeground(ColorResources.getBlack());

		styledText.addListener(SWT.Modify, new Listener() {

			public void handleEvent(Event arg0) {
				text2BBCode(styledText);
			}

		});

		formData = new FormData();
		formData.left = new FormAttachment(0, 0);
		formData.right = new FormAttachment(100, 0);
		formData.top = new FormAttachment(styledText, 5);
		formData.bottom = new FormAttachment(100, 0);

		formattedText = new StyledText(composite2, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL);
		formattedText.setLayoutData(formData);
		formattedText.setBackground(Colors.getForumPaper());
		formattedText.setForeground(ColorResources.getBlack());

		styledText.setText(draft);
		styledText.setStyleRanges(draftStyles);
		text2BBCode(styledText);
		styledText.addListener(SWT.Dispose, new Listener() {

			public void handleEvent(Event event) {
				draft = styledText.getText();
				draftStyles = styledText.getStyleRanges();
			}
		});
	}

	public void setSettings(SokkerViewerSettings sokkerViewerSettings) {
	}

	public void setTreeItem(TreeItem treeItem) {
		this.treeItem = treeItem;
		this.treeItem.setText(Messages.getString("tree.ViewBBCode"));
		this.treeItem.setImage(ImageResources.getImageResources("bbcode.png"));
	}

	public void set() {
	}

	public void dispose() {
	}

	private void checkRanges(StyledText styledText, int start, int length, Color foreground) {
		checkRanges(styledText, start, length, COLOR, foreground);
	}

	private void checkRanges(StyledText styledText, int start, int length, int style) {
		checkRanges(styledText, start, length, style, null);
	}

	private void checkRanges(StyledText styledText, int start, int length, int style, Color foreground) {

		StyleRange[] ranges = styledText.getStyleRanges(start, length);

		if (style == BOLD) {
			StyleRange styleRange = new StyleRange(start, length, styledText.getForeground(), styledText.getBackground());
			styleRange.fontStyle = SWT.BOLD;
			styledText.setStyleRange(styleRange);

		} else if (style == ITALIC) {
			StyleRange styleRange = new StyleRange(start, length, styledText.getForeground(), styledText.getBackground());
			styleRange.fontStyle = SWT.ITALIC;
			styledText.setStyleRange(styleRange);

		} else if (style == UNDERLINE) {
			StyleRange styleRange = new StyleRange(start, length, styledText.getForeground(), styledText.getBackground());
			styleRange.underline = true;
			styledText.setStyleRange(styleRange);
		} else if (style == COLOR) {
			StyleRange styleRange = new StyleRange(start, length, foreground, styledText.getBackground());
			styledText.setStyleRange(styleRange);
		}

		for (int i = 0; i < ranges.length; i++) {

			if (ranges[i].length < 0 || ranges[i].start < 0) {
				continue;
			}
			StyleRange range = new StyleRange(ranges[i].start, ranges[i].length, ranges[i].foreground, ranges[i].background);

			if (ranges[i].fontStyle == SWT.NORMAL) {

				range.fontStyle = ranges[i].fontStyle;

				if (style == BOLD) {
					range.fontStyle = SWT.BOLD;
				} else if (style == ITALIC) {
					range.fontStyle = SWT.ITALIC;
				}
				range.foreground = ranges[i].foreground;
				range.underline = ranges[i].underline;

			} else if (ranges[i].fontStyle == SWT.BOLD) {

				range.fontStyle = ranges[i].fontStyle;

				if (style == ITALIC) {
					range.fontStyle = (SWT.ITALIC | SWT.BOLD);
				} else if (style == BOLD) {
					range.fontStyle = SWT.NORMAL;
				}

				range.foreground = ranges[i].foreground;
				range.underline = ranges[i].underline;

			} else if (ranges[i].fontStyle == (SWT.BOLD | SWT.ITALIC)) {

				range.fontStyle = ranges[i].fontStyle;

				if (style == BOLD) {
					range.fontStyle = SWT.ITALIC;
				} else if (style == ITALIC) {
					range.fontStyle = SWT.BOLD;
				}
				range.foreground = ranges[i].foreground;
				range.underline = ranges[i].underline;

			} else if (ranges[i].fontStyle == SWT.ITALIC) {

				range.fontStyle = ranges[i].fontStyle;

				if (style == BOLD) {
					range.fontStyle = (SWT.ITALIC | SWT.BOLD);
				} else if (style == ITALIC) {
					range.fontStyle = SWT.NORMAL;
				}

				range.foreground = ranges[i].foreground;
				range.underline = ranges[i].underline;

			}

			if (style == UNDERLINE) {
				if (ranges[i].underline) {
					range.underline = false;
				} else {
					range.underline = true;
				}
			}

			if (style == COLOR) {
				range.foreground = foreground;
			}

			styledText.setStyleRange(range);
		}
	}

	private void text2BBCode(StyledText styleText) {
		String text = "";
		int start = 0;
		formattedText.setText("");

		StyleRange[] ranges = styleText.getStyleRanges();

		for (StyleRange range : ranges) {
			text += styleText.getTextRange(start, range.start - start);
			start = range.start + range.length;

			boolean bold = (range.fontStyle & SWT.BOLD) != 0;
			boolean italic = (range.fontStyle & SWT.ITALIC) != 0;
			boolean colored = !range.foreground.equals(ColorResources.getBlack());
			text += (bold ? "[b]" : "") + (italic ? "[i]" : "") + (range.underline ? "[u]" : "");
			if (colored) {
				text += "[color=" + ColorResources.rgb2hex(range.foreground) + "]";
			}
			text += styleText.getTextRange(range.start, range.length);
			if (colored) {
				text += "[/color]";
			}
			text += (range.underline ? "[/u]" : "") + (italic ? "[/i]" : "") + (bold ? "[/b]" : "");
		}
		text += styleText.getTextRange(start, styleText.getText().length() - start);

		// problems with meta data '?'
		text = text.replaceAll("\\?", "¿");

		Pattern pattern;
		Matcher matcher;

		pattern = Pattern.compile("[A-Za-z0-9.-]+\\@([A-Za-z0-9]+[.])+[A-Za-z0-9]{2,3}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

		matcher = pattern.matcher(text);
		if (matcher.find()) {
			for (int i = 0; i < matcher.groupCount(); i++) {
				text = text.replaceAll(matcher.group(i), "[email]" + matcher.group(i) + "[/email]");
			}
		}
		// *(/[a-z]+[.][a-z])*
		// (/[a-z]+[.][a-z]+)*
		pattern = Pattern.compile("http://([a-z]+[.])+[a-z]+(/[a-zA-Z_.0-9=¿-]+)*", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
		matcher = pattern.matcher(text);
		int j = 0;
		while (matcher.find(j)) {
			text = text.replaceAll(matcher.group(), "[url]" + matcher.group().replaceAll("/", "&#47;") + "[/url]");
			j++;
		}

		text = text.replaceAll("%", "&#37;");
		text = text.replaceAll("<", "&lt;");
		text = text.replaceAll(">", "&gt;");
		text = text.replaceAll("¿", "?");
		text = text.replaceAll("\\\\", "&#92;");
		text = text.replaceAll("(?<!\\[)/", "&#47;");

		formattedText.setText(text);
	}

	public void setSvBean(SvBean svBean) {
	}

	public void reload() {
	}
}
