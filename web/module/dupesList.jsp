<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patients, View Patient Cohorts" otherwise="/login.htm" redirect="/module/patientmatching/dupes.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>
<openmrs:htmlInclude file="/dwr/interface/DWRMatchingConfigUtilities.js"/>
<openmrs:htmlInclude file="/dwr/engine.js"/>
<openmrs:htmlInclude file="/dwr/util.js"/>

<script type="text/javascript">
function runReport() {
    if (confirm("Are you sure you want to generate new report?")) {
        DWRMatchingConfigUtilities.doAnalysis();
    }
}
</script>

<h2><spring:message code="patientmatching.report.title"/></h2>

<br/>

<b class="boxHeader"><spring:message code="patientmatching.report.run" /></b>
<form method="post">
<div class="box">
        <table cellspacing="2">
            <tr>
                <th colspan="2"><spring:message code="patientmatching.report.blocking"/></th>
                <c:forEach items="${blockingRuns}" var="blockingRun" varStatus="entriesIndex">
                  <tr>
                    <td>
                        <c:out value="${entriesIndex.count}" />.
                    </td>
                    <td>
                        <c:out value="${blockingRun}" />.
                    </td>
                  <tr>
                </c:forEach>
                  <tr>
                    <td colspan="2">
                        <a href="javascript:;" onClick="runReport();">
                            Run Report
                        </a>
                    </td>
                  <tr>
            </tr>
        </table>
        <table>
            <tr>
                <th colspan="2">Analysis Reports</th>
            </tr>
                <c:set var="currentGroupId" value="-9999" />
                <c:forEach items="${reportResults}" var="reportResult" varStatus="entriesIndex">
                  <tr>
                    <td>
                        <c:out value="${entriesIndex.count}" />.
                    </td>
                    <td>
                        <a href=href="javascript:;" onClick="displayReport('<c:out value="${reportResult}" />');">
                            <c:out value="${reportResult}" />.
                        </a>
                    </td>
                  <tr>
                </c:forEach>
        </table>
</div>
<!--<br/><input type="submit" value="<spring:message code="general.save" />" />-->
</form>
<br/>

<%@ include file="/WEB-INF/template/footer.jsp" %>