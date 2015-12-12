<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<openmrs:require privilege="View Patients, View Patient Cohorts" otherwise="/login.htm" redirect="/module/patientmatching/schedule.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>
<h2><spring:message code="patientmatching.schedule.title"/></h2>

<script type="text/javascript">

function validate(obj){
	var check = document.getElementsByName("taskId");
	var count = 0;
	for(var i=0;i<check.length;i++){
		if(check[i].checked == true){
			++count;
		}
	}

	if(count <= 0){
		alert("No tasks selected to "+obj.value);
		return false;
	}else{
		return true;
	}
}

</script>

<style>
	tr.top, tr.bottom { background-color: #E7E7E7; border: 1px solid black; }
	th, td { text-align: center; vertical-align: top; padding: 10px;  } 
	th.left, td.left { text-align: left; } 
	td.button { border: 0; } 
	tr.even { background-color: #F7F7F7; }
</style>

<br/>
<a href="schedule.form"><spring:message code="patientmatching.schedule.create"/></a>
<br/>
<br/>
<b class="boxHeader"><spring:message code="patientmatching.schedule.list" /></b>
<div>
<form method="post">
    <table cellspacing="2" class="box">
        <tr class="top">
            <th></th>
            <th><spring:message code="patientmatching.schedule.status"/></th>
            <th><spring:message code="patientmatching.schedule.tasks"/></th>
            <th><spring:message code="patientmatching.schedule.schedule"/></th>
            <th><spring:message code="patientmatching.schedule.last"/></th>
        </tr>
        <c:forEach items="${allTasks}" var="selectedTask" varStatus="taskIndex">
        
        		<c:set var="rowColor" value="oddRow" />
				<c:if test="${taskIndex.index % 2 == 0}">
					<c:set var="rowColor" value="evenRow"/>
				</c:if>
        <tr class="${rowColor}">
        	<td><input type="checkbox" size="3" name="taskId" value="${selectedTask.task.id}"/></td>
        	<td valign="top" align="center"><c:choose>
					<c:when test="${selectedTask.task.started}">
						<font color="green"><strong><spring:message code="patientmatching.schedule.started"/></strong></font><br>										
						<c:if test="${selectedTask.task.startTime!=null}">
							<i>Runs again in <strong>${selectedTask.task.secondsUntilNextExecutionTime}s</strong></i>
						</c:if>
					</c:when>
					<c:otherwise>
						<font color="red"><strong><spring:message code="patientmatching.schedule.stopped"/></strong></font>
					</c:otherwise>
				</c:choose></td>
            <td class="left"><a href="schedule.form?taskId=${selectedTask.task.id}"><strong>${selectedTask.name}</strong></a></td>
            <td class="left">Runs every <strong>${intervals[selectedTask.task]}</strong> 
				<c:if test="${selectedTask.task.startTime!=null}">	
						<fmt:formatDate var="taskStartTime" pattern="hh:mm:ssa" value="${selectedTask.task.startTime}" />
						<fmt:formatDate var="taskStartDate" pattern="MMM dd yyyy" value="${selectedTask.task.startTime}" />							 	
				 	 from <strong>${taskStartTime}</strong>, 
				 	<br/>starting on <strong>${taskStartDate}</strong>
				</c:if>							
			</td>
            <td class="left"><openmrs:formatDate date="${selectedTask.task.lastExecutionTime}" type="long" /></td>
        </tr>
        </c:forEach>
        <tr class="bottom">
			<td colspan="6">
				<input type="submit" value="<spring:message code="Scheduler.taskList.start"/>" name="action" onclick="return validate(this);">
				<input type="submit" value="<spring:message code="Scheduler.taskList.stop"/>" name="action" onclick="return validate(this);">
				<input type="submit" value="<spring:message code="Scheduler.taskList.delete"/>" name="action" onclick="return validate(this);">
						
			</td>
		</tr>
    </table>
    </form>
</div>
<br/>
<%@ include file="/WEB-INF/template/footer.jsp" %>