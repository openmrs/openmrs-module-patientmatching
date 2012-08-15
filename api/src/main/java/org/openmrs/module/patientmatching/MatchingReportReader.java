/**
 * 
 */
package org.openmrs.module.patientmatching;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

/**
 * Class to read chunk content of a report file and then send it back to the web
 * front end.
 * 
 * Current implementation will require multiple creation of this class object to
 * fully read the entire report content. Future implementation might use a 
 * singleton pattern and create only a single instance of this class to read the
 * entire content.
 */
public class MatchingReportReader{
    /**
     * Logger instance
     */
    protected final Log log = LogFactory.getLog(getClass());

    /**
     * Default paging size of the report. This indicate the total number of lines
     * that will be displayed in the show report web page.
     */
    public static final int REPORT_PAGE_SIZE = 20;

    /**
     * Cache for each page offset from the start of the file. Element i-th will
     * contains the page offset of the i-th page of the report.
     */
    private List<Long> pagePos;
    
    /**
     * Current report page that being displayed in the web page.
     */
    private int currentPage;
    
    /**
     * List of header string that will be displayed in the report web page
     */
    private List<String> header;
    
    /**
     * Current content of the report that will be displayed in the web page. One
     * element of the list, which is a list of string, correspond to a single row
     * in the report web page.
     */
    private List<List<String>> currentContent;
    
    /**
     * File name on the file system of the report currently being displayed in the
     * report web page
     */
    private File reportFile;
    
    /**
     * Flag to denote whether the report viewing reaches the end of file. This will
     * also determine whether user can do "view next page" or not.
     */
    private boolean eof = false;
    
    /**
     * Constructor to read a particular report file. Use this constructor for
     * initial viewing of a report web page.
     * 
     * The sequence for initial viewing of a report page are:
     * - Call this constructor
     * - Call getHeader()
     * - Call fetchContent(int)
     * - Call getCurrentContent()
     * 
     * Future implementation might combine the fetchContent(int) and the call
     * to getCurrentContent()
     */
    public MatchingReportReader() {
        pagePos = new ArrayList<Long>();
        pagePos.add(0L);
        
        currentContent = new ArrayList<List<String>>();
    }

    public void setReport(String reportName) {
        setReportFile(reportName);
    }
    
    /**
     * Subsequent call to view next or previous page will use this constructor.
     * 
     * The sequence when using this constructor are:
     * - Call this constructor
     * - Call fetchContent(int)
     * - Call getCurrentContent
     * 
     * @param currentPage currently requested report page
     * @param eof flag to denote whether the report reach the end of it
     * @param pagePos cache of each report page
     * @param filename report filename that will be loaded
     */
    public MatchingReportReader(int currentPage, boolean eof, List<Long> pagePos, String filename) {
        this.currentPage = currentPage;
        this.eof = eof;
        this.pagePos = pagePos;
        
        currentContent = new ArrayList<List<String>>();
        
        setReportFile(filename);
    }

    /**
     * Check whether the current reading process reach the end of the report or
     * not. This particularly important to determine whether user are allowed to
     * view the next page or not.
     * 
     * @return the eof
     */
    public boolean isEof() {
        return eof;
    }

    /**
     * Set the flag to determine whether the reading process reach the end of the
     * report file or not
     * 
     * @see MatchingReportReader#isEof()
     * @param eof the eof to set
     */
    public void setEof(boolean eof) {
        this.eof = eof;
    }

    /**
     * Method to get the cache page offset for the current report file. The
     * cache is used to speed up the report file reading process.
     * 
     * Element i-th will contains the page offset of the i-th page of the report
     * 
     * @return the pagePos
     */
    public List<Long> getPagePos() {
        return pagePos;
    }

    /**
     * Change the current cache page offset.
     * 
     * @see MatchingReportReader#getPagePos()
     * @param pagePos the pagePos to set
     */
    public void setPagePos(List<Long> pagePos) {
        this.pagePos = pagePos;
    }
    
    /**
     * Get the currently viewed page number of the report.
     * 
     * @return the currentPage
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Change the currently viewed page number of the report
     * 
     * @see MatchingReportReader#getCurrentPage()
     * @param currentPage the currentPage to set
     */
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    /**
     * Return list of string that will be used as the report header. The list
     * will be read from the first line of the report file.
     * 
     * @return the header line 
     * @throws IOException
     */
    public List<String> getHeader() throws IOException {
        RandomAccessFile raf = new RandomAccessFile(reportFile, "r");
        
        String s = null;
        // read the first line of the report file
        s = raf.readLine();
        s=s+"|Action";
        // Split the line using the separator char
        //TODO: Needs to move this separator char to the constants interface
        // and use it globally in the module.
        String[] split = s.split("[|]");
        List<String> list = Arrays.asList(split);
        
        header = new ArrayList<String>();
        header.addAll(list);
        
        currentPage = 1;
        
        // save the offset. the header will act as the 0th element in the cache
        // so the i-th element will be mapped to i-th page of the report
        pagePos.add(raf.getFilePointer());
        raf.close();
        return header;
    }

    /**
     * Return the current content of the page. One element of the list, which
     * is a list of string, correspond to a single row in the report web page.
     * 
     * @see MatchingReportReader#fetchContent(int)
     * @return the current page report content
     */
    public List<List<String>> getCurrentContent() {
        return currentContent;
    }
    
    /**
     * Select report file name that will be processed by this class.
     * 
     * @param filename
     */
    private void setReportFile(String filename) {
        String configLocation = MatchingConstants.CONFIG_FOLDER_NAME;
        File configFileFolder = OpenmrsUtil.getDirectoryInApplicationDataDirectory(configLocation);
        reportFile = new File(configFileFolder, filename);
    }
    
    /**
     * This method call will try to get the content of the currently requested
     * report page. This report reading process will utilize the offset cache in
     * which the offset cache will contains at least a single entry from the
     * getHeader process.
     * 
     * This method will fill up a buffer and the buffer can be retrieved using
     * the getCurrentContent() method call. The reading process will be performed
     * until the buffer is full or the end of file is reached, whichever comes
     * first.
     * 
     * @see MatchingReportReader#getCurrentContent()
     * @see MatchingReportReader#getHeader()
     * @param page
     * @throws IOException
     */
    public void fetchContent(int page) throws IOException {
        
        RandomAccessFile raf = new RandomAccessFile(reportFile, "r");
        
        // get the page offset. this will contains at least one entry
        long pageOffset = pagePos.get(page);
        raf.seek(pageOffset);
        
        int counter = 0;
        Patient patient = null;
        String s = null;
        String[] split = null;
        List<String> list = null;
        Collection<String> collec = null;
        while((s = raf.readLine()) != null) {
        	collec = null;
        	list = new ArrayList<String>();
            split = s.split("[|]");
            collec = Arrays.asList(split);
            list.addAll(collec);
            list.add(0,split[1]);
            patient = Context.getPatientService().getPatient(Integer.parseInt(split[1]));
            list.add(0,patient.getVoided().toString());
            currentContent.add(list);
            counter ++;
            // read until the report paging size or end of file is reached
            if (counter >= REPORT_PAGE_SIZE) {
                break;
            }
        }
        
        // set eof to true when the reading process reach the end
        if (s == null) {
            eof = true;
        }

        currentPage = page;
        
        // if the currently requested page is larger than total number of the
        // offset cache, then it means we're going to a page that is not reached
        // before and we need to add it to the offset cache
        
        if (!(page < pagePos.size() - 1)) {
            pagePos.add(raf.getFilePointer());
        }
        
        raf.close();
    }
}
