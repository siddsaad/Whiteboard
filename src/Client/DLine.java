package Client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;


public class DLine extends DShape{
	
	DLine(DLineModel lineModel, CanvasPanel canvas){
		super(lineModel, canvas);
	}
	
	public ArrayList<Point> getKnobs(){
		
		ArrayList<Point> points = new ArrayList<Point>();
		
		points.add(getModel().getP1());
		points.add(getModel().getP2());
		
		return points;
	}
	
	public DLineModel getModel(){
		return (DLineModel)super.getModel();
	}
	
	public Rectangle getBounds(){
		
		int x = Math.min(getModel().getP1().x, getModel().getP2().x);
		int y = Math.min(getModel().getP1().y, getModel().getP2().y);
		int width = Math.max(getModel().getP1().x, getModel().getP2().x) - x;
		int height = Math.max(getModel().getP1().y, getModel().getP2().y) - y;
		
		
		return (new Rectangle(x, y, width, height));
	}
	
	public void move(int dx, int dy){
		getModel().moveBy(dx, dy);
	}
	
	public void resize(Rectangle rect){
		getModel().resize(rect);
	}
	
	public Rectangle findNewBoundary(Point movingKnob, Point anchorKnob){
		getModel().setP1(new Point(movingKnob));
		getModel().setP2(new Point(anchorKnob));
		return super.findNewBoundary(movingKnob, anchorKnob);
	}
	
	public void modelChanged(DShapeModel model){		
		if(getCanvas().parentFrame.getIsServer())
			getCanvas().networkController.doSend("change", getModel());
		// if this is the client application
		else if(getCanvas().parentFrame.getIsClient()){
			getCanvas().oscController.sendOscMsg("change", getModel());
			getCanvas().repaint();
			return;
		}
		// if you are just running it on this machine
		else
			getCanvas().oscController.sendOscMsg("change", getModel());
		
		getCanvas().repaint();
	}
	
	public void drawShape(Graphics g){
		Point p1 = getModel().getP1();
		Point p2 = getModel().getP2();
		
		g.setColor(getModel().getColor());
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
		
		// Only Draw Selected version if it isn't a client
		//if(getIsSelected() && !getCanvas().getParentFrame().getIsClient())
		if(getIsSelected())
			drawSelected(g);
	}
	
	public void drawSelected(Graphics g){
		g.setColor(Color.BLACK);
		
		ArrayList<Point> points = new ArrayList<Point>();
		points.add(getModel().getP1());
		points.add(getModel().getP2());
		
		for(int i=0; i< points.size(); i++){
			int x = points.get(i).x;
			int y = points.get(i).y;
			
			g.fillRect(x - KNOB_SIZE/2, y - KNOB_SIZE/2,
					       KNOB_SIZE  ,     KNOB_SIZE);
			
		}
		
	}
	
}


