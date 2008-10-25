<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patients, View Patient Cohorts" otherwise="/login.htm" redirect="/module/patientmatching/dupes.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>
<openmrs:htmlInclude file="/dwr/interface/DWRMatchingConfigUtilities.js"/>
<openmrs:htmlInclude file="/dwr/engine.js"/>
<openmrs:htmlInclude file="/dwr/util.js"/>

<script type="text/javascript">

var s = 0;

function runReport() {
    if (confirm("Are you sure you want to generate new report?")) {
        showRunReport(false);
        DWRMatchingConfigUtilities.doAnalysis();
    }
}

function deleteFile(file) {
    if (confirm("Are you sure you want to delete \'" + file + "\'?")) {
        clearTimeout(s);
        DWREngine.beginBatch();
        DWRMatchingConfigUtilities.deleteReportFile(file);
        buildTable();
        DWREngine.endBatch();
        updateTimer();
    }
}

function viewFile(file) {
        clearTimeout(s);
        alert("Viewing option are going to be implemented in the future." +
               "\nFor now please check your server for the report." +
               "\nThe report file name is \'" + file + "\'");
        updateTimer();

//    if (confirm("Are you sure you want to delete \'" + file + "\'?")) {
//        clearTimeout(s);
//        DWREngine.beginBatch();
//        DWRMatchingConfigUtilities.deleteReportFile(file);
//        buildTable();
//        DWREngine.endBatch();
//        updateTimer();
//    }
}

function showRunReport(show) {
    var noRunReport = document.getElementById("noRunReport");
    var runReport = document.getElementById("runReport");
    if(show) {
        noRunReport.style.display = "none";
        runReport.style.display = "block";
    } else {
        noRunReport.style.display = "block";
        runReport.style.display = "none";
    }
}

function updateTimer() {
    var reTxt=new RegExp("\\d+");
    
    var timeVar = document.getElementById("serverTimer");
    
    if (timeVar != null) {
        var timeText = timeVar.innerHTML;

        var updateTime = timeText.match(reTxt);
        var pieces = timeText.split(reTxt);

        if (updateTime == 0) {
            updateStatus();
            updateTime = 30;
        } else {
            updateTime = updateTime - 1;
            timeText.innerHTML = updateTime;
        }
        
        timeVar.innerHTML = pieces[0] + updateTime + pieces[1];
    } else {
        updateStatus();
    }
    s = setTimeout("updateTimer()", 1000);
}

function updateStatus() {
    DWRMatchingConfigUtilities.getStatus(function updateString(data) {
        var statusLocation = document.getElementById("reportStatus");
        if (data != '<c:out value="${defaultStatus}" />'
                && data != '<c:out value="${premStatus}" />'
                && data != '<c:out value="${endStatus}" />') {
            showRunReport(false);
            
            var currData = statusLocation.innerHTML;
            
            var dataDotPos = data.indexOf(".");
            var subbedData = data.substring(0, dataDotPos);
            
            var currDataDotPos = currData.indexOf(".");
            if (currDataDotPos < 0) {
                currDataDotPos = currData.length;
            }
            var totalDot = currData.length - currDataDotPos;
            
            subbedCurrData = currData.substring(0, currDataDotPos);
            
            if (subbedData == subbedCurrData) {
                if (totalDot > 10) {
                    currData = subbedData;
                } else {
                    currData = currData + ".";
                }
            } else {
                currData = subbedData;
            }
            statusLocation.innerHTML = currData;
        } else {
            if (data == '<c:out value="${endStatus}" />') {
                clearTimeout(s);
                DWREngine.beginBatch();
                
                buildTable();
                
                var status = '<c:out value="${defaultStatus}" />';
                DWRMatchingConfigUtilities.setStatus(status);
                
                DWREngine.endBatch();
                updateTimer();
            }
            statusLocation.innerHTML = data;
            showRunReport(true);
        }
    });
}

function buildTable() {
    DWRMatchingConfigUtilities.getAllReports(function writeReports(reports) {
        DWRUtil.removeAllRows("report-list");
        var cellFuncs = [
            function(data) { return data; },
            function(data) { return "    ";},
            function(data) { return "<a href=\"javascript:;\" onClick=\"viewFile('" + data + "')\">View</a>"; },
            function(data) { return "<a href=\"javascript:;\" onClick=\"deleteFile('" + data + "')\">Delete</a>"; }
        ];
        DWRUtil.addRows( "report-list", reports, cellFuncs);
    });
}

window.onload = updateTimer();
</script>

<h2><spring:message code="patientmatching.report.title"/></h2>

<br/>

<b class="boxHeader"><spring:message code="patientmatching.report.run" /></b>
<form method="post">
    <div class="box">
        <table cellspacing="2">
            <tr>
                <th colspan="2">
                    <spring:message code="patientmatching.report.blocking"/>
                </th>
            </tr>
            <c:forEach items="${blockingRuns}" var="blockingRun" varStatus="entriesIndex">
            <tr>
                <td>
                    <c:out value="${entriesIndex.count}" />.
                </td>
                <td>
                    <c:out value="${blockingRun}" />.
                </td>
            </tr>
            </c:forEach>
        </table>
        
        <table cellspacing="2">
            <tr id="noRunReport">
                <td colspan="2">
                    <span style="font-weight: bold;">
                        [Run Report]
                        <span style="font-style: italic;">
                            Dedup is running
                        </span>
                    </span>
                </td>
            </tr>
            
            <tr id="runReport">
                <td colspan="2">
                    <a href="javascript:;" onClick="runReport();">
                        <span style="font-weight: bold;">Run Report</span>
                    </a>
                </td>
            </tr>
        </table>
        
        <table cellspacing="2">
            <tr>
                <td>
                    <span style="font-weight: bold;">
                        Currently:
                    </span>
                </td>
            </tr>
            
            <tr>
                <td>
                    &nbsp;&nbsp;&nbsp;&nbsp;
                    <span style="font-weight: bold;" id="reportStatus">
                        &nbsp;
                    </span>
                </td>
            </tr>
            
            <tr>
                <td>
                    <span style="font-style: italic; font-weight: bold;" id="serverTimer">
                        Contacting server in 30s
                    </span>
                </td>
            </tr>
            
            <tr>
                <td>
                    &nbsp;
                </td>
            </tr>
        </table>
        
        <table cellspacing="2">
            <tr>
                <th>
                    Analysis Reports
                </th>
                <th>
                    &nbsp;&nbsp;&nbsp;&nbsp;
                </th>
                <th colspan="2">
                    Select Action
                </th>
            </tr>
            
            <tbody id="report-list">
            <c:forEach items="${reportResults}" var="reportResult" varStatus="entriesIndex">
            <tr>
                <td>
                    <c:out value="${reportResult}" />
                </td>
                <td>
                    &nbsp;&nbsp;&nbsp;&nbsp;
                </td>
                <td>
                    <a href="javascript:;" onClick="viewFile('<c:out value="${reportResult}" />');">
                        View
                    </a>
                </td>
                <td>
                    <a href="javascript:;" onClick="deleteFile('<c:out value="${reportResult}" />');">
                        Delete
                    </a>
                </td>
            </tr>
            </c:forEach>
            </tbody>
            
        </table>
    
    </div>
</form>
<br/>

<%@ include file="/WEB-INF/template/footer.jsp" %>