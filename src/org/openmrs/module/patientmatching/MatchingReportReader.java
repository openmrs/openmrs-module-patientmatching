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
    
    private List<String> header;
    
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

    /**
     * @return the eof
     */
    public boolean isEof() {
        return eof;
    }

    /**
     * @param eof the eof to set
     */
    public void setEof(boolean eof) {
        this.eof = eof;
    }

    /**
     * @return the pagePos
     */
    public List<Long> getPagePos() {
        return pagePos;
    }

    /**
     * @param pagePos the pagePos to set
     */
    public void setPagePos(List<Long> pagePos) {
        this.pagePos = pagePos;
    }
    
    /**
     * @return the currentPage
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * @param currentPage the currentPage to set
     */
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    /**
     * @return the header line 
     * @throws IOException
     */
    public List<String> getHeader() throws IOException {
        RandomAccessFile raf = new RandomAccessFile(reportFile, "r");
        
        String s = null;
        header = new ArrayList<String>();
        s = raf.readLine();
        String[] split = s.split("[|]");
        List<String> list = Arrays.asList(split);
        header.addAll(list);
        
        currentPage = 1;
        
        pagePos.add(raf.getFilePointer());
        return header;
    }

    /**
     * @return the current page report content
     */
    public List<List<String>> getCurrentContent() {
        return currentContent;
    }
    
    private void setReportFile(String filename) {
        String configLocation = MatchingConstants.CONFIG_FOLDER_NAME;
        File configFileFolder = OpenmrsUtil.getDirectoryInApplicationDataDirectory(configLocation);
        reportFile = new File(configFileFolder, filename);
    }
    
    public void fetchContent(int page) throws IOException {
        
        RandomAccessFile raf = new RandomAccessFile(reportFile, "r");
        
        long pageOffset = pagePos.get(page);
        raf.seek(pageOffset);
        
        int counter = 0;
        String s = null;
        String[] split = null;
        List<String> list = null;
        while((s = raf.readLine()) != null) {
            split = s.split("[|]");
            list = Arrays.asList(split);
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
