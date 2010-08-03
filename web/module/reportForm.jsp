<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patients, View Patient Cohorts" otherwise="/login.htm" redirect="/module/patientmatching/config.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<openmrs:htmlInclude file="/dwr/interface/DWRMatchingConfigUtilities.js"/>
<openmrs:htmlInclude file="/dwr/engine.js"/>
<openmrs:htmlInclude file="/dwr/util.js"/>

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
            var tr = document.createElement("tr");
            
            if (currentGroup != row[2]) {
                groupChange = true;
    			mergeButton = true;
                currentGroup = row[2];
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
            tr.className = currentClass;
            main.appendChild(tr);
    		if(row[0]=="true"){
    			currentClass = preClass;
    		}
            for (j = 0; j < row.length; j ++) {
               if(j==0){
    			    var td = document.createElement("td");
    				td.innerHTML = "<input type=\"checkbox\" id=\""+row[2]+"#"+row[1]+"\" name=\"patientId\" value=\""+row[1]+"\" onclick=\"val(this);\"/>";
    				tr.appendChild(td);
    			}else if(j>=2){
    				var td = document.createElement("td");
    		 	    td.innerHTML = row[j];
    				tr.appendChild(td);
    			}
                
            }
    		if(mergeButton){
    			var td = document.createElement("td");
    				td.innerHTML = "<input type=\"button\" id=\""+row[2]+"\" value=\"Merge GroupId "+row[2]+"\" id=\""+row[2]+"Group\" onclick=\"return mergePatients(this)\" name=\"sub\" disabled/>";
    				tr.appendChild(td);
    				mergeButton = false;
    		}
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
		}
		if(count==1){
			document.getElementById(groupId).disabled = true;
		}
		
	}
}

function mergePatients(obj){
	var check = confirm("Are you sure you want to merge patients?");
	if(check){
		var patients = document.getElementById(obj.id+"hidden").value.split(',');
		var querParams = "";
		for(var i=0;i<patients.length-1;i++){
			querParams = querParams+"patientId="+patients[i];
			if((i+1)<patients.length-1){
				querParams = querParams+"&";
			}
		}
		window.location="/openmrs/admin/patients/mergePatients.form?"+querParams;
		return true;
	}else{
		return false;
	}
}

</script>

<h2><spring:message code="patientmatching.report.title"/></h2>

<br />
<a href="javascript:window.close()" title="Close This Window">Close This Window</a>
<br />
<br />

<b class="boxHeader"><spring:message code="patientmatching.report.run" /></b>
<div class="box">
        <table cellspacing="2" cellpadding="2" id="report-header">
            <tr>
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
					
                <tr class="<c:out value="${currentClass}" />">
                
                <c:forEach items="${reportResult}" var="reportResultCell" varStatus="entriesIndexInner">
                    <c:choose>
                    		<c:when  test="${entriesIndexInner.index==1}">
							<td>
                   				<input type="checkbox" id="${reportResult[2]}#${reportResult[1]}" name="patientId" value="${reportResult[1]}" onclick="val(this);"/>
		                    </td>
                   			</c:when>
                   			<c:when  test="${entriesIndexInner.index>=2}">
							<td>
		                        <c:out value="${reportResultCell}" />
		                    </td>
							</c:when>
		                </c:choose>
                </c:forEach>
                
                    <c:if test="${mergeButton}">
                        <td>
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
              