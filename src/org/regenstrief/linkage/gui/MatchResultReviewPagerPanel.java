package org.regenstrief.linkage.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.db.SavedResultDBConnection;
import org.regenstrief.linkage.matchresult.DBMatchResultStore;
import org.regenstrief.linkage.matchresult.MatchResultStore;

public class MatchResultReviewPagerPanel extends JPanel implements ActionListener{

	public static int VIEWS_PER_PAGE = 4;
	private int view_index;
	private List<MatchResultReviewPanel> rpanels;
	private MatchResultStore mrs;
	
	private JButton next_page, prev_page, goto_mr, open, save, exit;
	private JTextField row, first_unreviewed;
	
	private Hashtable<Integer,MatchResult> reviewed_match_results;
	
	public MatchResultReviewPagerPanel(){
		super();
		mrs = null;
		rpanels = new ArrayList<MatchResultReviewPanel>();
		view_index = 0;
		reviewed_match_results = new Hashtable<Integer,MatchResult>();
		initGUI();
	}
	
	private Date chooseDate(List<Date> options){
		Date ret = null;
		Object[] o = options.toArray();
		ret = (Date)JOptionPane.showInputDialog(
                this,
                "Multiple runs exist in review database.\nWhich run to open?",
                "Select date",
                JOptionPane.PLAIN_MESSAGE,
                null,
                o,
                o[1]);
		
		return ret;
	}
	
	private void initGUI(){
		this.setLayout(new BorderLayout());
		
		// add top section
		JPanel top = new JPanel();
		open = new JButton("Open Fiile");
		open.addActionListener(this);
		save = new JButton("Save Changes");
		save.addActionListener(this);
		JLabel first = new JLabel("First Unreviewed Row:");
		first_unreviewed = new JTextField(6);
		top.add(open);
		top.add(save);
		top.add(first);
		top.add(first_unreviewed);
		
		this.add(top, BorderLayout.PAGE_START);
		
		// create review panels
		MatchResultReviewPanel mrrp;
		JPanel middle = new JPanel();
		middle.setLayout(new BoxLayout(middle, BoxLayout.PAGE_AXIS));
		for(int i = 0; i < VIEWS_PER_PAGE; i++){
			String[] dummy_header = new String[8];
			for(int j = 0; j < dummy_header.length; j++){
				dummy_header[j] = "field";
			}
			mrrp = new MatchResultReviewPanel(dummy_header);
			rpanels.add(mrrp);
			middle.add(mrrp);
		}
		
		this.add(middle, BorderLayout.CENTER);
		
		// create paging buttons
		JPanel bottom = new JPanel();
		prev_page = new JButton("Previous");
		prev_page.addActionListener(this);
		next_page = new JButton("Next");
		next_page.addActionListener(this);
		goto_mr = new JButton("Goto Row");
		goto_mr.addActionListener(this);
		row = new JTextField(4);
		bottom.add(prev_page);
		bottom.add(next_page);
		bottom.add(goto_mr);
		bottom.add(row);
		
		this.add(bottom, BorderLayout.PAGE_END);
	}
	
	
	public static void main(String[] args){
		JFrame window = new JFrame();
		window.setSize(1200, 800);
		MatchResultReviewPagerPanel mrrpp = new MatchResultReviewPagerPanel();
		window.add(mrrpp);
		window.setVisible(true);
		
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public MatchResultStore getMatchResultStore(){
		return mrs;
	}
	
	public void setMatchResultStore(MatchResultStore mrs){
		this.mrs = mrs;
	}
	
	private void updateView(int index){
		// get VIEWS_PER_PAGE MatchResults out of MatchResult store beginning at given index
		// set GUI elements to show new MatchResults
		view_index = index;
		if(index < 0){
			view_index = 0;
		}
		int size = mrs.getSize();
		if(index > size - VIEWS_PER_PAGE){
			view_index = size - VIEWS_PER_PAGE;
		}
		for(int i = 0; i < rpanels.size(); i++){
			MatchResultReviewPanel mrrp = rpanels.get(i);
			MatchResult mr = mrs.getMatchResult(view_index + i);
			mrrp.setMatchResult(mr);
			mrrp.setRow(view_index + i);
			
			reviewed_match_results.put(new Integer(view_index + i), mr);
		}
	}
	
	private void showPrevPage(){
		int new_index = view_index - VIEWS_PER_PAGE;
		if(new_index < 0){
			new_index = 0;
		}
		updateView(new_index);
	}
	
	private void showNextPage(){
		int new_index = view_index + VIEWS_PER_PAGE;
		int max = mrs.getSize();
		if(new_index > max){
			new_index = max;
		}
		updateView(new_index);
	}
	
	private void setFirstUnreviewed(){
		if(mrs instanceof DBMatchResultStore){
			DBMatchResultStore dmrs = (DBMatchResultStore)mrs;
			int first = dmrs.getMinUnknownID();
			first_unreviewed.setText(Integer.toString(first));
		}
	}
	
	private void saveChanges(){
		// iterate over MatchResult objects in reviewed_match_results and update or delete/add them to persist possible changes
		if(mrs != null){
			boolean use_db = mrs instanceof DBMatchResultStore;
			
			Enumeration<Integer> e = reviewed_match_results.keys();
			while(e.hasMoreElements()){
				Integer id = e.nextElement();
				MatchResult reviewed = reviewed_match_results.get(id);
				if(use_db){
					DBMatchResultStore dmrs = (DBMatchResultStore)mrs;
					dmrs.updateMatchResult(id, reviewed.getNote(), reviewed.getMatch_status(), reviewed.getCertainty());
				} else {
					mrs.removeMatchResult(id);
					mrs.addMatchResult(reviewed, id);
				}
				
			}
			
		}
	}

	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource() instanceof JButton){
			JButton source = (JButton)ae.getSource();
			if(source == open){
				// get MatchResultStore set and updateview
				JFileChooser jfc = new JFileChooser();
				int retval = jfc.showOpenDialog(this);
				if(retval == JFileChooser.APPROVE_OPTION){
					File db_file = jfc.getSelectedFile();
					Connection db = SavedResultDBConnection.openDBResults(db_file);
					if(db != null){
						mrs = new DBMatchResultStore(db);
						List<Date> dates = ((DBMatchResultStore)mrs).getDates();
						if(dates.size() > 1){
							Date choice = chooseDate(dates);
							((DBMatchResultStore)mrs).setDate(choice);
						} else {
							((DBMatchResultStore)mrs).setDate(dates.get(0));
						}
						updateView(view_index);
						setFirstUnreviewed();
					}
				}
			} else if(source == next_page){
				showNextPage();
			} else if(source == prev_page){
				showPrevPage();
			} else if(source == save){
				saveChanges();
				setFirstUnreviewed();
			} else if(source == exit){
				
			} else if(source == goto_mr){
				int index = 0;
				try{
					index = Integer.parseInt(row.getText());
				}
				catch(NumberFormatException nfe){
					return;
				}
				updateView(index);
			}
		}
	}
}
