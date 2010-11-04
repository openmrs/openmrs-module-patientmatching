package org.regenstrief.linkage.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.Record;

public class MatchResultReviewPanel extends JPanel implements ActionListener, ChangeListener, KeyListener{
	public static final int CERTAINTY_LEVELS = 5;
	
	JPanel score_panel, dem_panel, status_panel;
	JTextField row;
	JTextField score;
	JTextField note;
	JRadioButton not_reviewed, match, not_match;
	ButtonGroup review_status;
	JSlider certainty;
	JTextField certainty_val;
	JTable values;
	
	String[] demographics;
	MatchResult mr;
	
	public MatchResultReviewPanel(String[] demographics){
		this.demographics = demographics;
		initGUI();
	}
	
	private void initGUI(){
		// init and add elements of score panel
		score_panel = new JPanel();
		row = new JTextField(5);
		score = new JTextField(12);
		score_panel.add(row);
		//score_panel.add(score);
		
		// init and add elements for demographic values table
		dem_panel = new JPanel();
		dem_panel.setLayout(new BoxLayout(dem_panel, BoxLayout.PAGE_AXIS));
		Object[][] data = new Object[2][demographics.length];
		for(int i = 0; i < demographics.length; i++){
			data[0][i] = "";
			data[1][i] = "";
		}
		values = new JTable(data, demographics);
		
		dem_panel.add(values);
		note = new JTextField(40);
		note.addKeyListener(this);
		dem_panel.add(note);
		
		
		// init and add elements for match status panel
		status_panel = new JPanel();
		status_panel.setLayout(new BoxLayout(status_panel, BoxLayout.PAGE_AXIS));
		not_reviewed = new JRadioButton("Not-Reviewed");
		not_reviewed.addActionListener(this);
		match = new JRadioButton("Match");
		match.addActionListener(this);
		not_match = new JRadioButton("Not-Match");
		not_match.addActionListener(this);
		review_status = new ButtonGroup();
		review_status.add(not_reviewed);
		review_status.add(match);
		review_status.add(not_match);
		String[] levels = new String[CERTAINTY_LEVELS];
		for(int i = 0; i < CERTAINTY_LEVELS; i++){
			levels[i] = Integer.toString(i);
		}
		certainty = new JSlider(JSlider.HORIZONTAL, 1, CERTAINTY_LEVELS, 1);
		certainty.setMajorTickSpacing(1);
		certainty.setMinorTickSpacing(1);
		certainty.setPaintTicks(true);
		certainty.setPaintLabels(true);
		certainty.addChangeListener(this);
		certainty_val = new JTextField(1);
		certainty_val.addActionListener(this);
		status_panel.add(not_reviewed);
		status_panel.add(match);
		status_panel.add(not_match);
		JPanel cpanel = new JPanel();
		cpanel.add(certainty);
		cpanel.add(certainty_val);
		status_panel.add(cpanel);
		
		// add panels to MatchResultReviewPanel
		this.add(score_panel);
		this.add(dem_panel);
		this.add(status_panel);
	}
	
	public void setRow(int i){
		row.setText(Integer.toString(i));
	}
	
	public void setMatchResult(MatchResult mr){
		this.mr = mr;
		score.setText(Double.toString(mr.getScore()));
		demographics = mr.getDemographics().toArray(demographics);
		Record r1 = mr.getRecord1();
		Record r2 = mr.getRecord2();
		Object[][] data = new Object[2][demographics.length];
		for(int i = 0; i < demographics.length; i++){
			data[0][i] = r1.getDemographic(demographics[i]);
			data[1][i] = r2.getDemographic(demographics[i]);
		}
		DemographicReviewTableModel drtm = new DemographicReviewTableModel(data, demographics);
		values.setModel(drtm);
		int status = mr.getMatch_status();
		switch(status){
		case MatchResult.MATCH:
			match.setSelected(true);
			break;
		case MatchResult.NON_MATCH:
			not_match.setSelected(true);
			break;
		default:
			not_reviewed.setSelected(true);
		}
		double c = mr.getCertainty();
		int display_certainty = getUICertaintyLevel(c);
		certainty.setValue(display_certainty);
		certainty_val.setText(Integer.toString(display_certainty));
		note.setText(mr.getNote());
	}
	
	private int getUICertaintyLevel(double certainty){
		int ret = (int) Math.round(( certainty * (CERTAINTY_LEVELS - 1) ) + 1);
		return ret;
	}
	
	private double getMatchResultCertainty(int certainty){
		double ret = (double)(certainty - 1) / (double)(CERTAINTY_LEVELS - 1);
		return ret;
	}

	public void actionPerformed(ActionEvent ae) {
		Object s = ae.getSource();
		if(s instanceof JTextField){
			JTextField source = (JTextField)s;
			try{
				int new_val = Integer.parseInt(source.getText());
				if(new_val < 1){
					new_val = 1;
				} else if(new_val > CERTAINTY_LEVELS){
					new_val = CERTAINTY_LEVELS;
				}
				certainty.setValue(new_val);
				certainty_val.setText(Integer.toString(new_val));
				double mr_certainty =(double)new_val / CERTAINTY_LEVELS;
				if(mr != null){
					mr.setCertainty(mr_certainty);
				}
			}
			catch(NumberFormatException nfe){
				// invalid value in TextField, don't assign anything
			}
		} else if(s instanceof JRadioButton){
			if(s == not_reviewed){
				mr.setMatch_status(MatchResult.UNKNOWN);
			} else if(s == match){
				mr.setMatch_status(MatchResult.MATCH);
			} else if(s == not_match){
				mr.setMatch_status(MatchResult.NON_MATCH);
			}
		}
		
	}

	public void stateChanged(ChangeEvent ce) {
		Object s = ce.getSource();
		if(s instanceof JSlider){
			JSlider source = (JSlider)s;
			if(!source.getValueIsAdjusting()){
				int c = source.getValue();
				// set c to certainty_val, convert to double to MatchResult object value
				certainty_val.setText(Integer.toString(c));
				if(mr != null){
					mr.setCertainty(getMatchResultCertainty(c));
				}
			}
		}
		
	}

	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void keyTyped(KeyEvent arg0) {
		if(mr != null){
			mr.setNote(note.getText());
		}
	}
}
