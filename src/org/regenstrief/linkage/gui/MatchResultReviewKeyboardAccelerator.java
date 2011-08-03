package org.regenstrief.linkage.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.JTextField;

public class MatchResultReviewKeyboardAccelerator implements KeyListener, ContainerListener{

	public static final MatchResultReviewKeyboardAccelerator INSTANCE = new MatchResultReviewKeyboardAccelerator();
	
	private List<MatchResultReviewPanel> review_panels;
	
	private MatchResultReviewKeyboardAccelerator(){
		
	}
	
	public void setReviewPanelList(List<MatchResultReviewPanel> panels){
		review_panels = panels;
	}

	public void componentAdded(ContainerEvent arg0) {
		addAccelerator(arg0.getChild());
	}
	
	private void addAccelerator(Component comp){
		//System.out.println("added key listener to " + comp);
		comp.addKeyListener(this);
		if(comp instanceof Container){
			Container c = (Container)comp;
			Component[] children = c.getComponents();
			for(int i = 0; i < children.length; i++){
				Component child = children[i];
				addAccelerator(child);
			}
		}
	}

	public void componentRemoved(ContainerEvent arg0) {
		removeAccelerator(arg0.getChild());
	}
	
	private void removeAccelerator(Component comp){
		//System.out.println("removed key listener to " + comp);
		comp.removeKeyListener(this);
		if(comp instanceof Container){
			Container c = (Container)comp;
			Component[] children = c.getComponents();
			for(int i = 0; i < children.length; i++){
				Component child = children[i];
				removeAccelerator(child);
			}
		}
	}

	public void keyPressed(KeyEvent e) {
		if(!(e.getSource() instanceof JTextField)){
			Container c = (Container)e.getSource();
			while(c != null && !(c instanceof MatchResultReviewPanel)){
				c = c.getParent();
			}
			if(c != null){
				int i = review_panels.indexOf(c);
				MatchResultReviewPanel mrrp = review_panels.get(i);
				if(e.getKeyCode() == KeyEvent.VK_M){
					mrrp.setAsMatch();
				} else if(e.getKeyCode() == KeyEvent.VK_N){
					mrrp.setAsNonMatch();
				} else if(e.getKeyCode() == KeyEvent.VK_R){
					mrrp.setAsNotReviewed();
				} else {
					try{
						int new_val =Integer.parseInt(Character.toString(e.getKeyChar()));
						mrrp.setGUICertainty(new_val);
					}
					catch(NumberFormatException nfe){
						
					}
				}
				
			}
			
		}
	}

	public void keyReleased(KeyEvent e) {
		
	}

	public void keyTyped(KeyEvent e) {
		
	}
}
