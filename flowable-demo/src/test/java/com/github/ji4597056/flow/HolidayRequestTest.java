package com.github.ji4597056.flow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;

/**
 * test holiday-request flow
 *
 * @author Jeffrey
 * @since 2018/01/22 13:31
 */
public class HolidayRequestTest extends BaseFlowableTest {

    private static final String DEPLOYMENT_RESOURCE = "processes/holiday-request.bpmn20.xml";

    public static void main(String[] args) {
        // get deployment
        Deployment deployment = repositoryService.createDeployment()
            .addClasspathResource(DEPLOYMENT_RESOURCE)
            .deploy();

        // get processDefinition
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
            .deploymentId(deployment.getId())
            .singleResult();
        System.err.println("Found process definition : " + processDefinition.getName());

        Scanner scanner = new Scanner(System.in);
        System.err.println("Who are you?");
        String employee = scanner.nextLine();
        System.err.println("How many holidays do you want to request?");
        Integer nrOfHolidays = Integer.valueOf(scanner.nextLine());
        System.err.println("Why do you need them?");
        String description = scanner.nextLine();

        Map<String, Object> variables = new HashMap<>();
        variables.put("employee", employee);
        variables.put("nrOfHolidays", nrOfHolidays);
        variables.put("description", description);

        // (startEvent)
        ProcessInstance processInstance =
            runtimeService.startProcessInstanceByKey(processDefinition.getKey(), variables);

        // (approveTask)
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("managers").list();
        System.err.println("You have " + tasks.size() + " tasks:");
        for (int i = 0; i < tasks.size(); i++) {
            System.err.println((i + 1) + ") " + tasks.get(i).getName());
        }

        System.err.println("Which task would you like to complete?");
        int taskIndex = Integer.valueOf(scanner.nextLine());
        Task task = tasks.get(taskIndex - 1);
        Map<String, Object> processVariables = taskService.getVariables(task.getId());
        System.err.println(processVariables.get("employee") + " wants " +
            processVariables.get("nrOfHolidays") + " of holidays. Do you approve this?");

        boolean approved = scanner.nextLine().toLowerCase().equals("y");
        variables = new HashMap<>();
        variables.put("approved", approved);
        taskService.complete(task.getId(), variables);

        // get HistoricActivityInstance
        List<HistoricActivityInstance> activities =
            historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstance.getId())
                .finished()
                .orderByHistoricActivityInstanceEndTime().asc()
                .list();
        for (HistoricActivityInstance activity : activities) {
            System.err.println(activity.getActivityId() + " took "
                + activity.getDurationInMillis() + " milliseconds");
        }
    }
}
