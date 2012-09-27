package Original;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
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
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

// Drag and Drop tabs based on the following blogpost  
// by TERAI Atsuhiro posted April 7, 2008 and released as public domain:
// http://java-swing-tips.blogspot.com/2008/04/drag-and-drop-tabs-in-jtabbedpane.html		
public class MyTabbedPane extends JTabbedPane 
{
  public static void main(String[] args)
  {
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
  
  private static final int LINEWIDTH = 3;
  private static final String NAME = "test";  
  private final Rectangle lineRect  = new Rectangle();
  private final Color   lineColor = new Color(0, 100, 255);
  private int dragTabIndex = -1;
  private static Rectangle rBackward = new Rectangle();
  private static Rectangle rForward  = new Rectangle();
  private static int rwh = 20;
  private static int buttonsize = 30;//XXX: magic number of scroll button size
  
  private void clickArrowButton(String actionKey) 
  {
    ActionMap map = getActionMap();
    
    if(map != null) 
    {
      Action action = map.get(actionKey);
      
      if(action != null && action.isEnabled()) 
      {
        action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null, 0, 0));
      }
    }
  }
    
  private void autoScrollTest(Point glassPt) 
  {
    Rectangle r = getTabAreaBounds();    
    int tabPlacement = getTabPlacement();
    
    // TODO: strategy_class.setForwardBackwardRectangle()
    
    if(tabPlacement==TOP || tabPlacement==BOTTOM) 
    {
      rBackward.setBounds(r.x, r.y, rwh, r.height);
      rForward.setBounds(r.x+r.width-rwh-buttonsize, r.y, rwh+buttonsize, r.height);
    }
    else if(tabPlacement==LEFT || tabPlacement==RIGHT) 
    {
      rBackward.setBounds(r.x, r.y, r.width, rwh);
      rForward.setBounds(r.x, r.y+r.height-rwh-buttonsize, r.width, rwh+buttonsize);
    }
    
    // rBackward and rForward are the red boxes that show up when you put a tab at fron
    rBackward = SwingUtilities.convertRectangle(getParent(), rBackward, glassPane);
    rForward  = SwingUtilities.convertRectangle(getParent(), rForward,  glassPane);
    
    // this is to simulate clicking on the forward and back buttons if scroll tabs is on    
    if(rBackward.contains(glassPt)) 
    {
      clickArrowButton("scrollTabsBackwardAction");
    }
    else if(rForward.contains(glassPt)) 
    {
      clickArrowButton("scrollTabsForwardAction");
    }
  }

  private DragSourceListener dragSourceListener = new DragSourceListener() 
  {
    @Override 
    public void dragEnter(DragSourceDragEvent dragSourceDragEvent) 
    {
      // user dragged tab to droppable spot
      dragSourceDragEvent.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
    }
    
    @Override 
    public void dragExit(DragSourceEvent dragSourceEvent) 
    {
      // user dragged tab to place where it cannot be dropped        
      dragSourceEvent.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
      
      // TODO: hide glassPane
      lineRect.setRect(0, 0, 0, 0);
      glassPane.setPoint(new Point(-1000,-1000));
      glassPane.repaint();
    }
    
    @Override 
    public void dragOver(DragSourceDragEvent dragSourceDragEvent) 
    {
      // user is dragging the tab      
      Point glassPt = dragSourceDragEvent.getLocation();
      SwingUtilities.convertPointFromScreen(glassPt, glassPane);
      int targetIdx = getTargetTabIndex(glassPt);
      Cursor cursor;
      
      // if user is dragging within region containing tabs
      // and it's not in an area already occupied by the dragged tab
      if(getTabAreaBounds().contains(glassPt) && 
          targetIdx >= 0 &&
          targetIdx != dragTabIndex && 
          targetIdx != dragTabIndex+1) 
      {
        cursor = DragSource.DefaultMoveDrop;
      }
      else
      {
        // user is trying to drop in area already occupied by tab
        cursor = DragSource.DefaultMoveNoDrop;
      }
      
      dragSourceDragEvent.getDragSourceContext().setCursor(cursor);
      glassPane.setCursor(cursor);
    }
    
    @Override 
    public void dragDropEnd(DragSourceDropEvent dragSourceDropEvent) 
    {
      // user has released tab
      
      lineRect.setRect(0,0,0,0);
      dragTabIndex = -1;
      glassPane.setVisible(false);
      
      if( hasGhost() ) 
      {
        glassPane.setImage(null);
      }
    }
    
    @Override 
    public void dropActionChanged(DragSourceDragEvent dragSourceDragEvent) {}
  };

  private Transferable transferable = new Transferable() 
  {
    private DataFlavor[] dataFlavors = { new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "Transfer Tab") };
    private List dataFlavorList = Arrays.asList(dataFlavors);
    
    @Override 
    public Object getTransferData(DataFlavor flavor) 
    {
      // TODO: return information about tab like text, location, dimension
      return MyTabbedPane.this;
    }
    
    @Override 
    public DataFlavor[] getTransferDataFlavors() 
    {
      return dataFlavors;
    }
    
    @Override 
    public boolean isDataFlavorSupported(DataFlavor dataFlavor) 
    {
      return dataFlavorList.contains(dataFlavor);
    }
  };
  
  public MyTabbedPane() 
  {
    super();
    
    new DropTarget(
      glassPane, 
      DnDConstants.ACTION_COPY_OR_MOVE, 
      new DropTargetListener() 
      {
        @Override 
        public void dragEnter(DropTargetDragEvent e) 
        {      
          // user has dragged tabbed within droppable region
          if(isDragAcceptable(e))
          {
            e.acceptDrag(e.getDropAction());
          }
          else
          {
            e.rejectDrag();
          }
        }
        
        @Override 
        public void dragExit(DropTargetEvent e) {}
        
        @Override 
        public void dropActionChanged(DropTargetDragEvent e) {}
    
        private Point _glassPt = new Point();
        
        @Override 
        public void dragOver(final DropTargetDragEvent e) 
        {
          // TODO: document
          Point glassPt = e.getLocation();
          
          if(getTabPlacement() == JTabbedPane.TOP || getTabPlacement() == JTabbedPane.BOTTOM) 
          {
            // draws horizontal line where tab will be dropped
            initTargetLeftRightLine(getTargetTabIndex(glassPt));
          }
          else
          {
            // draws vertical line where tab will be dropped
            initTargetTopBottomLine(getTargetTabIndex(glassPt));
          }
          
          if(hasGhost()) 
          {
            glassPane.setPoint(glassPt);
          }
          
          if(!_glassPt.equals(glassPt)) 
          {
            glassPane.repaint();
          }
          
          _glassPt = glassPt;
          autoScrollTest(glassPt);
        }
    
        @Override 
        public void drop(DropTargetDropEvent dropTargetDropEvent) 
        {
          boolean isDropAcceptable = isDropAcceptable(dropTargetDropEvent);
          
          if(isDropAcceptable) 
          {
            convertTab(dragTabIndex, getTargetTabIndex(dropTargetDropEvent.getLocation()));
          }
          
          dropTargetDropEvent.dropComplete(isDropAcceptable);      
          repaint();
        }
        
        // TODO: combine isDragAcceptable and isDropAcceptable (or use a design pattern)
        private boolean isDragAcceptable(DropTargetDragEvent dropTargetDragEvent) 
        {
          Transferable transferable = dropTargetDragEvent.getTransferable();
          
          if(transferable != null)
          {
            DataFlavor[] dataFlavors = dropTargetDragEvent.getCurrentDataFlavors();
            
            // TODO: not sure why checking if data flavor is supported
            return (transferable.isDataFlavorSupported(dataFlavors[0]) && dragTabIndex >= 0); 
          }
    
          return false;
        }
        
        private boolean isDropAcceptable(DropTargetDropEvent dropTargetDropEvent) 
        {
          Transferable transferable = dropTargetDropEvent.getTransferable();
       
          if(transferable != null)
          {
            DataFlavor[] dataFlavors = transferable.getTransferDataFlavors();
          
            // TODO: not sure why checking if data flavor is supported
            return (transferable.isDataFlavorSupported(dataFlavors[0]) && dragTabIndex >= 0); 
          }
          
          return false;
        }
      },                  
      true
    );
    
    new DragSource().createDefaultDragGestureRecognizer(
      this, 
      DnDConstants.ACTION_COPY_OR_MOVE,                 
      new DragGestureListener() 
      {
        @Override 
        public void dragGestureRecognized(DragGestureEvent dragGestureEvent) 
        {
          // if there's more than one tab
          if(getTabCount() > 1)
          {
            Point tabPt = dragGestureEvent.getDragOrigin();
            dragTabIndex = indexAtLocation(tabPt.x, tabPt.y);
            
            // if index of tab was found and tab is enabled
            if((dragTabIndex >= 0) && isEnabledAt(dragTabIndex))
            {
              // TODO: 
              initGlassPane(dragGestureEvent.getComponent(), dragGestureEvent.getDragOrigin());
              
              try
              {
                dragGestureEvent.startDrag(DragSource.DefaultMoveDrop, transferable, dragSourceListener);
              }
              catch(InvalidDnDOperationException invalidDnDOperationException) 
              {
                invalidDnDOperationException.printStackTrace();
              }            
            }
          }
        }
      }
    );
  }

  private boolean hasGhost = true;
  
  public void setPaintGhost(boolean flag) 
  {
    hasGhost = flag;
  }
  
  public boolean hasGhost() 
  {
    return hasGhost;
  }
  
  private boolean isPaintScrollArea = true;
  
  public void setPaintScrollArea(boolean flag) 
  {
    isPaintScrollArea = flag;
  }
  
  public boolean isPaintScrollArea() 
  {
    return isPaintScrollArea;
  }

  private int getTargetTabIndex(Point glassPt) 
  {
    // gets index of tab at specificed point
    
    // TODO: use state pattern instead of checking where tabs are placed (ie HorizontalTabbedPane, VerticalTabbedPane)
    Point tabPt = SwingUtilities.convertPoint(glassPane, glassPt, MyTabbedPane.this);
    boolean isTB = getTabPlacement() == JTabbedPane.TOP || getTabPlacement() == JTabbedPane.BOTTOM;
    
    for(int i = 0; i < getTabCount(); i++) 
    {
      Rectangle r = getBoundsAt(i);
      
      if(isTB)
      {
        r.setRect(r.x - r.width/2, r.y,  r.width, r.height);
      }
      else
      {
        r.setRect(r.x, r.y-r.height/2, r.width, r.height);
      }
      
      if(r.contains(tabPt)) 
      {
        return i;
      }
    }
    
    Rectangle r = getBoundsAt(getTabCount()-1);
    
    if(isTB)
    {
      r.setRect(r.x+r.width/2, r.y,  r.width, r.height);
    }
    else
    {
      r.setRect(r.x, r.y+r.height/2, r.width, r.height);
    }
    
    return   r.contains(tabPt) ? getTabCount() : -1;
  }
  
  private void convertTab(int prev, int next) 
  {
    if(next < 0 || prev == next) 
    {
      return;
    }
    
    // TODO: encapsulate this into a Tab object
    Component cmp = getComponentAt(prev);
    Component tab = getTabComponentAt(prev);
    String str  = getTitleAt(prev);
    Icon icon   = getIconAt(prev);
    String tip  = getToolTipTextAt(prev);
    boolean flg   = isEnabledAt(prev);
    
    int tgtindex  = prev > next ? next : next-1;
    
    remove(prev);
    insertTab(str, icon, cmp, tip, tgtindex);
    setEnabledAt(tgtindex, flg);
    
    // When you drag'n'drop a disabled tab, it finishes enabled and selected.
    // pointed out by dlorde
    if(flg)
    {
      setSelectedIndex(tgtindex);
    }

    // I have a component in all tabs (jlabel with an X to close the tab)
    // and when i move a tab the component disappear.
    // pointed out by Daniel Dario Morales Salas
    setTabComponentAt(tgtindex, tab);
  }

  private void initTargetLeftRightLine(int next) 
  {    
    if(next < 0 || dragTabIndex == next || next-dragTabIndex == 1) 
    {
      lineRect.setRect(0,0,0,0);
    }
    else if(next == 0) 
    {
      Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(0), glassPane);
      lineRect.setRect(r.x-LINEWIDTH/2,r.y,LINEWIDTH,r.height);
    }
    else
    {
      Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(next-1), glassPane);
      lineRect.setRect(r.x+r.width-LINEWIDTH/2,r.y,LINEWIDTH,r.height);
    }
  }
  
  private void initTargetTopBottomLine(int next) 
  {
    if(next < 0 || dragTabIndex==next || next-dragTabIndex==1) 
    {
      lineRect.setRect(0,0,0,0);
    }
    else if(next==0) 
    {
      Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(0), glassPane);
      lineRect.setRect(r.x,r.y-LINEWIDTH/2,r.width,LINEWIDTH);
    }
    else
    {
      Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(next-1), glassPane);
      lineRect.setRect(r.x,r.y+r.height-LINEWIDTH/2,r.width,LINEWIDTH);
    }
  }

  private void initGlassPane(Component c, Point tabPt) 
  {
    getRootPane().setGlassPane(glassPane);
    
    if(hasGhost()) 
    {
      // i think this is where the tab is being drawn
      Rectangle rect = getBoundsAt(dragTabIndex);
      
      BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
      Graphics g = image.getGraphics();
      
      // TODO: not sure why calling paint on component
      c.paint(g);
      
      rect.x = rect.x < 0 ? 0 : rect.x;
      rect.y = rect.y < 0 ? 0 : rect.y;
      
      image = image.getSubimage(rect.x, rect.y, rect.width, rect.height);
      glassPane.setImage(image);
    }
           
    Point glassPt = SwingUtilities.convertPoint(c, tabPt, glassPane);
    
    glassPane.setPoint(glassPt);
    glassPane.setVisible(true);
  }

  private Rectangle getTabAreaBounds() 
  {
    Rectangle bounds = getBounds();
        
    Component selectedComponent = getSelectedComponent();
    Rectangle componentBounds = (selectedComponent == null ? new Rectangle() : selectedComponent.getBounds());
    
    bounds.height = bounds.height - componentBounds.height;
    bounds.grow(2, 2);
    
    return bounds;
  }
  
  private final GhostGlassPane glassPane = new GhostGlassPane();
  
  class GhostGlassPane extends JPanel 
  {
    private final AlphaComposite composite;
    private Point location = new Point(0, 0);
    private BufferedImage draggingGhost = null;
    
    public GhostGlassPane() 
    {
      // TODO: research AlphaComposite
      // TODO: setting setOpaque to true
      
      setOpaque(false);
      composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
      //http://bugs.sun.com/view_bug.do?bug_id=6700748
      //setCursor(null);
    }
    
    public void setImage(BufferedImage draggingGhost) 
    {
      this.draggingGhost = draggingGhost;
    }
    
    public void setPoint(Point location) 
    {
      this.location = location;
    }
    
    @Override 
    public void paintComponent(Graphics g) 
    {
      Graphics2D g2 = (Graphics2D) g;
      g2.setComposite(composite);
      
      if(isPaintScrollArea() && getTabLayoutPolicy() == SCROLL_TAB_LAYOUT) 
      {
        g2.setPaint(Color.RED);
        g2.fill(rBackward);
        g2.fill(rForward);
      }
      
      if(draggingGhost != null) 
      {
        // double xx = location.getX() - (draggingGhost.getWidth(this) /2d);
        // double yy = location.getY() - (draggingGhost.getHeight(this)/2d);
        
        int xx = location.x - (draggingGhost.getWidth()/2);
        int yy = location.y - (draggingGhost.getHeight()/2);

        
        g2.drawImage(draggingGhost, (int)xx, (int)yy , null);
      }
      
      if(dragTabIndex >= 0) 
      {
        // g2.setPaint(lineColor);
        g2.setColor(lineColor);
        g2.fill(lineRect);
      }
    }
  }  

}

