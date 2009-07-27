<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="View Patients, View Patient Cohorts"
	otherwise="/login.htm" redirect="/module/patientmatching/dupes.list" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>
<openmrs:htmlInclude file="/dwr/interface/DWRMatchingConfigUtilities.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />

<script type="text/javascript">

var n_steps = 11; // hard-coded for now; unlikely to change
var s = 0;

function runReport() {
    if (confirm("Are you sure you want to generate a new report?")) {
<<<<<<< .mine
        // location.reload(); // to reset the checklist
=======
        //location.reload(); // to reset the checklist
>>>>>>> .r9428
        
        showRunReport(false);
        DWRMatchingConfigUtilities.doAnalysis();
        showRunReport(false);
    }
}

function deleteFile(file) {
    if (confirm("Are you sure you want to delete \'" + file + "\'?")) {
        DWREngine.beginBatch();
        DWRMatchingConfigUtilities.deleteReportFile(file);
        buildTable();
        DWREngine.endBatch();
        updateStatus();

<<<<<<< .mine
        // location.reload();
=======
        //location.reload();
>>>>>>> .r9428
    }
}

function viewFile(file) {
        window.open("${pageContext.request.contextPath}/module/patientmatching/report.form?<c:out value="${reportParam}" />=" + file,
                    "Report",
                    "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,copyhistory=no");
        updateStatus();
}

function showRunReport(show) {
    var runReport = document.getElementById("runReport");
    if (show) {
        runReport.style.display = "block";
    } else {
        runReport.style.display = "none";
    }
}

function updateTimer() { // deprecated
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
	var refreshPeriod = 5; // in seconds
	DWRMatchingConfigUtilities.getStep(function updateChecklist(nStr) {
		n = parseInt(nStr)+1; // add one to correspond to the step ids in the HTML
		strikeUpToStep(n);
		if (n == n_steps) {
            DWREngine.beginBatch();          
            buildTable();
            DWREngine.endBatch();

            DWRMatchingConfigUtilities.resetStep();

            strikeUpToStep(1);
		}
	});

    s = setTimeout('updateStatus()', refreshPeriod*1000);
}

function strikeStep(n) {
	step = document.getElementById("step" + n);
	step.innerHTML = "<span style=\"color: green;\">" + step.innerHTML + "</span>";
}

function unstrikeStep(n) {
	step = document.getElementById("step" + n);
	step.innerHTML = step.innerHTML.replace(/<\/?[^>]+(>|$)/g, ''); // strip the highlighting tags
}

function strikeUpToStep(n) {
	// strike everything up to n
	for (var i=1; i<=n; i++) {
		strikeStep(i);
	}
	// and unstrike everything that follows
	for (var i=n+1; i<=n_steps; i++) {
		unstrikeStep(i);
	}
}

function trim(str) {
	return str.replace(/^\s+|\s+$/g, '');
}

function trimPeriods(str) {
	return str.replace(/[.]/g, '');
}

function getStepNumberByName(name) {
	var maxNumSteps = 100;
	for (var i=1; i<=maxNumSteps; i++) {
		step = document.getElementById("step" + i);
		if (trim(trimPeriods(step.innerHTML)) == trim(trimPeriods(name))) {
			return i;
		}
	}

	return -1;
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

window.onload = updateStatus();
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
	<tr>
		<td colspan="2">
		<ol>
			<c:forEach items="${blockingRuns}" var="blockingRun"
				varStatus="entriesIndex">
				<li><c:out value="${blockingRun}" /></li>
			</c:forEach>
		</ol>
		</td>
	</tr>
</table>

<table cellspacing="2" cellpadding="2">
	<tr>
		<td colspan="2"><span style="font-weight: bold;"> <spring:message
			code="patientmatching.report.checklist" /></span>

		<ul>
			<c:forEach items="${stepList}" var="step" varStatus="entriesIndex">
				<li><span id="step${entriesIndex.count}"><c:out
					value="${step}" /></span></li>
			</c:forEach>
		</ul>
		</td>
	</tr>

	<tr id="runReport">
		<td colspan="2">
		<button onClick="runReport();"><spring:message
			code="patientmatching.report.run" /></button>
		</td>
	</tr>
</table>

<table cellspacing="2" cellpadding="2">

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
		<th>Report</th>
		<th>&nbsp;&nbsp;&nbsp;&nbsp;</th>
		<th colspan="2">Operation</th>
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