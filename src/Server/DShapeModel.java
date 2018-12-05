package Server;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.*;

public class DShapeModel {

	private int x;
	private int y;
	private int width;
	private int height;
	private int id;
	private int instrument;
	private Color color;
	private String type;
	private ArrayList<ModelListener> listeners;
		
	public DShapeModel(int x, int y, int width, int height, int id, int instrument, String type) {			
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.id = id;
		this.instrument = instrument;
		if(id == -1)
			color = Color.RED;
		else
			color = Color.GRAY;
		listeners = new ArrayList<ModelListener>();
		this.type = type;
		//System.out.println("Shape Constructor called: "+ getId());	
		
	}
	
	public DShapeModel() {
		x = 0;
		y = 0;
		width = 0;
		height = 0;
		color = Color.GRAY;
		instrument = 0;
		listeners = new ArrayList<ModelListener>();
		//System.out.println("Shape Default Ctor: "+ getId());
	}
	public int getInstrument(){
		return instrument;
	}
	public void setInstrument(int instrument){
		this.instrument = instrument;
	}
	
	// standard getters/setters for x/y/color
	public Color getColor() {
		return color;
	}
	public void setColor(Color color) {
		this.color = color;
		
		triggerListeners();
	}
	
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
		
		triggerListeners();
	}
	
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
		
		triggerListeners();
	}
	
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
		
		triggerListeners();
	}
	
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
		
		triggerListeners();
	}
	
	public int getId(){
		return id;
	}
	public void setId(int id){
		this.id = id;
		
		triggerListeners();
	}
	public String getType(){
		return type;
	}
	public void setType(String type){
		this.type = type;
		
		triggerListeners();
	}
	
	
	// Convenience setters for clients
	
	// Moves x,y both the given dx,dy
	public void moveBy(int dx, int dy) {
		x += dx;
		y += dy;
		
		triggerListeners();
	}
	
	// Moves x,y both the given dx,dy
	public void resize(Rectangle rect) {
		x = rect.x;
		y = rect.y;
		width = rect.width;
		height = rect.height;
		
		triggerListeners();
	}
	
	// Sets both x and y
	public void setXY(int x, int y) {
		this.x = x;
		this.y = y;
		
		triggerListeners();
	}
	
	public void scaleWidthHeight(int dx, int dy){
		width += dx;
		height += dy;
		
		triggerListeners();
	}
	
	
	public void setWidthHeight(int width, int height){
		this.width = width;
		this.height = height;
		
		triggerListeners();
	}
	
	public void mimic(DShapeModel other){
		this.setX(other.getX());
		this.setY(other.getY()); 
		this.setWidth(other.getWidth());
		this.setHeight(other.getHeight());
		this.setColor(other.getColor());
	}
	
	
	public List<Integer> getShapeModelAsList(){
		Integer[] dimensions = {x,y,width,height};
		return (new ArrayList<Integer>(Arrays.asList(dimensions)));
	}
	
	public void addListener(ModelListener model){
		listeners.add(model);
	}
	
	public void triggerListeners(){
		if( (listeners.size() > 0) && (listeners != null) ){
			for(ModelListener listener : listeners)
				listener.modelChanged(this);
		}
	}
	
}
