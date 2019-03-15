package org.regenstrief.linkage.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.Record;

public class MatchResultReviewPanel extends JPanel implements ActionListener, ChangeListener, KeyListener{
	private static final long serialVersionUID = 1L;
	public static final int CERTAINTY_LEVELS = 4;
	public static final Color FOCUS_COLOR = new Color(109,123,141);
	
	JPanel score_panel, dem_panel, status_panel;
	JTextField row;
	JTextField score;
	JTextField note;
	JTextField certainty_val;
	JTable values;
	
	List<String> order;
	Set<String> no_display;
	
	String[] demographics;
	MatchResult mr;
	
	int rowValue;
	
	boolean focused;
	
	public MatchResultReviewPanel(String[] demographics){
		this.demographics = demographics;
		initGUI();
	}
	
	public void setFocus(){
		certainty_val.requestFocus();
		showBorder(true);
	}
	
	public void clearFocus(){
		showBorder(false);
	}
	
	public void setOrder(List<String> order){
		this.order = order;
	}
	
	public void setNonDisplayFields(Set<String> fields){
		no_display = fields;
	}
	
	private void initGUI(){
		this.addContainerListener(MatchResultReviewKeyboardAccelerator.INSTANCE);
		
		this.setLayout(new BorderLayout());
		
		// init and add elements of score panel
		score_panel = new JPanel();
		JPanel spanel = new JPanel();
		spanel.setLayout(new BoxLayout(spanel, BoxLayout.PAGE_AXIS));
		JLabel row_label = new JLabel("Row:");
		row = new JTextField(5);
		//row.setEditable(false);
		row.setEditable(false);
		score = new JTextField(12);
		score.setEditable(false);
		spanel.add(row_label);
		spanel.add(row);
		score_panel.add(spanel);
		//score_panel.add(score);
		
		// init and add elements for demographic values table
		dem_panel = new JPanel();
		dem_panel.setLayout(new BorderLayout());
		Object[][] data = new Object[2][demographics.length];
		for(int i = 0; i < demographics.length; i++){
			data[0][i] = "";
			data[1][i] = "";
		}
		values = new JTable();
		values.setEnabled(false);
		DemographicReviewTableModel drtm = new DemographicReviewTableModel(data, demographics);
		values.setModel(drtm);
		
		
		JPanel dem_top = new JPanel();
		dem_top.setLayout(new BorderLayout());
		JLabel jl = new JLabel("Note:");
		JPanel dem_bottom = new JPanel();
		dem_bottom.add(jl);
		add(Box.createRigidArea(new Dimension(5,0)));
		note = new JTextField(40);
		note.addKeyListener(this);
		dem_bottom.add(note);
		
		dem_top.add(values.getTableHeader(), BorderLayout.PAGE_START);
		dem_top.add(values, BorderLayout.CENTER);
		
		dem_panel.add(dem_top, BorderLayout.PAGE_START);
		dem_panel.add(dem_bottom, BorderLayout.PAGE_END);
		
		
		// init and add elements for match status panel
		status_panel = new JPanel();
		//status_panel.addKeyListener(this);
		status_panel.setLayout(new BoxLayout(status_panel, BoxLayout.Y_AXIS));
		certainty_val = new JTextField(1);
		certainty_val.setEditable(false);
		certainty_val.addActionListener(this);
		JPanel cpanel = new JPanel();
		cpanel.add(new JLabel("Certainty (1-4):"));
		cpanel.add(certainty_val);
		status_panel.add(cpanel);
		status_panel.add(new JLabel("1 = Certain Non-Match"));
		status_panel.add(new JLabel("2 = Probable Non-Match"));
		status_panel.add(new JLabel("3 = Probable Match"));
		status_panel.add(new JLabel("4 = Certain Match"));
		
		// add panels to MatchResultReviewPanel
		this.add(score_panel, BorderLayout.WEST);
		this.add(dem_panel, BorderLayout.CENTER);
		this.add(status_panel, BorderLayout.EAST);
		score_panel.addKeyListener(MatchResultReviewKeyboardAccelerator.INSTANCE);
	}
	
	public void setRow(int i){
		row.setText(Integer.toString(i));
		rowValue = i;
	}
	
	public void setMatchResult(MatchResult mr){
		this.mr = mr;
		score.setText(Double.toString(mr.getScore()));
		//demographics = mr.getDemographics().toArray(demographics);
		HashSet<String> dem_set = new HashSet<String>();
		dem_set.addAll(mr.getRecord1().getDemographics().keySet());
		dem_set.addAll(mr.getRecord2().getDemographics().keySet());
		List<String> fields = new ArrayList<String>();
		
		if(order != null){
			fields.addAll(order);
			dem_set.removeAll(fields);
		}
		
		fields.addAll(dem_set);
		if(no_display != null){
			fields.removeAll(no_display);
		}
		demographics = fields.toArray(demographics);
		Record r1 = mr.getRecord1();
		Record r2 = mr.getRecord2();
		Object[][] data = new Object[2][demographics.length];
		boolean[] matches = new boolean[demographics.length];
		for(int i = 0; i < demographics.length; i++){
			String val1 = r1.getDemographic(demographics[i]);
			String val2 = r2.getDemographic(demographics[i]);
			if(val1 == null){
				val1 = "";
			}
			if(val2 == null){
				val2 = "";
			}
			data[0][i] = val1;
			data[1][i] = val2;
			matches[i] = false;
			if(val1.equals(val2) && !val1.equals("")){
				matches[i] = true;
			}
			
		}
		DemographicReviewTableModel drtm = new DemographicReviewTableModel(data, demographics);
		values.setModel(drtm);
		values.setDefaultRenderer(Object.class, new MatchResultReviewTableCellRenderer(matches));
		
		// set column widths
		int margin = 5;
		for (int i = 0; i < values.getColumnCount(); i++) {
            int vColIndex = i;
            TableColumnModel colModel  = values.getColumnModel();
            TableColumn col = colModel.getColumn(vColIndex);
            int width = 0;
            
            // Get width of column header
            TableCellRenderer renderer = col.getHeaderRenderer();
            
            if (renderer == null) {
                renderer = values.getTableHeader().getDefaultRenderer();
            }

            Component comp = renderer.getTableCellRendererComponent(values, col.getHeaderValue(), false, false, 0, 0);
            width = comp.getPreferredSize().width;
            
            // Get maximum width of column data
            for (int r = 0; r < values.getRowCount(); r++) {
                renderer = values.getCellRenderer(r, vColIndex);
                comp = renderer.getTableCellRendererComponent(values, values.getValueAt(r, vColIndex), false, false,r, vColIndex);
                width = Math.max(width, comp.getPreferredSize().width);
            }

            // Add margin
            width += 2 * margin;

            // Set the width
            if(width > 100){
            	width = 100;
            }
            col.setPreferredWidth(width);
            //col.setMaxWidth(100);
        }
		
		certainty_val.setText((mr.getMatch_status() == MatchResult.UNKNOWN) ? "" : Integer.toString(getUICertaintyLevel(mr.getCertainty())));
		
		note.setText(mr.getNote());
	}
	
	private int getUICertaintyLevel(double certainty){
		int ret = (int) Math.round(( certainty * (CERTAINTY_LEVELS - 1) ) + 1);
		return ret;
	}
	
	private void setMatchResultCertainty(MatchResult mr, int certainty){
		if (mr == null) {
			System.err.println("Called setMatchResultCertainty for a panel with no MatchResult");
		} else {
			final Date now = new Date();
			MatchResultReviewPagerPanel.printReviewLog(rowValue + "|" + mr.getRecord1().getUID() + "|" + mr.getRecord2().getUID() + "|" + certainty + "|" + now.getTime() + "|" + now);
			certainty_val.setText(Integer.toString(certainty));
			mr.setCertainty((double)(certainty - 1) / (double)(CERTAINTY_LEVELS - 1));
			mr.setMatch_status((certainty > (CERTAINTY_LEVELS / 2)) ? MatchResult.MATCH : MatchResult.NON_MATCH);
		}
	}
	
	public void showBorder(boolean display){
		focused = display;
		if(display){
			this.setBorder(BorderFactory.createLineBorder(FOCUS_COLOR, 3));
		} else {
			this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 0));
		}
		
	}
	
	public boolean setGUICertainty(int c){
		if(c > 0 && c <= CERTAINTY_LEVELS){
			setMatchResultCertainty(mr, c);
			return true;
		}
		return false;
	}

	@Override
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
				setMatchResultCertainty(mr, new_val);
			}
			catch(NumberFormatException nfe){
				// invalid value in TextField, don't assign anything
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent ce) {
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// only handles typing in note field
		if(arg0.getSource() == note){
			mr.setNote(note.getText());
		}
	}
}
