package NoDragSource;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JPanel;

public class GlassPane extends JPanel {
	
	private Rectangle lineRect;

	public GlassPane() {
		super();
		lineRect = new Rectangle(0, 0, 0, 0);
		setOpaque(false);
	}

	public void setLineRect(int x, int y, int width, int height) {
		lineRect.setRect(x, y, width, height);
	}

	public void paintComponent(Graphics graphics) {
		graphics.setColor(Color.BLUE);
		graphics.fillRect(lineRect.x, lineRect.y, lineRect.width, lineRect.height);
	}

}
