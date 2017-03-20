package org.openmrs.module.patientmatching.web;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmrs.api.context.Context;
import org.openmrs.module.patientmatching.MatchingConfigurationUtils;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.web.WebConstants;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class CreateScheduleFormController extends SimpleFormController {
	private static final String extention = "patientMatching";
	private static final String startTimePattern = "MM/dd/yyyy HH:mm:ss";
	private static SimpleDateFormat sdf = new SimpleDateFormat(startTimePattern);
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		
		HttpSession httpSession = request.getSession();
		
		SchedulerService ss = Context.getSchedulerService();
		boolean scheduleRunning = false;
		TaskDefinition task = new TaskDefinition();
		String taskId = request.getParameter("taskId");
		String[] blockList = request.getParameterValues("blockList");
		String strategies = "";
		for(String strategy:blockList){
			strategies = strategies+strategy+",";
		}
		String taskName = extention+"_"+request.getParameter("name").trim();
		if(!taskId.equals("")){
			if(ss.getTask(Integer.valueOf(taskId)) != null){
				task = ss.getTask(Integer.valueOf(taskId));
				if(task.getStarted()){
					ss.shutdownTask(task);
					scheduleRunning = true;
				}
			}
		}
		task.setName(taskName);
		Map<String,String> properties = new HashMap<String, String>(0);
		properties.put("blockList", strategies);
		task.setProperties(properties);
		task.setStartTimePattern(startTimePattern);
		Date startTime = sdf.parse(request.getParameter("startTime").trim());
		task.setStartTime(startTime);
		
		String repeatIntervalUnits = request.getParameter("repeatIntervalUnits");
		int repeatInterval = Integer.parseInt(request.getParameter("repeatInterval").trim());
		long interval = 0;
		if(repeatIntervalUnits.equals("days")){
			interval = repeatInterval*24*3600;
		}else if(repeatIntervalUnits.equals("weeks")){
			interval = repeatInterval*7*24*3600;
		}
		task.setRepeatInterval(interval);
		task.setStartOnStartup(false);
		task.setStarted(false);
		task.setDescription(request.getParameter("description").trim()+"");
		task.setTaskClass("org.openmrs.module.patientmatching.ScheduledReportGeneration");
		
		try {
			ss.saveTask(task);
		}
		catch (NoSuchMethodError ex) {
			//platform 2.0 renamed saveTask to saveTaskDefinition
			Method method = Context.getSchedulerService().getClass().getMethod("saveTaskDefinition",
			    new Class[] { TaskDefinition.class });
			method.invoke(Context.getSchedulerService(), task);
		}
		
		if(scheduleRunning)
			ss.scheduleTask(task);
		
		String success = request.getParameter("name")+" Task is saved";
		httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success);
		return new ModelAndView(new RedirectView(getSuccessView()));
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request)
			throws Exception {
		String text = "text";
		return text;
	}

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception {
		String taskId = request.getParameter("taskId");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("startTimePattern", startTimePattern);
		if(taskId != null){
			TaskDefinition task = Context.getSchedulerService().getTask(Integer.valueOf(taskId));
			map.put("taskId", taskId);
			int index = task.getName().indexOf("_");
			map.put("name", task.getName().substring(index+1));
			map.put("description", task.getDescription());
			map.put("blockList", task.getProperty("blockList"));
			map.put("startTime", sdf.format(task.getStartTime()));
			long intervalTime = task.getRepeatInterval();
			if((intervalTime%(7*24*3600))==0){
				map.put("repeatInterval", (intervalTime/(7*24*3600))+"");
				map.put("repeatIntervalUnits", "weeks");
			}else{
				map.put("repeatInterval", (intervalTime/(24*3600))+"");
				map.put("repeatIntervalUnits", "days");
			}
		}else{
			map.put("startTime", sdf.format(new Date()));
		}
		
		map.put("blockingRuns", MatchingConfigurationUtils.listAvailableBlockingRuns_db());
		
		return map;
	}

}
