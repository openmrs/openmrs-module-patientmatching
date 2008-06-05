package org.regenstrief.linkage.gui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.RecMatchConfig;

/**
 * @author james-egg
 *
 *
 */
public class SessionsPanel extends JPanel implements ActionListener, KeyListener, ListSelectionListener, ItemListener, FocusListener{
	public final String DEFAULT_NAME = "New match";
	
	JList runs;
	JTextField run_name;
	JTable session_options;
	JComboBox jcb;
	
	RecMatchConfig rm_conf;
	 
	MatchingConfig current_working_config;

    private JCheckBox randomSampleCheckBox;
    private JCheckBox randomSampleLockCheckBox;
    private JTextField randomSampleTextField;
    private JLabel randomSampleSizeLabel;
	
	public SessionsPanel(RecMatchConfig rmc){
		//super();
		rm_conf = rmc;
		createSessionPanel();
	}
	
	public void setRecMatchConfig(RecMatchConfig rmc){
		rm_conf = rmc;
		updateBlockingRunsList();
	}
	
	private void updateBlockingRunsList() {
        DefaultListModel dlm = new DefaultListModel();
        if(rm_conf != null){
            if(rm_conf.getMatchingConfigs().size() > 0){
                Iterator<MatchingConfig> it = rm_conf.getMatchingConfigs().iterator();
                while(it.hasNext()){
                    dlm.addElement(it.next());
                }
                runs.setSelectedIndex(0);
                current_working_config = rm_conf.getMatchingConfigs().get(0);
                displayThisMatchingConfig(current_working_config);
            } else {
                current_working_config = null;
            }
        }
        runs.setModel(dlm);
	}
	
	private void createSessionPanel(){
		// split the panel into two parts, top for table, bottom for other user interaction
		//JPanel session_panel = new JPanel(new GridLayout(2, 1));
		this.setLayout(new GridLayout(2,1));
		
		// item for the top panel
		this.add(getSessionTable());
		
		// add items for the bottom panel
        /* *********************************
         * list and move up down button area
         * *********************************/
        JPanel list_panel = new JPanel();
        list_panel.setBorder(BorderFactory.createTitledBorder("Session List"));
        
        JButton up = new JButton();
        up.addActionListener(this);
        up.setText("Move Up");
        
        JButton down = new JButton();
        down.addActionListener(this);
        down.setText("Move Down");
        
        runs = new JList();
        runs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        runs.addListSelectionListener(this);
        runs.setCellRenderer(new MatchingConfigCellRenderer());
        
        updateBlockingRunsList();

        JScrollPane blockingRunScrollPane = new JScrollPane();
        blockingRunScrollPane.setViewportView(runs);
        /* ****************************************
         * End of list and move up down button area
         * ****************************************/
        
        /* ******************************
         * Session Entry Modificator Area
         * ******************************/
        JPanel sessionListEntryPanel = new JPanel();
        
        run_name = new JTextField();
        run_name.addKeyListener(this);
        
        JButton remove = new JButton();
        remove.addActionListener(this);
        remove.setText("Remove");
        
        JButton rename = new JButton();
        rename.addActionListener(this);
        rename.setText("Rename");
        
        JButton new_run = new JButton();
        new_run.addActionListener(this);
        new_run.setText("New");

        sessionListEntryPanel.setBorder(BorderFactory.createTitledBorder("Edit Session Label"));
        /* ******************************
         * End Session Entry Modificator Area
         * ******************************/
        
        /* ******************************
         * Random Sample Area
         * ******************************/
        JPanel randomSamplePanel = new JPanel();
        randomSamplePanel.setBorder(BorderFactory.createTitledBorder("Session Global Parameter"));
        
        randomSampleCheckBox = new JCheckBox();
        randomSampleCheckBox.setText("Use Random Sampling");
        randomSampleCheckBox.addItemListener(this);
        
        randomSampleSizeLabel = new JLabel();
        randomSampleSizeLabel.setText("Sample Size");
        randomSampleSizeLabel.setEnabled(false);
        
        randomSampleTextField = new JTextField();
        randomSampleTextField.setText("100000");
        randomSampleTextField.setEnabled(false);
        randomSampleTextField.addFocusListener(this);
        
        randomSampleLockCheckBox = new JCheckBox();
        randomSampleLockCheckBox.setText("Lock u-Values");
        randomSampleLockCheckBox.setEnabled(false);
        randomSampleLockCheckBox.addItemListener(this);
        /* ******************************
         * End Random Sample Area
         * ******************************/
        
        /* ******************
         * Linkage Panel Area
         * ******************/
        JPanel linkagePanel = new JPanel();
        linkagePanel.setBorder(BorderFactory.createTitledBorder("Linkage Process"));
        
        JButton run_link = new JButton();
        run_link.addActionListener(this);
        run_link.setText("Run Linkage Process");
        /* **********************
         * End Linkage Panel Area
         * **********************/
        GridBagConstraints gridBagConstraints;
        JPanel flowPanel = new JPanel();
        JPanel mainPanelSessions = new JPanel();
        JPanel listPanel = new JPanel();
        
        list_panel.setLayout(new GridLayout(1, 0));

        flowPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

        mainPanelSessions.setLayout(new GridBagLayout());

        /*
         * Blocking Run List Section
         * 
         * Blocking run list section. This section contains list of
         * blocking runs and two button (up and down) to control the sequence of
         * the blocking run process.
         */
        listPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 0.7;
        gridBagConstraints.insets = new Insets(1, 1, 1, 1);
        listPanel.add(blockingRunScrollPane, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 15;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 0, 0, 2);
        listPanel.add(up, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(5, 2, 0, 0);
        listPanel.add(down, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = new Insets(7, 10, 8, 10);
        mainPanelSessions.add(listPanel, gridBagConstraints);
        /*
         * End of Blocking Run List Section
         */
        
        /*
         * Blocking Run Modifier Section
         * 
         * This sections is used to update the blocking runs list. User
         * can add, remove or rename a blocking run. The text field will
         * display current active blocking run.
         */
        sessionListEntryPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        sessionListEntryPanel.add(new_run, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        sessionListEntryPanel.add(run_name, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        sessionListEntryPanel.add(rename, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        sessionListEntryPanel.add(remove, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        mainPanelSessions.add(sessionListEntryPanel, gridBagConstraints);
        /*
         * End of Blocking Run Modifier Section
         */
        
        /*
         * Random Sampling Parameters Section
         * 
         * This section is used to modify the random sampling parameter
         * that will be used to generate the u-values. User can specify
         * whether to use random sampling and the sample size
         */
        randomSamplePanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.7;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        randomSamplePanel.add(randomSampleCheckBox, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        randomSamplePanel.add(randomSampleSizeLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        randomSamplePanel.add(randomSampleTextField, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.7;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        randomSamplePanel.add(randomSampleLockCheckBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        mainPanelSessions.add(randomSamplePanel, gridBagConstraints);
        /*
         * End of Random Sampling Parameters Section
         */

        /*
         * Linkage Process Section
         * 
         * This section is used to run the linkage process after the 
         * record is analyzed using EM with or without random sampling
         */
        linkagePanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.7;
        gridBagConstraints.insets = new Insets(0, 5, 5, 100);
        linkagePanel.add(run_link, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        mainPanelSessions.add(linkagePanel, gridBagConstraints);
        /*
         * End of Linkage Process Section
         */

        flowPanel.add(mainPanelSessions);

        list_panel.add(flowPanel);
		
		this.add(list_panel);
	}
	
	private Component getSessionTable(){
		session_options = new JTable(new SessionOptionsTableModel());		
		
		jcb = new JComboBox();
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
		
		// only display runs data if there's actually mc in the list model
		if(runs.getModel().getSize() > 0) {
			int selectedIndex = runs.getSelectedIndex();
			// this special case when a user already load they're config file
			// no item in the blocking runs is selected. pick the first blocking
			// runs
			if(selectedIndex == -1) {
				selectedIndex = 0;
			}
			// select the first element in the list or current selected index
			// (redundant selection process)
			runs.setSelectedIndex(selectedIndex);
			MatchingConfig m = (MatchingConfig) runs.getModel().getElementAt(selectedIndex);
			// update the text field to reflect currently selected element in the list
			run_name.setText(m.getName());
			
			// update the random sample size and check box based on the matching
			// config data
            if(mc.isUsingRandomSampling()) {
                randomSampleTextField.setText(String.valueOf(m.getRandomSampleSize()));
            }
			randomSampleCheckBox.setSelected(mc.isUsingRandomSampling());
			randomSampleSizeLabel.setEnabled(mc.isUsingRandomSampling());
            randomSampleTextField.setEnabled(mc.isUsingRandomSampling());
            randomSampleLockCheckBox.setSelected(mc.isLockedUValues());
		}
		current_working_config = mc;
		session_options.setModel(new SessionOptionsTableModel(mc));
		TableColumn tc = session_options.getColumnModel().getColumn(6);
		tc.setCellEditor(new DefaultCellEditor(jcb));
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
			rm_conf.getMatchingConfigs().remove(mc);
			
			// set another item to be selected
			if(runs.getModel().getSize() <= old_index){
				// set the new selected object as the last item
				if(runs.getModel().getSize() == 0){
					// removed last one, clear table to clear everything and crate a blank config
					setGuiElements();
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
					current_working_config = mc;
					displayThisMatchingConfig(current_working_config);
				}
			}
		}
		
	}
	
	public void actionPerformed(ActionEvent ae){
		if(ae.getSource() instanceof JButton){
			JButton source = (JButton)ae.getSource();
			System.out.println("Source: " + source.getText());
			if(source.getText().equals("Move Up")){
				int index = runs.getSelectedIndex();
				moveUpSessionConfig(index);
			} else if(source.getText().equals("Move Down")){
				int index = runs.getSelectedIndex();
				moveDownSessionConfig(index);
			} else if(source.getText().equals("Rename")){
				renameSessionConfig();
			} else if(source.getText().equals("New")){
				MatchingConfig mc = makeNewMatchingConfig();
				rm_conf.getMatchingConfigs().add(mc);
				DefaultListModel dlm = (DefaultListModel)runs.getModel();
				dlm.addElement(mc);
				runs.setSelectedIndex(dlm.getSize() - 1);
				displayThisMatchingConfig(mc);
			} else if(source.getText().equals("Run Linkage Process")){
				JFileChooser out_chooser = new JFileChooser();
				int ret = out_chooser.showDialog(this, "Choose output file");
				File out, match_file;
				if(ret == JFileChooser.APPROVE_OPTION){
					int choice = JOptionPane.showConfirmDialog(this,"Write XML output file?  Writing the XML output file can consume a lot of memory, and if the VM is not initialized with enough, it can fail","XML Output File",JOptionPane.YES_NO_OPTION);
					boolean write_xml;
					if(choice == JOptionPane.YES_OPTION){
						write_xml = true;
					} else {
						write_xml = false;
					}
					out = out_chooser.getSelectedFile();
					match_file = FileWritingMatcher.writeMatchResults(rm_conf, out, write_xml);
				} else {
					match_file = FileWritingMatcher.writeMatchResults(rm_conf);
				}
				
				if(match_file == null){
					JOptionPane.showMessageDialog(this, "Error writing match result file", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(this, "Matching results written to " + match_file, "Matching Successful", JOptionPane.INFORMATION_MESSAGE);
				}
			} else if(source.getText().equals("Remove")){
				Object o = runs.getSelectedValue();
				if(o instanceof MatchingConfig){
					MatchingConfig mc = (MatchingConfig)o;
					removeSessionConfig(mc);
				}
			}
		}
		
	}
	
	/*
	 * Method called to create a new MatchingConfig when the user selecs this pane for the
	 * first time (caught in the paint method, making sure there is at least one MatchingConfig
	 * to show in the table) or when the user clicks on the "New" button.  The main behaviour 
	 * requested is to have the order of the rows in the table reflect the include ordering
	 * of the included data columns from the Data tab
	 */
	private MatchingConfig makeNewMatchingConfig(){
		Hashtable<String, DataColumn> col_names = rm_conf.getLinkDataSource1().getIncludedDataColumns();
		String[] names = new String[col_names.keySet().size()];
		Enumeration<String> e = col_names.keys();
		while(e.hasMoreElements()){
			String name = e.nextElement();
			DataColumn dc = col_names.get(name);
			names[dc.getIncludePosition()] = name;
		}
		MatchingConfig ret = new MatchingConfig(DEFAULT_NAME, names);
		return ret;
	}
	
	/**
	 * Method first adds a check to see what MatchingConfig to set as active
	 * for the SessionOptionsTable to display
	 */
	public void setGuiElements(){
		if(rm_conf.getMatchingConfigs().size() == 0){
			DefaultListModel dlm = (DefaultListModel)runs.getModel();
			dlm.clear();
			// need to create an empty one to display in session options table, and add to rm_conf
			// but only if the two link data sources are both defined
			if(rm_conf.getLinkDataSource1() != null){
				MatchingConfig mc = makeNewMatchingConfig();
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
		
		displayThisMatchingConfig(current_working_config);
		
	}

    public void itemStateChanged(ItemEvent e) {
    	// update the ui based on whether the use random sample is checked or not
        if(e.getSource() == randomSampleCheckBox) {
            if(e.getStateChange() == ItemEvent.SELECTED) {
                randomSampleSizeLabel.setEnabled(true);
                randomSampleTextField.setEnabled(true);
                randomSampleLockCheckBox.setEnabled(true);
                randomSampleTextField.requestFocus();
                if(current_working_config != null) {
                    current_working_config.setUsingRandomSampling(true);
                    int sampleSize = Integer.parseInt(randomSampleTextField.getText());
                    current_working_config.setRandomSampleSize(sampleSize);
                }
            } else if(e.getStateChange() == ItemEvent.DESELECTED){
                randomSampleSizeLabel.setEnabled(false);
                randomSampleTextField.setEnabled(false);
                randomSampleLockCheckBox.setEnabled(false);
                randomSampleTextField.select(0, 0);
                if(current_working_config != null) {
                    current_working_config.setUsingRandomSampling(false);
                }
            }
        } else if(e.getSource() == randomSampleLockCheckBox) {
            if(e.getStateChange() == ItemEvent.SELECTED) {
                randomSampleSizeLabel.setEnabled(false);
                randomSampleTextField.select(0, 0);
                randomSampleTextField.setEnabled(false);
                if(current_working_config != null) {
                    current_working_config.setLockedUValues(true);
                }
            } else if(e.getStateChange() == ItemEvent.DESELECTED){
                randomSampleSizeLabel.setEnabled(true);
                randomSampleTextField.setEnabled(true);
                if(current_working_config != null) {
                    current_working_config.setLockedUValues(false);
                }
            }
        }
    }

	public void focusGained(FocusEvent e) {
		if(randomSampleTextField == e.getSource()) {
			randomSampleTextField.selectAll();
		}
	}

	public void focusLost(FocusEvent e) {
		if(randomSampleTextField == e.getSource()) {
			// only allows positive digit in the text field
			String digit = "\\d+";
			String textValue = randomSampleTextField.getText();
			Pattern pattern = Pattern.compile(digit);
			Matcher matcher = pattern.matcher(textValue);
			if (matcher.matches()) {
				if(current_working_config != null) {
					int sampleSize = Integer.parseInt(textValue);
					current_working_config.setRandomSampleSize(sampleSize);
				}
			} else {
				// should be showing a message using option pane here
				randomSampleTextField.setText("100000");
			}
		}
	}
}
