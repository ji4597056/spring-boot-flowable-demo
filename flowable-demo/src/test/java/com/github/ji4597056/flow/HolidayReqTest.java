package com.github.ji4597056.flow;

import com.github.ji4597056.support.util.FlowUtils;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.flowable.engine.common.impl.interceptor.CommandContext;
import org.flowable.engine.impl.persistence.entity.CommentEntityImpl;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Jeffrey
 * @since 2018/04/04 12:30
 */
public class HolidayReqTest extends BaseFlowableTest {

    private static final String DEPLOYMENT_RESOURCE = "processes/holiday-request.bpmn20.xml";

    private static final String PROCESS_KEY = "holidayRequest";

    private static final String CANDIDATE_GROUP = "managers";

    private static final String GROUP_USER_A = "Miss Li";

    private static final String ASSIGNEE = "jack";

    @Test
    public void deploy() {
        deploy(DEPLOYMENT_RESOURCE, PROCESS_KEY);
    }

    @Test
    public void clean() {
        clean(PROCESS_KEY);
    }

    @Test
    public void testComment() {
        ProcessInstance processInstance = runtimeService
            .startProcessInstanceByKey(PROCESS_KEY, getStartVariables());
        Task task = taskService.createTaskQuery().taskCandidateGroup(CANDIDATE_GROUP)
            .singleResult();
        // 先claim再complete,测试评论的userId
        taskService.claim(task.getId(), GROUP_USER_A);
        // 添加第一条评论
        FlowUtils.setComment(taskService, task, null, "第一条评论!");
        // 添加第二条评论
        FlowUtils.setComment(taskService, task, null, "第二条评论!");
        taskService.complete(task.getId(), getApprovedVariables(Boolean.TRUE));
        try {
            // 添加第三条评论
            FlowUtils.setComment(taskService, task, null, "第三条评论!");
        } catch (Exception e) {
            Assert.assertNotNull(e);
            System.out.println("==============error============");
            System.out.println(e.getMessage());
            System.out.println("==============error============");
        }
        // 查询评论
        Assert.assertEquals(taskService.getProcessInstanceComments(processInstance.getId()).size(),
            2);
        // 添加第四条评论
        addCommentByCommentDataManager(task, "第四条评论!");
        // 查询评论
        Assert.assertEquals(taskService.getProcessInstanceComments(processInstance.getId()).size(),
            3);
        Task task2 = taskService.createTaskQuery().taskAssignee(ASSIGNEE).singleResult();
        taskService.complete(task2.getId());
        checkProcessInstanceEnd(processInstance);
    }

    private Map<String, Object> getStartVariables() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("employee", ASSIGNEE);
        variables.put("nrOfHolidays", 1);
        variables.put("description", "I don't want to work!");
        return variables;
    }

    private Map<String, Object> getApprovedVariables(boolean approved) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", approved);
        return variables;
    }

    private void addCommentByCommentDataManager(Task task, String message) {
        managementService.executeCommand(commandContext -> {
            CommentEntityImpl commentEntity = new CommentEntityImpl();
            commentEntity.setMessage(message);
            commentEntity.setTaskId(task.getId());
            commentEntity.setProcessInstanceId(task.getProcessInstanceId());
            commentEntity.setTime(new Date());
            commentEntityManager.insert(commentEntity);
            return commentEntity;
        });
    }
}
