package TwoJFrames;

import java.awt.Component;
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
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.InvalidDnDOperationException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

// TODO: does not allow dragging of tabs from one frame to another
// TODO: test dragging tab from MyTabbedPane to BufferTabs

//Drag and Drop tabs based on the following blogpost  
//by TERAI Atsuhiro posted April 7, 2008 and released as public domain:
//http://java-swing-tips.blogspot.com/2008/04/drag-and-drop-tabs-in-jtabbedpane.html		

public class MyTabbedPane extends JTabbedPane implements DragSourceListener, DragGestureListener, DragSourceMotionListener { 
	
	// DragGestureListener listens for events that indicate the user has initiated dragging the component.
	// DragSourceListener is used to provide visual feedback during the dragging of the tab.
	// DragSourceMotionListener is used to update the location of the tab as it is being dragged on the screen.
	
//implements DragSourceMotionListener, DragGestureListener, DropTargetListener, DragSourceListener  {
	
	public static void main(String[] args) {		
		JTabbedPane tabbedPane;
		JFrame frame;
		
		for(int i = 1; i <= 2; i++)
		{
			tabbedPane = new MyTabbedPane();

			for(int j = 1; j <= 4; j++)
			{
				tabbedPane.addTab("Frame #" + i + ", Tab #" + j, new JPanel());	
			}

			frame = new JFrame("Frame #" + i);

			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setContentPane(tabbedPane);
			frame.setSize(300, 100);
			frame.setVisible(true);			
		}
		
//		Here's where I found DISPOSE_ON_CLOSE
//		http://stackoverflow.com/questions/11473391/how-to-make-jframe-exist-independently-without-parent
	}

	// Object to hold information about tab being dragged.
	private Tab tab;
	
	// JWindow is what the user sees when dragging the tab.
	private JWindow window;
	
	// Transferable holds data to be transfered from DragSource to DropTarget.
	private Transferable transferable;
			
	// Each MyTabbedPane needs its own GlassPane to draw drop indicators.
	private GlassPane glassPane;
	
	// {{{ MyTabbedPane()
	public MyTabbedPane() {
		super();
		
		tab = new Tab();
		transferable = new MyTransferable();
		glassPane = new GlassPane();
						
		DragSource dragSource = new DragSource();

		// createDefaultDragGestureRecognizer(Component c, int actions, DragGestureListener dgl)
		dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
		dragSource.addDragSourceMotionListener(this);
		
//		DropTarget dropTarget = new DropTarget();
//
//		dropTarget.setComponent(window); // TODO: why does it work with this?
//		dropTarget.setDefaultActions(DnDConstants.ACTION_MOVE);
//
//		try {
//			dropTarget.addDropTargetListener(this);
//		} catch (TooManyListenersException ex) { }
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
	
	
	
//	// {{{ Rectangle getTabAreaBounds()
//	private Rectangle getTabAreaBounds() {
//		Rectangle bounds = getBounds();
//		Component selectedComponent = getSelectedComponent();
//		Rectangle componentBounds = (selectedComponent == null ? new Rectangle() : selectedComponent.getBounds());
//
//		// take height of tabbed pane and subtract height of panel in currectly selected tab
//		bounds.height = bounds.height - componentBounds.height;
//		return bounds;
//	}
//	// }}}
	
	// {{{ DragGestureListener interface	
	public void dragGestureRecognized(DragGestureEvent dge) {

		// User has initiated a drag.		
		// Each JTabbedPane needs its own DragGestureListener in order to get image of tab.
		   		
		// if there's more than one tab
		if (getTabCount() > 1) {

			// Store where the dragged tab came from					
			((MyTransferable)transferable).setFromPane(this);
			
			// Store index of tab being dragged
			Point dragOrigin = dge.getDragOrigin();
			int index = indexAtLocation(dragOrigin.x, dragOrigin.y);
			draggedTab.setIndex(index);	

			// Store backup of tab's information			
			tab.setTitle(getTitleAt(index));						
			tab.setIcon(getIconAt(index));			
			tab.setComponent(getComponentAt(index));			
			tab.setToolTipText(getToolTipTextAt(index));						
			tab.setEnabled(isEnabledAt(index));			
			tab.setTabComponent(getTabComponentAt(index));

			Component component = this.getTabComponentAt(index);
			((MyTransferable)transferable).setComponent(component);

			// Show graphic of tab being dragged
			draggedTab.show();
			
			
			// Setup the GlassPane to show drop indicators.
			glassPane.setVisible(true);
			getRootPane().setGlassPane(glassPane);
			
			try {
				// startDrag(Cursor dragCursor, Transferable transferable, DragSourceListener dsl)
				transferable.setTab(tab);
				dge.startDrag(DragSource.DefaultMoveDrop, transferable, this); // TODO
//				dge.startDrag(DragSource.DefaultMoveDrop, new MyTransferable(component), this); // TODO				
			} catch (InvalidDnDOperationException ex) {
				window.dispose();
			}
		}
	}
	// }}}
	
	
	
	
	
	// {{{ DragSourceListener interface
	public void dragDropEnd(DragSourceDropEvent dsde) {
		// user has released tab
		draggedTab.hide();
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
		// Obtain the index of the tab being dragged
		int dragTabIndex = draggedTab.getIndex( );
		
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
		if (transferable.isDataFlavorSupported(dtde.getCurrentDataFlavors()[0]) {
			dtde.acceptDrag(dtde.getDropAction());
		}
		else{
			dtde.rejectDrag( );
		}
	}
		
		
//		// Dragged component has entered region where it can possibly be dropped		
//		Transferable transferable = dtde.getTransferable();
//		
//		int dragTabIndex = draggedTab.getIndex();
//
//		if (transferable != null) {
//			if (transferable.isDataFlavorSupported(dtde.getCurrentDataFlavors()[0]) && dragTabIndex > -1) {
//				dtde.acceptDrag(dtde.getDropAction());
//				return;
//			}
//		}
//
//		dtde.rejectDrag();
//	}

	public void dragExit(DropTargetEvent dte) { }

	public void dragOver(DropTargetDragEvent dtde) { }
	
	public void drop(DropTargetDropEvent dtde) {
		Tab tab = dtde.getTransferable().getTransferData(flavor);
		
		
		Point point = dtde.getLocation();
		point = SwingUtilities.convertPoint(window, point, this);
		// convertPoint(Component source, Point aPoint, Component destination)
				
		// Move  dragged tab to given panel at specified index.
		int index = getTargetTabIndex(point);
		draggedTab.moveTo(this, dtde.getLocation());

//		// if the index for where the tab is to be dropped was found
//		if((targetTabIndex >= 0)
//			// and the user isn't dropping the tab in area already occupied by tab
//			&& (dragTabIndex != targetTabIndex)) { }
		
		
		// Notify DragSource that Component has been dropped.
		dtde.dropComplete(true);
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
