package Client;

import java.awt.Graphics;


public class DOval extends DShape{

	DOval(DOvalModel ovalModel, CanvasPanel canvas){
		super(ovalModel, canvas);
	}
	
	public void drawShape(Graphics g){
		int x = getModel().getX();
		int y = getModel().getY();
		int width = getModel().getWidth();
		int height = getModel().getHeight();
		
		g.setColor(getModel().getColor());
		g.fillOval(x, y, width, height);
		
		// Only Draw Selected version if it isn't a client
		//if(getIsSelected() && !getCanvas().getParentFrame().getIsClient())
		if(getIsSelected())
			drawSelected(g);
	}
}
	