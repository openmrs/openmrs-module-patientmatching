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

</script>

<html>
<head>

</head>
<body>
<h4>Interchangeable Fields are</h4>
<h4>Set1:${key1}</h4><br>
<h4>Set2:${key2}</h4><br>
<h4>Set3:${key4}</h4><br>
<h4>Set4:${key3}</h4><br>
<h4>Set5:${key5}</h4><br>

<a href="javascript:window.close()" title="Close This Window">Close This Window</a>

</body>
</html>