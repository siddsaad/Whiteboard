package Server;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.Toolkit;

import javax.swing.*;
import java.awt.geom.*;

public class Canvas extends JPanel{

	public Whiteboard parentFrame;
	
	private List<DShape> shapes;
	private boolean dirty;
	
	
	private Point movingKnob;
	private Point anchorKnob;
	
	private boolean isShapeClicked;
	private int movingKnobIndex;
	private int anchorKnobIndex;
	private boolean resizing;
	private ArrayList<Point> lastShapeknobs;
	
	
	
	// remember the last shape for mouse tracking
	private int lastX, lastY;
	private DShape lastShape;
	private DShape selectedShape;
	private TableModel myTableModel;
	private JLabel status;
	public ServerClientControl networkController;
	public OscControl oscController;
	
	public Canvas(int width, int height,
					   Whiteboard parentFrame,
					   TableModel myModel, 
					   JLabel status) {
		
		setPreferredSize(new Dimension(width, height));

		this.parentFrame = parentFrame;
		this.myTableModel = myModel;
		this.status = status;
		// Subclasing off JPanel, these things work
		setOpaque(true);
		// optimization: set opaque true if we fill 100% of our pixels
		setBackground(Color.white);
		
		shapes = new ArrayList<DShape>();
		clear();
		
		lastShape = null;
		resizing = false;
		
		networkController = new ServerClientControl(parentFrame, this, status);
		oscController = new OscControl(parentFrame, this);
		// Checks to see if a shape is selected when mouse is pressed
		addMouseListener( new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				
				movingKnobIndex = -1;
				anchorKnobIndex = -1;
				
				DShape shape = findShape(e.getX(), e.getY());
				
				// if any shape is clicked find out if the newest click 
				// was on a knob of the shape
				if( isShapeClicked ){
					lastShapeknobs = lastShape.getKnobs();
					
					//find out if the click was on a knob or not
					movingKnobIndex = findKnob(e.getX(), e.getY());
					
					// if the click was on a knob it is the moving knob,
					// so now we can find where the index of the moving 
					// knob and anchor knob is in the knob array to resize
					if(movingKnobIndex >= 0){
						resizing = true;
						anchorKnobIndex = (movingKnobIndex+(lastShapeknobs.size()/2))%(lastShapeknobs.size());
						//System.out.println("knob selected: " + movingKnobIndex );
						//System.out.println("Anchor: " + anchorKnobIndex);
						movingKnob = lastShapeknobs.get(movingKnobIndex);
						anchorKnob = lastShapeknobs.get(anchorKnobIndex);
					}
					// if the click wasn't on one of the knobs
					// set resizing to false so the next if statement
					// can set a new selected shape or no selected shape
					else{
						resizing = false;
						//System.out.println("knob not selected");
					}
				}
					
				if ( (shape != null) && !resizing) {	// make
					clearLastShapeSelected();
					if(!getParentFrame().getIsClient());
						shape.setSelected(true);
					lastShape = shape;
					isShapeClicked = true;
					
					resizing = false;
					
				}
				else if(shape == null && !resizing){
					isShapeClicked = false;
					resizing = false;
					clearLastShapeSelected();
				}
					
				// Note the starting setup to compute deltas later
				lastX = e.getX();
				lastY = e.getY();

			}
		});


		addMouseMotionListener( new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				
				// if there is a selected shape whose knob was clicked
				// then resizing is true and we must now resize
				// by setting the new boundary of the shape based on the 
				// difference between the moving knob and the anchor knob
				if( (lastShape != null) && isShapeClicked && resizing){
					lastX = e.getX();
					lastY = e.getY();
					
					movingKnob.setLocation(e.getX(), e.getY());
				
					Rectangle newBoundary = lastShape.findNewBoundary(movingKnob, anchorKnob);
				
					// apply the delta to that shape
					repaintShape(lastShape);
					lastShape.resize(newBoundary);
				}
				// if there is a selected shape and the knob wasn't clicked,
				// then we must simply move the entire shape accross the screen
				// this is done by just adding the delta of the last mouse position
				// and the current mouse position to the x,y values of the shape's origin
				else if ( (lastShape != null) && isShapeClicked && !resizing) {
					// compute delta from last point
					int dx = e.getX()-lastX;
					int dy = e.getY()-lastY;
					lastX = e.getX();
					lastY = e.getY();

					// For fun, if shift-key is down, multiply dx,dy * -2
					// demonstrates that the UI appearance of "drag" is a just
					// a careful illusion
					if (e.isShiftDown()) {
						dx *= -2;
						dy *= -2;
					}

					// apply the delta to that shape
					repaintShape(lastShape);
					lastShape.move(dx, dy);
					//moveShape(lastShape, dx, dy);
				}
			}
		});
		
	}
	
	// Clears out all the data (used by new docs, and for opening docs)
	public void clear() {
		shapes.clear();
		dirty = false;
		repaint();
	}

	// Default ctor, uses a default size
	public Canvas() {
		setPreferredSize(new Dimension(400, 400));
	}

	
	// Adds a shape to the shape array
	public void addShape(DShapeModel shapeModel){
		
		clearLastShapeSelected();
		
		boolean selected = true;
		
		if(shapeModel instanceof DRectModel){
			DRectModel rectModel = (DRectModel) shapeModel;
			DRect rect = new DRect(rectModel, this);
			
			rectModel.addListener(rect);
			rectModel.addListener(myTableModel);
			//rectModel.addListener(networkController);
			
			addShapeToList(rect);
			lastShape = rect;
			if(!getParentFrame().getIsClient())
				rect.setSelected(selected);
			
			// send the network info if it is the server
			if(parentFrame.getIsServer())
				networkController.doSend("add", rectModel);
			// send the osc messages if it is the Client or no network
			else
				oscController.sendOscMsg("add", rectModel);
		}
		else if(shapeModel instanceof DOvalModel){
			DOvalModel ovalModel = (DOvalModel) shapeModel;
			DOval oval = new DOval(ovalModel, this);
			
			ovalModel.addListener(oval);
			ovalModel.addListener(myTableModel);
			//ovalModel.addListener(networkController);
			
			addShapeToList(oval);
			lastShape = oval;
			
			if(!getParentFrame().getIsClient())
				oval.setSelected(selected);
						
			// send the network info if it is the server
			if(parentFrame.getIsServer())
				networkController.doSend("add", ovalModel);
			// send the osc messages if it is the Client or no network
			else
				oscController.sendOscMsg("add", ovalModel);
		}
		else if(shapeModel instanceof DLineModel){
			DLineModel lineModel = (DLineModel) shapeModel;
			DLine line = new DLine(lineModel, this);
			
			lineModel.addListener(line);
			lineModel.addListener(myTableModel);
			//lineModel.addListener(networkController);
			
			addShapeToList(line);
			lastShape = line;
			
			if(!getParentFrame().getIsClient())
				line.setSelected(selected);
			
			
			// send the network info if it is the server
			if(parentFrame.getIsServer())
				networkController.doSend("add", lineModel);
			// send the osc messages if it is the Client or no network
			else
				oscController.sendOscMsg("add", lineModel);

		}
		else if(shapeModel instanceof DTextModel){
			DTextModel textModel = (DTextModel) shapeModel;
			DText text = new DText(textModel, this);
			
			textModel.addListener(text);
			textModel.addListener(myTableModel);
			//textModel.addListener(networkController);
			
			addShapeToList(text);
			lastShape = text;
			if(!getParentFrame().getIsClient())
				text.setSelected(selected);
			
			
			// send the network info if it is the server
			if(parentFrame.getIsServer())
				networkController.doSend("add", textModel);
			// send the osc messages if it is the Client or no network
			else
				oscController.sendOscMsg("add", textModel);

		}

		
		// should do smart repaint as well?
		repaint();
	}
	
	
	/**
	 Finds a dot in the data model that contains
	 the given x,y or returns null.
	 */
	public DShape findShape(int x, int y) {
		// Search through the shapes in reverse order, so
		// hit topmost ones first.
		for (int i=shapes.size()-1; i>=0; i--) {
			DShape shape = shapes.get(i);
			
			Rectangle bounds = shape.getBounds();
			
			if(   ( (bounds.x <= x) && (x <= (bounds.x + bounds.width)) )  
			   && ( (bounds.y <= y) && (y <= (bounds.y +bounds.height)) )  ){
				
				// moves selected shape to front automatically
				//moveToFront(shape, i);
				return shape;
			}
		}
		//System.out.println("returned null");
		return null;
	}
	
	public int findKnob(int x, int y){
		
		for(int i=0; i<lastShapeknobs.size(); i++){
			//first find the origin point of each knob in the array
			// because it is defined by it's center point which is
			// defined by 1 pixel within the boundary of the shape
			// this makes computations a bit easier
			int xZeroPoint = lastShapeknobs.get(i).x - DShape.KNOB_SIZE/2;
			int yZeroPoint = lastShapeknobs.get(i).y - DShape.KNOB_SIZE/2;
			
			// if the x coord of mouse click is greater than the knob's lowest
			// x coord value, and less than the knob's greatest x coord value
			// then the mouse's x coord is within the Knob Boundary
			// if the y coord of mouse click is greater than the knob's lowest
			// y coord value, and less than the knob's greatest y coord value
			// then the mouse's x coord is within the Knob Boundary
			if(   ( (xZeroPoint <= x) && (x <= (xZeroPoint + DShape.KNOB_SIZE)) )  
			   && ( (yZeroPoint <= y) && (y <= (yZeroPoint + DShape.KNOB_SIZE)) )  )
					
				return i;
		}
		return -1;
	}
	
	public Whiteboard getParentFrame(){
		return parentFrame;
	}
	
	public DShape getSelectedShape(){
		return lastShape;
	}

	public void clearLastShapeSelected(){
		if(lastShape != null)
			lastShape.setSelected(false);
	}
	
	public void moveToFront(DShape shape, int index){
		
		
		if((shape != null) && (index >= 0)){
			removeShape(index);
			addShapeToList(shape);
			
			// smart repaint only repaints the shape itself
			// if this is the client it does a regular repaint
			if(parentFrame.getIsClient())
				repaint();	
			else 
				repaintShape(shape);
			
			if(parentFrame.getIsServer())
				networkController.doSend("front", shape.getModel());
			return;
		}
			
		for (int i=shapes.size()-1; i>=0; i--) {
			shape = shapes.get(i);
			
			if(shape.getIsSelected()){
				shapes.remove(i);
				addShapeToList(shape);
				
				// smart repaint only repaints the shape
				repaintShape(shape);
				if(parentFrame.getIsServer())
					networkController.doSend("front", shape.getModel());
			}		
		}
	}
	
	public void moveToBack(){
		
		DShape shape;
		
		for (int i=shapes.size()-1; i>=0; i--) {
			shape = shapes.get(i);
			
			if(shape.getIsSelected()){
				removeShape(i);
				addShapeToList(shape, 0);
				
				//shape.setSelected(false);
				// smart repaint only repaints the shape
				repaintShape(shape);
				if(parentFrame.getIsServer())
					networkController.doSend("back", shape.getModel());
			}		
		}
	}
	
	// Only for client use
	public void moveToBack(DShape shape, int index){
		
		removeShape(index);
		addShapeToList(shape, 0);
		
		if(parentFrame.getIsClient())
			repaint();	
	}
	
	public void deleteShape(){
		
		DShape shape;
		
		for (int i=shapes.size()-1; i>=0; i--) {
			shape = shapes.get(i);
			
			if(shape.getIsSelected()){
				removeShape(i);
				isShapeClicked = false;
				resizing = false;
				//shape.setSelected(false);
				// smart repaint only repaints the shape
				repaintShape(shape);
			
				
				// send the network info if it is the server
				if(parentFrame.getIsServer())
					networkController.doSend("remove", shape.getModel());
				// send the osc messages if it is the Client or no network
				else
					oscController.sendOscMsg("remove", shape.getModel());
				
				
				return;
			}		
		}
	}
	
	
	public void removeShape(int index){
		shapes.remove(index);
		myTableModel.removeRow(index);
		
//		if(parentFrame.getIsClient())
//			repaint();	
	}
	
	public void clientRemoveShape(int index){		
		DShape shape = shapes.remove(index);
		myTableModel.removeRow(index);
		oscController.sendOscMsg("remove", shape.getModel());
		repaint();	
	}
	
	
	public void addShapeToList(DShape shape){
		shapes.add(shape);
		myTableModel.addRow(0);
	}
	
	public void addShapeToList(DShape shape, int index){
		shapes.add(index, shape);
		myTableModel.addRow(shapes.size()-1);
	}
	
//	public void moveShape(DShape lastShape, int dx, int dy){
//		
//		//repaintShape(lastShape);
//		
//		lastShape.move(dx, dy);
//		
//		//repaintShape(lastShape);
//		
//	}
	
	/**
	 Utility -- does a repaint rect just around one Shape. Used
	 by smart repaint when dragging a shape.
	 */
	public void repaintShape(DShape shape) {
		
		Rectangle bounds = shape.getBounds();
		
		repaint(bounds.x-(DShape.KNOB_SIZE), bounds.y - (DShape.KNOB_SIZE),
				bounds.width+DShape.KNOB_SIZE*2, bounds.height+DShape.KNOB_SIZE*2);
	}
	
	
	
	public void paintComponent(Graphics g) {
		// As a JPanel subclass we need call super.paintComponent()
		// so JPanel will draw the white background for us.
		super.paintComponent(g);

		// Go through all the shapes, drawing each one
		for (DShape shape : shapes) {
			shape.drawShape(g);
		}
		
		if(!parentFrame.getIsForServerUseOnly()){
			double xInc, yInc;
			final int GRID_SIZE = 25, PAD = 0;
			    
	        Graphics2D g2 = (Graphics2D)g;
	        g2.setColor(Color.gray);
	        //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	          //                  RenderingHints.VALUE_ANTIALIAS_ON);        
			// Get screen dimensions
			Toolkit kit = Toolkit.getDefaultToolkit();
			Dimension screenSize = kit.getScreenSize();		
			
	        double w = screenSize.width;
	        double h = screenSize.width;
	        xInc = 50;
	        yInc = 50;
	        
	        // row lines
	        double x1 = PAD, y1 = PAD, x2 = w - PAD, y2 = h - PAD;
	        
	        for(int j = 0; j <= GRID_SIZE; j++)
	        {
	            g2.draw(new Line2D.Double(x1, y1, x2, y1));
	            y1 += yInc;
	        }
	        // col lines
	        y1 = PAD;
	        for(int j = 0; j <= GRID_SIZE; j++)
	        {
	            g2.draw(new Line2D.Double(x1, y1, x1, y2));
	            x1 += xInc;
	        }     
		}
	}	

	
	public List<DShape> getShapesList(){
		return this.shapes;
	}
	/**
	 Saves out our state (all the dot models) to the given file.
	 Uses Java built-in XMLEncoder.
	 */
	public void save(File file) {
		try {
			XMLEncoder xmlOut = new XMLEncoder(
					new BufferedOutputStream(
							new FileOutputStream(file)));

			// Could do something like this to control which
			// properties are sent. By default, it just sends
			// all of them with getters/setters, which is fine in this case.
			//  xmlOut.setPersistenceDelegate(DotModel.class,
			//       new DefaultPersistenceDelegate(
			//           new String[]{ "x", "y", "color" }) );


			// Make a DShapeModel array of everything
			DShapeModel[] shapeModelArray = new DShapeModel[shapes.size()];
			
			int i = 0;
			for(DShape shape : shapes){
				shapeModelArray[i] = shape.getModel();
				i++;
			}

			// Dump that whole array
			xmlOut.writeObject(shapeModelArray);

			// And we're done!
			xmlOut.close();
			//setDirty(false);
			// cute: only clear dirty bit *after* all the things that
			// could fail/throw an exception
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 Reads in all the dots from the file and set the panel
	 to show them.
	 */
	public void open(File file) {
		DShapeModel[] shapeModelArray = null;
		shapes = new ArrayList<DShape>();
		
		try {
			XMLDecoder xmlIn = new XMLDecoder(new BufferedInputStream(
					new FileInputStream(file)));

			shapeModelArray = (DShapeModel[]) xmlIn.readObject();
			xmlIn.close();

			// now we have the data, so go ahead and wipe out the old state
			// and put in the new. Goes through the same doAdd() bottleneck
			// used by the UI to add dots.
			// Note that we do this after the operations that might throw.
			clear();
			for(DShapeModel shapeModel : shapeModelArray) {
				addShape(shapeModel);
			}
			//setDirty(false);

		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 Saves the current appearance of the DotPanel out as a PNG
	 in the given file.
	 */
	public void saveImage(File file) {
		// Create an image bitmap, same size as ourselves
		BufferedImage image = (BufferedImage) createImage(getWidth(), getHeight());

		// Get Graphics pointing to the bitmap, and call paintAll()
		// This is the RARE case where calling paint() is appropriate
		// (normally the system calls paint()/paintComponent())
		Graphics g = image.getGraphics();
		paintAll(g);
        g.dispose();  // Good but not required-- 
        // dispose() Graphics you create yourself when done with them.

		try {
			javax.imageio.ImageIO.write(image, "PNG", file);
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	
	
	//Trigger Server Controlls
	public int launchServer(){
		int result;
		result = networkController.doServer();
		
		return result;
	}
	//Trigger Server Controlls
	public int launchClient(){
		int result;
		result = networkController.doClient();
		
		return result;
	}
	
}
