package com.github.ji4597056.flow;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Jeffrey
 * @since 2018/03/21 20:19
 */
public class VariableFlowTest extends BaseFlowableTest {

    private static final String DEPLOYMENT_RESOURCE = "processes/VariableFlow.bpmn20.xml";

    private static final String PROCESS_KEY = "variableFlow";

    private static final LinkedList<String> GROUP_LIST = new LinkedList<>();

    private static final String GROUP_A = "groupA";
    private static final String GROUP_B = "groupB";
    private static final String GROUP_C = "groupC";

    // 对应xml中的candidateGroups
    private static final String GROUP_KEY = "group";

    @Before
    public void initialize() {
        // groupA --> groupB --> groupC --> null
        GROUP_LIST.offer(GROUP_A);
        GROUP_LIST.offer(GROUP_B);
        GROUP_LIST.offer(GROUP_C);
    }

    @Test
    public void deploy() {
        repositoryService.createDeployment().addClasspathResource(DEPLOYMENT_RESOURCE).deploy();
        Assert.assertTrue(
            repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS_KEY)
                .list().size() > 0);
    }

    @Test
    public void clean() {
        runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_KEY).list()
            .forEach(processInstance -> runtimeService
                .deleteProcessInstance(processInstance.getId(), null));
    }

    @Test
    public void test() {
        Map<String, Object> variables = new HashMap<>();
        variables.put(GROUP_KEY, GROUP_LIST.poll()); // groupA
        // 1.启动流程
        ProcessInstance processInstance = runtimeService
            .startProcessInstanceByKey(PROCESS_KEY, variables);
        // 2.groupA设置next group为groupB,完成任务
        doTask(GROUP_A);
        // 3.groupB设置next group为groupC,完成任务
        doTask(GROUP_B);
        // 4.groupC设置next group为null,完成任务
        doTask(GROUP_C);
        // 5.任务结束
        Assert.assertNull(
            runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId())
                .singleResult());
        // 6.查询历史
        Assert.assertEquals(historyService.createHistoricTaskInstanceQuery()
            .processInstanceId(processInstance.getId()).list().size(), 3);
        Assert.assertEquals(historyService.createHistoricVariableInstanceQuery()
            .processInstanceId(processInstance.getId()).list().size(), 1);
    }

    private void doTask(String group) {
        Task task = taskService.createTaskQuery().taskCandidateGroup(group).singleResult();
        Assert.assertNotNull(task);
        Map<String, Object> variables = new HashMap<>();
        // 设置next group
        variables.put(GROUP_KEY, GROUP_LIST.poll());
        taskService.complete(task.getId(), variables);
    }
}
