<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="View Patients, View Patient Cohorts"
				 otherwise="/login.htm" redirect="/module/patientmatching/config.list" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

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
	
    function changeSM(obj) {
		var c2b1=obj.name;
		document.getElementById(c2b1).style.color="green";
		document.getElementById(c2b1).style.fontWeight="bold";

    }
    
    function changeMM(obj) {
		var c2i1=obj.name;
		document.getElementById(c2i1).style.color="red";
		document.getElementById(c2i1).style.fontWeight="bold";

    }
	
    function ignore(obj) {
		var c2n1=obj.name;
		document.getElementById(c2n1).style.color="black";
		document.getElementById(c2n1).style.fontWeight="normal";
    }
</script>

<form method="post">
	<b class="boxHeader"><spring:message
			code="patientmatching.config.new" /> </b>
	<div class="box">
		<table cellspacing="2" cellpadding="2"
			   style="height: 93px; width: 414px;">
			<tr class="oddRow">
				<td><span style="padding-left: 5px; font-weight: bold;"><spring:message
							code="patientmatching.config.new.name" /> </span>
				</td>
				<td style="vertical-align: middle"><spring:bind
						path="patientMatchingConfiguration.configurationName">
						<input type="text" name="${status.expression}"
							   id="${status.expression}" value="${status.value}" size="40" />
					</spring:bind>
				</td>
			</tr>
		</table>
				
		<!--
		<table cellspacing="2" cellpadding="2">
			<tr>
				<td colspan="2">
				<spring:bind path="patientMatchingConfiguration.usingRandomSample">
					<input type="hidden" name="_<c:out value="${status.expression}"/>">
					<input type="checkbox" name="<c:out value="${status.expression}"/>" value="true"
					<c:if test="${status.value}">checked</c:if>/>
				</spring:bind><spring:message code="patientmatching.config.new.useRandomSampling"/>
				</td>
			</tr>
			<tr>
				<td><spring:message code="patientmatching.config.new.randomSampleSize"/></td>
				<td>
				<spring:bind path="patientMatchingConfiguration.randomSampleSize">
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
				<td valign="top"><b class="boxHeader"><spring:message
							code="patientmatching.config.new.availableField" /> </b>
					<div class="box" style="padding-right: 10px;">
						<table cellspacing="2" cellpadding="2">
							<tr>
								<th><spring:message
										code="patientmatching.config.new.fieldName" /></th>
								<th><spring:message
										code="patientmatching.config.new.fieldNameIgnore" /></th>
								<th><spring:message
										code="patientmatching.config.new.fieldNameInclude" /></th>
								<th><spring:message
										code="patientmatching.config.new.fieldNameBlocking" /></th>
								<th><spring:message
										code="patientmatching.config.new.interchangeable" /></th>
								<th>&nbsp;</th>
								<th><spring:message
										code="patientmatching.config.new.fieldName" /></th>
								<th><spring:message
										code="patientmatching.config.new.fieldNameIgnore" /></th>
								<th><spring:message
										code="patientmatching.config.new.fieldNameInclude" /></th>
								<th><spring:message
										code="patientmatching.config.new.fieldNameBlocking" /></th>
								<th><spring:message
										code="patientmatching.config.new.interchangeable" /></th>
							</tr>
							<c:forEach
								items="${patientMatchingConfiguration.configurationEntries}"
								var="configEntry" varStatus="entriesIndex">		
								<c:if test="${(entriesIndex.count - 1) % 2 == 0}">
									<tr
										<c:if test="${entriesIndex.count % 2 == 0}">class="oddRow"</c:if>
										<c:if test="${entriesIndex.count % 2 != 0}">class="evenRow"</c:if>>
									</c:if>				
									<spring:bind path="patientMatchingConfiguration.configurationEntries[${entriesIndex.count-1}].inclusion">
										<td nowrap="nowrap"
											name="<c:out value="${status.expression}"/>" id="fieldName"
											value="<spring:message code="${configEntry.fieldViewName}"/>">
											<font
												<c:if test='${status.value == "INCLUDED"}'>face="arial black" color="green"</c:if>
												<c:if test='${status.value == "BLOCKING"}'>face="arial black" color="red"</c:if>
												<c:if test='${status.value == "IGNORED"}'>color="black"</c:if>>
											<c:if
												test='${status.value == "INCLUDED" ||status.value == "BLOCKING"}'>
												<b>
												</c:if>
												<div id="<c:out value="${status.expression}"/>">
													<spring:message code="${configEntry.fieldViewName}" />
												</div> </b> </font>
										</td>
										<td align="center"><input type="radio"
																  name="<c:out value="${status.expression}"/>" value="IGNORED"
																  onclick="ignore(this)"
																  <c:if test='${status.value == "IGNORED"}'>checked</c:if> />
											</td>
											<td align="center"><input type="radio"
																	  name="<c:out value="${status.expression}"/>"
												value="INCLUDED" onclick="changeSM(this)"
												<c:if test='${status.value == "INCLUDED"}' >checked</c:if> />
											</td>
											<td align="center"><input type="radio"
																	  name="<c:out value="${status.expression}"/>"
												value="BLOCKING" onclick="changeMM(this)"
												<c:if test='${status.value == "BLOCKING"}'>checked</c:if> />
											</td>
									</spring:bind>
									<spring:bind
										path="patientMatchingConfiguration.configurationEntries[${entriesIndex.count - 1}].flag">
										<td align="center">
											<select name="interchangeable"
													id="${configEntry.fieldViewName}"
													onchange="document.getElementById('${configEntry.fieldViewName}h').value=document.getElementById('${configEntry.fieldViewName}').value;
												document.getElementById('${configEntry.fieldViewName}h').checked='true';">
												<option value="0" <c:if test='${status.value == "0"}'>selected</c:if>>None</option>
												<option value="1" <c:if test='${status.value == "1"}'>selected</c:if>>1</option>
													<br />
												</select></td>
										<div id="divh" >
											<input type="hidden" id="${configEntry.fieldViewName}h"
											   name="<c:out value="${status.expression}"/>" value="<c:out value="${status.value}"/>"/>
									</div>
								</spring:bind>

								<c:if test="${(entriesIndex.count - 1) % 2 < 1}">
									<td>&nbsp;</td>
								</c:if>
								<c:if test="${(entriesIndex.count - 1) % 2 == 1}">
									</tr>
								</c:if>
							</c:forEach>
						</table>
					</div>
				</td>
			</tr>
			<tr>

			</tr>
		</table>
		<td valign="top" style="height: 150px; width: 403px;"><b
				class="boxHeader"><spring:message
					code="patientmatching.config.new.inclusionLevels" /> </b>
			<div class="box" style="padding-right: 10px;">
				<ul>
					<li><b><spring:message
								code="patientmatching.config.new.fieldNameIgnore" /> </b>: <spring:message
								code="patientmatching.config.new.fieldNameIgnore.description" />
					</li>
					<li><b><spring:message
								code="patientmatching.config.new.fieldNameInclude" /> </b>: <spring:message
								code="patientmatching.config.new.fieldNameInclude.description" />
					</li>
					<li><b><spring:message
								code="patientmatching.config.new.fieldNameBlocking" /> </b>: <spring:message
								code="patientmatching.config.new.fieldNameBlocking.description" />
					</li>
				</ul>
			</div>
		</td>
	</div>
	<br /> <input type="submit"
				  value="<spring:message code="general.save" />" />
</form>
<%@ include file="/WEB-INF/template/footer.jsp"%>
