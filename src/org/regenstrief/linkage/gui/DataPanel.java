package org.regenstrief.linkage.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.RecMatchConfig;

/**
 * @author james-egg
 *
 *
 */
public class DataPanel extends JPanel implements MouseListener, ActionListener, TableColumnModelListener, WindowListener{
	public static final int ROWS_IN_TABLE = 15;
	public static final int TOP = 0;
	public static final int BOTTOM = 1;
	public static final int NO_TABLE = -1;
	
	// reference to the linked list holding the matching config objects
	RecMatchConfig rm_conf;
	
	JLabel tfn, bfn;
	JPanel top_content, bottom_content;
	JTable tjt, bjt;
	JPopupMenu column_options, bottom_column_options;
	JRadioButtonMenuItem string_type, number_type;
	JMenu unhide, bottom_unhide;
	
	Vector<TableColumn> top_hidden, bottom_hidden;
	Vector<JMenuItem> unhide_menu_items;
	boolean loading_file, need_to_write, need_to_sync;
	
	int current_model_col, current_col;
	MatchingConfig current_working_config;
	MatchingTableModel current_mtm;
	
	public DataPanel(RecMatchConfig rmc){
		super();
		rm_conf = rmc;
		top_hidden = new Vector<TableColumn>();
		bottom_hidden = new Vector<TableColumn>();
		unhide_menu_items = new Vector<JMenuItem>();
		createDataPanel();
		applyChanges();
	}
	
	/*
	 * Method useful when a new configuration is loaded and the object behind this GUI
	 * element changes
	 */
	public void setRecMatchConfig(RecMatchConfig rmc){
		rm_conf = rmc;
	}
	
	private void createDataPanel(){
		
		column_options = createColumnMenu();
		bottom_column_options = createBottomColumnMenu();
		
		// create the panels for the tab
		this.setLayout(new GridLayout(2,1));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		JLabel tfl = new JLabel("Top File Name:  ");
		JLabel bfl = new JLabel("Bottom File Name:  ");
		tfn = new JLabel("No file currently loaded");
		bfn = new JLabel("No file currently loaded");
		Insets top_and_bottom_padding = new Insets(5,0,5,0);
		
		// these two panels will hold the tables
		top_content = new JPanel(new BorderLayout());
		bottom_content = new JPanel(new BorderLayout());
		
		tjt = new JTable();
		bjt = new JTable();
		top_content.add(tjt.getTableHeader(), BorderLayout.PAGE_START);
		top_content.add(tjt, BorderLayout.CENTER);
		bottom_content.add(bjt.getTableHeader(), BorderLayout.PAGE_START);
		bottom_content.add(bjt, BorderLayout.CENTER);
		
		// grid layout version:
		JPanel top = new JPanel(new GridBagLayout());
		JPanel bottom = new JPanel(new GridBagLayout());
		
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		top.add(tfl, gbc);
		bottom.add(bfl, gbc);
		
		gbc.weightx = 1;
		gbc.gridx = 1;
		top.add(tfn, gbc);
		bottom.add(bfn, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 0.5;
		gbc.gridwidth = 2;
		gbc.insets = top_and_bottom_padding;
		top.add(top_content, gbc);
		bottom.add(bottom_content, gbc);
		
		this.add(top);
		this.add(bottom);
		
	}
	
	/*
	 * Method called when changes are made to the MatchingConfig list object
	 * or the InputFiles object that needs to be reflected in the GUI
	 * 
	 * Methods that will need to be called are:
	 * setTextArea - if a top or bottom file is loaded, but no delimiter set
	 * parseDataToTable - if a separator character is defined, file can be split to table
	 */
	public void applyChanges(){
		if(rm_conf != null){
			LinkDataSource lds1 = rm_conf.getLinkDataSource1();
			LinkDataSource lds2 = rm_conf.getLinkDataSource2();
			if(lds1.getName() != null){
				tfn.setText(lds1.getName());
			}
			if(lds2.getName() != null){
				bfn.setText(lds2.getName());
			}
			
			// if possible, populate tables with data
			if(lds1.getAccess() != null && lds1.getType() != null){
				
			}
		}
	}
	
	public void parseDataToTable(int which){
		// creates and sets table to the top or bottom display pane
		MatchingTableModel mtm;
		if(which == TOP){
			mtm = new MatchingTableModel(rm_conf.getLinkDataSource1());
		} else if(which == BOTTOM){
			mtm = new MatchingTableModel(rm_conf.getLinkDataSource2());
		} else {
			// invalid value, return early
			return;
		}
		
		if(which == TOP){
			// remove existing table
			top_content.remove(tjt);
			top_content.remove(tjt.getTableHeader());
			
			// create table, set options
			tjt = new JTable(mtm);
			tjt.getTableHeader().addMouseListener(this);
			tjt.getColumnModel().addColumnModelListener(this);
			
			// set tooltip options
			tjt.getTableHeader().setToolTipText("Right-click to change properties for the column, shift-click to hide column");
			
			// add new table to panel
			top_content.add(tjt.getTableHeader(), BorderLayout.PAGE_START);
			top_content.add(tjt, BorderLayout.CENTER);
			
			// if other half is loaded, need to sync
			if(rm_conf.getLinkDataSource2() != null){
				// when initially loading, parseDataToTable() will be called with top
				// table before bottom table is loaded
				// checking if bottom table model is a matching model
				// will see if default blank table is loaded currently
				if(bjt.getModel() instanceof MatchingTableModel){
					syncBottom();
				}
			}
		} else {
			bottom_content.remove(bjt);
			bottom_content.remove(bjt.getTableHeader());
			
			bjt = new JTable(mtm);
			bjt.getTableHeader().addMouseListener(this);
			bjt.getColumnModel().addColumnModelListener(this);
			bjt.getTableHeader().setToolTipText("Chanages made on the top will be reflected here, shift-click to hide column");
			
			bottom_content.add(bjt.getTableHeader(), BorderLayout.PAGE_START);
			bottom_content.add(bjt, BorderLayout.CENTER);
			
			// if other half is in a table, need to sync
			if(rm_conf.getLinkDataSource1() != null){
				syncBottom();
			}
		}
		
	}
	
	private JPopupMenu createBottomColumnMenu(){
		JPopupMenu jpm = new JPopupMenu();
		
		JMenuItem jmi = new JMenuItem("Hide column");
		jmi.addActionListener(this);
		jpm.add(jmi);
		bottom_unhide = new JMenu("Unhide column");
		jpm.add(bottom_unhide);
		
		return jpm;
	}
	
	private JPopupMenu createColumnMenu(){
		// initialize right click context menus for tables
		JPopupMenu jpm = new JPopupMenu();
		
		JMenuItem jmi = new JMenuItem("Rename column");
		jmi.addActionListener(this);
		jpm.add(jmi);
		jmi = new JMenuItem("Hide column");
		jmi.addActionListener(this);
		jpm.add(jmi);
		unhide = new JMenu("Unhide column");
		jpm.add(unhide);
		jpm.addSeparator();
		
		ButtonGroup data_types = new ButtonGroup();
		string_type = new JRadioButtonMenuItem("String data type");
		string_type.setSelected(true);
		data_types.add(string_type);
		jpm.add(string_type);
		number_type = new JRadioButtonMenuItem("Numerical data type");
		data_types.add(number_type);
		jpm.add(number_type);
		
		string_type.addActionListener(this);
		number_type.addActionListener(this);
		
		return jpm;
	}
	
	private boolean intArrayContains(int[] array, int num){
		for(int i = 0; i < array.length; i++){
			if(array[i] == num){
				return true;
			}
		}
		return false;
	}
	
	private void shiftHideColumn(JTable jt, int col){
		// called when the user shift-clicks on a column in a data table
		// calls hideColumn once the other arguments are determined
		// col needs to be the index in the table column model, same
		// as the index as drawn on screen
		int which;
		if(jt == tjt){
			which = TOP;
		} else if(jt == bjt){
			which = BOTTOM;
		} else {
			// some error in calling the method
			return;
		}
		
		hideColumn(which, jt.getColumnModel(), col);
		
	}
	
	private void syncTop(){
		// synchronizing the top means updating so new column names are displayed
		// method called when:
		//	column renamed
		//	top column moved
		System.out.println("synchronizing top table");
		// save order of columns
		int col_count = tjt.getColumnCount();
		int[] order = new int[col_count];
		for(int i = 0; i < col_count; i++){
			order[i] = tjt.convertColumnIndexToModel(i);
		}
		
		// save width information
		Vector<Integer> widths = new Vector<Integer>();
		int limit = tjt.getColumnCount();
		TableColumnModel tcm = tjt.getColumnModel();
		TableColumn tc;
		for(int i = 0; i < limit; i++){
			tc = tcm.getColumn(i);
			widths.add(new Integer(tc.getWidth()));
		}
		
		// rebuild table to apply new column name
		MatchingTableModel mtm = (MatchingTableModel)tjt.getModel();
		mtm.fireTableStructureChanged();
		
		// populate an array with columns in correct order
		TableColumn[] cols = new TableColumn[col_count];
		int col_index = 0;
		Enumeration<TableColumn> e = tcm.getColumns();
		while(e.hasMoreElements()){
			tc = e.nextElement();
			if(intArrayContains(order, tc.getModelIndex())){
				cols[col_index] = tcm.getColumn(order[col_index]);
				col_index++;
			}
		}
		
		// remove all columns
		int total_cols = tjt.getColumnCount();
		for(int i = 0; i < total_cols; i++){
			tc = tcm.getColumn(0);
			tcm.removeColumn(tc);
		}
		
		// add columns based on ordering in cols
		for(int i = 0; i < col_count; i++){
			tcm.addColumn(cols[i]);
		}
		
		// apply the widths saved in widths
		for(int i = 0; i < col_count; i++){
			tc = tcm.getColumn(i);
			tc.setPreferredWidth(((Integer)widths.elementAt(i)).intValue());
		}
	}
	
	private void syncBottom(){
		// synchronizing bottom means bottom table shows the top's column names
		// method called when:
		//	column renamed, if bottom table is loaded
		//	top column moved
		System.out.println("synchronziing bottom table");
		
		TableColumnModel tcm_top = tjt.getColumnModel();
		TableColumnModel tcm_bottom = bjt.getColumnModel();
		MatchingTableModel mtm_top = (MatchingTableModel)tjt.getModel();
		MatchingTableModel mtm_bottom = (MatchingTableModel)bjt.getModel();
		TableColumn tc;
		
		// set names on bottom equal to those on top, based on ColumnModel
		int limit = tcm_top.getColumnCount();
		String top_name;
		if(tcm_bottom.getColumnCount() < limit){
			limit = tcm_bottom.getColumnCount();
		}
		for(int i = 0; i < tcm_bottom.getColumnCount(); i++){
			// set table model columns to match the top column model columns
			if(i < limit){
				tc = tcm_top.getColumn(i);
				top_name = mtm_top.getColumnName(tc.getModelIndex());
				tc = tcm_bottom.getColumn(i);
				mtm_bottom.setColumnName(top_name, tc.getModelIndex());
			} else {
				mtm_bottom.setColumnName(null, bjt.convertColumnIndexToModel(i));
			}
			
		}
		
		// save order of columns
		int col_count = bjt.getColumnCount();
		int[] order = new int[col_count];
		for(int i = 0; i < col_count; i++){
			order[i] = bjt.convertColumnIndexToModel(i);
		}
		
		// save width information
		Vector<Integer> widths = new Vector<Integer>();
		for(int i = 0; i < col_count; i++){
			tc = tcm_bottom.getColumn(i);
			widths.add(new Integer(tc.getWidth()));
		}
		
		// fire table update
		mtm_bottom.fireTableStructureChanged();
		
		// apply saved order of columns
		// populate an array with columns in correct order
		TableColumnModel tcm = bjt.getColumnModel();
		TableColumn[] cols = new TableColumn[col_count];
		int col_index = 0;
		Enumeration<TableColumn> e = tcm.getColumns();
		while(e.hasMoreElements()){
			tc = e.nextElement();
			if(intArrayContains(order, tc.getModelIndex())){
				cols[col_index] = tcm.getColumn(order[col_index]);
				col_index++;
			}
		}
		
		// remove all columns
		limit = bjt.getColumnCount();
		for(int i = 0; i < limit; i++){
			tcm.removeColumn(tcm.getColumn(0));
		}
		
		// add columns based on ordering in cols
		for(int i = 0; i < col_count; i++){
			tcm.addColumn(cols[i]);
		}
		
		// apply the widths saved in widths
		for(int i = 0; i < col_count; i++){
			tc = tcm_bottom.getColumn(i);
			tc.setPreferredWidth(((Integer)widths.elementAt(i)).intValue());
		}
	}
	
	private void hideColumn(int which, TableColumnModel cm, int col){
		// removes a table column from the model and saves it for when it's uhid
		// col is the index in the table column model and might not match the
		// index in the table data model
		Vector<TableColumn> hidden;
		if(which == TOP){
			hidden = top_hidden;
		} else {
			hidden = bottom_hidden;
		}
		
		// remove column at given index
		TableColumn tc = cm.getColumn(col);
		hidden.add(tc);
		cm.removeColumn(tc);
		
		// need to sync between tables
		// if it were top, then would need to syncBottom to reflect top
		// if it were bottom, still need to match up bottom's columns with top
		// only a syncBottom is needed if both top and bottom are tabled
		if(rm_conf.getLinkDataSource1() != null && rm_conf.getLinkDataSource2() != null){
			syncBottom();
		}
		
		if(!loading_file){
			// columns get hidden automatically while loading a file, so
			// if file is loading this flag should be blocked
			need_to_write = true;
		}
		
	}
	
	public String[] getDataColumnNames(){
		// returns the column names of the two data tables that match
		if(!(rm_conf.getLinkDataSource1() != null && rm_conf.getLinkDataSource2() != null)){
			// need to have both tables setup for column names to match
			return null;
		}
		
		int limit = 0;
		if(tjt.getColumnCount() <= bjt.getColumnCount()){
			limit = tjt.getColumnCount();
		} else {
			limit = bjt.getColumnCount();
		}
		
		String[] names = new String[limit];
		for(int i = 0; i < limit; i++){
			// either table will work, since both have at least limit number of columns
			names[i] = tjt.getColumnName(i);
		}
		
		return names;
	}
	
	public void mouseEntered(MouseEvent me){
		// not using, but need to implement for interface
	}
	
	public void mouseClicked(MouseEvent me){
		// important to notice that 'int column' is set to table model's column index, NOT
		// column model's index
		
		// determine what collumn user clicked on
		if(me.getSource() instanceof JTableHeader){
			
			// following few lines from an example in Java tutorial on JTable mouse events
			JTableHeader jth = (JTableHeader)me.getSource();
			JTable jt = jth.getTable();
			TableColumnModel columnModel = jth.getColumnModel();
            int viewColumn = columnModel.getColumnIndexAtX(me.getX());
            int column = columnModel.getColumn(viewColumn).getModelIndex();
            current_col = viewColumn;
            
            System.out.println("\tColumn click event at model index: " + column);
            System.out.println("\tColumn click event at view index: " + viewColumn);
            
            // detect if shift key is modifying the click event
			if(((me.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK) &&
					(me.getButton() == MouseEvent.BUTTON1)){
				System.out.println("\tshift-left click, hiding column");
				shiftHideColumn(jt, viewColumn);
			} else if(me.getButton() == MouseEvent.BUTTON1 && me.getClickCount() == 2){
            	// double click with left mouse button
            	System.out.println("\tdouble left click . . .");
            	showColumnNameDialog(tjt, column);
            } else if(me.getButton() == MouseEvent.BUTTON3){
            	System.out.println("\tright click, change column options");
            	if(me.getSource() == tjt.getTableHeader()){
            		showColumnMenu(me, jt, column, column_options);
            	} else if(me.getSource() == bjt.getTableHeader()){
            		showColumnMenu(me, jt, column, bottom_column_options);
            	}
            }
            
		}
		
		
	}
	
	private void showColumnMenu(MouseEvent me, JTable jt, int col, JPopupMenu jpm){
		MatchingTableModel mtm = (MatchingTableModel)jt.getModel();
		
		// set the selected option to reflect this column
		current_model_col = col;
		current_mtm = mtm;
		if(mtm.isNumberType(col)){
			number_type.setSelected(true);
			System.out.println("\tcurrently column " + col + " has number value");
		} else {
			string_type.setSelected(true);
			System.out.println("\tcurrently column " + col + " has string value");
		}

		// build unhide menu list
		Vector<TableColumn> hidden;
		JMenu uh;
		JMenuItem jmi;
		
		unhide_menu_items = new Vector<JMenuItem>();
		if(jt == tjt){
			// look at top_hidden vector to determine what to add to menu
			hidden = top_hidden;
			uh = unhide;
		} else {
			// look at bottom_hidden
			hidden = bottom_hidden;
			uh = bottom_unhide;
		}
		
		// remove what is already there, since things might have changed
		Component[] menu_items = uh.getPopupMenu().getComponents();
		for(int i = 0; i < menu_items.length; i++){
			uh.remove(menu_items[i]);
		}
		
		// add correct menu items
		if(hidden.size() == 0){
			// set disabled option in menu
			jmi = new JMenuItem("empty");
			jmi.setEnabled(false);
			uh.add(jmi);
		} else {
			// loop through and for each column, add entry to menu
			TableColumn tc;
			String entry;
			for(int i = 0; i < hidden.size(); i++){
				tc = (TableColumn)hidden.elementAt(i);
				entry = jt.getModel().getColumnName(tc.getModelIndex());
				jmi = new JMenuItem(entry);
				jmi.addActionListener(this);
				uh.add(jmi);
				unhide_menu_items.add(jmi);
			}
		}
		
		// show column, get user input
		jpm.show(me.getComponent(), me.getX(), me.getY());
		
	}
	
	private void showColumnNameDialog(JTable jt, int col){
		String new_name, old_name;
		MatchingTableModel mtm;
		
		// get current column header for jt
		old_name = jt.getModel().getColumnName(col);
		
		new_name = (String)JOptionPane.showInputDialog(this, "Enter the name for this column:", old_name);
		
		if(new_name == null || new_name.length() <= 0){
			// nothing valid returned from prompt, can return quickly
			return;
		}
		
		// set name
		if(jt.getModel() instanceof MatchingTableModel){
			mtm = (MatchingTableModel)jt.getModel();
			mtm.setColumnName(new_name, col);
			
			// resync tables for TOP table
			syncTop();
			if(rm_conf.getLinkDataSource2() != null){
				syncBottom();
			}
		}
		
		
		
	}
	
	public void mouseReleased(MouseEvent me){
		// check if need to resync
		// assumption is that the flag would be set only if a column was dragged
		// when done repositioning, the mouse is released
		if(need_to_sync){
			System.out.print("moved a column ");
			need_to_sync = false;
			
			if(me.getSource() == tjt.getTableHeader()){
				System.out.println("on top");
				if(rm_conf.getLinkDataSource2() != null){
					syncBottom();
				}
			} else if(me.getSource() == bjt.getTableHeader()){
				System.out.println("on bottom");
				if(rm_conf.getLinkDataSource1() != null){
					syncBottom();
				}
			}
		}
		
	}
	
	public void mouseExited(MouseEvent me){
		// not using, but need to implement for interface
	}
	
	public void mousePressed(MouseEvent me){
		// not using, but need to implement for interface
	}
	
	public void actionPerformed(ActionEvent ae){
		if(ae.getSource() instanceof JMenuItem){
			JMenuItem source = (JMenuItem) ae.getSource();
			
			// first check if JMenuItem was dynamically created menu item to show column
			JMenuItem jmi;
			for(int i = 0; i < unhide_menu_items.size(); i++){
				jmi = unhide_menu_items.elementAt(i);
				if(jmi == source){
					System.out.println("uhide column " + source.getText());
					JMenu uh;
					Vector<TableColumn> hidden_columns;
					JTable jt;
					if(current_mtm == tjt.getModel()){
						// unhide column on the top
						uh = unhide;
						hidden_columns = top_hidden;
						jt = tjt;
					} else {
						// unhide column on the bottom
						uh = bottom_unhide;
						hidden_columns = bottom_hidden;
						jt = bjt;
					}
					unHideColumn(source.getText(), hidden_columns, jt);
					
					return;
				}
			}
			
			if(ae.getSource() instanceof JRadioButtonMenuItem){
				source = (JRadioButtonMenuItem)ae.getSource();
				if(source.getText().equals("String data type")){
					current_mtm.setDataType(current_model_col, MatchingTableModel.STRING);
				} else if(source.getText().equals("Numerical data type")){
					current_mtm.setDataType(current_model_col, MatchingTableModel.NUMBER);
				}
			}
			
			if(source.getText().equals("Rename column")){
				System.out.println("change column name");
				// we know it's for top JTable tjt, since it's a popup menu, 
				// and only the top header is registerd with a mouselistener
				showColumnNameDialog(tjt, current_model_col);
			} else if(source.getText().equals("Hide column")) {
				// need to set current column index as hidden, and resync the table
				System.out.println("hide column " + current_model_col);
				// use column model's remove column method to hide it, but save it
				if(current_mtm == tjt.getModel()){
					hideColumn(TOP, tjt.getColumnModel(), current_col);
				} else if(current_mtm == bjt.getModel()){
					hideColumn(BOTTOM, bjt.getColumnModel(), current_col);
				}
				
			}  
		}
		
	}
	
	private void unHideColumn(String label, Vector<TableColumn> hidden_columns, JTable table){
		// label is text to compare it to, hidden_columns hold TableColumn objects
		// table is where the matching table needs to be inserted
		TableColumn tc;
		Object header_obj;
		String header_label;
		if(!loading_file){
			// need to set this if it's not being blocked by a in-progress file load
			need_to_write = true;
		}
		for(int i = 0; i < hidden_columns.size(); i++){
			tc = hidden_columns.elementAt(i);
			header_obj = tc.getHeaderValue();
			if(header_obj instanceof String){
				// check to see if header of this column matches the label we're looking for
				header_label = (String)header_obj;
				if(label.equals(header_label)){
					// need to insert this column back into table's column model, remove from hidden_columns vector
					// since columns were chagned, need to synch after tc's been added
					table.getColumnModel().addColumn(tc);
					hidden_columns.remove(tc);
					if(rm_conf.getLinkDataSource1() != null){
						syncTop();
					}
					if(rm_conf.getLinkDataSource1() != null && rm_conf.getLinkDataSource2() != null){
						syncBottom();
					}
					
					// session table no longer valid
					rm_conf.getMatchingConfigs().clear();
					
					return;
				}
			}
		}
		
	}
	
	public void columnAdded(TableColumnModelEvent tcme){
		// not worried about this, but needed for interface
	}
	
	public void columnMarginChanged(ChangeEvent ce){
		// not worried about this, but needed for interface
	}
	
	public void columnMoved(TableColumnModelEvent tcme){
		int original_index, new_index;
		original_index = tcme.getFromIndex();
		new_index = tcme.getToIndex();
		
		if(original_index == new_index){
			// user is just dragging around without reordering
			// leave quickly
			return;
		}
		
		// since columns have been moved, set flag that files need to be written
		need_to_write = true;
		need_to_sync = true;
		
		// session table no longer valid
		//resetSessionTableWithBlankConfig();
	}
	
	public void columnRemoved(TableColumnModelEvent tcme){
		// not worried about this, but needed for interface
	}
	
	public void columnSelectionChanged(ListSelectionEvent tcme){
		// not worried about this, but needed for interface
	}
	
	public void windowClosed(WindowEvent we){
		// not used, interface implemented for the windowClosing method
	}
	
	public void windowOpened(WindowEvent we){
		// not used, interface implemented for the windowClosing method
	}
	
	public void windowIconified(WindowEvent we){
		// not used, interface implemented for the windowClosing method
	}
	
	public void windowDeiconified(WindowEvent we){
		// not used, interface implemented for the windowClosing method
	}
	
	public void windowActivated(WindowEvent we){
		// not used, interface implemented for the windowClosing method
	}
	
	public void windowDeactivated(WindowEvent we){
		// not used, interface implemented for the windowClosing method
		System.out.println("applying wizard changes");
		applyChanges();
	}
	
	public void windowGainedFocus(WindowEvent we){
		// not used, interface implemented for the windowClosing method
	}
	
	public void windowLostFocus(WindowEvent we){
		// not used, interface implemented for the windowClosing method
	}
	
	public void windowStateChanged(WindowEvent we){
		// not used, interface implemented for the windowClosing method
	}
	
	public void windowClosing(WindowEvent we){
		// not used
	}
}
