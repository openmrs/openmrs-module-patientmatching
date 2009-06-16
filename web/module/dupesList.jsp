<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="View Patients, View Patient Cohorts"
	otherwise="/login.htm" redirect="/module/patientmatching/dupes.list" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>
<openmrs:htmlInclude file="/dwr/interface/DWRMatchingConfigUtilities.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />

<script type="text/javascript">

var s = 0;

function runReport() {
    if (confirm("Are you sure you want to generate a new report?")) {
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
        window.open("${pageContext.request.contextPath}/module/patientmatching/report.form?<c:out value="${reportParam}" />=" + file,
                    "Report",
                    "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,copyhistory=no");
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
    var reTxt = new RegExp("\\d+");
    
    var timeVar = document.getElementById("serverTimer");
    
    var refreshPeriod = 5; // in seconds
    if (timeVar != null) {
        var timeText = timeVar.innerHTML;

        var updateTime = timeText.match(reTxt);
        var pieces = timeText.split(reTxt);

        if (updateTime == 0) {
            updateStatus();
            updateTime = refreshPeriod;
        } else {
            updateTime = updateTime - 1;
            timeText.innerHTML = updateTime;
        }
        
        timeVar.innerHTML = pieces[0] + updateTime + pieces[1];
        s = setTimeout("updateTimer()", 1000);
    } else {
		updateStatus();
        s = setTimeout("updateTimer()", refreshPeriod*1000);
    }    
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
                if (totalDot >= 3) {
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
        DWRUtil.addRows( "report-list", reports, cellFuncs, {
            rowCreator:function(options) {
                var row = document.createElement("tr");
                var index = options.rowIndex;
                if (index % 2)
                    row.className = "oddRow";
                else
                    row.className = "evenRow";
                return row;
            },
            cellCreator:function(options) {
                var td = document.createElement("td");
                return td;
            },
            escapeHtml:false
        });
    });
}

window.onload = updateTimer();
</script>

<h2><spring:message code="patientmatching.report.title" /></h2>

<br />

<b class="boxHeader"><spring:message
	code="patientmatching.report.run" /></b>
<form method="post">
<div class="box">
<table cellspacing="2" cellpadding="2">
	<tr>
		<th colspan="2"><spring:message
			code="patientmatching.report.blocking" /></th>
	</tr>
	<c:forEach items="${blockingRuns}" var="blockingRun"
		varStatus="entriesIndex">
		<tr>
			<td><c:out value="${entriesIndex.count}" />.</td>
			<td><c:out value="${blockingRun}" />.</td>
		</tr>
	</c:forEach>
</table>

<table cellspacing="2" cellpadding="2">
	<tr id="noRunReport">
		<td colspan="2"><span style="font-weight: bold;"> [<spring:message
			code="patientmatching.report.run" />] <span
			style="font-style: italic;"> <spring:message
			code="patientmatching.report.running" /> </span> </span></td>
	</tr>

	<tr id="runReport">
		<td colspan="2">
		<button onClick="runReport();"><spring:message
			code="patientmatching.report.run" /></button>
		</td>
	</tr>
</table>

<table cellspacing="2" cellpadding="2">
	<tr>
		<td><span style="font-weight: bold;"> <spring:message
			code="patientmatching.report.status" /> </span></td>
	</tr>

	<tr>
		<td>&nbsp;&nbsp;&nbsp;&nbsp; <span id="reportStatus">
		&nbsp; </span></td>
	</tr>

	<!--
            <tr>
                <td>
                    <span style="font-style: italic; font-weight: bold;" id="serverTimer">
                        <spring:message code="patientmatching.report.serverTimer"/>
                    </span>
                </td>
            </tr>
            -->

	<tr>
		<td>&nbsp;</td>
	</tr>
</table>

<b class="boxHeader">Available Reports</b>
<div class="box">
<table cellspacing="2" cellpadding="2">
	<tr>
		<th>Analysis Reports</th>
		<th>&nbsp;&nbsp;&nbsp;&nbsp;</th>
		<th colspan="2">Select Action</th>
	</tr>

	<tbody id="report-list">
		<c:forEach items="${reportResults}" var="reportResult"
			varStatus="entriesIndex">
			<tr <c:if test="${entriesIndex.count % 2 == 0}">class="oddRow"</c:if>
				<c:if test="${entriesIndex.count % 2 != 0}">class="evenRow"</c:if>>
				<td><c:out value="${reportResult}" /></td>
				<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
				<td><a href="javascript:;"
					onClick="viewFile('<c:out value="${reportResult}" />');"> View
				</a></td>
				<td><a href="javascript:;"
					onClick="deleteFile('<c:out value="${reportResult}" />');">
				Delete </a></td>
			</tr>
		</c:forEach>
	</tbody>

</table>
</div>

</div>
</form>
<br />

<%@ include file="/WEB-INF/template/footer.jsp"%>