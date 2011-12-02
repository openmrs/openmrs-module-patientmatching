<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require anyPrivilege="View Patients, View Patient Cohorts" otherwise="/login.htm" redirect="/module/patientmatching/config.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<openmrs:htmlInclude file="/dwr/interface/DWRMatchingConfigUtilities.js"/>
<openmrs:htmlInclude file="/dwr/engine.js"/>
<openmrs:htmlInclude file="/dwr/util.js"/>

<script type="text/javascript">
function deleteClicked(id) {
    if (confirm("Are you sure you want to delete \'" + id + "\'?")) {
        DWREngine.beginBatch();
        DWRMatchingConfigUtilities.deleteBlockingRun(id);
        buildTable();
        DWREngine.endBatch();
    }
}

function buildTable() {
    DWRMatchingConfigUtilities.getAllBlockingRuns(function writeBlockingRuns(block) {
        DWRUtil.removeAllRows("config-list");
        var cellFuncs = [
            function(data) { return data; },
            function(data) {
                return "<a href=\"${pageContext.request.contextPath}/module/patientmatching/config.form?<c:out value="${parameter}" />=" + data + "\"><c:out value="Edit" /></a>";
            },
            function(data) {
                return "<a href=\"javascript:;\" onClick=\"deleteClicked('" + data + "');\"><c:out value="Delete" /></a>";
            }
        ];
        DWRUtil.addRows( "config-list", block, cellFuncs, {
            rowCreator:function(options) {
                var row = document.createElement("tr");
                var index = options.rowIndex;
                if (index % 2)
                    row.className = "oddRow";
                else
                    row.className = "evenRow";
                return row;
            },
            cellCreator:function(options) {
                var td = document.createElement("td");
                return td;
            },
            escapeHtml:false
        });
    });
}

</script>

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
        <table  cellspacing="2" cellpadding="2">
            <tr>
                <th align="left"><spring:message code="patientmatching.config.list.name"/></th>
                <th colspan="2"><spring:message code="Operation"/></th>
            </tr>
            <tbody id="config-list">
            <c:forEach items="${files}" var="file" varStatus="entriesIndex">
                <tr <c:if test="${entriesIndex.count % 2 == 0}">class="oddRow"</c:if>
                    <c:if test="${entriesIndex.count % 2 != 0}">class="evenRow"</c:if>>
                    <td>
                        <c:out value="${file}" />
                    </td>
                    <td align="center">
                        <a href="${pageContext.request.contextPath}/module/patientmatching/config.form?<c:out value="${parameter}" />=${file}">
                            <c:out value="Edit" />
                        </a>
                    </td>
                    <td align="center">
                        <a href="javascript:;" onClick="deleteClicked('<c:out value="${file}" />');">
                            <c:out value="Delete" />
                        </a>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </form>
</div>
<br/>

<%@ include file="/WEB-INF/template/footer.jsp" %>