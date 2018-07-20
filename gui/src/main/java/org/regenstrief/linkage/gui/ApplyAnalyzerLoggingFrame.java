/**
 */
package org.regenstrief.linkage.gui;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.regenstrief.linkage.analysis.MatchingConfigAnalyzer;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;

/**
 * Derivate of the LoggingFrame specific for the RandomSampleAnalyzer class.
 * @see org.regenstrief.linkage.gui.LoggingFrame
 */
public class ApplyAnalyzerLoggingFrame extends LoggingFrame {
    
    private JButton applyValue;
    
    private MatchingConfig config;
    private SessionsPanel options;
    private MatchingConfig newConfig;
    
    /**
     * JTable gets refreshed when values are applied
     * 
     * @param config	the MatchingConfig that will be modified if the new Analyzer's values are accepted
     * @param options	panel displaying MatchingConfig objects
     */
    public ApplyAnalyzerLoggingFrame(MatchingConfig config, SessionsPanel options) {
        super(config.getName());
        this.config = config;
        this.options = options;
    }

    /**
     * @see org.regenstrief.linkage.gui.LoggingFrame#initGUI()
     */
    protected void initGUI() {
        super.initGUI();
        
        applyValue = new JButton("Apply Value");
        applyValue.addActionListener(this);
        button_panel.add(applyValue);
    }

    /**
     * @see org.regenstrief.linkage.gui.LoggingFrame#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae) {
        if(ae.getSource() == applyValue) {
            applyValue();
        } else {
            super.actionPerformed(ae);
        }
    }

    /**
     * Apply the u value generated from the random sampling to the non-agreement field
     * of the current matching configuration parameters
     */
    private void applyValue() {
        /*
         * replace currently processed matching config with the one that contains
         * random sampling result.
         */
        MatchingConfig analyzerConfig = (newConfig == null) ? ((MatchingConfigAnalyzer) logSources.get(0)).getAnalyzerMatchingConfig() : newConfig;
        for(MatchingConfigRow row: analyzerConfig.getMatchingConfigRows()) {
            String name = row.getName();
            MatchingConfigRow guiRow = config.getMatchingConfigRowByName(name);
            guiRow.setNonAgreement(row.getNonAgreement());
            guiRow.setAgreement((row.getAgreement()));
        }
        config.setScoreThreshold(analyzerConfig.getScoreThreshold());
        config.setNPairs(analyzerConfig.getNPairs());
        config.setP(analyzerConfig.getP());
        
        // repaint GUI element that displays the new values, if needed
        if(options != null){
        	options.displayThisMatchingConfig(config);
        }
        
        JOptionPane.showMessageDialog(
                this, "Values copied to current session parameter.",
                "Info", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
    
    public void setNewConfig(final MatchingConfig newConfig) {
    	this.newConfig = newConfig;
    }
}
