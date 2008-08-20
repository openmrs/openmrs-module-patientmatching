<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patients, View Patient Cohorts" otherwise="/login.htm" redirect="/module/patientmatching/dupes.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

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
            </tr>
        </table>
        <table>
            <tr>
                <th>Analysis Result</th>
            </tr>
                <c:forEach items="${analysisResults}" var="analysisResult">
                    <tr>
                        <td>
                            <c:out value="${analysisResult}" />.
                        </td>
                    </tr>
                </c:forEach>
        </table>
</div>
<!--<br/><input type="submit" value="<spring:message code="general.save" />" />-->
</form>
<br/>

<%@ include file="/WEB-INF/template/footer.jsp" %>