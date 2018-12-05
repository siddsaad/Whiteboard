package Server;

import java.awt.Color;
import java.awt.Point;
import java.awt.Font;
import java.util.ArrayList;


public class DTextModel extends DShapeModel{
	
	private String fontName;
	private int fontStyle;
	private String text;
	
	
	public DTextModel(int x, int y, int width, int height, int id, int instrument, String text) {	
		super(x, y, width, height, id, instrument, "Text");	
		//System.out.println("Text Constructor Called: "+ id);
		
		if(id == -1){
			//text = "Die Chuck! DIE!!";
			//fontName = "Monospaced";
			fontName = "Fiolex Girls";
			this.text = text;
		}
		else{
			fontName = "Dialog";
			
			if(text.equalsIgnoreCase("")){
				this.text = "Hello";
			}
			else
				this.text = text;

		}
	}
	
	public DTextModel(){
		super();
		//System.out.println("Text Default Ctor: "+ getId());	
		fontName = "Dialog";
		text = "Hello";
	}		
	
	public void setFontNameAndStyle(String fontName, int fontStyle){
		this.fontName = fontName;
		this.fontStyle = fontStyle;
		
		triggerListeners();
	}
	
	public String getFontName(){
		return fontName;
	}
	public void setFontName(String fontName){
		this.fontName = fontName;
	}
	
	public int getFontStyle(){
		return fontStyle;
	}
	public void setFontStyle(int fontStyle){
		this.fontStyle = fontStyle;
	}

	public String getText(){
		return text;
	}
	public void setText(String text){
		this.text = text;
		
		triggerListeners();
	}
	

	
	
	public void mimic(DShapeModel other){
		DTextModel otherText = (DTextModel) other;
		
		this.setX(other.getX());
		this.setY(other.getY()); 
		this.setWidth(other.getWidth());
		this.setHeight(other.getHeight());
		this.setColor(other.getColor());
		
		this.setText(otherText.getText());
		this.setFontName(otherText.getFontName());
		this.setFontStyle(otherText.getFontStyle());
	}
	
}
