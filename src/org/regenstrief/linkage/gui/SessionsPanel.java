package org.regenstrief.linkage.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.FileWritingMatcher;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.RecMatchConfig;

/**
 * @author james-egg
 *
 *
 */
public class SessionsPanel extends JPanel implements ActionListener, KeyListener, ListSelectionListener{
	public final String DEFAULT_NAME = "New match";
	
	JList runs;
	JTextField run_name;
	JTable session_options;
	
	RecMatchConfig rm_conf;
	 
	MatchingConfig current_working_config;
	
	public SessionsPanel(RecMatchConfig rmc){
		//super();
		rm_conf = rmc;
		createSessionPanel();
	}
	
	public void setRecMatchConfig(RecMatchConfig rmc){
		rm_conf = rmc;
		this.repaint();
	}
	
	private void createSessionPanel(){
		// split the panel into two parts, top for table, bottom for other user interaction
		//JPanel session_panel = new JPanel(new GridLayout(2, 1));
		this.setLayout(new GridLayout(2,1));
		
		// item for the top panel
		this.add(getSessionTable());
		
		// add items for the bottom panel
		
		JPanel list_panel = new JPanel();//new BoxLayout(list_panel, BoxLayout.LINE_AXIS));
		
		
		
		list_panel.setLayout(new BoxLayout(list_panel, BoxLayout.LINE_AXIS));
		JPanel left_list_panel = new JPanel();
		left_list_panel.setLayout(new BoxLayout(left_list_panel, BoxLayout.PAGE_AXIS));
		list_panel.add(left_list_panel);
		JPanel right_list_panel = new JPanel();
		right_list_panel.setLayout(new BoxLayout(right_list_panel, BoxLayout.PAGE_AXIS));
		list_panel.add(right_list_panel);
		list_panel.add(Box.createHorizontalGlue());
		
		// add the JList object to the left_list_panel
		DefaultListModel dlm = new DefaultListModel();
		runs = new JList(dlm);
		runs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		runs.addListSelectionListener(this);
		runs.setCellRenderer(new MatchingConfigCellRenderer());
		
		// if there are already MatchingConfig objects, add to runs
		if(rm_conf != null){
			if(rm_conf.getMatchingConfigs().size() > 0){
				Iterator<MatchingConfig> it = rm_conf.getMatchingConfigs().iterator();
				while(it.hasNext()){
					dlm.addElement(it.next());
				}
				current_working_config = rm_conf.getMatchingConfigs().get(0);
			} else {
				current_working_config = null;
			}
		}
		
		JScrollPane jsp = new JScrollPane(runs);
		jsp.setPreferredSize(new Dimension(150, 200));
		jsp.setMaximumSize(new Dimension(150, 200));
		left_list_panel.add(jsp);
		JButton up = new JButton("Move up");
		up.addActionListener(this);
		JButton down = new JButton("Move down");
		down.addActionListener(this);
		JPanel jp = new JPanel(new FlowLayout());
		jp.add(up);
		jp.add(down);
		left_list_panel.add(jp);
		
		// add JButtons and JTextField to the other panel
		JButton rename, remove, new_run, validate, write, run_link;
		run_name = new JTextField(18);
		run_name.setMaximumSize(run_name.getPreferredSize());
		run_name.addKeyListener(this);
		JLabel name = new JLabel("Name of configuration");
		rename = new JButton("Rename");
		rename.addActionListener(this);
		remove = new JButton("Remove");
		remove.addActionListener(this);
		new_run = new JButton("New");
		new_run.addActionListener(this);
		// validate selections button moved to an implicit process done as a part
		// of the linkage process
		//validate = new JButton("Check table selections");
		//validate.addActionListener(this);
		// separate button for writing meta files removed; will be part of
		// linkage process and done automatically
		//write = new JButton("Write session metafiles");
		//write.addActionListener(this);
		run_link = new JButton("Run linkage process");
		run_link.addActionListener(this);
		right_list_panel.add(name);
		right_list_panel.add(run_name);
		right_list_panel.add(rename);
		right_list_panel.add(remove);
		right_list_panel.add(new_run);
		//right_list_panel.add(validate);
		//right_list_panel.add(write);
		right_list_panel.add(run_link);
		
		this.add(list_panel);
	}
	
	private Component getSessionTable(){
		session_options = new JTable(new SessionOptionsTableModel());
		
		JComboBox jcb = new JComboBox();
		jcb.addItem(MatchingConfig.ALGORITHMS[0]);
		jcb.addItem(MatchingConfig.ALGORITHMS[1]);
		jcb.addItem(MatchingConfig.ALGORITHMS[2]);
		jcb.addItem(MatchingConfig.ALGORITHMS[3]);
		TableColumn tc = session_options.getColumnModel().getColumn(6);
		tc.setCellEditor(new DefaultCellEditor(jcb));
		JScrollPane table_pane = new JScrollPane(session_options);
		
		return table_pane;
		
	}
	
	private void displayThisMatchingConfig(MatchingConfig mc){
		//session_options.setModel(new SessionOptionsTableModel(mc));
		current_working_config = mc;
		session_options.setModel(new SessionOptionsTableModel(mc));
	}
	
	private MatchingConfig getSelectedConfig(){
		// gets the matching config object currely selected in the jlist object
		DefaultListModel dlm = (DefaultListModel)runs.getModel();
		MatchingConfig mc = (MatchingConfig)dlm.getElementAt(runs.getSelectedIndex());
		
		return mc;
	}
	
	private void removeSessionConfig(MatchingConfig mc){
		// removes mc from the DefaultListModel for the runs JList
		if(runs.getModel().getSize() > 0){
			int old_index = runs.getSelectedIndex();
			DefaultListModel dlm = (DefaultListModel)runs.getModel();
			boolean removed = dlm.removeElement(mc);
			if(!removed){
				System.out.println("error removing matching config from JList");
				return;
			}
			
			// set another item to be selected
			if(runs.getModel().getSize() <= old_index){
				// set the new selected object as the last item
				if(runs.getModel().getSize() == 0){
					// removed last one, clear table to clear everything and crate a blank config
					//resetSessionTableWithBlankConfig();
				}
				runs.setSelectedIndex(runs.getModel().getSize() - 1);
			} else {
				// set the old index as what is selected
				runs.setSelectedIndex(old_index);
			}
		}
		
	}
	
	private void moveUpSessionConfig(int index){
		// move the session config in runs at index up
		DefaultListModel dlm = (DefaultListModel)runs.getModel();
		
		// if item is at beginning of list, makes no sense to move up in order
		if(index > 0){
			Object to_move = dlm.remove(index);
			dlm.add(index - 1, to_move);
			runs.setSelectedIndex(index - 1);
		}
	}
	
	private void moveDownSessionConfig(int index){
		// move the session config in runs at index down
		DefaultListModel dlm = (DefaultListModel)runs.getModel();
		
		// if item is at the end of the list, then it can't move down further
		if(index < (dlm.getSize() - 1)){
			Object to_move = dlm.remove(index);
			dlm.add(index + 1, to_move);
			runs.setSelectedIndex(index + 1);
		}
	}
	
	private void renameSessionConfig(){
		// the current working session config needs to be renamed, and have it's new name displayed
		// in the JList runs component
		if(current_working_config != null && runs.getModel().getSize() > 0){
			String new_name = run_name.getText();
			current_working_config.setName(new_name);
			
			runs.repaint();
			// update the JList runs object to display the new name
			//DefaultListModel dlm = (DefaultListModel)runs.getModel();
			//int current_list_index = dlm.indexOf(current_working_config);
			//dlm.setElementAt(current_working_config, current_list_index);
		}
		
	}
	
	public void keyTyped(KeyEvent ke){
		// used for the rename text field on the session panel
		// if key is enter, rename the MatchingConfig
		if(ke.getSource() == run_name && ke.getKeyChar() == '\n'){
			renameSessionConfig();
		}
	}
	
	public void keyReleased(KeyEvent ke){
		// not currently used
	}
	
	public void keyPressed(KeyEvent ke){
		// not currently used
	}
	
	public void valueChanged(ListSelectionEvent lse){
		// part of ListSelectionListener used with the JList runs object
		if(!lse.getValueIsAdjusting()){
			if(current_working_config != null){
				// check to see if the current working config is the same as what is
				// selected in the JList runs
				MatchingConfig mc = (MatchingConfig)runs.getSelectedValue();
				if(mc != null && mc != current_working_config){
					// clicked on a new item
					displayThisMatchingConfig(mc);
				}
			}
		}
		
	}
	
	public void actionPerformed(ActionEvent ae){
		if(ae.getSource() instanceof JButton){
			JButton source = (JButton)ae.getSource();
			if(source.getText().equals("Move up")){
				int index = runs.getSelectedIndex();
				moveUpSessionConfig(index);
			} else if(source.getText().equals("Move down")){
				int index = runs.getSelectedIndex();
				moveDownSessionConfig(index);
			} else if(source.getText().equals("Rename")){
				renameSessionConfig();
			} else if(source.getText().equals("New")){
				Hashtable<String, DataColumn> col_names = rm_conf.getLinkDataSource1().getIncludedDataColumns();
				String[] names = new String[col_names.keySet().size()];
				Enumeration<String> e = col_names.keys();
				int i = 0;
				while(e.hasMoreElements()){
					names[i++] = e.nextElement();
				}
				MatchingConfig mc = new MatchingConfig(DEFAULT_NAME, names);
				rm_conf.getMatchingConfigs().add(mc);
				DefaultListModel dlm = (DefaultListModel)runs.getModel();
				dlm.addElement(mc);
				displayThisMatchingConfig(mc);
			} else if(source.getText().equals("Run linkage process")){
				JFileChooser out_chooser = new JFileChooser();
				int ret = out_chooser.showDialog(this, "Choose output file");
				File out, match_file;
				if(ret == JFileChooser.APPROVE_OPTION){
					out = out_chooser.getSelectedFile();
					match_file = FileWritingMatcher.writeMatchResults(rm_conf, out);
				} else {
					match_file = FileWritingMatcher.writeMatchResults(rm_conf);
				}
				
				if(match_file == null){
					JOptionPane.showMessageDialog(this, "Error writing match result file", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(this, "Matching results written to " + match_file, "Matching Successful", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		}
		
	}
	
	/**
	 * Method first adds a check to see what MatchingConfig to set as active
	 * for the SessionOptionsTable to display
	 */
	public void paint(Graphics g){
		if(rm_conf.getMatchingConfigs().size() == 0){
			DefaultListModel dlm = (DefaultListModel)runs.getModel();
			dlm.clear();
			// need to create an empty one to display in session options table, and add to rm_conf
			// but only if the two link data sources are both defined
			if(rm_conf.getLinkDataSource1() != null && rm_conf.getLinkDataSource2() != null){
				Hashtable<String, DataColumn> col_names = rm_conf.getLinkDataSource1().getIncludedDataColumns();
				String[] names = new String[col_names.keySet().size()];
				Enumeration<String> e = col_names.keys();
				int i = 0;
				while(e.hasMoreElements()){
					names[i++] = e.nextElement();
				}
				MatchingConfig mc = new MatchingConfig(DEFAULT_NAME, names);
				rm_conf.getMatchingConfigs().add(mc);
				
				// add to JList, set as active
				dlm.addElement(mc);
				current_working_config = mc;
			}
			
		} else if(runs.getModel().getSize() == 0){
			// rm_conf object has MatchingConfig objects, but they have not 
			// been inserted into the JList yet
			Iterator<MatchingConfig> it = rm_conf.iterator();
			DefaultListModel dlm = (DefaultListModel)runs.getModel();
			while(it.hasNext()){
				dlm.addElement(it.next());
			}
			current_working_config = (MatchingConfig)dlm.get(0);
		}
		
		session_options.setModel(new SessionOptionsTableModel(current_working_config));
		
		super.paint(g);
	}
}
