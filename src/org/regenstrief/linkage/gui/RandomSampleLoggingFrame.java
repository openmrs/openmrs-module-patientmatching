/**
 */
package org.regenstrief.linkage.gui;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.regenstrief.linkage.analysis.RandomSampleAnalyzer;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;

/**
 * Derivate of the LoggingFrame specific for the RandomSampleAnalyzer class.
 * @see org.regenstrief.linkage.gui.LoggingFrame
 */
public class RandomSampleLoggingFrame extends LoggingFrame {
    
    private JButton applyValue;
    
    private MatchingConfig config;
    private JTable options;
    
    public RandomSampleLoggingFrame(String title, JTable options) {
        super(title);
        this.options = options;
    }
    
    public RandomSampleLoggingFrame(MatchingConfig config, JTable options) {
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
        // there will only be one analyzer
        RandomSampleAnalyzer analyzer = (RandomSampleAnalyzer) logSources.get(0);
        /*
         * replace currently processed matching config with the one that contains
         * random sampling result.
         */
        MatchingConfig analyzerConfig = analyzer.getAnalyzerMatchingConfig();
        for(MatchingConfigRow row: analyzerConfig.getMatchingConfigRows()) {
            String name = row.getName();
            MatchingConfigRow guiRow = config.getMatchingConfigRowByName(name);
            guiRow.setNonAgreement(row.getNonAgreement());
        }
        
        // repaint GUI element that displays the new values, if needed
        if(options != null){
        	options.repaint();
        }
        
        JOptionPane.showMessageDialog(
                this, "Values copied to current session parameter.",
                "Info", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}
