package org.regenstrief.linkage.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class TextDisplayFrame extends JFrame implements ActionListener {
	
	JPanel button_panel;
	
	JTextArea jta;
	
	JButton close_window;
	
	public TextDisplayFrame(String title, String text) {
		super(title);
		initGUI();
		jta.setText(text);
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
		this.setSize(500, 500);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == close_window) {
			this.setVisible(false);
		}
	}
}
