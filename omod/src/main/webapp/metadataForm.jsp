<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:require privilege="View Patients, View Patient Cohorts" otherwise="/login.htm" redirect="/module/patientmatching/config.list" />
<%@ include file="/WEB-INF/template/header.jsp" %>
<openmrs:htmlInclude file="/dwr/interface/DWRMatchingConfigUtilities.js"/>
<openmrs:htmlInclude file="/dwr/engine.js"/>
<openmrs:htmlInclude file="/dwr/util.js"/>
<script type="text/javascript" src="${pageContext.request.contextPath}/moduleResources/patientmatching/includes/jquery-1.4.2.min.js"></script>
<openmrs:htmlInclude file="/scripts/jquery-ui/css/redmond/jquery-ui-1.7.2.custom.css" />
<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui-1.7.2.custom.min.js" />
<script type="text/javascript">
function viewReport(file) {
    window.open("${pageContext.request.contextPath}/module/patientmatching/report.form?<c:out value="${reportParam}" />=" + file,
                "Report",
                "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,copyhistory=no");
    //updateStatus();
}</script>

<html>
<head>
<title>Patient Matching Report Information</title>
</head>
<body>
<h2><spring:message code="patientmatching.report.info.title"/></h2>
<div id="full">
<div class="box">
<table  cellspacing="2" cellpadding="2">
<tr><h4><spring:message code="patientmatching.report.info.reportName"/></h4>${reportName}</tr>
<tr><h4><spring:message code="patientmatching.report.info.Createdby"/></h4>${createdBy}</tr>
<tr><h4><spring:message code="patientmatching.report.info.date"/></h4>${date}</tr>


<tr><td><h4><spring:message code="patientmatching.report.info.strategies"/></h4>
	<ul>
			<c:forEach items="${strategylist}" var="strats" varStatus="entriesIndex">
				<li style="list-style-type: none">${entriesIndex.index+1}.&nbsp;<c:out value="${strats}" /></li>
			</c:forEach>
		</ul>
		</td></tr>
</table>
    	<div class="box" style="padding-right: 10px;">	
    <table cellspacing="2" cellpadding="2">
    <h4 class="boxHeader"><spring:message code="patientmatching.report.info.processInfo"/></h4>
    		<tr> <th><spring:message code="patientmatching.report.info.pname"/></th>
    		     <th><spring:message code="patientmatching.report.info.ptime"/></th>
    		</tr>
		<tr><td valign="top">
		<ul>
			<c:forEach items="${stepList}" var="step" varStatus="entriesIndex">
				<li><span id="step${entriesIndex.count}" style="color:black"><c:out
					value="${step}" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span></li>
			</c:forEach>
		</ul></td>
		<td valign="top">
		<dl>
			<c:forEach items="${processlist}" var="pro" varStatus="entriesIndex">
				<span style="font-style: italic; font-weight: bold;"><dd style="list-style: inside;">-<span id="step${entriesIndex.count}" style="color:red"><c:out
				 value="${pro}" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span></dd></span>
			</c:forEach>
	</dl></td><tr></div>
 </table>
<a href="javascript:;"
					onClick="viewReport('<c:out value="${reportName}"/>');"> View Report </a>
</div>
</div>
<a href="javascript:window.close()" title="Close This Window">Close This Window</a>
</body>
</html>