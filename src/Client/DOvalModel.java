package Client;

public class DOvalModel extends DShapeModel{
	
	public DOvalModel(int x, int y, int width, int height, int id, int instrument){
		super(x, y, width, height, id, instrument, "Oval");
		//System.out.println("Oval Constructor Called: "+ id);
	}		
	
	public DOvalModel(){
		super();
		//System.out.println("Oval Default Ctor: "+ getId());	
	}	
	

}