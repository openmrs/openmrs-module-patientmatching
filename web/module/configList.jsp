<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patients, View Patient Cohorts" otherwise="/login.htm" redirect="/module/patientmatching/config.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="patientmatching.config.title"/></h2>

<br/>

<a href="${pageContext.request.contextPath}/module/patientmatching/config.form">
    <spring:message code="patientmatching.config.new" />
</a>

<br />
<br />

<b class="boxHeader"><spring:message code="patientmatching.config.list.available" /></b>
<div class="box">
    <form method="post">
        <table cellspacing="2">
            <tr>
                <th><spring:message code="patientmatching.config.list.name"/></th>
                <th><spring:message code="Operation"/></th>
            </tr>
            <c:forEach items="${files}" var="file">
                <tr>
                    <td>
                        <c:out value="${file}" />
                    </td>
                    <td align="center">
                        <a href="${pageContext.request.contextPath}/module/patientmatching/config.form?filename=${file}">
                            <c:out value="Edit" />
                        </a>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </form>
</div>
<br/>

<%@ include file="/WEB-INF/template/footer.jsp" %>