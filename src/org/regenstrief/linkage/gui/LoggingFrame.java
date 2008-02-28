package org.regenstrief.linkage.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.OutputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.regenstrief.linkage.util.LoggingObject;

/**
 * Class takes an object with a getLogger method and adds an
 * OutputStream appender to it.  This class then creates a
 * frame with a text area that displays the logging done by
 * the object until the close button is used.  Then the log
 * appender is removed and the frame set invisible
 * 
 * @author jegg
 *
 */

public class LoggingFrame extends JFrame implements ActionListener{
	
	LoggingObject log_source;
	Appender window_appender;
	
	JTextArea jta;
	JButton close_window;
	
	public LoggingFrame(LoggingObject lo){
		log_source = lo;
		initGUI();
		setLogging();
	}
	
	private void setLogging(){
		OutputStream os = new OutputStreamTextArea(jta);
		Logger l = log_source.getLogger();
		window_appender = new WriterAppender(new PatternLayout("%m%n"), os);
		l.addAppender(window_appender);
	}
	
	private void initGUI(){
		this.setLayout(new BorderLayout());
		JPanel button_panel = new JPanel();
		close_window = new JButton("Close Window");
		button_panel.add(close_window);
		close_window.addActionListener(this);
		this.add(button_panel, BorderLayout.SOUTH);
		
		jta = new JTextArea();
		jta.setEditable(false);
		JScrollPane jsp = new JScrollPane(jta);
		this.add(jsp, BorderLayout.CENTER);
		this.setSize(500, 500);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		
		
	}
	
	public void actionPerformed(ActionEvent ae){
		if(ae.getSource() == close_window){
			log_source.getLogger().removeAppender(window_appender);
			this.setVisible(false);
		}
	}
}
