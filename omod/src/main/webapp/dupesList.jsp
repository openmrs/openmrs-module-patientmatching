<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require anyPrivilege="View Patients, View Patient Cohorts"
	otherwise="/login.htm" redirect="/module/patientmatching/dupes.list" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<div id="openmrs_msg" style="display:none"><spring:message code="TaskMessage" text="Scheduled Task Report Generation is Running"/></div>
<%@ include file="localHeader.jsp"%>
<openmrs:htmlInclude file="/dwr/interface/DWRMatchingConfigUtilities.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />

<script type="text/javascript">

var n_steps = 11; // hard-coded for now; unlikely to change
var s = 0;
var count = 0;
var actRA = "true";

function runReport() {
    if (confirm("Are you sure you want to generate a new report?")) {
    	document.getElementById("openmrs_msg").style.display = "none";
		var blockList = document.getElementsByName("blockList");
		var blListStr = "";
		for(var i=0;i<blockList.length;i++){
			if(blockList[i].checked == true)
				blListStr = blListStr+blockList[i].value+",";
		}
		if(blListStr != ""){
			if(actRA=="true")
				DWRMatchingConfigUtilities.doAnalysis(blListStr);
			else
				s = setTimeout("updateStatus()", 100);
		}else alert("Select atleast one Strategy");
    }
}

function scheduledTaskRunning(){
	document.getElementById("openmrs_msg").style.display = "";
}

function selectAll(){
	var box = document.getElementsByName("blockList");
	for(var i=0;i<box.length;i++){
		box[i].checked = true;
	}
}

function deselectAll(){
	var box = document.getElementsByName("blockList");
	for(var i=0;i<box.length;i++){
		box[i].checked = false;
	}
}

function reportProcessStarted(){

	document.getElementById("run").disabled = true;
    for(var i=1;i<n_steps;i++){
		var processTime = document.getElementById("time"+i);
		processTime.innerHTML = "";
	}
	 var step = document.getElementById("step1");
		step.style.color="black";
		var step = document.getElementById("step11");
		step.style.color="black";
     var step = document.getElementById("step2");
		step.style.color="green";
}

function resetChecklist() {
	if (confirm("Are you sure you want to reset the checklist?")) {
		DWRMatchingConfigUtilities.resetStep();
	}
}
function reset(){
strikeUpToStep(0);
}

function enableGenReport(){
	document.getElementById("run").disabled = false;
}

function deleteFile(file) {
    if (confirm("Are you sure you want to delete \'" + file + "\'?")) {
        DWREngine.beginBatch();
        DWRMatchingConfigUtilities.deleteReportFile(file);
        buildTable();
        DWREngine.endBatch();
        //updateStatus();

        //location.reload();
    }
}

function viewFile(file) {
        window.open("${pageContext.request.contextPath}/module/patientmatching/report.form?<c:out value="${reportParam}" />=" + file,
                    "Report",
                    "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,copyhistory=no");
        //updateStatus();
}


function viewMetadata(file) {

    DWRMatchingConfigUtilities.setReportName(file);
	window.open("${pageContext.request.contextPath}/module/patientmatching/metadata.form?<c:out value="${reportParam}" />=" + file,
                "Report",
                "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,copyhistory=no");
    //updateStatus();
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
function updateChecklist(nStr) {
	var nSt = nStr.split(",");
	if(nSt[0]!="0"){
	n = parseInt(nSt[0])+1; // add one to correspond to the step ids in the HTML
	if(nSt[1].indexOf('p') != -1){
		var ti=nSt[1].split("p");
		var processTime = document.getElementById("time9");
		processTime.style.color = "red";
		processTime.innerHTML = ti[0]+" ms";
	}else{
		var processTime = document.getElementById("time"+nSt[0]);
		processTime.style.color = "red";
		processTime.innerHTML = nSt[1]+" ms";
	}
	strikeStep(n);
	if (n == n_steps) {
        DWREngine.beginBatch();          
        buildTable();
        DWREngine.endBatch();
	}
	}else if(nSt[0]=="0"){
		strikeUpToStep(0);
	}
}

function prevProStat(timeD){
	var times = timeD+"";
	var time = times.split(",");
	for(var i=0;i<time.length;i++){
		var processTime = document.getElementById("time"+(i+2));
		processTime.style.color = "red";
		processTime.innerHTML = time[i]+" ms";
	}
}

function check(currentStep){
	var stepDe = currentStep.split(",");
	var cuStep = parseInt(stepDe[0]);
	if(cuStep==0){
		if(stepDe[1]=="true"){
			document.getElementById("run").disabled = true;
		}
		s = setTimeout("strikeUpToStep(0)", 0.1*1000);
	}else if(cuStep>=2){
		var step = document.getElementById("step" + cuStep);
		step.style.color="green";
		document.getElementById("run").disabled = true; 
		DWRMatchingConfigUtilities.previousProcessStatus(prevProStat);
	}
	if(stepDe[2]=="false"){
		if(stepDe[3]=="-1"){
			reset();
		}
		if(stepDe[4]=="true"&&count==0){
			scheduledTaskRunning();
			count++;
		}
		actRA == "false";
		s = setTimeout("updateStatus()", 1000);
	}
}

function callOnloadFunctions(){
	dwr.engine.setActiveReverseAjax(true);
	<%
	if(session.getAttribute("selStrategy")!=null){
		String selected = (String) session.getAttribute("selStrategy");%>
		var selected = "<%=selected%>";
		if(selected != ""){
			var listSelected = selected.split(",");
			for(var i=0;i<listSelected.length;i++){
				document.getElementById(listSelected[i]).checked = true;
			}
		}
	<%}%>
	updateStatus();
}

function updateStatus() {
	DWRMatchingConfigUtilities.getStep(check);
}
function strikeStep(n) {
	var step = document.getElementById("step" + n);
	step.style.color="green";
	for (var i=1; i<=n_steps; i++) {
		if(i != n){
		step = document.getElementById("step" + i);
		step.style.color="black";
		}
	}
	if(n==n_steps){
		document.getElementById("run").disabled = false;
	}
}

function unstrikeStep(n) {
	step = document.getElementById("step" + n);
	if(n==1){
		step.style.color="green";
	}else {
		var processTime = document.getElementById("time"+n);
		processTime.innerHTML = "";
		step.style.color="black";
	}
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
            function(data) { return "<a href=\"javascript:;\" onClick=\"viewFile('" + data + "')\">View Report</a>"; },
            function(data) { return "<a href=\"javascript:;\" onClick=\"viewMetadata('" + data + "')\">View Statistics</a>"; },
            function(data) { return "<a href=\"javascript:;\" onClick=\"deleteFile('" + data + "')\">Delete Report</a>"; }
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

function storeSelStrategy(){
	var list = document.getElementsByName("blockList");
	var selected = "";
	var coun = 1;
	for(var i=0;i<list.length;i++){
		if(list[i].checked == true){
			if(coun>1)
				selected = selected+",";
			selected = selected+list[i].value;
			coun++;
		}
	}
		DWRMatchingConfigUtilities.selStrategy(selected);
}

window.onload = callOnloadFunctions;
window.onbeforeunload = storeSelStrategy;
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
				<li style="list-style-type: none"><input type="checkbox" id="${blockingRun}" name="blockList" value="${blockingRun}"/>${entriesIndex.index+1}.&nbsp;<c:out value="${blockingRun}" /></li>
			</c:forEach>
		</ol>
		</td>
	</tr>
	<tr><td><input type="button" value="Select All" onclick="selectAll()"/><input type="button" value="Deselect All" onclick="deselectAll()"/></td></tr>
</table>

<table cellspacing="2" cellpadding="2">
	<tr>
		<td colspan="2"><span style="font-weight: bold;"> <spring:message
			code="patientmatching.report.checklist" /></span>

		<ul>
			<c:forEach items="${stepList}" var="step" varStatus="entriesIndex">
				<li><span id="step${entriesIndex.count}" style="color:black"><c:out
					value="${step}" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<label id="time${entriesIndex.count}"></label></span></li>
			</c:forEach>
		</ul>
		</td>
	</tr>
	
	<tr id="runReport">
		<td>
		<input id="run" type="button" onClick="runReport();" value="<spring:message
			code="patientmatching.report.run" />"/>
		</td>
		<td>
		<input type="button" onClick="resetChecklist();" value="<spring:message code="patientmatching.report.reset"/>"/>
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
					onClick="viewFile('<c:out value="${reportResult}" />');"> View Report 
				</a></td>
				<td><form><input type="hidden" name="reportname" id="reportname"/></form><a href="javascript:;"
					onClick="viewMetadata('<c:out value="${reportResult}"/>');"> View Statistics 
				</a></td>
				<td><a href="javascript:;"
					onClick="deleteFile('<c:out value="${reportResult}" />');">
				Delete Report</a></td>
			
			</tr>
		</c:forEach>
	</tbody>

</table>
</div>

</div>
</form>
<br />

<%@ include file="/WEB-INF/template/footer.jsp"%>