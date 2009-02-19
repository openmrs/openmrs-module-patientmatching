<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patients, View Patient Cohorts" otherwise="/login.htm" redirect="/module/patientmatching/config.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<openmrs:htmlInclude file="/dwr/interface/DWRMatchingConfigUtilities.js"/>
<openmrs:htmlInclude file="/dwr/engine.js"/>
<openmrs:htmlInclude file="/dwr/util.js"/>

<script type="text/javascript">

    function getNextPage() {
        DWRMatchingConfigUtilities.getNextPage(function writeReports(reports) {
            DWRUtil.removeAllRows("report-list");
            
            var main = document.getElementById("report-list");
            
            currentGroup = -999;
            currentClass = "oddRow";
            groupChange = false;
            
            for(i = 0; i < reports.length; i ++) {
                var row = reports[i];
                var tr = document.createElement("tr");
                
                if (currentGroup != row[0]) {
                    groupChange = true;
                    currentGroup = row[0];
                } else {
                    groupChange = false;
                }
                
                if(groupChange) {
                    if (currentClass == "oddRow") {
                        currentClass = "evenRow";
                    } else {
                        currentClass = "oddRow";
                    }
                }
                tr.className = currentClass;
                main.appendChild(tr);
                
                for (j = 0; j < row.length; j ++) {
                    var td = document.createElement("td");
                    td.innerHTML = row[j];
                    tr.appendChild(td);
                }
            }
        });
    }

    function getPrevPage() {
        DWRMatchingConfigUtilities.getPrevPage(function writeReports(reports) {
            DWRUtil.removeAllRows("report-list");
            
            var main = document.getElementById("report-list");
            
            currentGroup = -999;
            currentClass = "oddRow";
            groupChange = false;
            
            for(i = 0; i < reports.length; i ++) {
                var row = reports[i];
                var tr = document.createElement("tr");
                
                if (currentGroup != row[0]) {
                    groupChange = true;
                    currentGroup = row[0];
                } else {
                    groupChange = false;
                }
                
                if(groupChange) {
                    if (currentClass == "oddRow") {
                        currentClass = "evenRow";
                    } else {
                        currentClass = "oddRow";
                    }
                }
                tr.className = currentClass;
                main.appendChild(tr);
                for (j = 0; j < row.length; j ++) {
                    var td = document.createElement("td");
                    td.innerHTML = row[j];
                    tr.appendChild(td);
                }
            }
        });
    }

</script>

<h2><spring:message code="patientmatching.report.title"/></h2>

<br />
<a href="javascript:window.close()" title="Close This Window">Close This Window</a>
<br />
<br />

<b class="boxHeader"><spring:message code="patientmatching.report.run" /></b>
<div class="box">
        <table cellspacing="2" cellpadding="2">
            <tr>
                <c:forEach items="${reportHeader}" var="headerCell">
                    <td>
                        <spring:message code="${headerCell}"/>
                    </td>
                </c:forEach>
            </tr>
            <tbody id="report-list">
            
            <c:set var="group" value="-999" scope="page" />
            <c:set var="groupChange" value="false" scope="page" />
            <c:set var="currentClass" value="oddRow" scope="page" />
            
            <c:forEach items="${report}" var="reportResult" varStatus="entriesIndex">
                <c:choose>
                    <c:when test="${group != reportResult[0]}">
                        <c:set var="group" value="${reportResult[0]}" scope="page" />
                        <c:set var="groupChange" value="true" scope="page" />
                    </c:when>
                    <c:when test="${group == reportResult[0]}">
                        <c:set var="groupChange" value="false" scope="page" />
                    </c:when>
                </c:choose>
            
                <c:if test="${groupChange}">
                    <c:choose>
                        <c:when test="${group % 2 == 0}">
                            <c:set var="currentClass" value="evenRow" scope="page" />
                        </c:when>
                        <c:when test="${group % 2 != 0}">
                            <c:set var="currentClass" value="oddRow" scope="page" />
                        </c:when>
                    </c:choose>
                </c:if>
            
                <tr class="<c:out value="${currentClass}" />">
                
                <c:forEach items="${reportResult}" var="reportResultCell" varStatus="entriesIndexInner">
                    <td>
                        <c:out value="${reportResultCell}" />
                    </td>
                </c:forEach>
            </tr>
            </c:forEach>
            </tbody>
            
        </table>
</div>

<br/>
<input type="submit" value="Prev" onClick="getPrevPage();"/>
<input type="submit" value="Next" onClick="getNextPage();"/>
<br />

<%@ include file="/WEB-INF/template/footer.jsp" %>
