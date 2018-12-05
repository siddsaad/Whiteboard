package Server;

import java.awt.Graphics;


public class DRect extends DShape{

	DRect(DRectModel rectModel, Canvas canvas){
		super(rectModel, canvas);
	}
	
	public void drawShape(Graphics g){
		int x = getModel().getX();
		int y = getModel().getY();
		int width = getModel().getWidth();
		int height = getModel().getHeight();
		
		g.setColor(getModel().getColor());
		g.fillRect(x, y, width, height);
		
		// Only Draw Selected version if it isn't a client
		//if(getIsSelected() && !getCanvas().getParentFrame().getIsClient())
		if(getIsSelected())
			drawSelected(g);
	}
	
}
