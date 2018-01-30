package com.github.ji4597056.flow;

import java.util.List;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.api.Task;

/**
 * @author Jeffrey
 * @since 2018/01/25 13:03
 */
public class FinancialReportTest extends BaseFlowableTest {

    private static final String DEPLOYMENT_RESOURCE = "processes/FinancialReportProcess.bpmn20.xml";

    public static void main(String[] args) {
        // deploy
        repositoryService.createDeployment().addClasspathResource(DEPLOYMENT_RESOURCE).deploy();

        // Start a process instance
        String procId = runtimeService.startProcessInstanceByKey("financialReport").getId();

        // Get the first task
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("accountancy").list();
        for (Task task : tasks) {
            System.err
                .println("Following task is available for accountancy group: " + task.getName());

            // claim it
            taskService.claim(task.getId(), "fozzie");
        }

        // Verify Fozzie can now retrieve the task
        tasks = taskService.createTaskQuery().taskAssignee("fozzie").list();
        for (Task task : tasks) {
            System.err.println("Task for fozzie: " + task.getName());

            // Complete the task
            taskService.complete(task.getId());
        }

        System.err.println("Number of tasks for fozzie: "
            + taskService.createTaskQuery().taskAssignee("fozzie").count());

        // Retrieve and claim the second task
        tasks = taskService.createTaskQuery().taskCandidateGroup("management").list();
        for (Task task : tasks) {
            System.err
                .println("Following task is available for management group: " + task.getName());
            taskService.claim(task.getId(), "kermit");
        }

        // Completing the second task ends the process
        for (Task task : tasks) {
            taskService.complete(task.getId());
        }

        // verify that the process is actually finished
        HistoricProcessInstance historicProcessInstance =
            historyService.createHistoricProcessInstanceQuery().processInstanceId(procId)
                .singleResult();
        System.err.println("Process instance end time: " + historicProcessInstance.getEndTime());
    }
}
