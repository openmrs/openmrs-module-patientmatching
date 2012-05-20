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
	private MatchResultReviewPagerPanel pager;
	private int focus_index;
	
	private MatchResultReviewKeyboardAccelerator(){
		focus_index = 0;
	}
	
	public void setReviewPanelList(List<MatchResultReviewPanel> panels){
		review_panels = panels;
	}
	
	public void setPagerPanel(MatchResultReviewPagerPanel mrppp){
		pager = mrppp;
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
	
	public void setFocusIndex(int index){
		MatchResultReviewPanel mrrp = review_panels.get(focus_index);
		mrrp.showBorder(false);
		
		focus_index = index;
		
		mrrp = review_panels.get(focus_index);
		mrrp.showBorder(true);
	}
	
	public int getPanelIndex(MatchResultReviewPanel mrrp){
		int ret = review_panels.indexOf(mrrp);
		return ret;
	}
	
	public void nextFocus(){
		MatchResultReviewPanel mrrp = review_panels.get(focus_index);
		if(focus_index == review_panels.size() - 1){
			if(!pager.isOnLastPage()){
				mrrp.showBorder(false);
				pager.updateView(pager.getViewIndex() + MatchResultReviewPagerPanel.VIEWS_PER_PAGE);
				focus_index = 0;
				mrrp = review_panels.get(focus_index);
				mrrp.showBorder(true);
			}
		} else {
			mrrp.showBorder(false);
			focus_index++;
			mrrp = review_panels.get(focus_index);
			mrrp.showBorder(true);
		}
	}
	
	public void previousFocus(){
		MatchResultReviewPanel mrrp = review_panels.get(focus_index);
		if(focus_index == 0){
			if(!pager.isOnFirstPage()){
				mrrp.showBorder(false);
				pager.updateView(pager.getViewIndex() - MatchResultReviewPagerPanel.VIEWS_PER_PAGE);
				focus_index = review_panels.size() - 1;
				review_panels.get(focus_index).showBorder(true);
			}
		} else {
			mrrp.showBorder(false);
			focus_index--;
			mrrp = review_panels.get(focus_index);
			mrrp.showBorder(true);
		}
	}

	public void keyPressed(KeyEvent e) {
		if(!(e.getSource() instanceof JTextField)){
			if(e.getKeyCode() == KeyEvent.VK_M){
				MatchResultReviewPanel mrrp = review_panels.get(focus_index);
				mrrp.setAsMatch();
			} else if(e.getKeyCode() == KeyEvent.VK_N){
				MatchResultReviewPanel mrrp = review_panels.get(focus_index);
				mrrp.setAsNonMatch();
			} else if(e.getKeyCode() == KeyEvent.VK_R){
				MatchResultReviewPanel mrrp = review_panels.get(focus_index);
				mrrp.setAsNotReviewed();
			} else if(e.getKeyCode() == KeyEvent.VK_PAGE_UP){
				pager.updateView(pager.getViewIndex() - MatchResultReviewPagerPanel.VIEWS_PER_PAGE);
			} else if(e.getKeyCode() == KeyEvent.VK_PAGE_DOWN){
				pager.updateView(pager.getViewIndex() + MatchResultReviewPagerPanel.VIEWS_PER_PAGE);
			} else if(e.getKeyCode() == KeyEvent.VK_SPACE){
				nextFocus();
			} else if(e.getKeyCode() == KeyEvent.VK_ENTER){
				nextFocus();
			} else if(e.getKeyCode() == KeyEvent.VK_DOWN){
				nextFocus();
			} else if(e.getKeyCode() == KeyEvent.VK_UP){
				previousFocus();
			} else {
				try{
					int new_val =Integer.parseInt(Character.toString(e.getKeyChar()));
					MatchResultReviewPanel mrrp = review_panels.get(focus_index);
					mrrp.setGUICertainty(new_val);
				}
				catch(NumberFormatException nfe){
					
				}
			}
			
			
		}
	}

	public void keyReleased(KeyEvent e) {
		
	}

	public void keyTyped(KeyEvent e) {
		
	}
}
