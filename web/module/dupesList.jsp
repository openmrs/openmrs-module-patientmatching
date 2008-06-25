<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patients, View Patient Cohorts" otherwise="/login.htm" redirect="/module/patientmatching/dupes.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="patientmatching.report.title"/></h2>

<br/>

<b class="boxHeader"><spring:message code="patientmatching.report.new" /></b>
<div class="box">
    <form method="post">
        <table cellspacing="2">
            <tr>
                <th colspan="2"><spring:message code="patientmatching.report.run"/></th>
            </tr>
        </table>
    </form>
</div>
<br/>

<%@ include file="/WEB-INF/template/footer.jsp" %>