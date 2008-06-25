<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patients, View Patient Cohorts" otherwise="/login.htm" redirect="/module/patientmatching/schedule.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<h2><spring:message code="patientmatching.schedule.title"/></h2>

<br/>

<b class="boxHeader"><spring:message code="patientmatching.schedule.new" /></b>
<div class="box">
    <form method="post">
        <table cellspacing="2">
            <tr>
                <th colspan="2"><spring:message code="patientmatching.schedule.parameter"/></th>
            </tr>
            <tr>
                <td><spring:message code="patientmatching.schedule.name" /></td>
                <td>
                    <input type="text" 
                        name="name" size="30" id="name"
                        value="" />
                </td>
            </tr>
            <tr>
                <td><spring:message code="patientmatching.schedule.date" /></td>
                <td>
                    <input type="text" 
                        name="startdate" size="10" id="startdate"
                        value=""
                        onClick="showCalendar(this)" />
                </td>
            </tr>
            <tr>
                <td><spring:message code="patientmatching.schedule.repeatable" /></td>
                <td>
                    <input type="checkbox" name="repeatable" id="repeatable" />
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <input type="submit" value="<spring:message code="general.save" />" />
                </td>
            </tr>
        </table>
    </form>
</div>
<br/><br/>

<b class="boxHeader"><spring:message code="patientmatching.schedule.list" /></b>
<div class="box">
    <table cellspacing="2">
        <tr>
            <th><spring:message code="patientmatching.schedule.name" /></th>
            <th><spring:message code="patientmatching.schedule.date" /></th>
            <th><spring:message code="patientmatching.schedule.repeatable" /></th>
        </tr>
        <tr>
            <td></td>
            <td></td>
            <td></td>
        </tr>
    </table>
</div>
<br/>
<%@ include file="/WEB-INF/template/footer.jsp" %>