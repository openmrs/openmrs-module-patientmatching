package org.regenstrief.linkage.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.regenstrief.linkage.analysis.EMAnalyzer;
import org.regenstrief.linkage.analysis.PairDataSourceAnalysis;
import org.regenstrief.linkage.io.OrderedDataSourceReader;
import org.regenstrief.linkage.io.ReaderProvider;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.RecMatchConfig;

/**
 * Class displays different analysis options available in the record linkaeg GUI
 * 
 * @author jegg
 *
 */

public class AnalysisPanel extends JPanel implements ActionListener{
	RecMatchConfig rm_conf;
	
	private JButton the_button;
	
	public AnalysisPanel(RecMatchConfig rmc){
		super();
		rm_conf = rmc;
		createAnalysisPanel();
	}
	
	public void setRecMatchConfig(RecMatchConfig rmc){
		rm_conf = rmc;
	}
	
	private void createAnalysisPanel(){
		//this.setLayout(new BorderLayout());
		the_button = new JButton("Perform EM Analysis");
		this.add(the_button);
		the_button.addActionListener(this);
	}
	
	private void runEMAnalysis(){
		ReaderProvider rp = new ReaderProvider();
		List<MatchingConfig> mcs = rm_conf.getMatchingConfigs();
		Iterator<MatchingConfig> it = mcs.iterator();
		while(it.hasNext()){
			MatchingConfig mc = it.next();
			
			OrderedDataSourceReader odsr1 = rp.getReader(rm_conf.getLinkDataSource1(), mc);
			OrderedDataSourceReader odsr2 = rp.getReader(rm_conf.getLinkDataSource2(), mc);
			if(odsr1 != null && odsr2 != null){
				// analyze with EM
				org.regenstrief.linkage.io.FormPairs fp2 = new org.regenstrief.linkage.io.FormPairs(odsr1, odsr2, mc, rm_conf.getLinkDataSource1().getTypeTable());
				PairDataSourceAnalysis pdsa = new PairDataSourceAnalysis(fp2);
				EMAnalyzer ema = new EMAnalyzer(rm_conf.getLinkDataSource1(), rm_conf.getLinkDataSource2(), mc);
				LoggingFrame lf = new LoggingFrame(ema);
				//try{
					pdsa.analyzeData();
				//}
				//catch(IOException ioe){
				//	JOptionPane.showMessageDialog(this, "IOException: " + ioe.getMessage() + " while running analysis","Analysis Error", JOptionPane.ERROR_MESSAGE);
				//}
			}
			odsr1.close();
			odsr2.close();
		}
	}
	
	public void actionPerformed(ActionEvent ae){
		if(ae.getSource() == the_button){
			runEMAnalysis();
		}
	}
}
