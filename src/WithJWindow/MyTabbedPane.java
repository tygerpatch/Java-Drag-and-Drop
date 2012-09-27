package WithJWindow;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.image.BufferedImage;
import java.util.TooManyListenersException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

//Drag and Drop tabs based on the following blogpost  
//by TERAI Atsuhiro posted April 7, 2008 and released as public domain:
//http://java-swing-tips.blogspot.com/2008/04/drag-and-drop-tabs-in-jtabbedpane.html		
public class MyTabbedPane extends JTabbedPane implements DragSourceMotionListener, DragGestureListener, DropTargetListener, DragSourceListener {

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

	private Transferable transferable;
	private GlassPane glassPane;
	private int dragTabIndex = -1;
	private JWindow window;
	private JLabel lbl;
	private ImageIcon icon;

	// {{{ MyTabbedPane()
	public MyTabbedPane() {
		super();

		window = new JWindow();
		
		transferable = new MyTransferable(this);
		glassPane = new GlassPane();

		icon = new ImageIcon();

		lbl = new JLabel();
		lbl.setIcon(icon);
		
		Container container = window.getContentPane();
		//container.setLayout(null);
		container.add(lbl);

		DragSource dragSource = new DragSource();

		// createDefaultDragGestureRecognizer(Component c, int actions, DragGestureListener dgl)
		dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
		dragSource.addDragSourceMotionListener(this);
		
		DropTarget dropTarget = new DropTarget();

		dropTarget.setComponent(window); // (this); // TODO: why does it work with this?
		dropTarget.setDefaultActions(DnDConstants.ACTION_MOVE);

		try {
			dropTarget.addDropTargetListener(this);
		} catch (TooManyListenersException ex) { }
	}
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

	// {{{ Rectangle getTabAreaBounds()
	private Rectangle getTabAreaBounds() {
		Rectangle bounds = getBounds();
		Component selectedComponent = getSelectedComponent();
		Rectangle componentBounds = (selectedComponent == null ? new Rectangle() : selectedComponent.getBounds());

		// take height of tabbed pane and subtract height of panel in currectly selected tab
		bounds.height = bounds.height - componentBounds.height;
		return bounds;
	}
	// }}}

	// {{{ DragGestureListener interface
	public void dragGestureRecognized(DragGestureEvent dge) {
		
		// if there's more than one tab
		if (getTabCount() > 1) {

			Point dragOrigin = dge.getDragOrigin();
			dragTabIndex = indexAtLocation(dragOrigin.x, dragOrigin.y);

			// if index of tab was found and tab is enabled
			if ((dragTabIndex >= 0) && isEnabledAt(dragTabIndex)) {

				getRootPane().setGlassPane(glassPane);

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

				glassPane.setVisible(true);

				try {
					// startDrag(Cursor dragCursor, Transferable transferable, DragSourceListener dsl)
					dge.startDrag(DragSource.DefaultMoveDrop, transferable, this);
				} catch (InvalidDnDOperationException ex) {
					window.dispose();
					return;
				}
			}
		}
	}
	// }}}

	// {{{ DragSourceListener interface
	public void dragDropEnd(DragSourceDropEvent dsde) {
		// user has released tab
		dragTabIndex = -1;
		window.dispose();
		glassPane.setVisible(false);
	}

	public void dragEnter(DragSourceDragEvent dsde) {
		dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
	}

	public void dragExit(DragSourceEvent dse) {
		dse.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
		glassPane.setLineRect(0, 0, 0, 0);
		glassPane.repaint();
	}

	public void dragOver(DragSourceDragEvent dsde) {
		
		// Get location of cursor relative to the tabbed pane.		
		Point cursorLocation = dsde.getLocation();		
		SwingUtilities.convertPointFromScreen(cursorLocation, glassPane);
		
		// Get the index of where the tab is to be dropped.
		int targetTabIndex = getTargetTabIndex(cursorLocation);
				
		if((dragTabIndex == targetTabIndex)				// If tab was dropped in area immediately to its left,						
			|| ((targetTabIndex - dragTabIndex) == 1)	// or immediately to its right,						
			|| (targetTabIndex < 0)) 					// or outside the acceptable drop region
		{
			// Don't draw any drop indicators.
			glassPane.setLineRect(0, 0, 0, 0);
			
			// Change cursor icon to indicate invalid drop
			dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
		}
		else
		{			
			if (targetTabIndex == 0) {
				// Tab was dragged to the front, before other tabs.
				Rectangle tabBounds = getBoundsAt(0);
				glassPane.setLineRect(tabBounds.x, tabBounds.y, 3, tabBounds.height);
			} else {
				Rectangle tabBounds = getBoundsAt(targetTabIndex - 1);
				glassPane.setLineRect(tabBounds.x + tabBounds.width, tabBounds.y, 3, tabBounds.height);
			}
			
			// Change cursor icon to indicate valid drop
			dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
		}
				
		glassPane.repaint();
	}
		
	public void dropActionChanged(DragSourceDragEvent dragSourceDragEvent) { }
	// }}}

	// {{{ DropTargetListener interface
	public void dragEnter(DropTargetDragEvent dtde) {
		Transferable transferable = dtde.getTransferable();

		if (transferable != null) {
			if (transferable.isDataFlavorSupported(dtde.getCurrentDataFlavors()[0]) && dragTabIndex > -1) {
				dtde.acceptDrag(dtde.getDropAction());
				return;
			}
		}

		dtde.rejectDrag();
	}

	public void dragExit(DropTargetEvent dte) { }

	public void dragOver(DropTargetDragEvent dtde) { }
	
	public void drop(DropTargetDropEvent dtde) {
		Transferable transferable = dtde.getTransferable();
		boolean completedDrop = false;

		if (transferable != null) {
			if (transferable.isDataFlavorSupported(transferable.getTransferDataFlavors()[0]) && (dragTabIndex >= 0)) {

				Point point = dtde.getLocation();
				point = SwingUtilities.convertPoint(window, point, this);
				
				int targetTabIndex = getTargetTabIndex(point);

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

				completedDrop = true;
			}
		}

		// notifiy DragSource on status drop transfer
		dtde.dropComplete(completedDrop);
	}

	public void dropActionChanged(DropTargetDragEvent e) { }
	// }}}

	// {{{ DragSourceMotionListener interface
	public void dragMouseMoved(DragSourceDragEvent dsde) {
		Point point = dsde.getLocation();
		Rectangle tabBounds = getBoundsAt(dragTabIndex);
		window.setLocation(point.x - (tabBounds.width / 2), point.y - (tabBounds.height / 2));
	}
	// }}}
}
