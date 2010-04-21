package org.regenstrief.linkage.gui;

import java.awt.Component;
import java.awt.Dimension;
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.regenstrief.linkage.analysis.CloseFormUCalculatorDedup;
import org.regenstrief.linkage.analysis.ClosedFormAnalysis;
import org.regenstrief.linkage.analysis.DataSourceAnalysis;
import org.regenstrief.linkage.analysis.DataSourceFrequency;
import org.regenstrief.linkage.analysis.DedupRandomSampleAnalyzer;
import org.regenstrief.linkage.analysis.EMAnalyzer;
import org.regenstrief.linkage.analysis.FrequencyAnalyzer;
import org.regenstrief.linkage.analysis.MemoryBackedDataSourceFrequency;
import org.regenstrief.linkage.analysis.PairDataSourceAnalysis;
import org.regenstrief.linkage.analysis.RandomSampleAnalyzer;
import org.regenstrief.linkage.io.DataSourceReader;
import org.regenstrief.linkage.io.DedupOrderedDataSourceFormPairs;
import org.regenstrief.linkage.io.FormPairs;
import org.regenstrief.linkage.io.OrderedDataSourceFormPairs;
import org.regenstrief.linkage.io.OrderedDataSourceReader;
import org.regenstrief.linkage.io.ReaderProvider;
import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.FileWritingMatcher;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;
import org.regenstrief.linkage.util.MatchingConfigValidator;
import org.regenstrief.linkage.util.RecMatchConfig;

/**
 * @author james-egg
 *
 *
 */
public class SessionsPanel extends JPanel implements ActionListener, KeyListener,
        ListSelectionListener, ItemListener, FocusListener, TableModelListener, MouseListener{
	public final String DEFAULT_NAME = "New match";
	
	JList runs;
	JTextField run_name;
	JTable session_options;
	JComboBox jcb;
	
	RecMatchConfig rm_conf;
	 
	MatchingConfig current_working_config;

    private boolean write_xml, groupAnalysis;
	
    private JTextField randomSampleTextField;
    private JLabel randomSampleSizeLabel;
    private JTextField thresholdTextField;
    private JCheckBox cbWriteXML;
    private JCheckBox cbGrouping;
	private JRadioButton ucalc_closed, ucalc_rand, mcalc_lock, mcalc_uinclude;
    private ButtonGroup ucalc_group, mcalc_group;
    private JButton calculate_uvalue, calculate_mvalue;
    private JPopupMenu resetm, resetu;
    
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
		
		// init popup menus
		JMenuItem reset;
		resetm = new JPopupMenu();
		reset = new JMenuItem("Reset m values to default");
		reset.addActionListener(this);
		resetm.add(reset);
		resetu = new JPopupMenu();
		reset = new JMenuItem("Reset u values to default");
		reset.addActionListener(this);
		resetu.add(reset);
		
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
        JPanel uvalue_panel = new JPanel();
        uvalue_panel.setBorder(BorderFactory.createTitledBorder("u-Value calculation"));
        ucalc_closed = new JRadioButton("Closed form");
        ucalc_closed.addActionListener(this);
        ucalc_rand = new JRadioButton("Random Sample");
        ucalc_rand.addActionListener(this);
        
        ucalc_group = new ButtonGroup();
        ucalc_group.add(ucalc_closed);
        ucalc_group.add(ucalc_rand);
        
        randomSampleSizeLabel = new JLabel();
        randomSampleSizeLabel.setText("Sample Size");
        randomSampleSizeLabel.setEnabled(false);
        
        randomSampleTextField = new JTextField();
        randomSampleTextField.setText("100000");
        randomSampleTextField.setEnabled(false);
        randomSampleTextField.addFocusListener(this);
        
        calculate_uvalue = new JButton("Calculate u-values");
        calculate_uvalue.addActionListener(this);
        /* ******************************
         * End Random Sample Area
         * ******************************/
        
        /* ******************************
         * M value calculation area
         * ******************************/
        JPanel mvalue_panel = new JPanel();
        mvalue_panel.setBorder(BorderFactory.createTitledBorder("m / u-Value calculation"));
        mcalc_lock = new JRadioButton("Lock existing u-values in EM calculation");
        mcalc_lock.addActionListener(this);
        mcalc_uinclude = new JRadioButton("Calculate u-values along with m-values in EM");
        mcalc_uinclude.addActionListener(this);
        
        mcalc_group = new ButtonGroup();
        mcalc_group.add(mcalc_lock);
        mcalc_group.add(mcalc_uinclude);
        
        calculate_mvalue = new JButton("Calculate values");
        calculate_mvalue.addActionListener(this);
        /* ******************************
         * End M value calculation area
         * ******************************/
        
        /* ******************
         * Linkage Panel Area
         * ******************/
        JPanel linkagePanel = new JPanel();
        linkagePanel.setBorder(BorderFactory.createTitledBorder("Linkage Process"));
        
        cbWriteXML = new JCheckBox("Include XML When Writing Output");
        cbWriteXML.addActionListener(this);
        cbGrouping = new JCheckBox("Perform Grouping When Writing Output");
        cbGrouping.addActionListener(this);
        
        JButton run_link = new JButton();
        run_link.addActionListener(this);
        run_link.setText("Run Linkage Process");
        /* **********************
         * End Linkage Panel Area
         * **********************/
        
        /* *******************************
         * Threshold Area
         * *******************************/
        JPanel thresholdPanel = new JPanel();
        JLabel thresholdLabel = new JLabel();
        thresholdTextField = new JTextField();
        thresholdTextField.addFocusListener(this);
        /* *******************************
         * End Threshold Area
         * *******************************/
        
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
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = new Insets(5, 10, 5, 10);
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
        //gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
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
        uvalue_panel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.7;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        uvalue_panel.add(ucalc_closed, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.7;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        uvalue_panel.add(ucalc_rand, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        uvalue_panel.add(randomSampleSizeLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        uvalue_panel.add(randomSampleTextField, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.7;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        uvalue_panel.add(calculate_uvalue, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        mainPanelSessions.add(uvalue_panel, gridBagConstraints);
        /*
         * End of Random Sampling Parameters Section
         */
        
        /*
         * Begin m-calculation panel section
         */
        mvalue_panel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.7;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        mvalue_panel.add(mcalc_uinclude, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.7;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        mvalue_panel.add(mcalc_lock, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.7;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        mvalue_panel.add(calculate_mvalue, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        mainPanelSessions.add(mvalue_panel, gridBagConstraints);
        
        /*
         * end m-calculation panel section
         */

        /*
         * Linkage Process Section
         * 
         * This section is used to run the linkage process after the 
         * record is analyzed using EM with or without random sampling
         */
        linkagePanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        linkagePanel.add(cbWriteXML, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        linkagePanel.add(cbGrouping, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        linkagePanel.add(run_link, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
       // gridBagConstraints.gridheight = 4;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        mainPanelSessions.add(linkagePanel, gridBagConstraints);
        /*
         * End of Linkage Process Section
         */

        /*
         * Matching Threshold Section
         * 
         * This section is used input a threshold value to determine whether a record is match
         * or not. #807
         */
        thresholdPanel.setBorder(BorderFactory.createTitledBorder("Matching Threshold"));
        thresholdPanel.setLayout(new GridBagLayout());

        thresholdLabel.setText("Threshold Value");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 2);
        thresholdPanel.add(thresholdLabel, gridBagConstraints);

        thresholdTextField.setText("0");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new Insets(5, 2, 5, 5);
        thresholdPanel.add(thresholdTextField, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        //gridBagConstraints.anchor = GridBagConstraints.NORTH;
        mainPanelSessions.add(thresholdPanel, gridBagConstraints);
        /*
         * End of Linkage Process Section
         */

        flowPanel.add(mainPanelSessions);

        list_panel.add(flowPanel);
		
		this.add(list_panel);
	}
	
	private void invalidBlockingSchemeNotification(){
		JOptionPane.showMessageDialog(this,
			    "Invalid blocking scheme; cannot run process",
			    "Invalid Configuration",
			    JOptionPane.ERROR_MESSAGE);
	}
	
	private Component getSessionTable(){
	    SessionOptionsTableModel model = new SessionOptionsTableModel();
	    model.addTableModelListener(this);
		session_options = new JTable(model);
		
		jcb = new JComboBox();
		jcb.addItem(MatchingConfig.ALGORITHMS[0]);
		jcb.addItem(MatchingConfig.ALGORITHMS[1]);
		jcb.addItem(MatchingConfig.ALGORITHMS[2]);
		jcb.addItem(MatchingConfig.ALGORITHMS[3]);
		TableColumn tc = session_options.getColumnModel().getColumn(6);
		tc.setCellEditor(new DefaultCellEditor(jcb));
		
		JScrollPane table_pane = new JScrollPane(session_options);
		table_pane.setPreferredSize(new Dimension(800, 300));
		session_options.getTableHeader().addMouseListener(this);
		
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
            if(m.isUsingRandomSampling()) {
                randomSampleTextField.setText(String.valueOf(m.getRandomSampleSize()));
                ucalc_rand.setSelected(true);
                randomSampleSizeLabel.setEnabled(true);
                randomSampleTextField.setEnabled(true);
            } else {
            	ucalc_closed.setSelected(true);
            }
            if(m.isLockedUValues()){
            	this.mcalc_lock.setSelected(true);
            } else {
            	this.mcalc_uinclude.setSelected(true);
            }
			
            thresholdTextField.setText(String.valueOf(m.getScoreThreshold()));
		}
		current_working_config = mc;
		SessionOptionsTableModel model = new SessionOptionsTableModel(mc);
		model.addTableModelListener(this);
		session_options.setModel(model);
		TableColumn tc = session_options.getColumnModel().getColumn(6);
		tc.setCellEditor(new DefaultCellEditor(jcb));
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
            // swap matching config
            MatchingConfig configA = rm_conf.getMatchingConfigs().remove(index);
            MatchingConfig configB = rm_conf.getMatchingConfigs().remove(index - 1);
            rm_conf.getMatchingConfigs().add(index - 1, configA);
            rm_conf.getMatchingConfigs().add(index, configB);
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
            // swap matching config
            MatchingConfig configA = rm_conf.getMatchingConfigs().remove(index);
            MatchingConfig configB = rm_conf.getMatchingConfigs().remove(index + 1);
            rm_conf.getMatchingConfigs().add(index, configB);
            rm_conf.getMatchingConfigs().add(index + 1, configA);
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
		if(ae.getSource() instanceof JRadioButton){
			JRadioButton source = (JRadioButton)ae.getSource();
			if(source.getText().equals("Closed form")){
				randomSampleTextField.setEnabled(false);
				randomSampleSizeLabel.setEnabled(false);
				if(current_working_config != null) {
                    current_working_config.setUsingRandomSampling(false);
                }
			} else if(source.getText().equals("EM")){
				randomSampleTextField.setEnabled(false);
				randomSampleSizeLabel.setEnabled(false);
				if(current_working_config != null) {
                    current_working_config.setUsingRandomSampling(false);
                }
			} else if(source.getText().equals("Random Sample")){
				randomSampleTextField.setEnabled(true);
				randomSampleSizeLabel.setEnabled(true);
                randomSampleTextField.setEnabled(true);
                randomSampleTextField.requestFocus();
                if(current_working_config != null) {
                    current_working_config.setUsingRandomSampling(true);
                    int sampleSize = Integer.parseInt(randomSampleTextField.getText());
                    current_working_config.setRandomSampleSize(sampleSize);
                }
			} else if(source.getText().equals("Calculate u-values along with m-values in EM")){
				if(current_working_config != null){
					current_working_config.setLockedUValues(false);
				}
			} else if(source.getText().equals("Lock existing u-values in EM calculation")){
				if(current_working_config != null){
					current_working_config.setLockedUValues(true);
				}
			}
		}
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
				runLinkageProcess();
			} else if(source.getText().equals("Remove")){
				Object o = runs.getSelectedValue();
				if(o instanceof MatchingConfig){
					MatchingConfig mc = (MatchingConfig)o;
					removeSessionConfig(mc);
				}
			} else if(source.getText().equals("Calculate u-values")){
				// run either closed form, random sampling, or EM
				if(ucalc_closed.isSelected()){
					calculateClosedUValues();
				} else if(ucalc_rand.isSelected()){
					performRandomSampling();
				}
			} else if(source.getText().equals("Calculate values")){
				if(current_working_config != null){
					runEMAnalysis();
				}
			}
		} else if(ae.getSource() instanceof JCheckBox){
			JCheckBox source = (JCheckBox)ae.getSource();
			if(source.getText().equals("Include XML When Writing Output")){
				write_xml = cbWriteXML.isSelected();
			} else if(source.getText().equals("Perform Grouping When Writing Output")){
				groupAnalysis = cbGrouping.isSelected();
			}
		} else if(ae.getSource() instanceof JMenuItem){
			JMenuItem jmi = (JMenuItem)ae.getSource();
			if(jmi.getText().equals("Reset u values to default")){
				resetUValues();
			} else if(jmi.getText().equals("Reset m values to default")){
				resetMValues();
			}
		}
		
	}
	
	private void runLinkageProcess(){
		if(!MatchingConfigValidator.validMatchingConfig(current_working_config)){
			invalidBlockingSchemeNotification();
			return;
		}
		JFileChooser out_chooser = new JFileChooser();
		int ret = out_chooser.showDialog(this, "Choose output file");
		File out = null;
		File match_file = null;
		if(ret == JFileChooser.APPROVE_OPTION){
			out = out_chooser.getSelectedFile();
			match_file = FileWritingMatcher.writeMatchResults(rm_conf, out, write_xml, groupAnalysis);
            
            if(match_file == null){
                JOptionPane.showMessageDialog(this,
                        "Error writing match result file",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Matching results written to " + match_file,
                        "Matching Successful", JOptionPane.INFORMATION_MESSAGE);
            }
		}
	}
	
	private void resetUValues(){
		List<MatchingConfigRow> rows = current_working_config.getMatchingConfigRows();
		Iterator<MatchingConfigRow> it = rows.iterator();
		while(it.hasNext()){
			MatchingConfigRow mcr = it.next();
			mcr.setNonAgreement(MatchingConfigRow.DEFAULT_NON_AGREEMENT);
		}
		session_options.repaint();
	}
	
	private void resetMValues(){
		List<MatchingConfigRow> rows = current_working_config.getMatchingConfigRows();
		Iterator<MatchingConfigRow> it = rows.iterator();
		while(it.hasNext()){
			MatchingConfigRow mcr = it.next();
			mcr.setAgreement(MatchingConfigRow.DEFAULT_AGREEMENT);
		}
		session_options.repaint();
	}
	
	private void performRandomSampling() {
		if(!MatchingConfigValidator.validMatchingConfig(current_working_config)){
			invalidBlockingSchemeNotification();
			return;
		}
		
        ReaderProvider rp = ReaderProvider.getInstance();
        
        MatchingConfig mc = current_working_config;
        // if the user not choose to use random sampling, then do nothing
        // if the u-values is already locked then do nothing as well
        if(mc.isUsingRandomSampling()) {
            OrderedDataSourceReader odsr1 = rp.getReader(rm_conf.getLinkDataSource1(), mc);
            OrderedDataSourceReader odsr2 = rp.getReader(rm_conf.getLinkDataSource2(), mc);
            if(odsr1 != null && odsr2 != null){
                FormPairs fp2 = null;
                fp2 = new OrderedDataSourceFormPairs(odsr1, odsr2, mc, rm_conf.getLinkDataSource1().getTypeTable());
                
                PairDataSourceAnalysis pdsa = new PairDataSourceAnalysis(fp2);
                
                RandomSampleLoggingFrame frame = new RandomSampleLoggingFrame(mc, session_options);
                
                MatchingConfig mcCopy = (MatchingConfig) mc.clone();
                
                OrderedDataSourceReader rsa_odsr1 = rp.getReader(rm_conf.getLinkDataSource1(), mc);
                OrderedDataSourceReader rsa_odsr2 = rp.getReader(rm_conf.getLinkDataSource2(), mc);
				FormPairs rsa_fp2 = 	new OrderedDataSourceFormPairs(rsa_odsr1, rsa_odsr2, mc, rm_conf.getLinkDataSource1().getTypeTable());
				RandomSampleAnalyzer rsa ;
				if (rm_conf.isDeduplication()) {
					rsa = new DedupRandomSampleAnalyzer(mcCopy, rsa_fp2);
				} else {
					rsa = new RandomSampleAnalyzer(mcCopy, rsa_fp2);
				}
                
                
                pdsa.addAnalyzer(rsa);
                frame.addLoggingObject(rsa);
                    
                frame.configureLoggingFrame();
                pdsa.analyzeData();
                
                
            }
            odsr1.close();
            odsr2.close();
        }
        session_options.repaint();
    }
	
	private void calculateClosedUValues(){
		if(!MatchingConfigValidator.validMatchingConfig(current_working_config)){
			invalidBlockingSchemeNotification();
			return;
		}
		
		if(!MatchingConfigValidator.validMatchingConfig(current_working_config)){
			invalidBlockingSchemeNotification();
			return;
		}
		
		ReaderProvider rp = ReaderProvider.getInstance();
		
		MatchingConfig mc = current_working_config;
		if(rm_conf.isDeduplication()){
			DataSourceFrequency dsf = rm_conf.getDataSourceFrequency1();
			if(dsf == null){
				DataSourceReader dsr = rp.getReader(rm_conf.getLinkDataSource1());
				DataSourceAnalysis dsa = new DataSourceAnalysis(dsr);
				dsf = new MemoryBackedDataSourceFrequency();
				dsa.addAnalyzer(new FrequencyAnalyzer(rm_conf.getLinkDataSource1(), mc, dsf));
				dsa.analyzeData();
				rm_conf.setDataSourceFrequency1(dsf);
			}
			CloseFormUCalculatorDedup cfucd = new CloseFormUCalculatorDedup(mc, dsf);
			cfucd.calculateUValues();
			
		} else {
			OrderedDataSourceReader odsr1 = rp.getReader(rm_conf.getLinkDataSource1(), mc);
			OrderedDataSourceReader odsr2 = rp.getReader(rm_conf.getLinkDataSource2(), mc);
			if(odsr1 != null && odsr2 != null){
				// analyze with EM
				FormPairs fp2 = null;
				fp2 = new OrderedDataSourceFormPairs(odsr1, odsr2, mc, rm_conf.getLinkDataSource1().getTypeTable());
				
				PairDataSourceAnalysis pdsa = new PairDataSourceAnalysis(fp2);
					// create u value analyzer, add to pdsa, and run analysis
				ClosedFormAnalysis cfa = new ClosedFormAnalysis(mc);
				pdsa.addAnalyzer(cfa);
				pdsa.analyzeData();
				}
		}
		session_options.repaint();
	}
	
	private void runEMAnalysis(){
		if(!MatchingConfigValidator.validMatchingConfig(current_working_config)){
			invalidBlockingSchemeNotification();
			return;
		}
		
		ReaderProvider rp = ReaderProvider.getInstance();
		MatchingConfig mc = current_working_config;
		
		OrderedDataSourceReader odsr1 = rp.getReader(rm_conf.getLinkDataSource1(), mc);
		OrderedDataSourceReader odsr2 = rp.getReader(rm_conf.getLinkDataSource2(), mc);
		if(odsr1 != null && odsr2 != null){
			// analyze with EM
		    FormPairs fp2 = null;
		    if (rm_conf.isDeduplication()) {
		        fp2 = new DedupOrderedDataSourceFormPairs(odsr1, mc, rm_conf.getLinkDataSource1().getTypeTable());
		    } else {
		        fp2 = new OrderedDataSourceFormPairs(odsr1, odsr2, mc, rm_conf.getLinkDataSource1().getTypeTable());
		    }
			LoggingFrame frame = new LoggingFrame(mc.getName());
			PairDataSourceAnalysis pdsa = new PairDataSourceAnalysis(fp2);
			
			EMAnalyzer ema = new EMAnalyzer(mc);
			pdsa.addAnalyzer(ema);
			frame.addLoggingObject(ema);
			frame.configureLoggingFrame();
			pdsa.analyzeData();
		}
		odsr1.close();
		odsr2.close();
		session_options.repaint();
		thresholdTextField.setText(Double.toString(mc.getScoreThreshold()));
		
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
        if(e.getSource() == ucalc_rand) {
            if(e.getStateChange() == ItemEvent.SELECTED) {
                randomSampleSizeLabel.setEnabled(true);
                randomSampleTextField.setEnabled(true);
                //lock_uvalue.setEnabled(true);
                randomSampleTextField.requestFocus();
                if(current_working_config != null) {
                    current_working_config.setUsingRandomSampling(true);
                    int sampleSize = Integer.parseInt(randomSampleTextField.getText());
                    current_working_config.setRandomSampleSize(sampleSize);
                }
            } else if(e.getStateChange() == ItemEvent.DESELECTED){
                randomSampleSizeLabel.setEnabled(false);
                randomSampleTextField.setEnabled(false);
                //lock_uvalue.setEnabled(false);
                randomSampleTextField.select(0, 0);
                if(current_working_config != null) {
                    current_working_config.setUsingRandomSampling(false);
                }
            }
        }
    }

	public void focusGained(FocusEvent e) {
		if(randomSampleTextField == e.getSource()) {
			randomSampleTextField.selectAll();
		} else if(thresholdTextField == e.getSource()) {
		    thresholdTextField.selectAll();
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
		} else if (thresholdTextField == e.getSource()) {
		    String thresholdPattern = "(\\d+)(.\\d*){0,1}";
		    String textValue = thresholdTextField.getText();
		    Pattern pattern = Pattern.compile(thresholdPattern);
		    Matcher matcher = pattern.matcher(textValue);
		    if (matcher.matches()) {
                if(current_working_config != null) {
                    double threshold = Double.parseDouble(textValue);
                    current_working_config.setScoreThreshold(threshold);
                }
		    } else {
		        thresholdTextField.setText("0");
		    }
		}
	}

    public void tableChanged(TableModelEvent e) {
        /*--NOTES--*/
        // changes to the SessionOptionsTableModel must be propagated to this method
        // changes that need to be propagated are:
        // - adding new columns
        // - deleting columns
        int rowIndex = e.getFirstRow();
        int columnIndex = e.getColumn();
        TableModel model = (TableModel) e.getSource();
        if(columnIndex == 3) {
            Boolean included = (Boolean) model.getValueAt(rowIndex, columnIndex);
            if(included){
                model.setValueAt(0, rowIndex, 1);
            }
        } else if (columnIndex == 1) {
            Integer blockOrder = (Integer) model.getValueAt(rowIndex, columnIndex);
            if(blockOrder > 0) {
                model.setValueAt(new Boolean(false), rowIndex, 3);
            }
        }
    }
    
    public void mouseClicked(MouseEvent me){
		// important to notice that 'int column' is set to table model's column index, NOT
		// column model's index
		
		// determine what collumn user clicked on
		if(me.getSource() instanceof JTableHeader){
			JTableHeader jth = (JTableHeader)me.getSource();
			TableColumnModel columnModel = jth.getColumnModel();
            int viewColumn = columnModel.getColumnIndexAtX(me.getX());
            
            if(me.getButton() == MouseEvent.BUTTON3){
            	// show popup menu for resetting values
            	if(viewColumn == 4){
            		resetm.show(this, me.getX(), me.getY());
            	} else if(viewColumn == 5){
            		resetu.show(this, me.getX(), me.getY());
            	}
            }
		}
		
    }
    
    public void mousePressed(MouseEvent me){
    	
    }
    
    public void mouseReleased(MouseEvent me){
    	
    }
    
    public void mouseExited(MouseEvent me){
    	
    }
    
    public void mouseEntered(MouseEvent me){
    	
    }
}
