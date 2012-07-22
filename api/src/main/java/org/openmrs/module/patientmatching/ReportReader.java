package org.openmrs.module.patientmatching;

import java.io.IOException;
import java.util.List;

public interface ReportReader {

    boolean isEof();

    void setEof(boolean eof);

    List<Long> getPagePos();

    void setPagePos(List<Long> pagePos);

    int getCurrentPage();

    void setCurrentPage(int currentPage);

    List<String> getHeader() throws IOException;

    List<List<String>> getCurrentContent();

    void fetchContent(int page) throws IOException;

    void setReport(String reportName);
}
