package TwoJFrames;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;

import javax.swing.SwingUtilities;

public class MyDragSourceListener implements DragSourceListener {


	
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
		// get location of cursor
		
		Point cursorLocation = dsde.getLocation();
		
		// TODO: clean up
		SwingUtilities.convertPointFromScreen(cursorLocation, glassPane);		
		cursorLocation = SwingUtilities.convertPoint(glassPane, cursorLocation, this);

		int targetTabIndex = getTargetTabIndex(cursorLocation);
		
		// if unable to find the index for where tab is to be dropped
		if ((targetTabIndex < 0)
				// or user is trying to drop tab in spot before dragged tab
				|| (dragTabIndex == targetTabIndex)
				// or user is trying to drop tab on tab being dragged
				|| ((targetTabIndex - dragTabIndex) == 1)) {
			
			// don't draw vertical line
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

		// glassPane.setPoint(cursorLocation);
		glassPane.repaint();

		// if location of cursor is in region of tabs
		if (getTabAreaBounds().contains(cursorLocation)
		// and index for where tab is to be dropped was found
				&& (targetTabIndex >= 0)
				// and the user isn't trying to drop the tab in space before dragged tab
				&& (targetTabIndex != dragTabIndex)
				// and the user isn't trying to drop the tab in space after dragged tab
				&& (targetTabIndex != dragTabIndex + 1)) {

			dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
		} else {
			dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
		}
	}

	public void dropActionChanged(DragSourceDragEvent dragSourceDragEvent) { }
	// }}}
	
}
