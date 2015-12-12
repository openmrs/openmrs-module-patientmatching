<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patients, View Patient Cohorts" otherwise="/login.htm" redirect="/module/patientmatching/schedule.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<h2><spring:message code="patientmatching.schedule.title"/></h2>

<br/>

<script type="text/javascript">
window.onload = blockListDisplay;

function blockListDisplay(){
	var selected = "${blockList}";
	if(selected != ""){
		var listSelected = selected.split(",");
		for(var i=0;i<listSelected.length-1;i++){
			document.getElementById(listSelected[i]).checked = true;
		}
	}
}

function selectAll(){
	var box = document.getElementsByName("blockList");
	for(var i=0;i<box.length;i++){
		box[i].checked = true;
	}
}

function trim(myString)
{
return myString.replace(/^ +/,'').replace(/ +$/,'');
}

function deselectAll(){
	var box = document.getElementsByName("blockList");
	for(var i=0;i<box.length;i++){
		box[i].checked = false;
	}
}

function validate(){
	
	var count = 0;
	var check = document.getElementsByName("blockList");
	var startTime = document.getElementById("startTime").value;
	var repeatInterval = trim(document.getElementById("repeatInterval").value);
	var reg1 = /[0-1][0-9]\/[0-3][0-9]\/[0-9]{4} [0-2][0-9]:[0-6][0-9]:[0-6][0-9]/;
	var reg2 = /^[1-9][0-9]*/;
	if(repeatInterval != ""){
		var interval = reg2.exec(repeatInterval);
		document.getElementById("intervalError1").style.display = "none";
	}else if(repeatInterval == ""){
		interval = repeatInterval; 
		document.getElementById("intervalError1").style.display = "";
		++count;
	}
	var selected = false;
	for(var i=0;i<check.length;i++){
		if(check[i].checked==true){
			selected = true;
		}
	}
	
	if(trim(document.getElementById("name").value)==""){
		document.getElementById("nameError").style.display = "";
		++count;
	}else{
		document.getElementById("nameError").style.display = "none";
	}

	if(trim(document.getElementById("description").value)==""){
		document.getElementById("descriptionError").style.display = "";
		++count;
	}else{
		document.getElementById("descriptionError").style.display = "none";
	}

	if(!selected){
		document.getElementById("blockListError").style.display = "";
		++count;
	}else{
		document.getElementById("blockListError").style.display = "none";
	}
	
	if(!(reg1.test(startTime))){
		document.getElementById("startTimeError").style.display = "";
		++count;
	}else{
		document.getElementById("startTimeError").style.display = "none";
	}

	if(interval==null||interval!=repeatInterval){
		document.getElementById("intervalError2").style.display = "";
		++count;
	}else{
		document.getElementById("intervalError2").style.display = "none";
	}

	if(count>0){
		return false;
	}else{
		return true;
	}
	 
}
</script>

<b class="boxHeader"><spring:message code="patientmatching.schedule.new" /></b>
<div class="box">
    <form method="post">
    <input type="hidden" name="taskId" value=""/>
        <table cellspacing="2">
            <tr>
                <th colspan="2"><spring:message code="patientmatching.schedule.parameter"/></th>
            </tr>
            <tr>
                <td><spring:message code="patientmatching.schedule.name" />:</td>
                <td>
                    <input type="text" 
                        name="name" size="30" id="name" value="${name}" />&nbsp;<span class="error" id="nameError" style="display:none"><spring:message code="patientmatching.schedule.nameRequired"/></span>
                </td>
            </tr>
            <tr><td><spring:message code="general.description"/>:</td><td>
            	<textarea name="description" id="description" rows="3" cols="60">${description}</textarea>&nbsp;<span class="error" id="descriptionError" style="display:none"><spring:message code="patientmatching.schedule.descriptionRequired"/></span></td>
            </tr>
            <tr>
            	<td>
	            	<table cellspacing="2" cellpadding="2">
						<tr>
							<th colspan="2"><spring:message
								code="patientmatching.report.blocking" />&nbsp;<span class="error" id="blockListError" style="display:none"><spring:message code="patientmatching.schedule.strategy"/></span></th>
						</tr>
						<tr>
							<td colspan="2">
							<ol>
								<c:forEach items="${blockingRuns}" var="blockingRun"
									varStatus="entriesIndex">
									<li style="list-style-type: none"><input type="checkbox" id="${blockingRun}" name="blockList" value="${blockingRun}"/>${entriesIndex.index+1}.&nbsp;<c:out value="${blockingRun}" /></li>
								</c:forEach>
							</ol>
							</td>
						</tr>
						<tr><td><input type="button" value="Select All" onclick="selectAll()"/><input type="button" value="Deselect All" onclick="deselectAll()"/></td></tr>
					</table>
				</td>
			</tr>
            <tr>
				<td><spring:message code="Scheduler.scheduleForm.startTimePattern"/>:</td>
				<td>
						<input type="text" id="startTimePattern" name="startTimePattern" size="25" value="${startTimePattern}" disabled/>
				</td>
			</tr>
			<tr>
				<td><spring:message code="patientmatching.schedule.date"/>:</td>
				<td>
						<input type="text" id="startTime" name="startTime" size="25" value="${startTime}"/>
						&nbsp;<span class="error" id="startTimeError" style="display:none"><spring:message code="patientmatching.schedule.dateRequired"/></span>
				</td>
			</tr>
			<tr>
				<td><spring:message code="patientmatching.schedule.repeat"/></td>
				<td><input type="text" id="repeatInterval" name="repeatInterval" size="10" value="${repeatInterval}"/>&nbsp;<select name="repeatIntervalUnits"><option value="days" <c:if test="${repeatIntervalUnits=='days'}">selected</c:if>>days</option><option value="weeks" <c:if test="${repeatIntervalUnits=='weeks'}">selected</c:if>>weeks</option></select>&nbsp;<span class="error" id="intervalError1" style="display:none"><spring:message code="patientmatching.schedule.repeatRequired"/></span><span class="error" id="intervalError2" style="display:none"><spring:message code="patientmatching.schedule.startWith"/></span></td>
			</tr>
            <tr>
                <td colspan="2">
                    <input type="submit" value="<spring:message code="general.save"/>" onclick="return validate();"/>
                </td>
            </tr>
        </table>
    </form>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>