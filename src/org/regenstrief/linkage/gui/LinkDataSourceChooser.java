package org.regenstrief.linkage.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.regenstrief.linkage.util.LinkDataSource;

/**
 * Class created to get the information of a LinkDataSource through
 * a GUI, being either a database or flat file
 * 
 * @author jegg
 *
 */

public class LinkDataSourceChooser implements ActionListener{
	
	// create this to return in chooseLinkDataSource()
	LinkDataSource lds;
	
	JTabbedPane tabs;
	JTextField file, delim, table, url, user, driver;
	JPasswordField passwd;
	JButton ok, cancel, choose_file;
	JDialog dialog;
	
	// variables to save for creating the return object
	String name;
	String type;
	String access;
	int id;
	
	public LinkDataSourceChooser(){
		
	}
	
	private JTabbedPane getTabs(){
		tabs = new JTabbedPane();
		JPanel file = getDelimFilePanel();
		JPanel db = getDBPanel();
		
		tabs.addTab("File Datasource", file);
		tabs.addTab("DB Datasource", db);
		
		return tabs;
	}
	
	private JPanel getDBPanel(){
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.PAGE_AXIS));
		
		JPanel line;
		
		line = new JPanel();
		line.add(new JLabel("Database table name:"));
		table = new JTextField(15);
		line.add(table);
		ret.add(line);
		
		line = new JPanel();
		line.add(new JLabel("Database URL:"));
		url = new JTextField(15);
		line.add(url);
		ret.add(line);
		
		line = new JPanel();
		line.add(new JLabel("JDBC Driver:"));
		driver = new JTextField(15);
		line.add(driver);
		ret.add(line);
		
		line = new JPanel();
		line.add(new JLabel("User:"));
		user = new JTextField(15);
		line.add(user);
		ret.add(line);
		
		line = new JPanel();
		line.add(new JLabel("Password:"));
		passwd = new JPasswordField(15);
		passwd.setEchoChar('*');
		line.add(passwd);
		ret.add(line);
		
		return ret;
	}
	
	private JPanel getDelimFilePanel(){
		JPanel ret = new JPanel();
		
		JPanel line;
		
		line = new JPanel();
		line.add(new JLabel("File:"));
		file = new JTextField(15);
		line.add(file);
		choose_file = new JButton("Choose file");
		choose_file.addActionListener(this);
		line.add(choose_file);
		ret.add(line);
		
		line = new JPanel();
		line.add(new JLabel("Delimiter:"));
		delim = new JTextField(3);
		delim.setText("|");
		line.add(delim);
		ret.add(line);
		
		return ret;
	}
	
	private void setResult(){
		if(tabs.getSelectedIndex() == 0){
			name = file.getText();
			type = "CharDelimFile";
			access = delim.getText();
			id = 0;
		} else {
			name = table.getText();
			type = "DataBase";
			access = url + "," + user.getText() + "," + new String(passwd.getPassword());
			id = 0;
		}
		
		lds = new LinkDataSource(name, type, access, id);
	}
	
	public void actionPerformed(ActionEvent ae){
		if(ae.getSource() == choose_file){
			JFileChooser jfc = new JFileChooser();
			int ret = jfc.showOpenDialog(null);
			if(ret == JFileChooser.APPROVE_OPTION){
				File f = jfc.getSelectedFile();
				file.setText(f.getPath());
			}
		} else {
			if(ae.getSource() == ok){
				// create and set LinkDataSource lds
				setResult();
			} else {
				lds = null;
			}
			dialog.setVisible(false);
			dialog.dispose();
		}
		
	}
	
	private LinkDataSource getResult(){
		return lds;
	}
	
	public void showDialog(){
		dialog = getDialog();
		dialog.setVisible(true);
	}
	
	private JDialog getDialog(){
		Frame f = null;
		JDialog dialog = new JDialog(f, "Define datasource", true);
		Container contentPane = dialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(getTabs(), BorderLayout.CENTER);
        
		ok = new JButton("Ok");
		ok.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		JPanel bottom_panel = new JPanel();
		bottom_panel.add(ok);
		bottom_panel.add(cancel);
		
		contentPane.add(bottom_panel, BorderLayout.SOUTH);
        
        dialog.setSize(350, 250);
        
		return dialog;
	}
	
	/**
	 * The method to use to get a LinkDataSource using a GUI.  No verification
	 * of the options is done, such as database URL's or delimiting character
	 * tests
	 * 
	 * @return	the LinkDataSource representing the user's choices
	 */
	public static LinkDataSource chooseLinkDataSource(){
		LinkDataSourceChooser ldsc = new LinkDataSourceChooser();
		ldsc.showDialog();
		
		return ldsc.getResult();
	}
}
