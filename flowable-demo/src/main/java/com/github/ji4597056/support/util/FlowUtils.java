package com.github.ji4597056.support.util;

import com.github.ji4597056.common.User;
import com.github.ji4597056.support.constant.VariableConstant;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.task.api.Task;

/**
 * @author Jeffrey
 * @since 2018/03/02 13:15
 */
public class FlowUtils {

    private FlowUtils() {
        throw new AssertionError("No FlowUtils instances for you!");
    }

    // apply permission process util

    /**
     * 获取申请人
     *
     * @param execution DelegateExecution
     * @return User
     */
    public static User getProposer(DelegateExecution execution) {
        String proposerId = (String) execution.getVariable(VariableConstant.PROPOSER_ID);
        return User.getUser(proposerId);
    }

    /**
     * 设置评论
     *
     * @param taskService TaskService
     * @param task Task
     * @param type comment type
     * @param message comment message
     */
    public static void setComment(TaskService taskService, Task task, String type, String message) {
        taskService.addComment(task.getId(), task.getProcessInstanceId(), type, message);
    }
}
