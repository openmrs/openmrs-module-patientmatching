package org.regenstrief.linkage.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.JTextField;

public class MatchResultReviewKeyboardAccelerator implements KeyListener, ContainerListener {

	public static final MatchResultReviewKeyboardAccelerator INSTANCE = new MatchResultReviewKeyboardAccelerator();
	
	private List<MatchResultReviewPanel> review_panels;
	private MatchResultReviewPagerPanel pager;
	private int focus_index;
	
	private MatchResultReviewKeyboardAccelerator() {
		focus_index = 0;
	}
	
	public void setReviewPanelList(final List<MatchResultReviewPanel> panels) {
		review_panels = panels;
	}
	
	public void setPagerPanel(final MatchResultReviewPagerPanel mrppp) {
		pager = mrppp;
	}

	@Override
	public void componentAdded(final ContainerEvent arg0) {
		addAccelerator(arg0.getChild());
	}
	
	private void addAccelerator(final Component comp) {
		comp.addKeyListener(this);
		if (comp instanceof Container) {
			for (final Component child : ((Container) comp).getComponents()) {
				addAccelerator(child);
			}
		}
	}

	@Override
	public void componentRemoved(final ContainerEvent arg0) {
		removeAccelerator(arg0.getChild());
	}
	
	private void removeAccelerator(final Component comp) {
		comp.removeKeyListener(this);
		if (comp instanceof Container) {
			for (final Component child : ((Container) comp).getComponents()) {
				removeAccelerator(child);
			}
		}
	}
	
	public void setFocusIndex(final int index) {
		MatchResultReviewPanel mrrp = review_panels.get(focus_index);
		mrrp.showBorder(false);
		
		focus_index = index;
		
		mrrp = review_panels.get(focus_index);
		mrrp.showBorder(true);
	}
	
	public int getPanelIndex(final MatchResultReviewPanel mrrp) {
		return review_panels.indexOf(mrrp);
	}
	
	public void nextFocus() {
		MatchResultReviewPanel mrrp = review_panels.get(focus_index);
		if (focus_index == review_panels.size() - 1) {
			if (!pager.isOnLastPage()) {
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
	
	public void previousFocus() {
		MatchResultReviewPanel mrrp = review_panels.get(focus_index);
		if (focus_index == 0) {
			if (!pager.isOnFirstPage()) {
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

	@Override
	public void keyPressed(final KeyEvent e) {
		if (e.getSource() instanceof JTextField) {
			return;
		}
		final int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_M) {
			review_panels.get(focus_index).setAsMatch();
		} else if (keyCode == KeyEvent.VK_N) {
			review_panels.get(focus_index).setAsNonMatch();
		} else if (keyCode == KeyEvent.VK_R) {
			review_panels.get(focus_index).setAsNotReviewed();
		} else if (keyCode == KeyEvent.VK_PAGE_UP) {
			pager.updateView(pager.getViewIndex() - MatchResultReviewPagerPanel.VIEWS_PER_PAGE);
		} else if (keyCode == KeyEvent.VK_PAGE_DOWN) {
			pager.updateView(pager.getViewIndex() + MatchResultReviewPagerPanel.VIEWS_PER_PAGE);
		} else if (keyCode == KeyEvent.VK_SPACE) {
			nextFocus();
			e.consume();
		} else if (keyCode == KeyEvent.VK_TAB) {
			if (e.isShiftDown()) {
				previousFocus();
			} else {
				nextFocus();
			}
			e.consume();
		} else if (keyCode == KeyEvent.VK_ENTER) {
			nextFocus();
		} else if (keyCode == KeyEvent.VK_DOWN) {
			nextFocus();
			e.consume();
		} else if (keyCode == KeyEvent.VK_UP) {
			previousFocus();
			e.consume();
		} else {
			try {
				review_panels.get(focus_index).setGUICertainty(Integer.parseInt(Character.toString(e.getKeyChar())));
			} catch (final NumberFormatException nfe) {
			}
		}
	}

	@Override
	public void keyReleased(final KeyEvent e) {
	}

	@Override
	public void keyTyped(final KeyEvent e) {
	}
}
