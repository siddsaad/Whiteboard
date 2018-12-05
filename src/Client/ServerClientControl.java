package Client;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;

import javax.swing.*;

import java.util.*;
import java.util.List;
import java.io.*;
import java.net.*;


public class ServerClientControl implements ModelListener{

	private WhiteboardFrame parentFrame; 
	private CanvasPanel canvas;
	private JLabel status;
	// The are thread inner classes to handle
	// the networking.
	private ClientHandler clientHandler;
	private ServerAccepter serverAccepter;
	
	
	// List of object streams to which we send data
	private java.util.List<ObjectOutputStream> outputs;
	
	
	public ServerClientControl(WhiteboardFrame parentFrame, 
							   CanvasPanel canvas,
							   JLabel status){
		
		this.parentFrame = parentFrame;
		this.canvas = canvas;
		this.status = status;
		
		outputs  = new ArrayList<ObjectOutputStream>();
	}
	
	// listens to All DShapeModels for trigger to send data
	public void modelChanged(DShapeModel model){
		//Do stuff here
		
		
	}
	
	
// BEGINNING OF ALL SERVER RELATED TASKS
	/**
	 writes out the Model that changed to all the clients
	 input is a string describing the action to perform
	 and the modelToSend that is the model changed
	 */
	public synchronized void doSend(String actionToPerform, DShapeModel modelToSend) {

		//System.out.println("Server:  sent  " + actionToPerform);
		
		// Convert the message object into an xml string.
		OutputStream memStream = new ByteArrayOutputStream();
		XMLEncoder xmlOut = new XMLEncoder(memStream);
		
		// this line is calling the constructor and changing the ID
		xmlOut.writeObject(modelToSend);
		//System.out.println("Server:   debug");
		xmlOut.close();
		
		String xmlString = memStream.toString();	
		
		
		// Now write that xml string to all the clients.
		Iterator<ObjectOutputStream> it = outputs.iterator();
		
		while (it.hasNext()) {
			ObjectOutputStream out = it.next();
			try {
				out.writeObject(actionToPerform);
				out.writeObject(xmlString);
				out.flush();
			}
			catch (Exception ex) {
				ex.printStackTrace();
				it.remove();
				// Cute use of iterator and exceptions --
				// drop that socket from list if have probs with it
			}
		}	
	}
	
	
	
	// Adds an object stream to the list of outputs
	// (this and sendToOutputs() are synchronized to avoid conflicts)
	public synchronized void addOutput(ObjectOutputStream out) {
		outputs.add(out);
	}
	
	// Server thread accepts incoming client connections
	class ServerAccepter extends Thread {
		private int port;
		ServerAccepter(int port) {
			this.port = port;
		}

		public void run() {
			try {
				ServerSocket serverSocket = new ServerSocket(port);
				while (true) {
					Socket toClient = null;
					// this blocks, waiting for a Socket to the client
					toClient = serverSocket.accept();
					System.out.println("server: got client");

					// Get an output stream to the client, and add it to
					// the list of outputs
					// (our server only uses the output stream of the connection)
					addOutput(new ObjectOutputStream(toClient.getOutputStream()));
				}  

			} catch (IOException ex) {
				ex.printStackTrace();
				showError("Socket Connection Denied");
			}
		}
	}

	
	
	// Starts the sever accepter to catch incoming client connections.
	// Wired to Server button.
	public int doServer() {
		String result = JOptionPane.showInputDialog("Run server on port", "8001");
		int resultInt = Integer.parseInt(result.trim());
		
		if (result!=null && resultInt > 1024) {
			status.setText("   Server Mode");
			System.out.println("server: start");
			
			// Thread inner Class that stays open for business
			serverAccepter = new ServerAccepter(resultInt);
			serverAccepter.start();
			return 1;
		}
		else
			return -1;
	}

	public ServerAccepter getServerAccepter(){
		return this.serverAccepter;
	}
// END OF ALL SERVER RELATED TASKS	
	
	
// BEGINNING OF ALL CLIENT RELATED TASKS


	
	
	private synchronized void handleEvent(String verb, DShapeModel recievedModel){
		// do stuff based on the verb command
		if(verb.equalsIgnoreCase("add")){
			canvas.addShape(recievedModel);
			return;
		}
		else if(verb.equalsIgnoreCase("remove")){
			List<DShape> shapes = canvas.getShapesList();
			int index = 0;
			
			for(DShape shape : shapes){
				
				if( shape.getModel().getId() == recievedModel.getId() ){
					canvas.clientRemoveShape(index);
					return;
				}
				
				index++;
			}						
		}
		else if(verb.equalsIgnoreCase("front")){
			List<DShape> shapes = canvas.getShapesList();
			int index = 0;
			
			for(DShape shape : shapes){
				
				if( shape.getModel().getId() == recievedModel.getId() ){
					canvas.moveToFront(shape, index);
					return;
				}
				
				index++;
			}		
		}
		else if(verb.equalsIgnoreCase("back")){
			List<DShape> shapes = canvas.getShapesList();
			int index = 0;
			
			for(DShape shape : shapes){
				
				if( shape.getModel().getId() == recievedModel.getId() ){
					canvas.moveToBack(shape, index);
					return;
				}
				
				index++;
			}
		}
		else if(verb.equalsIgnoreCase("change")){
			List<DShape> shapes = canvas.getShapesList();
			int index = 0;
			
			for(DShape shape : shapes){
				
				if( shape.getModel().getId() == recievedModel.getId() ){
					shape.getModel().mimic(recievedModel);
					return;
				}
				
				index++;
			}
		}
		
		System.err.println("Shape not Found!!\n" +
						   "This Shouln't Ever Happen!");
		
	}
	
	
	
	// Client runs this to handle incoming messages
	// (our client only uses the inputstream of the connection)
	private class ClientHandler extends Thread {
		private String name;
		private int port;

		ClientHandler(String name, int port) {
			this.name = name;
			this.port = port;
		}

		// Connect to the server, loop getting messages
		public void run() {
			try {
				// make connection to the server name/port
				Socket toServer = new Socket(name, port);

				// get input stream to read from server and wrap in object input stream
				ObjectInputStream in = new ObjectInputStream(toServer.getInputStream());
				System.out.println("client: connected!");
				showConnected("client: connected!");
				
				// we could do this if we wanted to write to server in addition
				// to reading
				// out = new ObjectOutputStream(toServer.getOutputStream());

				while (true) {
					// Get the xml string, decode to a Message object.
					// Blocks in readObject(), waiting for server to send something.
					String verb = (String) in.readObject();
					
					String xmlString = (String) in.readObject();
					XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(xmlString.getBytes()));
					Server.DShapeModel tempRecievedModel = (Server.DShapeModel) decoder.readObject();
					DShapeModel recievedModel =null;
                                        if(tempRecievedModel instanceof Server.DRectModel){
                                            recievedModel = new Client.DRectModel(tempRecievedModel.getX()
                                            ,tempRecievedModel.getY(),tempRecievedModel.getWidth(),tempRecievedModel.getHeight()
                                            ,tempRecievedModel.getId(),tempRecievedModel.getInstrument());
                                            recievedModel.setColor(tempRecievedModel.getColor());
                                        }
                                        else if(tempRecievedModel instanceof Server.DOvalModel){
                                            recievedModel = new Client.DOvalModel(tempRecievedModel.getX()
                                            ,tempRecievedModel.getY(),tempRecievedModel.getWidth(),tempRecievedModel.getHeight()
                                            ,tempRecievedModel.getId(),tempRecievedModel.getInstrument());
                                            recievedModel.setColor(tempRecievedModel.getColor());
                                        }
                                        else if(tempRecievedModel instanceof Server.DLineModel){
                                            recievedModel = new Client.DLineModel(tempRecievedModel.getX()
                                            ,tempRecievedModel.getY(),tempRecievedModel.getWidth(),tempRecievedModel.getHeight()
                                            ,tempRecievedModel.getId(),tempRecievedModel.getInstrument());
                                            recievedModel.setColor(tempRecievedModel.getColor());
                                        }
                                        else if(tempRecievedModel instanceof Server.DTextModel){
                                            recievedModel = new Client.DTextModel(tempRecievedModel.getX()
                                            ,tempRecievedModel.getY(),tempRecievedModel.getWidth(),tempRecievedModel.getHeight()
                                            ,tempRecievedModel.getId(),tempRecievedModel.getInstrument(),((Server.DTextModel)tempRecievedModel).getText());
                                            recievedModel.setColor(tempRecievedModel.getColor());
                                        }
                                        
					//System.out.println("client: read    " + verb);
					//System.out.println("client: Model    " + recievedModel.getType() + "  " + recievedModel.getId());
					
					handleEvent(verb, recievedModel);
				}	
			}
			catch (Exception ex) { // IOException and ClassNotFoundException
				ex.printStackTrace();
				if(!parentFrame.getIsForServerUseOnly()){
					parentFrame.toggleServerClientControls(true);
					status.setText("                       ");
				}
				else
					parentFrame.resetClientButton(true);
				
				showError("Not Connected!!  Server Missing");
			}
			// Could null out client ptr.
			// Note that exception breaks out of the while loop,
			// thus ending this thread run.
		}
	}

	
	
	
	
	
	// Runs a client handler to connect to a server.
	// Wired to Client button.
	public int doClient() {
		//String result = JOptionPane.showInputDialog("Connect to host:port", "10.1.1.228:8001");
		String result = JOptionPane.showInputDialog("Connect to host:port", "127.0.0.1:8001");
		
		if (result!=null) {
			status.setText("   Client Mode");
			String[] parts = result.split(":");
			System.out.println("client: start  IPaddr: "+ parts[0].trim()+"    Port: "+ Integer.parseInt(parts[1].trim()));
			clientHandler = new ClientHandler(parts[0].trim(), Integer.parseInt(parts[1].trim()));
			clientHandler.start();
			return 1;
		}
		else
			return -1;
	}
	
	
	// create a showError method
	protected void showError(String anErrorMessage) {
		// tell the JOptionPane to showMessageDialog
		JOptionPane.showMessageDialog(parentFrame, anErrorMessage);
	}
	
	// create a showError method
	protected void showConnected(String connectedMessage) {
		// tell the JOptionPane to showMessageDialog
		JOptionPane.showMessageDialog(parentFrame, connectedMessage);
	}
}

//END OF ALL CLIENT RELATED TASKS


