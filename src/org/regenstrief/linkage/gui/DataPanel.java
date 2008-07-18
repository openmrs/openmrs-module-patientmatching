package org.regenstrief.linkage.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
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
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.RecMatchConfig;

/**
 * @author james-egg
 *
 *
 */
public class DataPanel extends JPanel implements MouseListener, ActionListener, TableColumnModelListener, WindowListener, ItemListener {
	public static final int ROWS_IN_TABLE = 15;
	public static final int TOP = 0;
	public static final int BOTTOM = 1;
	public static final int NO_TABLE = -1;
	
	// reference to the linked list holding the matching config objects
	RecMatchConfig rm_conf;
	
	JLabel tfn, bfn;
	JTable tjt, bjt;
	JPopupMenu column_options, bottom_column_options;
	JRadioButtonMenuItem string_type, number_type;
	JMenu unhide, bottom_unhide;
	
	private JCheckBoxMenuItem checkBoxMenuItem;
	
	Vector<JMenuItem> unhide_menu_items;
	boolean loading_file, need_to_write, need_to_sync;
	
	int current_model_col, current_col;
	MatchingConfig current_working_config;
	MatchingTableModel current_mtm;
	
	public DataPanel(RecMatchConfig rmc){
		super();
		rm_conf = rmc;
		
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
        /*
         * Remove the previously loaded table model. This to prevent the table being
         * synchronized when only the top table is loaded from the new config file which
         * will cause the application to synch table from two different config file (top
         * table from new config and bottom table from old config file).
         */
        tjt.setModel(new DefaultTableModel());
        bjt.setModel(new DefaultTableModel());
		applyChanges();
	}
	
	private void createDataPanel(){
		
		column_options = createColumnMenu();
		bottom_column_options = createBottomColumnMenu();
		
		// create the panels for the tab
		this.setLayout(new GridLayout(2,1));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		JLabel tfl = new JLabel("Data Source A:  ");
		JLabel bfl = new JLabel("Data Source B:  ");
		tfn = new JLabel("No data source configured");
		bfn = new JLabel("No data source configured");
		Insets top_and_bottom_padding = new Insets(5,0,5,0);
		
		// these two panels will hold the tables
		JPanel top_content = new JPanel(new BorderLayout());
		JPanel bottom_content = new JPanel(new BorderLayout());
		
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
			if(lds1 != null && lds1.getName() != null){
				tfn.setText(lds1.getName());
				parseDataToTable(TOP);
			}
			if(lds2 != null && lds2.getName() != null){
				bfn.setText(lds2.getName());
				parseDataToTable(BOTTOM);
			}
		}
	}
	
	public void clearTable(int which) {
	    // assume only bottom
	    if (which == TOP) {
            tjt.setColumnModel(new DefaultTableColumnModel());
            tjt.setModel(new DefaultTableModel());
            tfn.setText("No data source configured");
	    } else {
            bjt.setColumnModel(new DefaultTableColumnModel());
            bjt.setModel(new DefaultTableModel());
            bfn.setText("No data source configured");
	    }
	}
	
	public void parseDataToTable(int which){
		// creates and sets table to the top or bottom display pane
		MatchingTableModel mtm;
		MatchingTableColumnModel mtcm;
		if(which == TOP){
			mtm = new MatchingTableModel(rm_conf.getLinkDataSource1());
			mtcm = new MatchingTableColumnModel(rm_conf.getLinkDataSource1());
			tfn.setText(rm_conf.getLinkDataSource1().getName());
		} else if(which == BOTTOM){
			mtm = new MatchingTableModel(rm_conf.getLinkDataSource2());
			mtcm = new SecondaryMatchingTableColumnModel(rm_conf.getLinkDataSource2());
			bfn.setText(rm_conf.getLinkDataSource2().getName());
		} else {
			// invalid value, return early
			return;
		}
		
		if(which == TOP){
            /*
             * Remarking this area to remove GUI modification outside Panel initialization.
             */
			// remove existing table
			// top_content.remove(tjt);
			// top_content.remove(tjt.getTableHeader());
			
			// create table, set options
			// tjt = new JTable(mtm, mtcm);
		    
            /*
             * Use this two line of codes to update the table
             */
            tjt.setColumnModel(mtcm);
            tjt.setModel(mtm);
			
		    tjt.createDefaultColumnsFromModel();
			
			tjt.getTableHeader().addMouseListener(this);
			tjt.getColumnModel().addColumnModelListener(this);
			
			// set tooltip options
			tjt.getTableHeader().setToolTipText("Right-click to change properties for the column, shift-click to hide column");
			
			// add new table to panel
			// top_content.add(tjt.getTableHeader(), BorderLayout.PAGE_START);
			// top_content.add(tjt, BorderLayout.CENTER);
			
			// if other half is loaded, need to sync
			if(rm_conf.getLinkDataSource2() != null){
				// when initially loading, parseDataToTable() will be called with top
				// table before bottom table is loaded
				// checking if bottom table model is a matching model
				// will see if default blank table is loaded currently
				if(bjt.getModel() instanceof MatchingTableModel){
					//syncBottom();
	                unhideColumns();
					syncTables();
				}
			}
		} else {
            /*
             * Remarking this section to remove GUI modification outside Panel initialization.
             */
			// bottom_content.remove(bjt);
			// bottom_content.remove(bjt.getTableHeader());
			
			// bjt = new JTable(mtm, mtcm);
		    
            /*
             * Use this two line of codes to update the table
             */
            bjt.setColumnModel(mtcm);
            bjt.setModel(mtm);
			
		    bjt.createDefaultColumnsFromModel();
			
			bjt.getTableHeader().addMouseListener(this);
			bjt.getColumnModel().addColumnModelListener(this);
			bjt.getTableHeader().setToolTipText("Chanages made on the top will be reflected here, shift-click to hide column");
			
			// bottom_content.add(bjt.getTableHeader(), BorderLayout.PAGE_START);
			// bottom_content.add(bjt, BorderLayout.CENTER);
			
			// if other half is in a table, need to sync
			if(rm_conf.getLinkDataSource1() != null){
				//syncBottom();
			    unhideColumns();
				syncTables();
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
        
        checkBoxMenuItem = new JCheckBoxMenuItem("Unique Id Column");
        checkBoxMenuItem.addItemListener(this);
        jpm.add(checkBoxMenuItem);
		
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
	
	private void hideColumn(JTable jt, String col_name){
		MatchingTableColumnModel mtcm = (MatchingTableColumnModel)jt.getColumnModel();
		TableColumn tc = mtcm.getColumn(mtcm.getColumnIndex(col_name));
		mtcm.hideColumn(tc);
		
		// sync tables if needed
		//if(rm_conf.getLinkDataSource1() != null && rm_conf.getLinkDataSource2() != null){
		//	syncTables();
		//}
	}
	
	private void syncTables(){
		// will need to synhronize the two tables, using the top table for information
		System.out.println("sync tables");
		MatchingTableModel mtm_top = (MatchingTableModel)tjt.getModel();
		MatchingTableModel mtm_bottom = (MatchingTableModel)bjt.getModel();
		LinkDataSource lds1 = rm_conf.getLinkDataSource1();
		LinkDataSource lds2 = rm_conf.getLinkDataSource2();
		
		// will need to iterate over the top columns of the table
		for(int i = 0; i < tjt.getColumnCount() && i < bjt.getColumnCount(); i++){
			TableColumn tc_top = tjt.getColumnModel().getColumn(i);
			TableColumn tc_bottom = bjt.getColumnModel().getColumn(i);
			
			DataColumn master = lds1.getDataColumn(tc_top.getModelIndex());
			DataColumn to_sync = lds2.getDataColumn(tc_bottom.getModelIndex());
			
			// need to make sure column names match, and data types
			to_sync.setName(master.getName());
			to_sync.setType(master.getType());
			
			// update UI elements
			tc_bottom.setHeaderValue(master.getName());
			tc_bottom.setIdentifier(master.getName());
		}
		
		mtm_top.fireTableStructureChanged();
		mtm_bottom.fireTableStructureChanged();
		
		// brute force way of showing updates to table is firing update, then removing and adding
		// all the columns
		removeAndReplaceTableColumns(tjt);
		removeAndReplaceTableColumns(bjt);
	}
	
	/*
	 * 
	 */
	private void removeAndReplaceTableColumns(JTable jt){
		Vector<TableColumn> tcs = new Vector<TableColumn>();
		Enumeration<TableColumn> e = jt.getColumnModel().getColumns();
		while(e.hasMoreElements()){
			TableColumn tc = e.nextElement();
			tcs.add(tc);
		}
		
		Iterator<TableColumn> it = tcs.iterator();
		while(it.hasNext()){
			TableColumn tc = it.next();
			jt.removeColumn(tc);
		}
		
		it = tcs.iterator();
		while(it.hasNext()){
			TableColumn tc = it.next();
			jt.addColumn(tc);
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
				String col_name = columnModel.getColumn(viewColumn).getHeaderValue().toString();
				hideColumn(jt, col_name);
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
		List<String> hidden;
		JMenu uh;
		JMenuItem jmi;
		
		unhide_menu_items = new Vector<JMenuItem>();
		if(jt == tjt){
			// look at top_hidden vector to determine what to add to menu
	        String columnName = tjt.getColumnModel().getColumn(current_col).getHeaderValue().toString();
	        String uniqueId = rm_conf.getLinkDataSource1().getUniqueID();
	        
	        if (columnName.equals(uniqueId)) {
	            checkBoxMenuItem.setSelected(true);
	        } else {
	            checkBoxMenuItem.setSelected(false);
	        }
			hidden = ((MatchingTableColumnModel)tjt.getColumnModel()).getHiddenColumns();
			uh = unhide;
		} else {
			// look at bottom_hidden
			hidden = ((MatchingTableColumnModel)bjt.getColumnModel()).getHiddenColumns();
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
			String entry;
			Iterator<String> it = hidden.iterator();
			while(it.hasNext()){
				entry = it.next();
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
			
			//int display_index = mtm.get
			//jt.getColumnModel().getColumn(col).setHeaderValue(new_name);
			//jt.getColumnModel().getColumn(col).setIdentifier(new_name);
			
			//mtm.fireTableStructureChanged();
			
			// synchronize the two table names
			if(rm_conf.getLinkDataSource2() != null){
				syncTables();
			} else {
				removeAndReplaceTableColumns(tjt);
			}
		}
		
		// need to update this columns name in the MatchingConfig objects, since any existing ones were
		// instantiated with old_name for the column
		Iterator<MatchingConfig> it = rm_conf.iterator();
		while(it.hasNext()){
			MatchingConfig mc = it.next();
			mc.getMatchingConfigRowByName(old_name).setName(new_name);
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
					syncTables();
				}
			} else if(me.getSource() == bjt.getTableHeader()){
				System.out.println("on bottom");
				if(rm_conf.getLinkDataSource1() != null){
					syncTables();
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
					String hidden_col_name = source.getText();
					System.out.println("uhide column " + hidden_col_name);
					JTable jt;
					if(current_mtm == tjt.getModel()){
						// unhide column on the top
						jt = tjt;
					} else {
						// unhide column on the bottom
						jt = bjt;
					}
					unHideColumn(jt, hidden_col_name);
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
					String col_name = tjt.getColumnModel().getColumn(current_col).getHeaderValue().toString();
					hideColumn(tjt, col_name);
				} else if(current_mtm == bjt.getModel()){
					String col_name = bjt.getColumnModel().getColumn(current_col).getHeaderValue().toString();
					hideColumn(bjt, col_name);
				}
				
			}  
		}
		
	}
	
	private void unHideColumn(JTable jt, String col_name){
		// table is where the matching table needs to be inserted
		if(!loading_file){
			// need to set this if it's not being blocked by a in-progress file load
			need_to_write = true;
		}
		
		MatchingTableColumnModel mtcm = (MatchingTableColumnModel)jt.getColumnModel();
		mtcm.unHideColumn(col_name);
		
		// sync the two tables if needed
		//if(rm_conf.getLinkDataSource1() != null && rm_conf.getLinkDataSource2() != null){
		//	syncTables();
		//}
		return;
		
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
		
		boolean in_range = bjt.getColumnCount() > original_index && bjt.getColumnCount() > new_index;
		if(tcme.getSource() == tjt.getColumnModel() && in_range){
			// can update columns on lower table and lds2
			// commented lines out, since currently lds objects are being synced in the
			// syncTables() method when the mouse is release,
			// and just the GUI can be done now
			//MatchingTableModel mtm_top = (MatchingTableModel)tjt.getModel();
			//MatchingTableModel mtm_bottom = (MatchingTableModel)bjt.getModel();
			
			//TableColumn tc_bottom = bjt.getColumnModel().getColumn(original_index);
			//TableColumn tc2_bottom = bjt.getColumnModel().getColumn(new_index);
			
			//TableColumn tc = tjt.getColumnModel().getColumn(original_index);
			//TableColumn tc2 = tjt.getColumnModel().getColumn(new_index);
			
			//tc_bottom.setHeaderValue(tc.getHeaderValue());
			//tc2_bottom.setHeaderValue(tc2.getHeaderValue());
			
			//rm_conf.getLinkDataSource2().getDataColumn(tc_bottom.getModelIndex()).setIncludePosition(new_index);
			//rm_conf.getLinkDataSource2().getDataColumn(tc2_bottom.getModelIndex()).setIncludePosition(original_index);
			
			//removeAndReplaceTableColumns(bjt);
		}
		
		
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

    public void itemStateChanged(ItemEvent e) {
        if(e.getSource() == checkBoxMenuItem) {
            String columnName = tjt.getColumnModel().getColumn(current_col).getHeaderValue().toString();
            LinkDataSource lds = null;
            
            if(e.getStateChange() == ItemEvent.SELECTED) {
                lds = rm_conf.getLinkDataSource1();
                lds.setUniqueID(columnName);
                lds = rm_conf.getLinkDataSource2();
                lds.setUniqueID(columnName);
            } else {
                lds = rm_conf.getLinkDataSource1();
                if (columnName.equals(lds.getUniqueID())) {
                    lds.setUniqueID(null);
                    lds = rm_conf.getLinkDataSource2();
                    lds.setUniqueID(null);
                }
            }
        }
    }
    
    private void unhideColumns() {
        if (bjt.getColumnCount() != tjt.getColumnCount()) {
            List<String> topHiddenColumn = ((MatchingTableColumnModel)tjt.getColumnModel()).getHiddenColumns();
            for (String columnName : topHiddenColumn) {
                unHideColumn(tjt, columnName);
            }
            List<String> bottomHiddenColumn = ((MatchingTableColumnModel)bjt.getColumnModel()).getHiddenColumns();
            for (String columnName : bottomHiddenColumn) {
                unHideColumn(bjt, columnName);
            }
        }
    }
}
