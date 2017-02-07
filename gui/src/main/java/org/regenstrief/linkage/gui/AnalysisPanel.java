package org.regenstrief.linkage.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.MatchVector;
import org.regenstrief.linkage.analysis.AverageFrequencyAnalyzer;
import org.regenstrief.linkage.analysis.ClosedFormAnalyzer;
import org.regenstrief.linkage.analysis.ClosedFormDedupAnalyzer;
import org.regenstrief.linkage.analysis.DataSourceAnalysis;
import org.regenstrief.linkage.analysis.DataSourceFrequency;
import org.regenstrief.linkage.analysis.EMAnalyzer;
import org.regenstrief.linkage.analysis.EntropyAnalyzer;
import org.regenstrief.linkage.analysis.FrequencyAnalyzer;
import org.regenstrief.linkage.analysis.MaximumEntropyAnalyzer;
import org.regenstrief.linkage.analysis.MemoryBackedDataSourceFrequency;
import org.regenstrief.linkage.analysis.MutualInformationAnalyzer;
import org.regenstrief.linkage.analysis.NullAnalyzer;
import org.regenstrief.linkage.analysis.ObservedVectorAnalyzer;
import org.regenstrief.linkage.analysis.PairDataSourceAnalysis;
import org.regenstrief.linkage.analysis.RandomSampleAnalyzer;
import org.regenstrief.linkage.analysis.RecordFrequencies;
import org.regenstrief.linkage.analysis.SummaryStatisticsStore;
import org.regenstrief.linkage.analysis.UniqueAnalyzer;
import org.regenstrief.linkage.analysis.ValueFrequencyAnalyzer;
import org.regenstrief.linkage.analysis.VectorTable;
import org.regenstrief.linkage.analysis.VectorValuesFrequencyAnalyzer;
import org.regenstrief.linkage.io.DataSourceReader;
import org.regenstrief.linkage.io.DedupOrderedDataSourceFormPairs;
import org.regenstrief.linkage.io.FormPairs;
import org.regenstrief.linkage.io.OrderedDataSourceFormPairs;
import org.regenstrief.linkage.io.OrderedDataSourceReader;
import org.regenstrief.linkage.io.ReaderProvider;
import org.regenstrief.linkage.util.FSSyntheticRecordGenerator;
import org.regenstrief.linkage.util.FileWritingMatcher;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MUSyntheticRecordGenerator;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.RecMatchConfig;
import org.regenstrief.linkage.util.SyntheticRecordGenerator;

/**
 * Class displays different analysis options available in the record linkage GUI
 * 
 * @author jegg
 *
 *I have been to this code to show my first day experience in handling tickets. 
 *Mehari Kassahun
 */

public class AnalysisPanel extends JPanel implements ActionListener{
	private static final long serialVersionUID = -6402375274052004924L;

	RecMatchConfig rm_conf;
	
	private JButton random_button;
	
	private JButton em_button, vector_button, summary_button, freq_button, closed_form_button, vector_obs_button, synthetic_button, mi_score_button;
	
	public AnalysisPanel(RecMatchConfig rmc){
		super();
		rm_conf = rmc;
		createAnalysisPanel();
	}
	
	public void setRecMatchConfig(RecMatchConfig rmc){
		rm_conf = rmc;
	}
	
	private void createAnalysisPanel(){
		FlowLayout fl = new FlowLayout();
		JPanel row1 = new JPanel();
		JPanel row2 = new JPanel();
		JPanel row3 = new JPanel();
		row1.setLayout(fl);
		row2.setLayout(fl);
		row3.setLayout(fl);
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.add(row1);
		this.add(row2);
		this.add(row3);
		
	    random_button = new JButton("Perform Random Sampling");
        row1.add(random_button);
        random_button.addActionListener(this);
        
		em_button = new JButton("Perform EM Analysis");
		row1.add(em_button);
		em_button.addActionListener(this);
		
		vector_button = new JButton("View score tables");
		row1.add(vector_button);
		vector_button.addActionListener(this);
		
		summary_button = new JButton("Perform Summary Statistic Analyses");
		row1.add(summary_button);
		summary_button.addActionListener(this);
		
		freq_button = new JButton("Perform Frequency Analysis");
		row2.add(freq_button);
		freq_button.addActionListener(this);
		
		closed_form_button = new JButton("Perform Closed U value calculation");
		row2.add(closed_form_button);
		closed_form_button.addActionListener(this);
		
		vector_obs_button = new JButton("Perform Vector Observation Analysis");
		row2.add(vector_obs_button);
		vector_obs_button.addActionListener(this);
		
		synthetic_button = new JButton("Create Synthetic Data");
		row2.add(synthetic_button);
		synthetic_button.addActionListener(this);
		
		mi_score_button = new JButton("Calculate Mutual Information Score");
		row3.add(mi_score_button);
		mi_score_button.addActionListener(this);
		
		this.add(Box.createVerticalGlue());
	}
	
	private void runEMAnalysis(){
		ReaderProvider rp = ReaderProvider.getInstance();
		List<MatchingConfig> mcs = rm_conf.getMatchingConfigs();
		Iterator<MatchingConfig> it = mcs.iterator();
		while(it.hasNext()){
			MatchingConfig mc = it.next();
			
			OrderedDataSourceReader odsr1 = rp.getReader(rm_conf.getLinkDataSource1(), mc);
			OrderedDataSourceReader odsr2 = rp.getReader(rm_conf.getLinkDataSource2(), mc);
			if(odsr1 != null && odsr2 != null){
				// analyze with EM
			    FormPairs fp2 = null;
			    if (rm_conf.isDeduplication()) {
			        fp2 = new DedupOrderedDataSourceFormPairs(odsr1, mc, rm_conf.getLinkDataSource1().getTypeTable());
			    } else {
			        fp2 = new OrderedDataSourceFormPairs(odsr1, odsr2, mc, rm_conf.getLinkDataSource1().getTypeTable());
			    }
				/*
				 * Using two analyzer at a time in the PairDataSourceAnalysis. The order when adding the analyzer to
				 * PairDataSourceAnalysis will affect the end results. For example in the following code fragment,
				 * RandomSampleAnalyzer will be run first followed by the EMAnalyzer. But this will be depend on
				 * current Java's ArrayList implementation, if they add new element add the end of the list, then
				 * this will work fine.
				 * 
				 * In the following code, RandomSampleAnalyzer and EMAnalyzer will work independent each other.
				 * RandomSampleAnalyzer will generate the u value and save it in MatchingConfigRow object, while
				 * EMAnalyzer will check MatchingConfig to find out whether the blocking run use random sampling
				 * (where u value that will be used is the one generated by RandomSampleAnalyzer) or not using 
				 * random sampling (where u value will be the default value).
				 * 
				 * I don't think we need to instantiate the RandomSampleAnalyzer here if the user doesn't want to
				 * use random sampling :D
				 */
                LoggingFrame frame = new LoggingFrame(mc.getName());
				PairDataSourceAnalysis pdsa = new PairDataSourceAnalysis(fp2);
				// if not using random sampling then don't instantiate random sample analyzer
				// if the value is locked then don't instantiate random sampler analyzer
				if(mc.isUsingRandomSampling() && !mc.isLockedUValues()) {
					// create FormPairs for rsa to use
					OrderedDataSourceReader rsa_odsr1 = rp.getReader(rm_conf.getLinkDataSource1(), mc);
					OrderedDataSourceReader rsa_odsr2 = rp.getReader(rm_conf.getLinkDataSource2(), mc);
					FormPairs rsa_fp2 = null;
				    if (rm_conf.isDeduplication()) {
				        rsa_fp2 = new DedupOrderedDataSourceFormPairs(rsa_odsr1, mc, rm_conf.getLinkDataSource1().getTypeTable());
				    } else {
				        rsa_fp2 = new OrderedDataSourceFormPairs(rsa_odsr1, rsa_odsr2, mc, rm_conf.getLinkDataSource1().getTypeTable());
				    }
				    RandomSampleAnalyzer rsa = new RandomSampleAnalyzer(mc, rsa_fp2);
	                pdsa.addAnalyzer(rsa);
	                frame.addLoggingObject(rsa);
				}
				EMAnalyzer ema = new EMAnalyzer(mc);
				pdsa.addAnalyzer(ema);
				frame.addLoggingObject(ema);
				frame.configureLoggingFrame();
				pdsa.analyzeData();
			}
			odsr1.close();
			odsr2.close();
		}
	}
	
	private void createSyntheticData(){
		// do frequency analysis to get values to sample from
		ReaderProvider rp = ReaderProvider.getInstance();
		List<MatchingConfig> mcs = rm_conf.getMatchingConfigs();
		Iterator<MatchingConfig> it = mcs.iterator();
		int n = 1000;
		boolean valid_n = false;
		do{
			String s = (String)JOptionPane.showInputDialog(this,"Number of pairs to create:","Synthetic Pairs",JOptionPane.PLAIN_MESSAGE,null,null,"1000");
			if(s == null){
				return;
			} else {
				try{
					n = Integer.parseInt(s);
					valid_n = true;
				}
				catch(NumberFormatException nfe){
					
				}
			}
		}while(!valid_n);
				
		// get file to write synthetic data files to
		File output_base = null;
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			output_base = fc.getSelectedFile();
			
			while(it.hasNext()){
				MatchingConfig mc = it.next();
				OrderedDataSourceReader odsr1 = rp.getReader(rm_conf.getLinkDataSource1(), mc);
				OrderedDataSourceReader odsr2 = rp.getReader(rm_conf.getLinkDataSource2(), mc);
				
				String order[] = mc.getIncludedColumnsNames();
				String rank_order[] = new String[order.length];
				for(int i = 0; i < order.length; i++){
					rank_order[i] = order[i] + FSSyntheticRecordGenerator.DEMOGRAPHIC_RANK_SUFFIX;
				}
				
				if(odsr1 != null && odsr2 != null){
					// analyze frequencies in data sources first
					DataSourceAnalysis dsa = new DataSourceAnalysis(odsr1);
					ValueFrequencyAnalyzer vfa = new ValueFrequencyAnalyzer(rm_conf.getLinkDataSource1(), mc);
					dsa.addAnalyzer(vfa);
					dsa.analyzeData();
					RecordFrequencies rf1 = vfa.getRecordFrequencies();
					RecordFrequencies rf2 = null;
					
					// analyze data source 2 if not de-duplication
					if(!rm_conf.isDeduplication()){
						dsa = new DataSourceAnalysis(odsr2);
						vfa = new ValueFrequencyAnalyzer(rm_conf.getLinkDataSource1(), mc);
						dsa.addAnalyzer(vfa);
						dsa.analyzeData();
						rf2 = vfa.getRecordFrequencies();
					}
					
					// analyze frequencies of pairs second
					odsr1 = rp.getReader(rm_conf.getLinkDataSource1(), mc);
					odsr2 = rp.getReader(rm_conf.getLinkDataSource2(), mc);
					
				    FormPairs fp2 = null;
				    if (rm_conf.isDeduplication()) {
				        fp2 = new DedupOrderedDataSourceFormPairs(odsr1, mc, rm_conf.getLinkDataSource1().getTypeTable());
				    } else {
				        fp2 = new OrderedDataSourceFormPairs(odsr1, odsr2, mc, rm_conf.getLinkDataSource1().getTypeTable());
				    }
				    
				    PairDataSourceAnalysis pdsa = new PairDataSourceAnalysis(fp2);
				    VectorValuesFrequencyAnalyzer vvfa = new VectorValuesFrequencyAnalyzer(mc);
				    pdsa.addAnalyzer(vvfa);
				    pdsa.analyzeData();
				    // two lines to remove UID field from MatchingConfig object.  synthetic records shouldn't have this generated like other fields, nor
				    // is it in the frequency tables
				    MatchingConfig mc_clone = (MatchingConfig)mc.clone();
				    mc_clone.getMatchingConfigRows().remove(mc_clone.getMatchingConfigRowByName(rm_conf.getLinkDataSource1().getUniqueID()));
				    SyntheticRecordGenerator srg = new MUSyntheticRecordGenerator(mc_clone, rf1, mc.getP());
				    //SyntheticRecordGenerator srg = new FSSyntheticRecordGenerator(mc, rf1, vvfa.getVectorFrequencies());
				    if(!rm_conf.isDeduplication()){
				    	srg.setRecordFrequencies2(rf2);
				    }
				    Date start = new Date();
				    System.out.println("data analyzed, creating records at " + start);
				    File synthetic_output = new File(output_base.getPath() + "_" + mc.getName() + "_synthetic.txt");
				    File synthetic_rank_output = new File(output_base.getPath() + "_" + mc.getName() + "_synthetic_rank.txt");
				    Hashtable<MatchVector,Integer> mv_counter = new Hashtable<MatchVector,Integer>();
				    try{
				    	BufferedWriter fout = new BufferedWriter(new FileWriter(synthetic_output));
				    	BufferedWriter rfout = new BufferedWriter(new FileWriter(synthetic_rank_output));
				    	for(int i = 0; i < n; i++){
					    	//System.out.println("=========== pair " + i + " ===========");
					    	MatchResult mr = srg.getRecordPair();
					    	String output_line = FileWritingMatcher.getOutputLine(mr, order);
					    	String is_match = "false";
					    	if(mr.isMatch()){
					    		is_match = "true";
					    	}
					    	fout.write(output_line + "|" + is_match + "\n");
					    	output_line = FileWritingMatcher.getOutputLine(mr, rank_order);
					    	rfout.write(output_line + "|" + is_match + "\n");
					    	//System.out.println(r[0]);
						    //System.out.println(r[1]);
					    	Integer mv_count = mv_counter.get(mr.getMatchVector());
					    	if(mv_count == null){
					    		mv_counter.put(mr.getMatchVector(), 1);
					    	} else {
					    		Integer new_count = mv_count + 1;
					    		mv_counter.put(mr.getMatchVector(), new_count);
					    	}
					    }
				    	fout.flush();
				    	fout.close();
				    	rfout.flush();
				    	rfout.close();
				    	/*Iterator<MatchVector> mv_it = mv_counter.keySet().iterator();
				    	System.out.println("generated vectors:");
				    	while(mv_it.hasNext()){
				    		MatchVector mv = mv_it.next();
				    		System.out.println(mv + ":\t" + mv_counter.get(mv));
				    	}*/
				    }
				    catch(IOException ioe){
				    	System.err.println("error writing synthetic data to file: " + ioe.getMessage());
				    }
				    
				    Date end = new Date();
				    System.out.println(n + " records created between " + start + " and " + end);
				}
				
				
				System.out.println("data generated");
			}
		}
		
		
	}
	
	private void calculateMIScores() {		
		DataSourceFrequency dsf = new MemoryBackedDataSourceFrequency();
		rm_conf.setDataSourceFrequency1(dsf);
		MutualInformationAnalyzer fa = new MutualInformationAnalyzer(rm_conf, rm_conf.getLinkDataSource1(), rm_conf.getDataSourceFrequency1());
		ReaderProvider rp = ReaderProvider.getInstance();

		System.out
				.println("Calculating pairwise combinations for columns in Dataset...");
		DataSourceAnalysis dsa = new DataSourceAnalysis(rp.getReader(rm_conf
				.getLinkDataSource1()));
		dsa.addAnalyzer(fa);
		dsa.analyzeData();
		
		if(!rm_conf.isDeduplication()){
			DataSourceFrequency dsf2 = new MemoryBackedDataSourceFrequency();
			rm_conf.setDataSourceFrequency2(dsf2);
			MutualInformationAnalyzer fa2 = new MutualInformationAnalyzer(rm_conf, rm_conf.getLinkDataSource2(), rm_conf.getDataSourceFrequencyf2());
			ReaderProvider rp2 = ReaderProvider.getInstance();
			System.out
					.println("Calculating pairwise combinations for columns in Dataset...");
			DataSourceAnalysis dsa2 = new DataSourceAnalysis(rp.getReader(rm_conf
					.getLinkDataSource2()));
			dsa2.addAnalyzer(fa2);
			dsa2.analyzeData();
		}
	}
	


	private void displayVectorTables(){
		Iterator<MatchingConfig> it = rm_conf.getMatchingConfigs().iterator();
		while(it.hasNext()){
			MatchingConfig mc = it.next();
			VectorTable vt = new VectorTable(mc);
			TextDisplayFrame tdf = new SaveTextDisplayFrame(mc.getName(), vt.toString());
		}
	}
	
	public void actionPerformed(ActionEvent ae){
		if(ae.getSource() == em_button){
			runEMAnalysis();
		} else if(ae.getSource() == vector_button){
			displayVectorTables();
		} else if(ae.getSource() == random_button) {
		    performRandomSampling();
		} else if(ae.getSource() == summary_button) {
			performSummaryStatistics();
		} else if(ae.getSource() == freq_button) {
			performFrequencyAnalysis();
		} else if(ae.getSource() == closed_form_button){
			calculateClosedUValues();
		} else if(ae.getSource() == vector_obs_button){
			performVectorObsAnalysis();
		} else if(ae.getSource() == synthetic_button){
			createSyntheticData();
		}else if (ae.getSource() == mi_score_button) {
			calculateMIScores();
		}
	}
	
	private void performVectorObsAnalysis(){
		ReaderProvider rp = ReaderProvider.getInstance();
		List<MatchingConfig> mcs = rm_conf.getMatchingConfigs();
		Iterator<MatchingConfig> it = mcs.iterator();
		while(it.hasNext()){
			MatchingConfig mc = it.next();
			
			OrderedDataSourceReader odsr1 = rp.getReader(rm_conf.getLinkDataSource1(), mc);
			OrderedDataSourceReader odsr2 = rp.getReader(rm_conf.getLinkDataSource2(), mc);
			if(odsr1 != null && odsr2 != null){
				// analyze with EM
			    FormPairs fp2 = null;
			    if (rm_conf.isDeduplication()) {
			        fp2 = new DedupOrderedDataSourceFormPairs(odsr1, mc, rm_conf.getLinkDataSource1().getTypeTable());
			    } else {
			        fp2 = new OrderedDataSourceFormPairs(odsr1, odsr2, mc, rm_conf.getLinkDataSource1().getTypeTable());
			    }
			    
			    LoggingFrame frame = new SaveTextLoggingFrame(mc.getName());
				PairDataSourceAnalysis pdsa = new PairDataSourceAnalysis(fp2);
				// if not using random sampling then don't instantiate random sample analyzer
				// if the value is locked then don't instantiate random sampler analyzer
				
				ObservedVectorAnalyzer ova = new ObservedVectorAnalyzer(mc);
				pdsa.addAnalyzer(ova);
				frame.addLoggingObject(ova);
				frame.configureLoggingFrame();
				pdsa.analyzeData();
			}
			odsr1.close();
			odsr2.close();
			
		}
	}
	
	private void calculateClosedUValues(){
		
		/*if(rm_conf.isDeduplication()){
				// run dedpulication u value calculator
				CloseFormUCalculatorDedup cfucd = new CloseFormUCalculatorDedup(rm_conf.getMatchingConfigs().get(0), dsf1);
				cfucd.calculateUValues();
			} else {
				// run normal u value calculator
				CloseFormUCalculator cfuc = new CloseFormUCalculator(rm_conf.getMatchingConfigs().get(0), dsf1, dsf2);
				cfuc.calculateUValues();
			}*/
		
		ReaderProvider rp = ReaderProvider.getInstance();
		List<MatchingConfig> mcs = rm_conf.getMatchingConfigs();
		Iterator<MatchingConfig> it = mcs.iterator();
		while(it.hasNext()){
			MatchingConfig mc = it.next();
			if(rm_conf.isDeduplication()){
				DataSourceReader dsr = rp.getReader(rm_conf.getLinkDataSource1());
				if(dsr != null){
					DataSourceAnalysis pdsa = new DataSourceAnalysis(dsr);
					// create u value analyzer, add to pdsa, and run analysis
					ClosedFormDedupAnalyzer cfda = new ClosedFormDedupAnalyzer(rm_conf.getLinkDataSource1(), mc);
					pdsa.addAnalyzer(cfda);
					pdsa.analyzeData();
				}
				
			} else {
				OrderedDataSourceReader odsr1 = rp.getReader(rm_conf.getLinkDataSource1(), mc);
				OrderedDataSourceReader odsr2 = rp.getReader(rm_conf.getLinkDataSource2(), mc);
				if(odsr1 != null && odsr2 != null){
					// analyze with EM
					FormPairs fp2 = null;
					fp2 = new OrderedDataSourceFormPairs(odsr1, odsr2, mc, rm_conf.getLinkDataSource1().getTypeTable());
					
					PairDataSourceAnalysis pdsa = new PairDataSourceAnalysis(fp2);

					// create u value analyzer, add to pdsa, and run analysis
					ClosedFormAnalyzer cfa = new ClosedFormAnalyzer(mc);
					pdsa.addAnalyzer(cfa);
					pdsa.analyzeData();

				}
			}

			
		}
	}
	
	private void performFrequencyAnalysis(){
		DataSourceFrequency dsf1 = new MemoryBackedDataSourceFrequency();
		FrequencyAnalyzer fa1 = new FrequencyAnalyzer(rm_conf.getLinkDataSource1(), null, dsf1);
		DataSourceFrequency dsf2 = new MemoryBackedDataSourceFrequency();
		FrequencyAnalyzer fa2 = new FrequencyAnalyzer(rm_conf.getLinkDataSource2(), null, dsf2);
		
		ReaderProvider rp = ReaderProvider.getInstance();
		
		System.out.println("ready to read datasources for frequencies");
		DataSourceAnalysis dsa = new DataSourceAnalysis(rp.getReader(rm_conf.getLinkDataSource1()));
		dsa.addAnalyzer(fa1);
		dsa.analyzeData();
		rm_conf.setDataSourceFrequency1(dsf1);
		
		if(!rm_conf.isDeduplication()){
			System.out.println("analyzed source 1, getting ready to read source 2");
			dsa = new DataSourceAnalysis(rp.getReader(rm_conf.getLinkDataSource1()));
			dsa.addAnalyzer(fa2);
			dsa.analyzeData();
			rm_conf.setDataSourceFrequency2(dsf2);
			System.out.println("counted frequency for both sources");
		} else {
			System.out.println("analyzed data");
		}
		
		
	}
	
	private void performSummaryStatistics() {
		ReaderProvider rp = ReaderProvider.getInstance();
		List<MatchingConfig> mcs = rm_conf.getMatchingConfigs();
		Iterator<MatchingConfig> it = mcs.iterator();
		SummaryStatisticsStore sss1 = new SummaryStatisticsStore(rm_conf.getLinkDataSource1());
		SummaryStatisticsStore sss2 = new SummaryStatisticsStore(rm_conf.getLinkDataSource2());
		
		while(it.hasNext()){
			MatchingConfig mc = it.next();
			
			LinkDataSource lds1 = rm_conf.getLinkDataSource1();
			LinkDataSource lds2 = rm_conf.getLinkDataSource2();
			
			OrderedDataSourceReader odsr1 = rp.getReader(lds1, mc);
			OrderedDataSourceReader odsr2 = rp.getReader(lds2, mc);

			if(lds1 != null && lds2 != null){
				// compute summary statistics
                LoggingFrame frame = new LoggingFrame(mc.getName());
				DataSourceAnalysis dsa1 = new DataSourceAnalysis(odsr1);
				DataSourceAnalysis dsa2 = new DataSourceAnalysis(odsr2);

				// Null - compute the number of null elements for each demographic
				NullAnalyzer na1 = new NullAnalyzer(lds1, mc, sss1);
				NullAnalyzer na2 = new NullAnalyzer(lds2, mc, sss2);
				
				dsa1.addAnalyzer(na1);
				dsa2.addAnalyzer(na2);
				/*
				frame.addLoggingObject(na1);
				frame.addLoggingObject(na2);
				*/ 
				
				// Entropy - compute the entropy of a demographic
				EntropyAnalyzer ea1 = new EntropyAnalyzer(lds1, mc, sss1);
				EntropyAnalyzer ea2 = new EntropyAnalyzer(lds2, mc, sss2);
				
				dsa1.addAnalyzer(ea1);
				dsa2.addAnalyzer(ea2);
				
				// Unique - compute the number of unique values of a demographic
				UniqueAnalyzer ua1 = new UniqueAnalyzer(lds1, mc, sss1);
				UniqueAnalyzer ua2 = new UniqueAnalyzer(lds2, mc, sss2);
				
				dsa1.addAnalyzer(ua1);
				dsa2.addAnalyzer(ua2);
				
				// Average Frequency - compute the average frequency of values in a demographic
				AverageFrequencyAnalyzer afa1 = new AverageFrequencyAnalyzer(lds1, mc, ua1.getResults(), sss1);
				AverageFrequencyAnalyzer afa2 = new AverageFrequencyAnalyzer(lds2, mc, ua2.getResults(), sss2);
				
				dsa1.addAnalyzer(afa1);
				dsa2.addAnalyzer(afa2);
				
				// Maximum Entropy - compute the maximum entropy of a demographic
				MaximumEntropyAnalyzer mea1 = new MaximumEntropyAnalyzer(lds1, mc, afa1.getResults(), ua1.getResults(), sss1);
				MaximumEntropyAnalyzer mea2 = new MaximumEntropyAnalyzer(lds2, mc, afa2.getResults(), ua2.getResults(), sss2);
				
				dsa1.addAnalyzer(mea1);
				dsa2.addAnalyzer(mea2);
				
				// Finish by configuring the frame and looping through all Analyzers
				frame.configureLoggingFrame();
				dsa1.analyzeData();
				dsa2.analyzeData();
			}
			odsr1.close();
			odsr2.close();
		}
	}

    private void performRandomSampling() {
        ReaderProvider rp = ReaderProvider.getInstance();
        List<MatchingConfig> mcs = rm_conf.getMatchingConfigs();
        Iterator<MatchingConfig> it = mcs.iterator();
        while(it.hasNext()){
            MatchingConfig mc = it.next();
            // if the user not choose to use random sampling, then do nothing
            // if the u-values is already locked then do nothing as well
            if(mc.isUsingRandomSampling() && !mc.isLockedUValues()) {
                OrderedDataSourceReader odsr1 = rp.getReader(rm_conf.getLinkDataSource1(), mc);
                OrderedDataSourceReader odsr2 = rp.getReader(rm_conf.getLinkDataSource2(), mc);
                if(odsr1 != null && odsr2 != null){
                    FormPairs fp2 = null;
                    if(rm_conf.isDeduplication()) {
                        fp2 = new DedupOrderedDataSourceFormPairs(odsr1, mc, rm_conf.getLinkDataSource1().getTypeTable());
                    } else {
                        fp2 = new OrderedDataSourceFormPairs(odsr1, odsr2, mc, rm_conf.getLinkDataSource1().getTypeTable());
                    }
                    
                    PairDataSourceAnalysis pdsa = new PairDataSourceAnalysis(fp2);
                    
                    ApplyAnalyzerLoggingFrame frame = new ApplyAnalyzerLoggingFrame(mc, null);
                    
                    MatchingConfig mcCopy = (MatchingConfig) mc.clone();
                    
                    OrderedDataSourceReader rsa_odsr1 = rp.getReader(rm_conf.getLinkDataSource1(), mc);
					OrderedDataSourceReader rsa_odsr2 = rp.getReader(rm_conf.getLinkDataSource2(), mc);
					FormPairs rsa_fp2 = null;
				    if (rm_conf.isDeduplication()) {
				        rsa_fp2 = new DedupOrderedDataSourceFormPairs(rsa_odsr1, mc, rm_conf.getLinkDataSource1().getTypeTable());
				    } else {
				        rsa_fp2 = new OrderedDataSourceFormPairs(rsa_odsr1, rsa_odsr2, mc, rm_conf.getLinkDataSource1().getTypeTable());
				    }
                    RandomSampleAnalyzer rsa = new RandomSampleAnalyzer(mcCopy, rsa_fp2);
                    
                    pdsa.addAnalyzer(rsa);
                    frame.addLoggingObject(rsa);
                    
                    frame.configureLoggingFrame();
                    pdsa.analyzeData();
                }
                odsr1.close();
                odsr2.close();
            }
        }
    }
}

