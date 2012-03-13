package org.regenstrief.linkage.gui;

import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class SaveTextDisplayFrame extends TextDisplayFrame {
	JButton save;
	
	public SaveTextDisplayFrame(String title, String text){
		super(title, text);
	}
	
	protected void initGUI(){
		super.initGUI();
		save = new JButton("Save Text to File");
		save.addActionListener(this);
		button_panel.add(save);
	}
	
	public void actionPerformed(ActionEvent ae){
		if(ae.getSource() == save){
			boolean success = saveFile();
			if(!success){
				JOptionPane.showMessageDialog(this,
					    "Error writing text to file",
					    "File error",
					    JOptionPane.ERROR_MESSAGE);
			}
		} else {
			super.actionPerformed(ae);
		}
	}
	
	protected boolean saveFile(){
		JFileChooser jfc = new JFileChooser();
		int ret = jfc.showSaveDialog(this);
		if(ret == JFileChooser.APPROVE_OPTION){
			try{
				File f = jfc.getSelectedFile();
				BufferedWriter out = new BufferedWriter(new FileWriter(f));
				out.write(jta.getText());
				out.flush();
				out.close();
			}
			catch(IOException ioe){
				System.err.println(ioe.getMessage());
				return false;
			}
			
		}
		return true;
	}
}
