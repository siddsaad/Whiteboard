package Client;

import java.util.*;

import javax.swing.table.AbstractTableModel;


public class TableModel extends AbstractTableModel implements ModelListener{
	WhiteboardFrame parentFrame;
	
	private List<String> colNames;	// defines the number of cols
	
	public TableModel(String[] colNames, WhiteboardFrame parentFrame) {
		this.colNames = new ArrayList<String>(Arrays.asList(colNames));
		this.parentFrame = parentFrame;
	}
	
	
	// Returns the name of each col, numbered 0..columns-1
	public String getColumnName(int col) {
		return colNames.get(col);
	}
	
	
	public int getColumnCount() {
		return colNames.size();
	}

	public int getRowCount() {
		return parentFrame.getCanvasPanel().getShapesList().size();
	}

	
	// Returns the data for each cell, identified by its
	// row, col index.
	public Object getValueAt(int row, int col) {
		
		List<DShape> shapes = parentFrame.getCanvasPanel().getShapesList();
		
		DShape shapeToUse = shapes.get(shapes.size()-1-row);
		
		Object result = shapeToUse.getModel().getShapeModelAsList().get(col);
		
		// _apparently_ it's ok to return null for a "blank" cell
		return(result);
	}
		
	public int addRow(int row) {
		fireTableRowsInserted(row, row);
		return(row);
	}
	
	public int removeRow(int row) {
		fireTableRowsDeleted(row, row);
		return(row);
	}
	
	
	public void modelChanged(DShapeModel model){
		int rowNum = -1;
		
		List<DShape> shapes = parentFrame.getCanvasPanel().getShapesList();
		DShape shape;
		
		for(int i=0; i<shapes.size(); i++){
			shape = shapes.get(i);
			if(shape.getModel().equals(model))
				rowNum = shapes.size()-1-i;
		}
		
		if(0 <= rowNum)
			fireTableRowsUpdated(rowNum, rowNum);
	}

}

