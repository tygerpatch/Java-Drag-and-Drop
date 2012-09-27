package TwoJFrames;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.Icon;

public class TransferableTab implements Transferable {

	// The Transferable interface provides the API to transfer data from DragSource to DropTarget
	private DataFlavor[] dataFlavors = { new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, null) };
	// javaJVMLocalObjectMimeType - transfer Java Object within the same JVM
		

	
	private Tab tab;
	
	@Override
	public Object getTransferData(DataFlavor flavor) {
		return component;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		// Create copy of dataFlavors array to prevent access of private variable
		DataFlavor[] copy = new DataFlavor[dataFlavors.length];
		System.arraycopy(dataFlavors, 0, copy, 0, dataFlavors.length);
		return copy;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
		return dataFlavors[0].equals(dataFlavor);
	}
}
