package org.openmrs.module.patientmatching.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.web.WebConstants;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class ScheduleSimpleFormController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	private static final String extention = "patientMatching";
	
	public static String DEFAULT_DATE_PATTERN = "MM/dd/yyyy HH:mm:ss";
	
	@Override
	protected String formBackingObject(HttpServletRequest request) throws Exception {
		
		//String text = Context.getService(HelloWorldService.class);
		
		String text = "Not used";
		
		log.debug("Returning text: " + text);
		
		return text;
		
	}
	
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command,
	        BindException errors) throws Exception {
		
		String view = getFormView();
		HttpSession httpSession = request.getSession();
		
		StringBuffer success = new StringBuffer();
		StringBuffer error = new StringBuffer();
		String action = request.getParameter("action");
		
		String[] taskList = request.getParameterValues("taskId");
		
		SchedulerService schedulerService = Context.getSchedulerService();
		
		if (taskList != null) {
			for (String taskId : taskList) {
				// Argument to pass to the success/error message
				TaskDefinition task = schedulerService.getTask(Integer.valueOf(taskId));
				// If we can get the name, let's use it
				int index = task.getName().indexOf("_");
				String name = task.getName().substring(index + 1);
				try {
					
					if (action.equals("Delete")) {
						schedulerService.deleteTask(Integer.valueOf(taskId));
						success.append("Deleted " + name + "\n");
					} else if (action.equals("Stop")) {
						schedulerService.shutdownTask(task);
						success.append("Stopped " + name + "\n");
					} else if (action.equals("Start")) {
						schedulerService.scheduleTask(task);
						success.append("Started " + name + "\n");
					}
				}
				catch (APIException e) {
					log.warn("Error processing schedulerlistcontroller task", e);
					error.append("error " + name + "\n");
				}
			}
		}
		view = getSuccessView();
		
		if (!success.toString().equals("")) {
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success.toString());
		}
		if (!error.toString().equals("")) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error.toString());
		}
		
		return new ModelAndView(new RedirectView(view));
	}
	
	@Override
	protected Map<String, Object> referenceData(HttpServletRequest req) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		List<Map<String, Object>> schedules = new ArrayList<Map<String, Object>>();
		Map<TaskDefinition, String> intervals = new HashMap<TaskDefinition, String>();
		
		Collection<TaskDefinition> rTasks = Context.getSchedulerService().getRegisteredTasks();
		List<TaskDefinition> tasks = new ArrayList<TaskDefinition>();
		for (TaskDefinition td : rTasks) {
			if (td.getName().contains(extention)) {
				tasks.add(td);
			}
		}
		
		for (TaskDefinition task : tasks) {
			Map<String, Object> schedule = new HashMap<String, Object>();
			int index = task.getName().indexOf("_");
			schedule.put("name", task.getName().substring(index + 1));
			schedule.put("task", task);
			schedules.add(schedule);
			
			Long intervalTime = task.getRepeatInterval();
			
			if ((intervalTime % (7 * 24 * 3600)) == 0) {
				intervals.put(task, (intervalTime / (7 * 24 * 3600)) + " weeks");
			} else {
				intervals.put(task, (intervalTime / (24 * 3600)) + " days");
			}
		}
		
		map.put("intervals", intervals);
		map.put("allTasks", schedules);
		
		return map;
		
	}
	
}
