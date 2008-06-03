package org.regenstrief.linkage.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.regenstrief.linkage.util.CharDelimLDSInspector;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.RecMatchConfig;
import org.regenstrief.linkage.util.XMLTranslator;

/*
 * Class is a rewrite of the original RecMatch class
 * 
 * Originally GUI elements were all in RecMatch class
 * with lots of global variables and poor data abstraction
 * away from the GUI layer.
 */

public class RecMatch implements ActionListener, WindowListener, ChangeListener {
	public static final int CONCURRENT_LINKAGE_RUNS_LIMIT = 1;
	public static final int ROWS_IN_TABLE = 15;
	public static final String PROGRAM_NAME = "Record Linker";
	
	JFrame main_window;
	JTabbedPane tabs;
	SessionsPanel spanel;
	DataPanel dpanel;
	AnalysisPanel apanel;
	RecMatchConfig rm_conf;
	
	private File current_program_config_file;
	
	public RecMatch(File config){
		current_program_config_file = config;
		
		// load the config file if one was given when program was started
		if(config != null){
			rm_conf = XMLTranslator.createRecMatchConfig(XMLTranslator.getXMLDocFromFile(config));
		} else {
			rm_conf = new RecMatchConfig();
		}
		
		// according to Sun, the recommended way of being thread-safe
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	/* create and show the GUI */
		    	initGui();
		    }
		});
		
		
	}
	
	private void initGui(){
		main_window = new JFrame(PROGRAM_NAME);
		main_window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		main_window.addWindowListener(this);
		main_window.setSize(800, 700);
		main_window.setLocationRelativeTo(null);
		
		main_window.setJMenuBar(createMenu());
		
		// add tabs to main_window
		tabs = new JTabbedPane();
		dpanel = new DataPanel(rm_conf);
		tabs.addTab("Data sources", dpanel);
		apanel = new AnalysisPanel(rm_conf);
		tabs.addTab("Analysis", apanel);
		spanel = new SessionsPanel(rm_conf);
		tabs.addTab("Sessions", spanel);
		tabs.addChangeListener(this);
		
		main_window.getContentPane().add(tabs);
		main_window.setVisible(true);
		
		// display data in rm_conf
		if(rm_conf != null){
			if(rm_conf.getLinkDataSource1() != null){
				dpanel.parseDataToTable(DataPanel.TOP);
			}
			if(rm_conf.getLinkDataSource2() != null){
				dpanel.parseDataToTable(DataPanel.BOTTOM);
			}
		}
		
	}
	
	private JMenuBar createMenu(){
		JMenuBar jmb = new JMenuBar();
		JMenu jm;
		JMenuItem jmi;
		
		jm = new JMenu("File");
		jm.setMnemonic(KeyEvent.VK_F);
		jmb.add(jm);
		jmi = new JMenuItem("Open configuration");
		jmi.setMnemonic(KeyEvent.VK_O);
        jmi.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
		jm.add(jmi);
		jmi.addActionListener(this);
		jmi = new JMenuItem("Save configuration");
		jmi.setMnemonic(KeyEvent.VK_S);
        jmi.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
		jm.add(jmi);
		jmi.addActionListener(this);
		jmi = new JMenuItem("Save configuration as . . .");
		jmi.setMnemonic(KeyEvent.VK_A);
        jmi.setAccelerator(KeyStroke.getKeyStroke("ctrl A"));
		jm.add(jmi);
		jmi.addActionListener(this);
		jm.addSeparator();
		jmi = new JMenuItem("Exit");
		jmi.setMnemonic(KeyEvent.VK_X);
		jmi.addActionListener(this);
		jm.add(jmi);
		
		jm = new JMenu("Data Sources");
		jm.setMnemonic(KeyEvent.VK_D);
		jmb.add(jm);
		jmi = new JMenuItem("Configure Data Source A");
		jm.add(jmi);
		jmi.addActionListener(this);
		jmi = new JMenuItem("Configure Data Source B");
		jm.add(jmi);
		jmi.addActionListener(this);
		jm.add(jmi);
		//jm.addSeparator();
		//jmi = new JMenuItem("Run Table Wizard");
		//jmi.setMnemonic(KeyEvent.VK_R);
		//jm.add(jmi);
		//jmi.addActionListener(this);
		//jm.addSeparator();
		
		return jmb;
	}
	
	public static File getFileFromChooser(){
		// launches a JFileChooser and returns a File object
		// if user cancels, return null
		JFileChooser jfc = new JFileChooser();
		int ret = jfc.showOpenDialog(null);
		
		if(ret == JFileChooser.APPROVE_OPTION){
			return jfc.getSelectedFile();
		} else {
			return null;
		}
	}
	
	/*
	 * Methoc called when loading a config file or other changes are made to
	 * the rm_conf object
	 */
	private void updateGUI(){
		dpanel.setRecMatchConfig(rm_conf);
		dpanel.update(main_window.getGraphics());
	}
	
	private void exitProgram(){
		System.exit(0);
	}
	
	private void saveConfig(){
		// should clean up this and saveAsConfig later to remove redundant code;
		// move similar code to writeXMLConfig method
		
		System.out.println("save current program configuration");
		if(current_program_config_file != null){
			if(!writeXMLConfig(rm_conf, current_program_config_file)){
				JOptionPane.showMessageDialog(main_window, "Error writing configuration file");
			}
		} else {
			// need to bring up a file selection dialog
			saveAsConfig();
		}
	}
	
	private void saveAsConfig(){
		// bring up file selection dialog to choose what config file to save the current state in
		System.out.println("save current program configuration");
		File f = saveConfigFilewithChooser();
		if(f == null){
			System.out.println("nevermind . . . .");
		} else {
			System.out.println(f);
			if(!writeXMLConfig(rm_conf, f)){
				JOptionPane.showMessageDialog(main_window, "Error writing configuration file");
			}
		}
	}
	
	private boolean writeXMLConfig(RecMatchConfig rmc, File f){
		return XMLTranslator.writeXMLDocToFile(XMLTranslator.toXML(rmc), f);
	}
	
	private File saveConfigFilewithChooser(){
		// launches JFileChooser to get File object
		// if selection already exists, ask user if they want to overwrite existing file
		// show JFileChooser until they cancel, say yes to over writing, or enter new name
		
		JFileChooser jfc = new JFileChooser();
		boolean escape_condition = false;
		File selection = null;
		
		// possibly have a config already loaded, set that as the selection
		if(!(current_program_config_file == null)){
			jfc.setSelectedFile(current_program_config_file);
		}
		
		while(!escape_condition){
			int ret = jfc.showSaveDialog(main_window);
			if(ret == JFileChooser.APPROVE_OPTION){
				File possibility = jfc.getSelectedFile();
				
				// check if file name has .xml extension, adding it if it does
				String file_name = possibility.getName();
				String extension = file_name.substring(file_name.length() - 4);
				if(extension.compareTo(".xml") != 0){
					// append ".xml" to filename
					possibility = new File(possibility.getPath() + ".xml");
				}
				
				if(possibility.exists()){
					// need to see if user really wants to overwrite
					int choice = JOptionPane.showConfirmDialog(main_window,
							"File " + possibility.getName() + " already exists\nOverwrite File?",
							"File Exists", JOptionPane.YES_NO_OPTION);
					if(choice == JOptionPane.YES_OPTION){
						// will overwrite, set selection to possiblity, and escape loop
						current_program_config_file = selection = possibility;
						escape_condition = true;
					}
				} else {
					current_program_config_file = selection = possibility;
					escape_condition = true;
				}
			} else {
				escape_condition = true;
			}
		}
		
		return selection;
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 * 
	 * ActionListener used for the main menu of the program
	 */
	public void actionPerformed(ActionEvent ae){
		if(ae.getSource() instanceof JMenuItem){
			JMenuItem source = (JMenuItem) ae.getSource();
			
			// check for other menu items
			if(source.getText().equals("Exit")){
				System.out.println("exiting . . . .");
				exitProgram();
			} else if(source.getText().equals("Save configuration")){
				saveConfig();
			} else if(source.getText().equals("Save configuration as . . .")){
				saveAsConfig();
			} else if(source.getText().equals("Open configuration")){
				File config = getFileFromChooser();
				if(config != null){
					rm_conf = XMLTranslator.createRecMatchConfig(XMLTranslator.getXMLDocFromFile(config));
					dpanel.setRecMatchConfig(rm_conf);
					current_program_config_file = config;
					spanel.setRecMatchConfig(rm_conf);
					apanel.setRecMatchConfig(rm_conf);
					// need to reflect the rm_conf object in the GUI
					//updateGUI();
				}
				// error message telling user open failed
				//JOptionPane.showMessageDialog(main_window, "Error opening configuration file");
				
				
			} else if(source.getText().equals("Configure Data Source A")){
				LinkDataSource lds = LinkDataSourceChooser.chooseLinkDataSource();
				if(lds != null){
					lds.setDataSource_ID(0);
					if(lds.getType().equals("CharDelimFile")){
						CharDelimLDSInspector cdldsi = new CharDelimLDSInspector();
						cdldsi.setDefaultDataColumns(lds);
					}
					rm_conf.setLinkDataSource1(lds);
					dpanel.parseDataToTable(DataPanel.TOP);
				}
			} else if(source.getText().equals("Configure Data Source B")){
				LinkDataSource lds = LinkDataSourceChooser.chooseLinkDataSource();
				if(lds != null){
					lds.setDataSource_ID(0);
					if(lds.getType().equals("CharDelimFile")){
						CharDelimLDSInspector cdldsi = new CharDelimLDSInspector();
						cdldsi.setDefaultDataColumns(lds);
					}
					rm_conf.setLinkDataSource2(lds);
					dpanel.parseDataToTable(DataPanel.BOTTOM);
				}
			}
		}
	}
	
	/*
	 * Windowlistener methods
	 */
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
		// copy behaviour from the Exit option in the menu to give the user
		// the option to save their work
		exitProgram();
	}
	
	public void stateChanged(ChangeEvent ce){
		// currently, only the tab selection fires this event
		
		if(ce.getSource() == tabs){
			if(tabs.getSelectedComponent() instanceof SessionsPanel){
				spanel.setGuiElements();
			}
		}
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File config = null;
		if(args.length == 1){
			config = new File(args[0]);
			if(!config.exists()){
				config = null;
			}
		}
		RecMatch rm = new RecMatch(config);
	}
	
}
