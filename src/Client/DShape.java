package Client;


import java.awt.*;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;

abstract public class DShape implements ModelListener{

	private DShapeModel shapeModel;
	private boolean isSelected;
	private CanvasPanel canvas;
	
	public DShape(DShapeModel shapeModel, CanvasPanel canvas){
		
		this.shapeModel = shapeModel;
		this.canvas = canvas;
		isSelected = false;
		
	}
	
	public Rectangle getBounds(){
		
		return (new Rectangle(shapeModel.getX(), 
							  shapeModel.getY(), 
							  shapeModel.getWidth(), 
							  shapeModel.getHeight()));
	}
	
	public DShapeModel getModel(){
		return this.shapeModel;
	}
	
	public void setSelected(boolean selected){
		this.isSelected = selected;
		canvas.repaintShape(this);
	}
	
	public boolean getIsSelected(){
		return this.isSelected;
	}
	
	public CanvasPanel getCanvas(){
		return this.canvas;
	}
	
	public ArrayList<Point> getKnobs(){
		
		ArrayList<Point> points = new ArrayList<Point>();
		
		Rectangle bounds = getBounds();
		
		points.add(new Point(bounds.x+1, bounds.y+1));  // Top left
		points.add(new Point(bounds.x+1, bounds.y+bounds.height-1));  // bottom left
		points.add(new Point(bounds.x+bounds.width-1, bounds.y+bounds.height-1));  // bottom right
		points.add(new Point(bounds.x+bounds.width-1, bounds.y+1)); // top right
				
		return points;
	}
	
	
	public void move(int dx, int dy){
		shapeModel.moveBy(dx, dy);
	}
	
	public void scaleWidthHeight(int dx, int dy){
		shapeModel.scaleWidthHeight(dx, dy);
	}
	
	public void resize(Rectangle rect){
		shapeModel.resize(rect);
	}
	
	public Rectangle findNewBoundary(Point movingKnob, Point anchorKnob){
		int x;
		int y;
		int width;
		int height;
	
		// we have to subtract 1 because the Point of each knob is 
		// defined by the point 1 pixel inside each corner of the 
		// shapes bounding rectangle
		if(movingKnob.x <= anchorKnob.x)
			x = (movingKnob.x-1);
		else
			x = anchorKnob.x-1;
		
		if(movingKnob.y <= anchorKnob.y)
			y = movingKnob.y-1;
		else
			y = anchorKnob.y-1;
		
		width = Math.abs(movingKnob.x - anchorKnob.x);
		height = Math.abs(movingKnob.y - anchorKnob.y);
		
		return (new Rectangle(x, y, width+1, height+1));
	}
	
	
	public void modelChanged(DShapeModel model){
		if(canvas.parentFrame.getIsServer())
			canvas.networkController.doSend("change", getModel());
		else if(getCanvas().parentFrame.getIsClient()){
			getCanvas().oscController.sendOscMsg("change", getModel());
			
			canvas.repaint();
			return;
		}
		else
			canvas.oscController.sendOscMsg("change", getModel());
		
		canvas.repaintShape(this);
	}
	
	public abstract void drawShape(Graphics g);	
	
	public void drawSelected(Graphics g){
		g.setColor(Color.BLACK);
		
		ArrayList<Point> points = getKnobs();
		
		for(int i=0; i< points.size(); i++){
			int x = points.get(i).x;
			int y = points.get(i).y;
			
			g.fillRect(x - KNOB_SIZE/2, y - KNOB_SIZE/2,
					       KNOB_SIZE  ,     KNOB_SIZE);
			
		}
		
	}
	
	
	public static final int KNOB_SIZE = 9;
//	// Change the model
//	shape.moveBy(dx, dy);
}

