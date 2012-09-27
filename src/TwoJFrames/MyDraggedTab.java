package TwoJFrames;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

// TODO: should a DraggedTab just be a model, or also a JWindow.

// Class to hold information about a dragged tab.
// A dragged tab is shown as a JWindow.
public class MyDraggedTab extends JWindow {

	private JTabbedPane fromPane;
	
	// Store where the dragged tab came from.
	public void setFromPane(JTabbedPane fromPane) {
		this.fromPane = fromPane;
	}
	
	
	
	
	
	
	
	
	private JWindow window;
	private JLabel label;
	private ImageIcon icon;

	public MyDraggedTab() {
		window = new JWindow();
		icon = new ImageIcon();

		label = new JLabel();
		label.setIcon(icon);

		Container container = window.getContentPane();
		container.add(label);
	}


	private int index = -1;


	// Store index of tab being dragged.
	public void setIndex(int index) {
		this.index = index;
	}

	// Retrieve the index of the tab being dragged.
	public int getIndex() {
		return index;
	}

	// Show visual representation of tab being dragged.
	public void show() {
		// Get dimensions of tab being dragged.
		Rectangle tabBounds = fromPane.getBoundsAt(index);

		// Resize label and window to tab's dimensions
		window.setSize(tabBounds.width, tabBounds.height);
		label.setSize(tabBounds.width, tabBounds.height);

		// Take a 'picture' of the tabbed pane.
		BufferedImage bufferedImage = new BufferedImage(fromPane.getWidth(), fromPane.getHeight(), BufferedImage.TYPE_INT_ARGB);
		fromPane.paint(bufferedImage.getGraphics());

		// Crop image.
		bufferedImage = bufferedImage.getSubimage(tabBounds.x + 1, tabBounds.y, tabBounds.width, tabBounds.height);

		// Give label image of tab to display.
		icon.setImage(bufferedImage);

		// Have mouse pointer appear in the middle of the tab.
		Point point = new Point(tabBounds.x, tabBounds.y);
		SwingUtilities.convertPointToScreen(point, fromPane);
		window.setLocation(point.x - (tabBounds.width / 2), point.y - (tabBounds.height / 2));

		// Show the tab being dragged.
		window.setVisible(true);
	}

	// Hide visual representation of tab being dragged.
	public void hide() {
		window.dispose();
	}

	
	

	
	public void moveTo(JTabbedPane toPane, int targetTabIndex) {		
		// If dragged tab has a greater index the dropped area, use the dropped areas index
		// that is, use is dragging the tab to the left
		// otherwise user is dragging tab to the right
		int dragTabIndex = this.index;
		int index = dragTabIndex > targetTabIndex ? targetTabIndex : targetTabIndex - 1; // TODO:
	
		fromPane.remove(index);
		
		toPane.insertTab(title, icon, component, tip, index);		
		toPane.setEnabledAt(index, enabled);

		if(enabled){
			toPane.setSelectedIndex(index);
		}

		toPane.setTabComponentAt(index, tabComponent);		
	}
}
