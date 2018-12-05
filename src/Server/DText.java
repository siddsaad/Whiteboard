package Server;

import java.awt.*;

public class DText extends DShape{

	DText(DTextModel textModel, Canvas canvas){
		super(textModel, canvas);
	}
	
	public DTextModel getModel(){
		return (DTextModel)super.getModel();
	}
	
	public void setFontNameAndStyle(String fontName, int style){
		getModel().setFontNameAndStyle(fontName, style);
	}
	
	public void setText(String text){
		getModel().setText(text);
		
		
	}
	
	public void setSelected(boolean selected){
		if(!getCanvas().getParentFrame().getIsClient()){
			getCanvas().parentFrame.toggleTextControls(selected);
			super.setSelected(selected);
		}
		else
			super.setSelected(selected);
	}
	
	public Object[] computeFont(Graphics g, int height){
		int fontHeight;
		double fontSize = 1.0;
		
		Font f = new Font(getModel().getFontName(), getModel().getFontStyle(), (int)fontSize);
		FontMetrics metrics = g.getFontMetrics(f);
		fontHeight = metrics.getHeight();
		
		while(fontHeight <= height){
			fontSize = (fontSize*1.10)+1;
			f = new Font(getModel().getFontName(), getModel().getFontStyle(), (int)fontSize);
			metrics = g.getFontMetrics(f);
			fontHeight = metrics.getHeight();
		}
		
		// Create an object with the font and font height
		return (new Object[] {f, fontHeight});
	}
	
	public void drawShape(Graphics g){
		int x = getModel().getX();
		int y = getModel().getY();
		//int width = getModel().getWidth();
		int height = getModel().getHeight();
		String text = getModel().getText();
		Shape clip;
		
		// Compute the Font and return a Font and Integer
		// in the Object[] array
		Object[] fontAndHeight = computeFont(g, height);
		
		// set the Font to the first argument in
		//  the Object[] array
		g.setFont((Font)fontAndHeight[0]);
	
		clip = g.getClip();
		g.setClip(clip.getBounds().createIntersection(getBounds()));
		
		// Set the color and draw the String
		// make sure the bottom of the string
		// isn't cliped by scaling the y value
		// that it draws at by the font size
		int scaler = (Integer)fontAndHeight[1];
		//System.out.println("Scaler  " + scaler);
		g.setColor(getModel().getColor());
		g.drawString(text, x, y+height-(scaler/4));
		
		g.setClip(clip);
		
		// Only Draw Selected version if it isn't a client
		//if(getIsSelected() && !getCanvas().getParentFrame().getIsClient())
		if(getIsSelected())
			drawSelected(g);
	}

}
