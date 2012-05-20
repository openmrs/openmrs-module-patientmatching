<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require anyPrivilege="View Patients, View Patient Cohorts" otherwise="/login.htm" redirect="/module/patientmatching/config.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<openmrs:htmlInclude file="/dwr/interface/DWRMatchingConfigUtilities.js"/>
<openmrs:htmlInclude file="/dwr/engine.js"/>
<openmrs:htmlInclude file="/dwr/util.js"/>
<script type="text/javascript" src="${pageContext.request.contextPath}/moduleResources/patientmatching/includes/jquery-1.4.2.min.js"></script>
<openmrs:htmlInclude file="/scripts/jquery-ui/css/redmond/jquery-ui-1.7.2.custom.css" />
<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui-1.7.2.custom.min.js" />
<script type="text/javascript">
	var checkbox_choices = 0;
	var str="";

    function getNextPage() {
        DWRMatchingConfigUtilities.getNextPage(writeReports);
    }

    function getPrevPage() {
        DWRMatchingConfigUtilities.getPrevPage(writeReports);
    }

    function getStartPage(){
    	DWRMatchingConfigUtilities.getStartPage(writeReports);
    }

    function getEndPage(){
    	DWRMatchingConfigUtilities.getEndPage(writeReports);
    }

    function writeReports(reports) {
        DWRUtil.removeAllRows("report-list");
        
        var main = document.getElementById("report-list");
        
        currentGroup = -999;
        currentClass = "oddRow";
        groupChange = false;
    	mergeButton = false;
        preClass = "oddRow";

        for(i = 0; i < reports.length; i ++) {
            var row = reports[i];
            
            if (currentGroup != row[2]) {
                groupChange = true;
    			mergeButton = true;
                currentGroup = row[2];
				var tr = document.createElement("tr");
				var td = document.createElement("td");
				td.style.borderBottom = "2px solid black";
				td.colSpan = 100;
				tr.appendChild(td);
				main.appendChild(tr);

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
    		if(row[0]=="true"){
    					preClass = currentClass;
    					currentClass = "error";
    		}
			var tr = document.createElement("tr");
            tr.className = currentClass;
			tr.id = row[1]+"";
			tr.ondblclick = function ondblclick(){patientInfo(this)}
    		if(row[0]=="true"){
    			currentClass = preClass;
    		}
            for (j = 0; j < row.length; j ++) {
               if(j==0){
    			    var td = document.createElement("td");
					var hidField = document.getElementById(row[2]);
    				td.innerHTML = "<input type=\"checkbox\" id=\""+row[2]+"#"+row[1]+"\" name=\"patientId\" value=\""+row[1]+"\" onclick=\"val(this);\"/>";
    				tr.appendChild(td);
    			}else if(j>=2){
    				var td = document.createElement("td");
					if(j==2){
						var b = document.createElement("b");
						b.innerHTML = row[j];
						td.appendChild(b);
					}else if(j==3){
						var a = document.createElement("a");
						a.href = "javascript:patientInfo("+row[j]+");";
						a.innerHTML = row[j];
						td.appendChild(a);
						
					}else{
    		 			td.innerHTML = row[j];
					}
    				tr.appendChild(td);
    			}
                
            }
    		if(mergeButton){
    			var td = document.createElement("td");
    				td.innerHTML = "<input type=\"button\" id=\""+row[2]+"\" value=\"Merge GroupId "+row[2]+"\" id=\""+row[2]+"Group\" onclick=\"return mergePatients(this)\" name=\"sub\" disabled/>";
    				tr.appendChild(td);
    				mergeButton = false;
    		}
			main.appendChild(tr);
        }

    	var checkNames = document.getElementsByName("patients");
    	if(checkNames.length>0){
    		for(var i=0;i<checkNames.length;i++){
    			var num = 0;
    			var value = checkNames[i].value.split(',');
    			var id = checkNames[i].id.substring(0,checkNames[i].id.indexOf('h'));
    			for(var k=0;k<value.length-1;k++){
    				var box = document.getElementById(id+"#"+value[k]);
    				if(box!=null){
    					box.checked = true;
    				}
    				num++;
    			}
    			if(num>1 && document.getElementById(id)!=null){
    				document.getElementById(id).disabled = false;
    			}
    		}

			var patientId = document.getElementsByName("patientId");
			var id = checkNames[0].id.substring(0,checkNames[0].id.indexOf('h'));
			for(var i=0;i<patientId.length;i++){
				if(patientId[i].id == id+"#"+patientId[i].value){
					patientId[i].disabled = false;
				}else{
					patientId[i].disabled = true;
				}
			}
    	}
    }
    
 
function val(obj){
	
	var block = document.getElementById("report-header");

	var index = obj.id.indexOf('#');
	var groupId = obj.id.substring(0,index);
	var hidField = document.getElementById(groupId+"hidden");
	if(obj.checked){
		if(hidField==null){
			var element1 = document.createElement("input");
			element1.id=groupId+"hidden";
			element1.name="patients";
			element1.type="hidden";
			element1.value=obj.value+",";
			block.appendChild(element1);
			var patientId = document.getElementsByName("patientId");
			for(var i=0;i<patientId.length;i++){
				//var id = patientId[i].val
				if(patientId[i].id == groupId+"#"+patientId[i].value){
					patientId[i].disabled = false;
				}else{
					patientId[i].disabled = true;
				}
			}
		}else{
			hidField.value=hidField.value+obj.value+",";
			var size = hidField.value.split(",");
			if(size.length >2){
				document.getElementById(groupId).disabled = false;
			}
		}

	}else{
		var value = "";
		var count = 0;
		var patientIds = hidField.value.split(",");
		for(var i=0;i<patientIds.length-1;i++){
			if(patientIds[i]!=obj.value){
				value = value+patientIds[i]+",";
				count++;
			}
		}
		hidField.value = value;
		if(value.indexOf(',')==-1){
			document.getElementById(groupId).disabled = true;
			block.removeChild(hidField);
			var patientId = document.getElementsByName("patientId");
			for(var i=0;i<patientId.length;i++){
					patientId[i].disabled = false;
			}
		}
		if(count==1){
			document.getElementById(groupId).disabled = true;
		}
		
	}
}

function mergePatients(obj){
	var check = confirm("Warning! This cannot be undone.Are you sure you want to merge patients?");
	
	if(check){
		var patients = document.getElementById(obj.id+"hidden").value.split(',');
		var querParams = "";
		for(var i=0;i<patients.length-1;i++){
			querParams = querParams+"patientId="+patients[i];
			if((i+1)<patients.length-1){
				querParams = querParams+"&";
			}
		}
		window.location="${productionServerUrl}"+"?"+querParams;
		return true;
	}else{
		return false;
	}
}

function displayPatient(patientDetails){
	
	var patient = patientDetails[0];
	var names = patient.names;
	var patientNames = "";
	for(var i=0;i<names.length;i++){
		patientNames = patientNames+"<li>"+names[i].givenName+" "+names[i].middleName+" "+names[i].familyName;
	}
	document.getElementById("name").innerHTML = patientNames;
	document.getElementById("info0").innerHTML = patient.patientId;
	var gender = patient.gender;
	if(gender.indexOf('f')>=0){
		gender = "<img src=\"${pageContext.request.contextPath}/images/female.gif\" />"
	}else{
		gender = "<img src=\"${pageContext.request.contextPath}/images/male.gif\" />"
	}
	document.getElementById("info1").innerHTML = gender;
	var date = new Date();
	date = patient.birthdate;
	document.getElementById("info2").innerHTML = date.getDate()+"/"+(date.getMonth()+1)+"/"+(date.getYear()+1900);
	if(patient.dead){
		date = patient.deathdate;
		document.getElementById("info3").innerHTML = date.getDate()+"/"+(date.getMonth()+1)+"/"+(date.getYear()+1900);
	}else{
		document.getElementById("info3").innerHTML = "";
	}
	var voided = patient.voided;
	if(voided){
		voided = "Yes";
	}else if(!voided){
		voided = "No";
	}
	document.getElementById("info4").innerHTML = voided;
	var patientIdentifiers = patientDetails[1];
	
	var identifiers = "";
	for(var i=0; i<patientIdentifiers.length; i++){
		identifiers = identifiers+"<li>"+patientIdentifiers[i].identifier;
	}
	document.getElementById("identifier").innerHTML = identifiers;
	var patientAddresses = patientDetails[2];
	
	var addresses = "";
	for(var i=0; i<patientAddresses.length; i++){
		addresses = addresses+"<li>"+patientAddresses[i].address1+" "+patientAddresses[i].address2+" "+patientAddresses[i].cityVillage;
	}
	document.getElementById("address").innerHTML = addresses;
}

function patientInfo(obj){
	var patientId = "";
	if(obj.id == undefined){
		patientId = obj;
	}else{
		patientId = obj.id;
	}
	DWRMatchingConfigUtilities.getPatient(patientId,displayPatient);
}

$(document).ready(function() {

	$('#full').dialog({
			autoOpen: false,
			modal: true,
			title: '<spring:message code="Module.addOrUpgrade" javaScriptEscape="true"/>',
			width: '30%'
			
		});

	$("[id='report-list']").dblclick(function() {

			$("#full").dialog('open');

		});
});

</script>

<h2><spring:message code="patientmatching.report.title"/></h2>
<div id="full">
<div class="box">
	<table>
	<tr><td><h4><spring:message code="Patient.names"/></h4>
				<ol id="name"></ol></td>
	</tr>
	<tr><td><h4><spring:message code="Patient.identifiers"/></h4>
				<ol id="identifier"></ol></td>
	</tr>
	<tr><td><h4><spring:message code="Patient.addresses"/></h4>
				<ol id="address"></ol></td>
	</tr>
	<tr><td><h4><spring:message code="Patient.information"/></h4>
				
		<table>
			<tr>
				<th align="left"><spring:message code="general.id"/></th>
				<td id="info0"></td>
			</tr>
			<tr>
				<th align="left"><spring:message code="Person.gender"/></th>
				<td id="info1"></td>
			</tr>
			<tr>
				<th align="left"><spring:message code="Person.birthdate"/></th>
				<td id="info2"></td>
			</tr>
			
			<tr>
				<th align="left"><spring:message code="Person.deathDate"/></th>
				<td id="info3"></td>
			</tr>
			<tr>
				<th align="left"><spring:message code="general.voided"/></th>
				<td id="info4"></td>
			</tr>
		</table></td>
	</tr>
	</table>
</div>
</div>
<br />
<a href="javascript:window.close()" title="Close This Window">Close This Window</a>
<br />
<br />

<b class="boxHeader"><spring:message code="patientmatching.report.run" /></b>
<div class="box">
        <table cellspacing="2" cellpadding="2" id="report-header">
            <tr style="background-color: #E7E7E7;">
            	<td>
                    <label>Checkbox</label>
                </td>
                <c:forEach items="${reportHeader}" var="headerCell">
                    <td>
                
                        <spring:message code="${headerCell}"/>
                    </td>
                    
                </c:forEach>
            </tr>
            <tbody id="report-list">
            
            <c:set var="group" value="-999" scope="page" />
            <c:set var="mergeButton" value="false" scope="page" />
            <c:set var="groupChange" value="false" scope="page" />
            <c:set var="currentClass" value="oddRow" scope="page" />
            
            <c:forEach items="${report}" var="reportResult" varStatus="entriesIndex">
                <c:choose>
                    <c:when test="${group != reportResult[2]}">
                        <c:set var="group" value="${reportResult[2]}" scope="page" />
                        <c:set var="groupChange" value="true" scope="page" />
                         <c:set var="mergeButton" value="true" scope="page" />
						<tr>
							<td colspan="100%" style="border-bottom: 2px solid black;"></td>
						</tr>
                    </c:when>
                    <c:when test="${group == reportResult[2] && reportResult[0]=='true'}">
                        <c:set var="groupChange" value="false" scope="page" />
                    </c:when>
                </c:choose>
             <c:if test="${reportResult[0]=='true'}">
         				 <c:set var="groupChange" value="true" scope="page" />
			 </c:if>
                <c:if test="${groupChange}">
                    <c:choose>
                        <c:when test="${group % 2 == 0}">
                        	<c:if test="${reportResult[0]=='true'}">
                        	<c:set var="currentClass" value="error" scope="page" />
                        	</c:if>
                        	<c:if test="${reportResult[0]=='false'}">
                        	<c:set var="currentClass" value="evenRow" scope="page" />
                        	</c:if>
                        </c:when>
                        <c:when test="${group % 2 != 0}">
                            <c:if test="${reportResult[0]=='true'}">
                        	<c:set var="currentClass" value="error" scope="page" />
                        	</c:if>
                        	<c:if test="${reportResult[0]=='false'}">
                        	<c:set var="currentClass" value="oddRow" scope="page" />
                        	</c:if>
                        </c:when>
                    </c:choose>
                </c:if>
					
                <tr ondblclick="patientInfo(this);" id="${reportResult[1]}" class="<c:out value="${currentClass}" />">
                
                <c:forEach items="${reportResult}" var="reportResultCell" varStatus="entriesIndexInner">
                    <c:choose>
                    		<c:when  test="${entriesIndexInner.index==1}">
							<td>
                   				<input type="checkbox" id="${reportResult[2]}#${reportResult[1]}" name="patientId" value="${reportResult[1]}" onclick="val(this);"/>
		                    </td>
                   			</c:when>
                   			<c:when  test="${entriesIndexInner.index>=2}">
							<td>
							<c:choose>
								<c:when  test="${entriesIndexInner.index==2}"><b>
									<c:out value="${reportResultCell}" /></b>
								</c:when>
								<c:when  test="${entriesIndexInner.index==3}"><a href="javascript:patientInfo(${reportResultCell});">
									<c:out value="${reportResultCell}" /></a>
								</c:when>
								<c:otherwise>
									<c:out value="${reportResultCell}" />
								</c:otherwise>
							</c:choose>
		                    </td>
							</c:when>
		                </c:choose>
                </c:forEach>
                
                    <c:if test="${mergeButton}">
                        <td>
                            <input type="hidden" value="${productionServerUrl}" id="hidden"/>
                 			<input type="button" id="${reportResult[2]}" value="Merge GroupId ${reportResult[2]}" id="${reportResult[2]}Group" onclick="return mergePatients(this)" name="sub" disabled/>
                 		</td>
                        <c:set var="mergeButton" value="false" scope="page" />
                    </c:if>
                  </tr>
            </c:forEach>
            </tbody>
            
        </table>
</div>

<br/>
<input type="submit" value="Prev" onClick="getPrevPage();"/>
<input type="submit" value="Next" onClick="getNextPage();"/>
<input type="submit" value="Start" onClick="getStartPage();"/>
<input type="submit" value="End" onClick="getEndPage();"/>
<br />

<%@ include file="/WEB-INF/template/footer.jsp" %>
              