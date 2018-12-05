package Client;

public class DRectModel extends DShapeModel{
	
	
	public DRectModel(int x, int y, int width, int height, int id, int instrument){
		super(x, y, width, height, id, instrument, "Rect");
		//System.out.println("Rect Constructor Called: "+ id);	
	}
	
	public DRectModel(){
		super();	
		//System.out.println("Rect Default Ctor: "+ getId());	
	}		
		

}