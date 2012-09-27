package WithoutJWindow;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
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
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.image.BufferedImage;
import java.util.TooManyListenersException;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

//Drag and Drop tabs based on the following blogpost  
//by TERAI Atsuhiro posted April 7, 2008 and released as public domain:
//http://java-swing-tips.blogspot.com/2008/04/drag-and-drop-tabs-in-jtabbedpane.html		
public class MyTabbedPane extends JTabbedPane implements DragGestureListener, DragSourceListener, DropTargetListener {

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

	private int dragTabIndex = -1;
	private Transferable transferable;
	private GlassPane glassPane;

	public MyTabbedPane() {
		super();

		transferable = new MyTransferable(this);
		glassPane = new GlassPane();

		DropTarget dropTarget = new DropTarget();

		dropTarget.setComponent(this);
		dropTarget.setDefaultActions(DnDConstants.ACTION_MOVE);

		try {
			dropTarget.addDropTargetListener(this);
		} catch (TooManyListenersException ex) { }

		dropTarget.setActive(true);

		DragSource dragSource = new DragSource();

		// createDefaultDragGestureRecognizer(Component c, int actions, DragGestureListener dgl)
		dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
	}

	// {{{ int getTargetTabIndex(Point point)
	private int getTargetTabIndex(Point point) {
		// Returns the tab index corresponding to the tab whose bounds intersect the specified location.
		point = SwingUtilities.convertPoint(glassPane, point, this);
		int index = indexAtLocation(point.x, point.y);

		// check if tab was dragged to end of tabs
		if (index == -1) {
			
			// returns rectangular bounds of tab at specifiec index
			Rectangle rect = getBoundsAt(getTabCount() - 1);
			rect.setRect(rect.x + rect.width / 2, rect.y, rect.width, rect.height);

			if (rect.contains(point)) {
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

		// take height of tabbed pane and subtract height of panel in currently selected tab
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
				
				// mimic screen capture by painting onto buffered image
				// and then take a clip of specific area
				BufferedImage bufferedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);								
				paint(bufferedImage.getGraphics());				
				bufferedImage = bufferedImage.getSubimage(tabBounds.x + 1, tabBounds.y, tabBounds.width, tabBounds.height);
				
				glassPane.setImage(bufferedImage);
				glassPane.setPoint(dragOrigin);
				glassPane.setVisible(true);
				
				try {
					// startDrag(Cursor dragCursor, Transferable transferable, DragSourceListener dsl)
					dge.startDrag(DragSource.DefaultMoveDrop, transferable, this);
				} catch (InvalidDnDOperationException invalidDnDOperationException) {
					invalidDnDOperationException.printStackTrace();
				}
			}
		}
	}
	// }}}

	// {{{ DragSourceListener interface
	public void dragDropEnd(DragSourceDropEvent dsde) {
		// user has released tab
		dragTabIndex = -1;
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
		// user is dragging the tab
		Point cursorLocation = dsde.getLocation();
		SwingUtilities.convertPointFromScreen(cursorLocation, glassPane);

		int tabIndex = getTargetTabIndex(cursorLocation);
		Cursor cursor;

		// if location of cursor is in region of tabs
		if (getTabAreaBounds().contains(cursorLocation)
				// and index for where tab is to be dropped was found
				&& (tabIndex >= 0)
				// and the user isn't trying to drop the tab in space before dragged tab
				&& (tabIndex != dragTabIndex)
				// and the user isn't trying to drop the tab in space after dragged tab
				&& (tabIndex != dragTabIndex + 1)) {

			cursor = DragSource.DefaultMoveDrop;
		} else {
			// user is trying to drop in area already occupied by tab
			cursor = DragSource.DefaultMoveNoDrop;
		}

		dsde.getDragSourceContext().setCursor(cursor);
		glassPane.setCursor(cursor);
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

	public void dragOver(DropTargetDragEvent dtde) {
		// get location of cursor
		Point cursorLocation = dtde.getLocation();
		int targetTabIndex = getTargetTabIndex(cursorLocation);

		// Note: For now, just assume tabs are on top

		// if unable to find the index for where tab is to be dropped
		if ((targetTabIndex < 0)
				// or user is trying to drop tab in spot before dragged tab
				|| (dragTabIndex == targetTabIndex)
				// or user is trying to drop tab on tab being dragged
				|| ((targetTabIndex - dragTabIndex) == 1)) {
			
			glassPane.setLineRect(0, 0, 0, 0);
		}
		// else if tab was dragged to front of tabs
		else if (targetTabIndex == 0) {
			
			Rectangle tabBounds = getBoundsAt(0);
			Point point = SwingUtilities.convertPoint(glassPane, 0, 0, this);
			glassPane.setLineRect(tabBounds.x, tabBounds.y - point.y, 3, tabBounds.height);
		} else {
			
			// draw line before area where dragged tab is to be dropped
			Rectangle tabBounds = getBoundsAt(targetTabIndex - 1);
			Point point = SwingUtilities.convertPoint(glassPane, 0, 0, this);
			glassPane.setLineRect(tabBounds.x + tabBounds.width, tabBounds.y - point.y, 3, tabBounds.height);
		}

		glassPane.setPoint(cursorLocation);
		glassPane.repaint();
	}

	public void drop(DropTargetDropEvent dtde) {
		
		Transferable transferable = dtde.getTransferable();
		boolean completedDrop = false;

		if (transferable != null) {

			if (transferable.isDataFlavorSupported(transferable.getTransferDataFlavors()[0]) && (dragTabIndex >= 0)) {
				
				int targetTabIndex = getTargetTabIndex(dtde.getLocation());

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

}
