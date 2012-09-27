package TwoJFrames;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DragSourceDragEvent;

public class MyDragSourceMotionListener {

	// {{{ DragSourceMotionListener interface
	public void dragMouseMoved(DragSourceDragEvent dsde) {
		Point point = dsde.getLocation();
		Rectangle tabBounds = getBoundsAt(dragTabIndex);
		window.setLocation(point.x - (tabBounds.width / 2), point.y - (tabBounds.height / 2));
	}
	// }}}
	
}
