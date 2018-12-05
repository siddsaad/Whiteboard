package Server;

import java.awt.*;

public class DLineModel extends DShapeModel{
	
	private Point p1;
	private Point p2;
	
	
	public DLineModel(int x, int y, int width, int height, int id, int instrument){
		super(x, y, width, height, id, instrument, "Line");
		//System.out.println("Line Constructor Called: "+ id);
		
		p1 = new Point(x,y);
		p2 = new Point(x+width, y+height);
	}
	
	public DLineModel(){
		super();
		//System.out.println("Line Default Ctor: "+ getId());	
	}		
	
	public Point getP1(){
		return p1;
	}
	public void setP1(Point p){
		this.p1 = p;
	}
	
	public Point getP2(){
		return p2;
	}
	public void setP2(Point p){
		this.p2 = p;
	}
	
	
	public void moveBy(int dx, int dy){
		p1.x += dx;
		p1.y += dy;
		
		p2.x += dx;
		p2.y += dy;
		
		super.moveBy(dx, dy);
		triggerListeners();
	}
	
	public void mimic(DShapeModel other){
		DLineModel otherLine = (DLineModel) other;
		
		this.setX(other.getX());
		this.setY(other.getY()); 
		this.setWidth(other.getWidth());
		this.setHeight(other.getHeight());
		this.setColor(other.getColor());
		
		this.setP1(otherLine.getP1());
		this.setP2(otherLine.getP2());
	}
	
}
