<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patients, View Patient Cohorts" otherwise="/login.htm" redirect="/module/patientmatching/config.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<br />

<form method="post">
<b class="boxHeader"><spring:message code="patientmatching.config.new"/></b>
<div class="box">
    <table cellspacing="2" cellpadding="2">
        <tr>
            <td><spring:message code="patientmatching.config.new.name"/></td>
            <td>
            <spring:bind path="patientMatchingConfig.configName">
                <input type="text" 
                    name="${status.expression}" id="${status.expression}"
                    value="${status.value}" size="40" />
            </spring:bind>
            </td>
        </tr>
    </table>
    <table cellspacing="2" cellpadding="2">
        <tr>
            <td colspan="2">
            <spring:bind path="patientMatchingConfig.useRandomSampling">
                <input type="hidden" name="_<c:out value="${status.expression}"/>">
                <input type="checkbox" name="<c:out value="${status.expression}"/>" value="true"
                    <c:if test="${status.value}">checked</c:if>/>
            </spring:bind><spring:message code="patientmatching.config.new.useRandomSampling"/>
            </td>
        </tr>
        <tr>
            <td><spring:message code="patientmatching.config.new.randomSampleSize"/></td>
            <td>
            <spring:bind path="patientMatchingConfig.randomSampleSize">
                <input type="text" 
                    name="${status.expression}" id="${status.expression}"
                    value="${status.value}" size="10" />
            </spring:bind>
            </td>
        </tr>
    </table>
    <table table cellspacing="2" cellpadding="2">
        <tr>
            <td valign="top">
                <b class="boxHeader"><spring:message code="patientmatching.config.new.availableField"/></b>
                <div class="box">
                    <table cellspacing="2" cellpadding="2">
                        <tr>
                            <th><spring:message code="patientmatching.config.new.fieldName"/></th>
                            <th><spring:message code="patientmatching.config.new.fieldNameInclude"/></th>
                            <th><spring:message code="patientmatching.config.new.fieldName"/></th>
                            <th><spring:message code="patientmatching.config.new.fieldNameInclude"/></th>
                            <th><spring:message code="patientmatching.config.new.fieldName"/></th>
                            <th><spring:message code="patientmatching.config.new.fieldNameInclude"/></th>
                        </tr>
                        <c:forEach items="${patientMatchingConfig.configEntries}" var="configEntry" varStatus="entriesIndex">
                            <c:choose>
                                <c:when test="${(entriesIndex.count - 1) % 3 == 0}">
                                    <tr>
                                </c:when>
                                <td nowrap="nowrap">
                                    <spring:message code="${configEntry.fieldName}"/>
                                </td>
                                <td align="center">
                                <spring:bind path="patientMatchingConfig.configEntries[${entriesIndex.count - 1}].selected">
                                    <input type="hidden" name="_<c:out value="${status.expression}"/>">
                                    <input type="checkbox" name="<c:out value="${status.expression}"/>" value="true"
                                        <c:if test="${status.value}">checked</c:if>/>
                                </spring:bind>
                                </td>
                                <c:when test="${(entriesIndex.count - 1) % 3 == 2}">
                                    </tr>
                                </c:when>
                            </c:choose>
                        </c:forEach>
                    </table>
                </div>
            </td>
        </tr>
    </table>
</div>
<br/><input type="submit" value="<spring:message code="general.save" />" />
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>
