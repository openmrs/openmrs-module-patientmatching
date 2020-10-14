package org.regenstrief.linkage.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

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
 * Class takes an object with a getLogger method and adds an OutputStream appender to it. This class
 * then creates a frame with a text area that displays the logging done by the object until the
 * close button is used. Then the log appender is removed and the frame set invisible
 * 
 * @author jegg
 */

public class LoggingFrame extends JFrame implements ActionListener {
	
	/*
	 * List of object that will output the log to the current frame
	 */
	protected List<LoggingObject> logSources;
	
	protected JPanel button_panel;
	
	Appender window_appender;
	
	JTextArea jta;
	
	JButton close_window;
	
	public LoggingFrame(String title) {
		super(title);
		logSources = new ArrayList<LoggingObject>();
	}
	
	/**
	 * Add a new object that will output the log to the current frame
	 * 
	 * @param l object that will output the log
	 */
	public void addLoggingObject(LoggingObject l) {
		logSources.add(l);
	}
	
	/**
	 * Prepare the logging frame and add new appender to each logging object to enable each object
	 * output their log to the current frame
	 */
	public void configureLoggingFrame() {
		initGUI();
		OutputStream os = new OutputStreamTextArea(jta);
		window_appender = new WriterAppender(new PatternLayout("%m%n"), os);
		for (LoggingObject lo : logSources) {
			Logger l = lo.getLogger();
			l.addAppender(window_appender);
		}
		configureGUI();
	}
	
	protected void initGUI() {
		this.setLayout(new BorderLayout());
		button_panel = new JPanel();
		close_window = new JButton("Close Window");
		button_panel.add(close_window);
		close_window.addActionListener(this);
		this.add(button_panel, BorderLayout.SOUTH);
		
		jta = new JTextArea();
		jta.setEditable(false);
		JScrollPane jsp = new JScrollPane(jta);
		this.add(jsp, BorderLayout.CENTER);
	}
	
	private void configureGUI() {
		this.setSize(500, 500);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	public void actionPerformed(ActionEvent ae) {
		for (LoggingObject lo : logSources) {
			Logger l = lo.getLogger();
			l.removeAppender(window_appender);
		}
		this.dispose();
	}
}
