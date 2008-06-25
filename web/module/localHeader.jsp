<ul id="menu">
    <li class="first">
        <a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
    </li>
    <openmrs:hasPrivilege privilege="View Patients, View Patient Cohorts">
        <li <c:if test="<%= request.getRequestURI().contains("schedule") %>">class="active"</c:if>>
            <a href="${pageContext.request.contextPath}/module/patientmatching/schedule.list">
                <spring:message code="patientmatching.schedule.view"/>
            </a>
        </li>
    </openmrs:hasPrivilege>
    <openmrs:hasPrivilege privilege="View Patients, View Patient Cohorts">
        <li <c:if test="<%= request.getRequestURI().contains("config") %>">class="active"</c:if>>
            <a href="${pageContext.request.contextPath}/module/patientmatching/config.list">
                <spring:message code="patientmatching.config.view"/>
            </a>
        </li>
    </openmrs:hasPrivilege>
    <openmrs:hasPrivilege privilege="View Patients, View Patient Cohorts">
        <li <c:if test="<%= request.getRequestURI().contains("dupes") %>">class="active"</c:if>>
            <a href="${pageContext.request.contextPath}/module/patientmatching/dupes.list">
                <spring:message code="patientmatching.report.view"/>
            </a>
        </li>
    </openmrs:hasPrivilege>
</ul>