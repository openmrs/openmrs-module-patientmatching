<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patients, View Patient Cohorts" otherwise="/login.htm" redirect="/module/patientmatching/config.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<br />

<script type="text/javascript">
    function selectOnly(fieldName) {
        var blocking = "blocking";
        var included = "included";
        
        var fieldSep = fieldName.indexOf(".");
        var objectName = fieldName.substring(0, fieldSep + 1);
        
        var includedField = document.getElementById(objectName + included);
        var blockingField = document.getElementById(objectName + blocking);
        
        if (fieldName == includedField.name) {
            if(includedField.checked) {
                blockingField.checked = false;
            }
        } else {
            if(blockingField.checked) {
                includedField.checked = false;
            }
        }
    }
</script>

<form method="post">
<b class="boxHeader"><spring:message code="patientmatching.config.new"/></b>
<div class="box">
    <table cellspacing="2" cellpadding="2">
        <tr class="oddRow">
            <td>
                <span style="padding-left: 5px; font-weight: bold;"><spring:message code="patientmatching.config.new.name"/></span>
            </td>
            <td style="vertical-align: middle">
            <spring:bind path="patientMatchingConfig.configName">
                <input type="text" 
                    name="${status.expression}" id="${status.expression}"
                    value="${status.value}" size="40" />
            </spring:bind>
            </td>
        </tr>
    </table>
    <!--
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
    -->
    <table cellspacing="2" cellpadding="2">
        <tr>
            <td valign="top">
                <b class="boxHeader"><spring:message code="patientmatching.config.new.availableField"/></b>
                <div class="box" style="padding-right: 10px;">
                    <table cellspacing="2" cellpadding="2">
                        <tr>
                            <th><spring:message code="patientmatching.config.new.fieldName"/></th>
                            <th><spring:message code="patientmatching.config.new.fieldNameInclude"/></th>
                            <th><spring:message code="patientmatching.config.new.fieldNameBlocking"/></th>
                            <th>&nbsp;</th>
                            <th><spring:message code="patientmatching.config.new.fieldName"/></th>
                            <th><spring:message code="patientmatching.config.new.fieldNameInclude"/></th>
                            <th><spring:message code="patientmatching.config.new.fieldNameBlocking"/></th>
                            <th>&nbsp;</th>
                            <th><spring:message code="patientmatching.config.new.fieldName"/></th>
                            <th><spring:message code="patientmatching.config.new.fieldNameInclude"/></th>
                            <th><spring:message code="patientmatching.config.new.fieldNameBlocking"/></th>
                        </tr>
                        <c:forEach items="${patientMatchingConfig.configEntries}" var="configEntry" varStatus="entriesIndex">
                            <c:choose>
                                <c:when test="${(entriesIndex.count - 1) % 3 == 0}">
                                    <tr <c:if test="${entriesIndex.count % 2 == 0}">class="oddRow"</c:if>
                                        <c:if test="${entriesIndex.count % 2 != 0}">class="evenRow"</c:if>>
                                </c:when>
                                <td nowrap="nowrap">
                                    <spring:message code="${configEntry.fieldViewName}"/>
                                </td>
                                <td align="center">
                                <spring:bind path="patientMatchingConfig.configEntries[${entriesIndex.count - 1}].included">
                                    <input type="hidden" name="_<c:out value="${status.expression}"/>">
                                    <input type="checkbox"
                                        name="<c:out value="${status.expression}"/>" value="true"
                                        id="<c:out value="${status.expression}"/>"
                                        onClick="selectOnly('<c:out value="${status.expression}"/>')"
                                        <c:if test="${status.value}">checked</c:if>/>
                                </spring:bind>
                                </td>
                                <td align="center">
                                <spring:bind path="patientMatchingConfig.configEntries[${entriesIndex.count - 1}].blocking">
                                    <input type="hidden" name="_<c:out value="${status.expression}"/>">
                                    <input type="checkbox"
                                        name="<c:out value="${status.expression}"/>" value="true"
                                        id="<c:out value="${status.expression}"/>"
                                        onClick="selectOnly('<c:out value="${status.expression}"/>')"
                                        <c:if test="${status.value}">checked</c:if>/>
                                </spring:bind>
                                </td>
                                <c:if test="${(entriesIndex.count - 1) % 3 < 2}">
                                    <td>&nbsp;</td>
                                </c:if>
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
