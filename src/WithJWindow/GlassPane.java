package WithJWindow;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class GlassPane extends JPanel {
	
	private Rectangle lineRect;
	private Point mousePointer;
	private BufferedImage tabImage;

	public GlassPane() {
		super();
		lineRect = new Rectangle(0, 0, 0, 0);
		setOpaque(false);
	}

	public void setLineRect(int x, int y, int width, int height) {
		lineRect.setRect(x, y, width, height);
	}

	public void setImage(BufferedImage tabImage) {
		this.tabImage = tabImage;
	}

	public void setPoint(Point mousePointer) {
		this.mousePointer = mousePointer;
	}

	public void paintComponent(Graphics graphics) {
		
       if((tabImage != null) && (mousePointer != null)) 
       {
	        // draw mouse pointer as being in the middle of the ghost tab 
	        graphics.drawImage(tabImage, 
	          (mousePointer.x - (tabImage.getWidth()/2)), 
	          (mousePointer.y - (tabImage.getHeight()/2)), null);
       }
	      
		graphics.setColor(Color.BLUE);
		graphics.fillRect(lineRect.x, lineRect.y, lineRect.width,
				lineRect.height);
	}
}
