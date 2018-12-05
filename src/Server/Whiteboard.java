package Server;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.*;
import java.io.File;
import java.lang.*;
import java.awt.GraphicsEnvironment;
import java.awt.Font;

import java.util.*;
import java.io.*;
import java.net.*;


import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.JColorChooser;


public class Whiteboard extends JFrame {

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) { }
		
		File fileToOpen = null;
		
		// Notice command line arg filename
		if (args.length != 0) {
			fileToOpen = new File(args[0]);
		}
		
		new Whiteboard(fileToOpen);
        
	}
	
	private int nextId;
	
	private boolean forServerUseOnly = false;
	private int serverControlPannelWidth = 400;
	private int clientControlPannelWidth = 250;
	private int killChuckButtonDistance = 30;
	
	private Canvas canvas;
	private File saveFile; // the last place we saved, or null
	private boolean isServer;
	private boolean isClient;
	
	int strutSize = 20;
	
	private JButton serverButton;
	private JButton clientButton;
	private JLabel  status;
	int instrument = 0;
	
	public JComboBox chooseFontComboBox;
	public DefaultTableModel model;
	public JTextField setTextField;
	public TableModel myModel;
	
	
	// Parameter Text Field Labels
	private JLabel xLabel;
	private JLabel yLabel;
	private JLabel widthLabel;
	private JLabel heightLabel;
	private JLabel textLabel;
	private JLabel instrumentLabel;
	
	// Parameter Text Fields
	public JTextField xTextField;
	public JTextField yTextField;
	public JTextField widthTextField;
	public JTextField heightTextField;
	public JTextField textTextField;
	public JTextField instrumentTextField;
	
	
	private JPanel saveOpenPanel;
	private JPanel frontBackRemovePanel;
	private JPanel setColorPanel;
	private JPanel setParametersPanel;
	private JPanel drawControlPanel;
	private JPanel textFieldPanel;
	
	
	
	public Whiteboard(File file) {
		isServer = false;
		isClient = false;
		nextId = 0;
	if(forServerUseOnly == false){
			/*
			 Create the content
			*/
		 // DRAWING CONTROLS
		 
			// Panel to hold drawing controls
			drawControlPanel = new JPanel();
			drawControlPanel.setLayout(new BoxLayout(drawControlPanel, BoxLayout.X_AXIS));
			
			// Label that says "Add" next to buttons for drawing
			JLabel addLabel = new JLabel("Add:  ");
			drawControlPanel.add(addLabel);
			
			// Button to add Rectangle
			JButton addRectButton = new JButton("Rect");
			drawControlPanel.add(addRectButton);
			addRectButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){					
					//perform addRect event here
					Integer params[] = getShapeParams();
					
					DRectModel rect = new DRectModel(params[0], params[1], 
													 params[2], params[3], 
													 nextId   , params[4]);
					canvas.addShape(rect);
					nextId++;
					//System.out.println(nextId);
				}
			});
			
			// Button to add Oval
			JButton addOvalButton = new JButton("Oval");
			drawControlPanel.add(addOvalButton);
			addOvalButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					//perform addOval event here
					Integer params[] = getShapeParams();
					
					DOvalModel oval = new DOvalModel(params[0], params[1], 
													 params[2], params[3], 
													 nextId   , params[4]);
					canvas.addShape(oval);
					nextId++;
				}
			});
			
			// Button to add Line
			JButton addLineButton = new JButton("Line");
			drawControlPanel.add(addLineButton);
			addLineButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					//perform addLine event here
					Integer params[] = getShapeParams();
					
					DLineModel line = new DLineModel(params[0], params[1], 
													 params[2], params[3], 
													 nextId   , params[4]);
					canvas.addShape(line);
					nextId++;
				}
			});
			
			// Button to add Text
			JButton addTextButton = new JButton("Text");
			drawControlPanel.add(addTextButton);
			addTextButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					//perform addText event here
					Integer params[] = getShapeParams();
					String displayText = textTextField.getText();
					
					DTextModel text = new DTextModel(params[0], params[1], 
													 params[2], params[3], 
													 nextId   , params[4],
													 displayText			);
					canvas.addShape(text);
					nextId++;
				}
			});
		
		//END DRAWING CONTROLS
			
		// SET SHAPE PARAMETERS PANEL
			
			// Panel to hold set Color Button
			setParametersPanel = new JPanel();
			setParametersPanel.setLayout(new BoxLayout(setParametersPanel, BoxLayout.X_AXIS));
			
			//Create labels for this panel
			xLabel =	  new JLabel("   X:  ");
			yLabel =      new JLabel("   Y:  ");
			widthLabel =  new JLabel("   Width:  ");
			heightLabel = new JLabel("   Height:  ");

			
			
			// Add the x text Field
			xTextField = new JTextField(String.valueOf("10"));
			xTextField.setMaximumSize(new Dimension(serverControlPannelWidth/8, 50));
			setParametersPanel.add(xLabel);
			setParametersPanel.add(xTextField);
			
			// Add the y text Field
			yTextField = new JTextField(String.valueOf("10"));
			yTextField.setMaximumSize(new Dimension(serverControlPannelWidth/8, 50));
			setParametersPanel.add(yLabel);
			setParametersPanel.add(yTextField);
			
			// Add the width text Field
			widthTextField = new JTextField(String.valueOf("20"));
			widthTextField.setMaximumSize(new Dimension(serverControlPannelWidth/8, 50));
			setParametersPanel.add(widthLabel);
			setParametersPanel.add(widthTextField);
			
			// Add the width text Field
			heightTextField = new JTextField(String.valueOf("20"));
			heightTextField.setMaximumSize(new Dimension(serverControlPannelWidth/8, 50));
			setParametersPanel.add(heightLabel);
			setParametersPanel.add(heightTextField);
			
			
		// END SET SHAPE PARAMETERS PANEL				
			
			
		 // SET COLOR PANEL
			
			// Panel to hold set Color Button
			setColorPanel = new JPanel();
			setColorPanel.setLayout(new BoxLayout(setColorPanel, BoxLayout.X_AXIS));
			
			// Button to add Text
			JButton setColorButton = new JButton("Set Color");
			setColorPanel.add(setColorButton);
			setColorButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					//perform addOval event here
					//ColorSelectionModel colorSelector = new DefaultColorSelectionModel();
					//JColorChooser colorChooser = new JColorChooser();
					DShape shapeToColor = canvas.getSelectedShape();
					if(shapeToColor != null){
						Color currentColor = shapeToColor.getModel().getColor();
						
						Color newColor = JColorChooser.showDialog(
			                     Whiteboard.this,
			                     "Choose Shape Color",
			                     currentColor);
						
						shapeToColor.getModel().setColor(newColor);
						//canvas.repaint();
					}
				}
			});
			
			//Create labels for this panel
			textLabel = 	  new JLabel("   Text   ");
			instrumentLabel = new JLabel("   Inst   ");
			
			// Add the width text Field
			textTextField = new JTextField(String.valueOf(""));
			textTextField.setMaximumSize(new Dimension(serverControlPannelWidth/3, 50));
			setColorPanel.add(textLabel);
			setColorPanel.add(textTextField);
			
			// Add the width text Field
			instrumentTextField = new JTextField(String.valueOf("0"));
			instrumentTextField.setMaximumSize(new Dimension(serverControlPannelWidth/8, 50));
			setColorPanel.add(instrumentLabel);
			setColorPanel.add(instrumentTextField);
	
		// END SET COLOR PANEL	
		
			
		// TEXT FIELD PANEL	
			// Panel to hold text controls
			textFieldPanel = new JPanel();
			textFieldPanel.setLayout(new BoxLayout(textFieldPanel, BoxLayout.X_AXIS));
			
			
			// Add the textField
			setTextField = new JTextField(String.valueOf(""));
			setTextField.setMaximumSize(new Dimension(serverControlPannelWidth/2, 50));
			textFieldPanel.add(setTextField);
			setTextField.getDocument().addDocumentListener(new FieldListener());

			
			// Add listener to textField
			//textFieldPanel.getDocument().addDocumentListener(new FieldListener());
			
			// Add the chooseFont JComboBox and an action listener
			//String[] names = {"Serif", "SansSerif", "Monospaced", "Dialog", 
				//			  "DialogInput","Times New Roman", "Ariel"      };
			GraphicsEnvironment graphics = GraphicsEnvironment.getLocalGraphicsEnvironment();
			
			String[] names = graphics.getAvailableFontFamilyNames();
			chooseFontComboBox = new JComboBox(names);
			chooseFontComboBox.setMaximumSize(new Dimension( serverControlPannelWidth/2, 50));
			textFieldPanel.add(chooseFontComboBox);
			chooseFontComboBox.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					DShape shape = canvas.getSelectedShape();
					
					if( (shape != null) && (shape instanceof DText)){
						
						DText textShape = (DText)shape;
						
						String fontName = (String)chooseFontComboBox.getSelectedItem();
						
						textShape.getModel().setFontNameAndStyle(fontName, 1);
					}			
					
				}
			});
			
			toggleTextControls(false);
			
		// END TEXT FIELD PANEL	
			
			
		// MOVE FRONT, BACK AND REMOVE SHAPE PANEL
			// Panel to hold set Color Button
			frontBackRemovePanel = new JPanel();
			frontBackRemovePanel.setLayout(new BoxLayout(frontBackRemovePanel, BoxLayout.X_AXIS));
			
			// Button to move selected to front
			JButton moveToFrontButton = new JButton("Move To Front");
			frontBackRemovePanel.add(moveToFrontButton);
			moveToFrontButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					//perform moveToFront
					canvas.moveToFront(null, -2);
				}
			});
			
			// Button to move selected to back
			JButton moveToBackButton = new JButton("Move To Back");
			frontBackRemovePanel.add(moveToBackButton);
			moveToBackButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					//perform moveToBack
					canvas.moveToBack();
				}
			});
			
			// Button to Remove selected
			JButton removeShapeButton = new JButton("Remove Shape");
			frontBackRemovePanel.add(removeShapeButton);
			removeShapeButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					//perform moveToFront
					canvas.deleteShape();
				}
			});
			
		// END MOVE FRONT, BACK AND REMOVE SHAPE PANEL
			
		// SAVE AND OPEN PANEL
			// Panel to hold set Color Button
			saveOpenPanel = new JPanel();
			saveOpenPanel.setLayout(new BoxLayout(saveOpenPanel, BoxLayout.X_AXIS));
			// Buttons for file save/open
			
			JButton button;
			button = new JButton("Save...");
			saveOpenPanel.add(button);
			button.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					doSave(false);
				}
			});
			
			button = new JButton("Open...");
			saveOpenPanel.add(button);
			button.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					doOpen();
				}
			});
			
	 		// Make a Save Image button
			JButton imageButton = new JButton("Save PNG Image");
			saveOpenPanel.add(imageButton);
			imageButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = createFileChooser();
					int status = chooser.showSaveDialog(Whiteboard.this);
					// depending on the os, it may help if the user
					// gives the file a name ending in .png
					if (status == JFileChooser.APPROVE_OPTION) {
						File dest = chooser.getSelectedFile();
						canvas.saveImage(dest);
					}
				}
			});
			
		// END SAVE AND OPEN PANEL
		}		
		
	// SERVER CLIENT BUTTONS PANEL
		// Panel to hold set Color Button
		JPanel serverClientPanel = new JPanel();
		serverClientPanel.setLayout(new BoxLayout(serverClientPanel, BoxLayout.X_AXIS));
		// Buttons for file save/open
		
		if(forServerUseOnly == false){
			serverButton = new JButton("Server Start");
			serverClientPanel.add(serverButton);
			serverButton.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setIsServer(true);
					setIsClient(false);
					if(canvas.launchServer() > 0)
						toggleServerClientControls(false);
				}
			});
		}
		
		clientButton = new JButton("Client Start");
		serverClientPanel.add(clientButton);
		clientButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setIsServer(false);
				setIsClient(true);
				int clientLaunched = canvas.launchClient();
				if(clientLaunched > 0 && !getIsForServerUseOnly()){
					toggleServerClientControls(false);
				}
				else if(clientLaunched > 0 && getIsForServerUseOnly())
					clientButton.setEnabled(false);
			}
		});
							 
		status = new JLabel("                       ");
		serverClientPanel.add(status);
		
		serverClientPanel.add(Box.createHorizontalStrut(killChuckButtonDistance));
		
		if(forServerUseOnly == false){
			JButton killChuckButton;
			killChuckButton = new JButton("Kill Chuck");
			serverClientPanel.add(killChuckButton);
			killChuckButton.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//doSomething();
					int instrument = 0;
					toggleServerClientControls(true);
					status.setText(" Chuck Dead!           ");
					String displayText = "Death => Chuck!";
					DTextModel text = new DTextModel(50, 50, 800, 90, -1, instrument, displayText);
					canvas.addShape(text);
				}
			});
		}

	// END SERVER CLIENT BUTTONS PANEL
		
		
		
		
	// TABLE PANEL	
		// Add a table Panel to hold the table model
		JPanel tablePanel = new JPanel();
		
		// create a table model to add information on objects
		myModel = new TableModel(new String[] { "X", "Y", "Width", "Height"}, this);
		model = new DefaultTableModel(new String[] { "X", "Y", "Width", "Height"}, 0);

		//readFile(file , model);
		
		// Create a tabel with the table model in it
		JTable table = new JTable(myModel);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);		
		
		// Add Scrollpane with Table in it
		JScrollPane scrollpane = new JScrollPane(table);
		if(forServerUseOnly == false)
			scrollpane.setPreferredSize(new Dimension(serverControlPannelWidth,500));
		else
			scrollpane.setPreferredSize(new Dimension(clientControlPannelWidth,500));
		tablePanel.add(scrollpane);
		
	// END TABLE PANEL
		
		
	// SET UP THE FRAME
		String frameName = "";
		if(forServerUseOnly == false)
			frameName = "Server";
		else if(forServerUseOnly == true)
			frameName = "Client";
		
		setTitle(frameName);
		
		canvas = new Canvas(500, 400, this, myModel, status);
		//if (file != null) canvas.open(file);
		add(canvas, BorderLayout.CENTER);
		
		JPanel viewBox = new JPanel();
		viewBox.setLayout(new BoxLayout(viewBox, BoxLayout.Y_AXIS));
		add(viewBox, BorderLayout.WEST);
		
		if(forServerUseOnly == false){
			viewBox.add(drawControlPanel);
			viewBox.add(Box.createVerticalStrut(strutSize));
			viewBox.add(setParametersPanel);
			viewBox.add(Box.createVerticalStrut(strutSize));
			viewBox.add(setColorPanel);
			viewBox.add(Box.createVerticalStrut(strutSize));
			viewBox.add(textFieldPanel);			
			viewBox.add(Box.createVerticalStrut(strutSize));
			viewBox.add(frontBackRemovePanel);
			viewBox.add(Box.createVerticalStrut(strutSize));
			viewBox.add(saveOpenPanel);
		}
		viewBox.add(Box.createVerticalStrut(strutSize));
		viewBox.add(serverClientPanel);
		viewBox.add(Box.createVerticalStrut(strutSize));
		viewBox.add(tablePanel);

		
		
		// alling everything to the left
		for(Component comp : viewBox.getComponents()){
			((JComponent)comp).setAlignmentX(Box.LEFT_ALIGNMENT);
		}
		if(forServerUseOnly == false)
			viewBox.setPreferredSize(new Dimension(serverControlPannelWidth, 400));
		else
			viewBox.setPreferredSize(new Dimension(clientControlPannelWidth, 400));
		
		
//		if(forServerUseOnly == false){
//			toggleTextControls(false);
//		}
		
		pack();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	public boolean getIsServer(){
		return isServer;
	}
	public void setIsServer(boolean isServer){
		this.isServer = isServer;
	}
	public boolean getIsClient(){
		return isClient;
	}
	public void setIsClient(boolean isClient){
		this.isClient = isClient;
	}
	
	public boolean getIsForServerUseOnly(){
		return forServerUseOnly;
	}
	
	public void toggleTextControls(boolean enabled){
		chooseFontComboBox.setEnabled(enabled);
		setTextField.setEnabled(enabled);
	}
	
	public void toggleServerClientControls(boolean enable){
		serverButton.setEnabled(enable);
		clientButton.setEnabled(enable);
	}
	public void resetClientButton(boolean enable){
		clientButton.setEnabled(enable);
		status.setText("                       ");
	}
	
	// This is used if you want to easily
	// disable all the controls except 
	// the client start button
	public void turnOffAuxillaryControls(boolean enable){
		chooseFontComboBox.setEnabled(enable);
		setTextField.setEnabled(enable);
		serverButton.setEnabled(enable);
	}
	
	public Canvas getCanvas(){
		return this.canvas;
	}
	
//	// Parameter Text Fields
//	public JTextField xTextField;
//	public JTextField yTextField;
//	public JTextField widthTextField;
//	public JTextField heightTextField;
//	public JTextField textTextField;
//	public JTextField instrumentTextField;
	
	
	public Integer[] getShapeParams(){
		Integer shapeParameters[] = new Integer[5];
		
		// Get X integer value
		if(!xTextField.getText().equalsIgnoreCase(""))
			shapeParameters[0] = Integer.parseInt(xTextField.getText());
		else
			shapeParameters[0] = 10;
	
		// Get Y integer value
		if(!yTextField.getText().equalsIgnoreCase(""))
			shapeParameters[1] = Integer.parseInt(yTextField.getText());
		else
			shapeParameters[1] = 10;
	
		// Get width integer value
		if(!widthTextField.getText().equalsIgnoreCase(""))
			shapeParameters[2] = Integer.parseInt(widthTextField.getText());
		else
			shapeParameters[2] = 20;
		
		// Get height integer value
		if(!heightTextField.getText().equalsIgnoreCase(""))
			shapeParameters[3] = Integer.parseInt(heightTextField.getText());
		else
			shapeParameters[3] = 20;

		// Get height integer value
		if(!instrumentTextField.getText().equalsIgnoreCase(""))
			shapeParameters[4] = Integer.parseInt(instrumentTextField.getText());
		else
			shapeParameters[4] = 0;
		
		return shapeParameters;
	}
	
	
	// Listener for changes in the text field
	private class FieldListener implements DocumentListener {
		/*
		 We get these three notifications on changes
		 in the text field.
		*/
		public void insertUpdate(DocumentEvent e) {
			sendToModel();
		}
		public void changedUpdate(DocumentEvent e) {
			sendToModel();
		}
		public void removeUpdate(DocumentEvent e) {
			sendToModel();
		}
		
		// In all cases, send the new text to the censorModel
		public void sendToModel() {
			DText textShape = (DText)canvas.getSelectedShape();
			textShape.getModel().setText(setTextField.getText());
		}
	}
	
	// Save (or saveAs) the panel
	public void doSave(boolean saveAs) {
		// prompt where to save if needed
		if (saveFile == null || saveAs) {
			JFileChooser chooser = createFileChooser();
			int status = chooser.showSaveDialog(this);
			if (status == JFileChooser.APPROVE_OPTION) {
				saveFile = chooser.getSelectedFile();
			}
			else return;  // i.e. cancel the whole operation
		}
		
		// Do the save, set the frame title
		canvas.save(saveFile);
		setTitle(saveFile.getName());
	}
	
	// Prompts for a file and reads it in
	// Asks to write out old contents first if dirty.
	public void doOpen() {
//		if (dotPanel.getDirty()) {
//			boolean ok = saveOk();
//			if (!ok) return;
//		}
//		
		JFileChooser chooser = createFileChooser();
		int status = chooser.showOpenDialog(this);
		if (status == JFileChooser.APPROVE_OPTION) {
			canvas.open(chooser.getSelectedFile());
			// now that file is the "current" file for the window
			saveFile = chooser.getSelectedFile();
			setTitle(saveFile.getName());
		}
	}
	
	
	// Creates a new JFileChooser, doing the boilerplate
	// to start it in the current directory.
	private JFileChooser createFileChooser() {
		JFileChooser chooser = new JFileChooser();
		try {
			// The "." stuff attempts to open in the "current"
			// directory.
			File dir = new File(new File(".").getCanonicalPath());
			chooser.setCurrentDirectory(dir);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return chooser;
	}
	
}
