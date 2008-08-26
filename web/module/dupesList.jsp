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
                <th colspan="2">Analysis Result</th>
            </tr>
                <c:set var="currentGroupId" value="-9999" />
                <c:forEach items="${analysisResults}" var="analysisResult">
                    <c:choose>
                        <c:when test="${currentGroupId == analysisResult['Group Id']}">
                        </c:when>
                        <c:otherwise>
                            <c:set var="currentGroupId" value="${analysisResult['Group Id']}" />
                            <tr>
                                <td colspan="2">****************************************</td>
                            </tr>
                            <tr>
                                <td>Group</td>
                                <td><c:out value="${analysisResult['Group Id']}" /></td>
                            </tr>
                            <tr>
                                <td colspan="2">&nbsp;</td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                    <tr>
                        <td>UID</td>
                        <td><c:out value="${analysisResult['UID']}" /></td>
                    </tr>
                    <c:forEach var="item" items="${analysisResult}">
                        <c:if test="${item.key != 'Group Id' && item.key != 'UID'}">
                            <tr>
                                <td>
                                    <spring:message code="${item.key}" />
                                </td>
                                <td>
                                    <c:out value="${item.value}" />.
                                </td>
                            </tr>
                        </c:if>
                    </c:forEach>
                    <tr>
                        <td colspan="2">&nbsp;</td>
                    </tr>
                </c:forEach>
            <tr>
                <td colspan="2">****************************************</td>
            </tr>
        </table>
</div>
<!--<br/><input type="submit" value="<spring:message code="general.save" />" />-->
</form>
<br/>

<%@ include file="/WEB-INF/template/footer.jsp" %>