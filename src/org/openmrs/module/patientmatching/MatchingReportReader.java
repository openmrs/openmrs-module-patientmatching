package org.openmrs.module.patientmatching;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.OpenmrsUtil;

public class MatchingReportReader {
    protected final Log log = LogFactory.getLog(getClass());
    
    private static final int REPORT_PAGE_SIZE = 20;
    
    private List<Long> pagePos;
    
    private int currentPage;
    
    private List<List<String>> currentContent;
    
    private File reportFile;
    
    private boolean eof = false;
    
    public MatchingReportReader(String filename) {
        pagePos = new ArrayList<Long>();
        pagePos.add(0L);
        
        currentContent = new ArrayList<List<String>>();
        
        setReportFile(filename);
    }
    
    public MatchingReportReader(int currentPage, boolean eof, List<Long> pagePos, String filename) {
        this.currentPage = currentPage;
        this.eof = eof;
        this.pagePos = pagePos;
        
        currentContent = new ArrayList<List<String>>();
        
        setReportFile(filename);
    }

    public List<Long> getPagePos() {
        return pagePos;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public List<List<String>> getCurrentContent() {
        return currentContent;
    }

    public boolean isEof() {
        return eof;
    }
    
    private void setReportFile(String filename) {
        String configLocation = MatchingConstants.CONFIG_FOLDER_NAME;
        File configFileFolder = OpenmrsUtil.getDirectoryInApplicationDataDirectory(configLocation);
        reportFile = new File(configFileFolder, filename);
    }

    public void fetchInitialContent() throws IOException {
        RandomAccessFile raf = new RandomAccessFile(reportFile, "r");
        
        int counter = 0;
        String s = null;
        while((s = raf.readLine()) != null) {
            String[] split = s.split("[|]");
            List<String> list = Arrays.asList(split);
            currentContent.add(list);
            counter ++;
            if (counter >= REPORT_PAGE_SIZE) {
                break;
            }
        }
        
        if (s == null) {
            eof = true;
        }
        
        currentPage = 0;
        
        pagePos.add(raf.getFilePointer());
        
        raf.close();
    }
    
    public void fetchContent(int page) throws IOException {
        
        RandomAccessFile raf = new RandomAccessFile(reportFile, "r");
        
        long pageOffset = pagePos.get(page);
        raf.seek(pageOffset);
        
        int counter = 0;
        String s = null;
        while((s = raf.readLine()) != null) {
            String[] split = s.split("[|]");
            List<String> list = Arrays.asList(split);
            currentContent.add(list);
            counter ++;
            if (counter >= REPORT_PAGE_SIZE) {
                break;
            }
        }
        
        if (s == null) {
            eof = true;
        }

        currentPage = page;
        
        if (!(page < pagePos.size() - 1)) {
            pagePos.add(raf.getFilePointer());
        }
        
        raf.close();
    }
}
