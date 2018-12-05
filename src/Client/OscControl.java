package Client;

import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import com.illposed.osc.*;
import javax.swing.JOptionPane;

import javax.swing.JLabel;



public class OscControl {

	private WhiteboardFrame parentFrame; 
	private CanvasPanel canvas;
	
	private OSCPortOut sender;
	
	// List of object streams to which we send data
	private java.util.List<ObjectOutputStream> outputs;
	
	
	public OscControl(WhiteboardFrame parentFrame, 
							   CanvasPanel canvas){
		
		this.parentFrame = parentFrame;
		this.canvas = canvas;		
		
		try {
			sender = new OSCPortOut(InetAddress.getByName("localhost"), 8002);
		}
		catch(Exception e) {
			showError("Couldn't set address");
		}

		//outputs  = new ArrayList<ObjectOutputStream>();
	}
	//OSC Message
	//("/tajmusic", args)

	//(action, type, id, x, y, width, height, color_r, color_g, color_b)
	// Integer[10];
		//action
	//	    "add" = 0;
	//	    "change" = 1;
	//	    "remove" = 2;
	//		"kill" = 3;
		//
		//type
	//	     "square" = 0;
	//	     "oval"  = 1;
	//	     "line"  = 2;
	//	     "text" = 3;
	//
	//
	//id = int;
	//x,y,width, height  = int;
	//color_r, color_g, color_b = int;	
	public void sendOscMsg(String action, DShapeModel model){
		
		Object args[] = new Object[11];
		// Integer args[] = new Integer[10];
		
		//ACTION    add=0   change=1     remove=2    kill=3
		args[0] = getAction(action);
		
		//TYPE      rect=0   oval=1   line=2    text=3
		args[1] = getShapeType(model.getType());
		
		//ID
		args[2] = model.getId();
		
		//BOUNDS: x, y, width, height,
		args[3] = model.getX();
		args[4] = model.getY();
		args[5] = model.getWidth();
		args[6] = model.getHeight();
			
		//COLOR:  color_r, color_g, color_b
		args[7] = model.getColor().getRed();
		args[8] = model.getColor().getGreen();
		args[9] = model.getColor().getBlue();
		
		//INSTRUMENT:  AAAA = 0;   AAAA = 1;   AAAA = 2;    AAAA = 3;  
		args[10]= model.getInstrument();
		
		OSCMessage msg = new OSCMessage("/tajmusic", args);
		
		try {
			sender.send(msg);
		}
		catch (Exception e) {
			 showError("Couldn't send");
		}
	}

	
	//ACTION    add=0   change=1     remove=2    kill=3
	public Integer getAction(String action){
		
		if(action.equals("add"))
			return new Integer(0);
		else if(action.equals("change"))
			return new Integer(1);
		else if(action.equals("remove"))
			return new Integer(2);
		else if(action.equals("kill"))
			return new Integer(3);
		else{
			System.err.println("Action not Recognized");
			return null;
		}
	}
	
	//TYPE      rect=0   oval=1   line=2    text=3
	public Integer getShapeType(String shapeType){
		
		if(shapeType.equalsIgnoreCase("rect"))
			return new Integer(0);
		else if(shapeType.equalsIgnoreCase("oval"))
			return new Integer(1);
		else if(shapeType.equalsIgnoreCase("line"))
			return new Integer(2);
		else if(shapeType.equalsIgnoreCase("text"))
			return new Integer(3);
		else{
			System.err.println("Shape-Type not Recognized");
			return null;
		}
	}
	
	
	
	// create a showError method
	protected void showError(String anErrorMessage) {
		// tell the JOptionPane to showMessageDialog
		JOptionPane.showMessageDialog(parentFrame, anErrorMessage);
	}
	
}
