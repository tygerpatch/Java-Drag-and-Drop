package WithJWindow;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

public class MyTransferable implements Transferable{
	  private DataFlavor[] dataFlavors = { new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, null) };	  	 
	  private MyTabbedPane tabbedPane;

	  public MyTransferable(MyTabbedPane tabbedPane){
	    this.tabbedPane = tabbedPane;
	  }
		
	  @Override 
	  public Object getTransferData(DataFlavor dataFlavor) throws UnsupportedFlavorException {
		  return tabbedPane; // TODO:		  
	  }
	    
	  @Override 
	  public DataFlavor[] getTransferDataFlavors(){
	    DataFlavor[] copy = new DataFlavor[dataFlavors.length];    
	    System.arraycopy(dataFlavors, 0, copy, 0, dataFlavors.length);     
	    return copy;
	  }
	    
	  @Override 
	  public boolean isDataFlavorSupported(DataFlavor dataFlavor){
	    return dataFlavors[0].equals(dataFlavor);
	  }
	}
