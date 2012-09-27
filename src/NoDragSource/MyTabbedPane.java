package NoDragSource;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

// Some parts of Drag and Drop JTabbedPane were derived from the following blogpost  
// by TERAI Atsuhiro posted April 7, 2008 and released as public domain:
// http://java-swing-tips.blogspot.com/2008/04/drag-and-drop-tabs-in-jtabbedpane.html		

public class MyTabbedPane extends JTabbedPane implements MouseListener, MouseMotionListener {

	public static void main(String[] args) {
		JTabbedPane tabbedPane = new MyTabbedPane();

		tabbedPane.addTab("Tab 1", new JPanel());
		tabbedPane.addTab("Tab 2", new JPanel());
		tabbedPane.addTab("Tab 3", new JPanel());
		tabbedPane.addTab("Tab 4", new JPanel());

		JFrame frame = new JFrame("~ Detachable Tabs ~");

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(tabbedPane);
		frame.setSize(300, 100);
		frame.setVisible(true);
	}

	private GlassPane glassPane;
	private JWindow window;
	private JLabel lbl;
	private ImageIcon icon;
	
	private int dragTabIndex = -1;
		
	public MyTabbedPane(){
		super();

		glassPane = new GlassPane();
		window = new JWindow();
		icon = new ImageIcon();
		
		lbl = new JLabel();
		lbl.setIcon(icon);
		
		Container container = window.getContentPane();
		container.add(lbl);
				
		addMouseMotionListener(this);
		addMouseListener(this);
	}
	
	// {{{ MouseListener	
	// Invoked when the mouse button has been clicked (pressed and released) on a component.
	public void mouseClicked(MouseEvent e){ }
            
	// Invoked when the mouse enters a component.
	public void mouseEntered(MouseEvent e){ }
        
	// Invoked when the mouse exits a component.
	public void mouseExited(MouseEvent e){ }
            
	// Invoked when a mouse button has been pressed on a component.      
	public void mousePressed(MouseEvent e){ }
            
	// Invoked when a mouse button has been released on a component.      
	public void mouseReleased(MouseEvent e){

		// if index for dragged tab was found
		if ((dragTabIndex >= 0)
				// and the index for where the tab is to be dropped was found
				&& (targetTabIndex >= 0)
				// and the user isn't dropping the tab in area already occupied by tab
				&& (dragTabIndex != targetTabIndex)) {

			// store backup of tab's information before removing it
			String title = getTitleAt(dragTabIndex);
			Icon icon = getIconAt(dragTabIndex);
			Component component = getComponentAt(dragTabIndex);
			String tip = getToolTipTextAt(dragTabIndex);
			boolean enabled = isEnabledAt(dragTabIndex);
			Component tabComponent = getTabComponentAt(dragTabIndex);

			// if dragged tab has a greater index the dropped area, use the dropped areas index
			// that is, use is dragging the tab to the left
			// otherwise user is dragging tab to the right
			int index = dragTabIndex > targetTabIndex ? targetTabIndex : targetTabIndex - 1;
			
			remove(dragTabIndex);
			insertTab(title, icon, component, tip, index);
			setEnabledAt(index, enabled);

			// When you drag'n'drop a disabled tab, it finishes enabled and selected.
			// pointed out by dlorde
			if (enabled) {
				setSelectedIndex(index);
			}

			// I have a component in all tabs (jlabel with an X to close the tab)
			// and when i move a tab the component disappear.
			// pointed out by Daniel Dario Morales Salas
			setTabComponentAt(index, tabComponent);
		}
		
		// keep
		dragTabIndex = -1;
		window.dispose();
		glassPane.setVisible(false);				
	}
    // }}}  
	
	private int targetTabIndex;
	
	// {{{ MouseMotionListener	
	// Invoked when a mouse button is pressed on a component and then dragged.
	public void mouseDragged(MouseEvent e){ 
		if(dragTabIndex == -1){
			Point dragOrigin = e.getPoint();
			dragTabIndex = indexAtLocation(dragOrigin.x, dragOrigin.y);
			
			Rectangle tabBounds = getBoundsAt(dragTabIndex);
			Point point = new Point(tabBounds.x, tabBounds.y);
			SwingUtilities.convertPointToScreen(point, this);

			// mimic screen capture by painting onto buffered image
			// and then take a clip of specific area
			BufferedImage bufferedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
			paint(bufferedImage.getGraphics());
			bufferedImage = bufferedImage.getSubimage(tabBounds.x + 1, tabBounds.y, tabBounds.width, tabBounds.height);

			icon.setImage(bufferedImage);
			lbl.setSize(tabBounds.width, tabBounds.height);

			window.setSize(tabBounds.width, tabBounds.height);
			window.setLocation(point.x, point.y);
			window.setVisible(true);
			
			getRootPane().setGlassPane(glassPane);
			glassPane.setVisible(true);
		}
		else{
			Point point = e.getLocationOnScreen();
			Rectangle tabBounds = getBoundsAt(dragTabIndex);
			window.setLocation(point.x - (tabBounds.width / 2), point.y - (tabBounds.height / 2));
			
			point = e.getPoint();
			targetTabIndex = getTargetTabIndex(point);
			
			// If tab was dropped in area immediately to its left,
			if((dragTabIndex == targetTabIndex)
					// or immediately to its right,
					|| ((targetTabIndex - dragTabIndex) == 1)
					// or outside the acceptable drop region
					|| (targetTabIndex < 0)) { 					
				
				// Don't draw any drop indicators.
				glassPane.setLineRect(0, 0, 0, 0);
			
				// Change cursor icon to indicate invalid drop
//				this.setCursor(DragSource.DefaultMoveNoDrop);
//				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
			}
			else{
				
				if (targetTabIndex == 0) {
					// Tab was dragged to the front, before other tabs.
					tabBounds = getBoundsAt(0);
					glassPane.setLineRect(tabBounds.x, tabBounds.y, 3, tabBounds.height);
				} else {
					tabBounds = getBoundsAt(targetTabIndex - 1);
					glassPane.setLineRect(tabBounds.x + tabBounds.width, tabBounds.y, 3, tabBounds.height);
				}
			
				// Change cursor icon to indicate valid drop
//				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);				
			}
			
			glassPane.repaint();
		}
	}
	
	// Invoked when the mouse cursor has been moved onto a component but no buttons have been pushed.	
	public void mouseMoved(MouseEvent e){ }    
	// }}}
	
	// {{{ int getTargetTabIndex(Point point)
	private int getTargetTabIndex(Point point) {
		int index = indexAtLocation(point.x, point.y);
				
		// Check if tab was dragged to end of tabs
		if (index == -1) {
			Rectangle tabBounds = getBoundsAt(getTabCount() - 1);			
			tabBounds.x = tabBounds.x + (tabBounds.width / 2);
			// Note: We want to check the region between two tabs.

			if (tabBounds.contains(point)) {
				index = getTabCount();
			}
		}

		return index;
	}
	// }}}
	
}
