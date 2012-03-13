/**
 * Auto generated file comment
 */
package org.regenstrief.linkage.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 * Implemented using double linked list
 */
public class RecentFile {
    private Hashtable<String, RecentFileEntry> hashRecentList;
    
    private LinkedList<RecentFileEntry> recentList;
    
    private int max;

    public RecentFile(int max) {
        super();
        this.hashRecentList = new Hashtable<String, RecentFileEntry>();
        this.recentList = new LinkedList<RecentFileEntry>();
        this.max = max;
    }
    
    /**
     * Add a new recent-file-entry to the entry list. If the entry is
     * already exist, then the new one will override the old one.
     * 
     * @param entry recent-file-entry that will be added
     */
    public void addEntry(RecentFileEntry entry) {
        RecentFileEntry hashEntry = hashRecentList.get(entry.getFilePath());
        if(hashEntry != null) {
            // replace old entry with the new one
            hashRecentList.put(entry.getFilePath(), entry);
            // remove the entry to the first element of the list
            recentList.remove(hashEntry);
            recentList.addFirst(entry);
        } else {
            if (hashRecentList.size() == max) {
                recycleEntry();
            }
            // put it in the front of the list
            hashRecentList.put(entry.getFilePath(), entry);
            recentList.addFirst(entry);
        }
    }
    
    /**
     * Remove an entry from the recent entry list.
     * 
     * @param entry recent-file-entry that will be removed
     */
    public void removeEntry(RecentFileEntry entry) {
        RecentFileEntry hashEntry = hashRecentList.remove(entry.getFilePath());
        recentList.remove(hashEntry);
    }
    
    /**
     * Remove all recent-file-entry from the entry list
     */
    public void clearEntries(){
        this.hashRecentList = new Hashtable<String, RecentFileEntry>();
        this.recentList = new LinkedList<RecentFileEntry>();
    }
    
    /**
     * Remove the least used recent-file-entry from the list. The least used is
     * the last element of list.
     */
    public void recycleEntry() {
        RecentFileEntry lastEntry = recentList.removeLast();
        hashRecentList.remove(lastEntry.getFilePath());
    }
    
    /**
     * Get all element of the list. This is typically used for displaying the menu
     * item for the recent-file-entry menu.
     * @return
     */
    public RecentFileEntry[] getEntries() {
        RecentFileEntry[] entries = new RecentFileEntry[recentList.size()];
        for (int i = 0; i < entries.length; i++) {
            entries[i] = recentList.get(i);
        }
        return entries;
    }
    
    /**
     * Save the entire entry to a file to make it available for next run of the GUI
     * 
     * @param entries recent-file-entry that will be saved
     * @param file path of a file that will hold the entries
     */
    public static void persistEntries(RecentFileEntry[] entries, File file) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < entries.length; i++) {
                bw.write(entries[i].getFilePath());
                bw.write(System.getProperty("line.separator"));
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Read recent-file-entry from the file.
     * 
     * @param file location of the recent-file-entry file in the system
     * @return all recent-file-entry that can be read from the file
     */
    public static RecentFileEntry[] readEntries(File file) {
        RecentFileEntry[] entries = null;
        try {
            String filePath = RecMatch.RECENT_FILE_PATH;
            BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
            String line = null;
            List<RecentFileEntry> entriesList = new ArrayList<RecentFileEntry>();
            while((line = br.readLine()) != null) {
                File f = new File(line);
                RecentFileEntry entry = new RecentFileEntry();
                entry.setFileName(f.getName());
                entry.setFilePath(f.getAbsolutePath());
                entriesList.add(entry);
            }
            Collections.reverse(entriesList);
            entries = entriesList.toArray(new RecentFileEntry[entriesList.size()]);
        } catch (FileNotFoundException e) {
            
            // better be using the logging method from log4j
            System.out.println("File can't be found. No entry can be loaded");
        } catch (IOException e) {
        }
        return entries;
    }

    /**
     * @return the max number of recent-file-entry
     */
    public int getMax() {
        return max;
    }

    /**
     * Set the maximum number of recent-file-entry that will be retained by the system
     * @param max the max to set
     */
    public void setMax(int max) {
        this.max = max;
    }
}

class RecentFileEntry {
    private String fileName;
    
    private String pathDisplay;
    
    private String filePath;

    /**
     * @return the pathDisplay is a part of the recent-file-entry path that will be displayed in the menu.
     */
    public String getPathDisplay() {
        return pathDisplay;
    }

    /**
     * @param pathDisplay the pathDisplay to set
     */
    public void setPathDisplay(String pathDisplay) {
        this.pathDisplay = pathDisplay;
    }

    /**
     * @return the fileName of the recent-file-entry element
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the filePath is the recent-file-entry full path that will used to load the config data
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @param filePath the filePath to set
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
        if (filePath.length() > 20) {
            String pathDisplay = filePath.substring(0, 17);
            pathDisplay = pathDisplay.concat("...");
            this.pathDisplay = pathDisplay;
        }
    }
}
