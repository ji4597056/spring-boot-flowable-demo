package com.github.ji4597056.flow;

import com.github.ji4597056.common.User;
import com.github.ji4597056.common.UserGroup;
import com.github.ji4597056.support.constant.VariableConstant;
import com.github.ji4597056.support.enums.CommentEnum;
import com.github.ji4597056.support.enums.OperationEnum;
import com.github.ji4597056.support.enums.PermissionEnum;
import com.github.ji4597056.support.util.FlowUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.flowable.engine.delegate.BpmnError;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.StringUtils;

/**
 * @author Jeffrey
 * @since 2018/3/1 10:44
 */
public class ApplyPermissionTest extends BaseFlowableTest {

    private static final String DEPLOYMENT_RESOURCE = "processes/ApplyPermission.bpmn20.xml";

    private static final String PROCESS_KEY = "applyPermission";

    private static final Random RANDOM = new Random();

    private static final String GROUP_A = "groupA";

    private static final String GROUP_B = "groupB";

    @BeforeClass
    public static void initialize() {
        // initialize user
        User.createUser("1", "张一");
        User.createUser("2", "张二");
        User.createUser("3", "张三");
        User.createUser("4", "张四");
        User.createUser("5", "张五");
        User.createUser("6", "张六");

        // initialize group (groupId必须与流程中设置的id对应)
        UserGroup.createGroup(GROUP_A);
        UserGroup.createGroup(GROUP_B);
        UserGroup.addUsers(GROUP_A,
            Stream.of(User.getUser("3"), User.getUser("4")).collect(Collectors.toSet()));
        UserGroup.addUsers(GROUP_B,
            Stream.of(User.getUser("5"), User.getUser("6")).collect(Collectors.toSet()));
    }

    /**
     * 测试部署成功(第一次启动或修改流程xml需要重新部署)
     */
    @Test
    public void testDeploy() {
        repositoryService.createDeployment().addClasspathResource(DEPLOYMENT_RESOURCE).deploy();
        Assert.assertTrue(
            repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS_KEY)
                .list().size() > 0);
    }

    /**
     * 测试正常运行流程(提交->A审核通过->B审核通过)
     */
    @Test
    public void testSuccessProcess() {
        String proposerId = "1";
        // 设置申请人id,申请合法权限
        Map<String, Object> variables = new HashMap<>();
        variables.put(VariableConstant.PROPOSER_ID, proposerId);
        variables.put(VariableConstant.PERMISSION, PermissionEnum.LEGAL_PERMISSION.getName());
        // 1.启动流程
        ProcessInstance processInstance = runtimeService
            .startProcessInstanceByKey(PROCESS_KEY, variables);
        // 2.申请人提交更新权限
        Task task = taskService.createTaskQuery().taskAssignee(proposerId).singleResult();
        Assert.assertNotNull(task);
        variables = new HashMap<>();
        variables.put(VariableConstant.OPERATION, OperationEnum.SUBMIT.getType());
        FlowUtils.setComment(taskService, task, CommentEnum.OPERATION.getType(),
            OperationEnum.SUBMIT.getType());
        taskService.complete(task.getId(), variables);
        // 3.审批组A审批通过
        task = taskService.createTaskQuery().taskCandidateGroup(GROUP_A).singleResult();
        Assert.assertNotNull(task);
        User user = getRandomUser(GROUP_A);
        // 该组下随机用户接收任务
        if (StringUtils.isEmpty(task.getAssignee())) {
            taskService.claim(task.getId(), user.getId());
        }
        variables = new HashMap<>();
        variables.put(VariableConstant.OPERATION, OperationEnum.AUDIT_SUCCESS.getType());
        // 添加评论和完成任务(先评论和完成)必须为一个事务,要么同时成功,要么同时失败
        // 添加评论(操作类型,备注信息),评论可以给各个task绑定相同参数
        FlowUtils.setComment(taskService, task, CommentEnum.OPERATION.getType(),
            OperationEnum.AUDIT_SUCCESS.getType());
        FlowUtils.setComment(taskService, task, CommentEnum.REMARK.getType(),
            user.getName() + ":" + OperationEnum.AUDIT_SUCCESS.getType());
        // 完成任务
        taskService.complete(task.getId(), variables);
        // 4.审批组B审批通过
        task = taskService.createTaskQuery().taskCandidateGroup(GROUP_B).singleResult();
        Assert.assertNotNull(task);
        user = getRandomUser(GROUP_B);
        if (StringUtils.isEmpty(task.getAssignee())) {
            taskService.claim(task.getId(), user.getId());
        }
        variables = new HashMap<>();
        variables.put(VariableConstant.OPERATION, OperationEnum.AUDIT_SUCCESS.getType());
        FlowUtils.setComment(taskService, task, CommentEnum.OPERATION.getType(),
            OperationEnum.AUDIT_SUCCESS.getType());
        FlowUtils.setComment(taskService, task, CommentEnum.REMARK.getType(),
            user.getName() + ":" + OperationEnum.AUDIT_SUCCESS.getType());
        taskService.complete(task.getId(), variables);
        // 5.自动执行赋权系统任务,流程实例结束
        Assert.assertNull(
            runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId())
                .singleResult());
        // 6.查询流程历史
        // 查询申请人参与的已结束流程实例
        List<HistoricProcessInstance> historicProcessInstances = historyService
            .createHistoricProcessInstanceQuery().finished().involvedUser(proposerId)
            .orderByProcessInstanceStartTime().asc().list();
        Assert.assertTrue(!historicProcessInstances.isEmpty());
        // 查询流程实例历史评论
        historicProcessInstances.forEach(historicProcessInstance -> {
            List<Comment> comments = taskService
                .getProcessInstanceComments(historicProcessInstance.getId());
            Assert.assertTrue(!comments.isEmpty());
        });
        // 查询流程实例历史任务
        historicProcessInstances.forEach(historicProcessInstance -> {
            List<HistoricTaskInstance> historicTaskInstances = historyService
                .createHistoricTaskInstanceQuery()
                .processInstanceId(historicProcessInstance.getId()).list();
            Assert.assertTrue(!historicTaskInstances.isEmpty());
        });
    }

    /**
     * 测试审核失败流程(提交->A审核成功->B审核失败->修改->提交->A审核成功->B审核成功)
     */
    @Test
    public void testAuditFailureProcess() {
        String proposerId = "1";
        Map<String, Object> variables = new HashMap<>();
        variables.put(VariableConstant.PROPOSER_ID, proposerId);
        variables.put(VariableConstant.PERMISSION, PermissionEnum.LEGAL_PERMISSION.getName());
        // 1.启动流程
        ProcessInstance processInstance = runtimeService
            .startProcessInstanceByKey(PROCESS_KEY, variables);
        // 2.申请人提交
        doProposerTask(proposerId, OperationEnum.SUBMIT, null);
        // 3.A审核成功
        doAuditorTask(GROUP_A, OperationEnum.AUDIT_SUCCESS, null);
        // 4.B审核失败
        doAuditorTask(GROUP_B, OperationEnum.AUDIT_FAILURE, null);
        // 5.申请人修改
        variables = new HashMap<>();
        variables.put(VariableConstant.PERMISSION, PermissionEnum.ADD_APP_PERMISSION.getName());
        doProposerTask(proposerId, OperationEnum.UPDATE, variables);
        // 6.申请人提交
        doProposerTask(proposerId, OperationEnum.SUBMIT, null);
        // 7.A审核成功
        doAuditorTask(GROUP_A, OperationEnum.AUDIT_SUCCESS, null);
        // 8.B审核成功
        doAuditorTask(GROUP_B, OperationEnum.AUDIT_SUCCESS, null);
        // 9.流程结束
        Assert.assertNull(
            runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId())
                .singleResult());
        // 该流程实例历史任务 == 7(user task)
        Assert.assertEquals(historyService.createHistoricTaskInstanceQuery()
            .processInstanceId(processInstance.getId())
            .list().size(), 7);
        // 该流程实例评论个数 == 11(申请人任务comment==1, 审批组任务comment==2)
        Assert.assertEquals(taskService.getProcessInstanceComments(processInstance.getId()).size(),
            11);
    }

    /**
     * 测试异常流程(申请非法权限)
     */
    @Test
    public void testErrorProcess() {
        String proposerId = "1";
        Map<String, Object> variables = new HashMap<>();
        variables.put(VariableConstant.PROPOSER_ID, proposerId);
        // 申请非法权限
        variables.put(VariableConstant.PERMISSION, PermissionEnum.ILLEGAL_PERMISSION.getName());
        // 1.启动流程
        ProcessInstance processInstance = runtimeService
            .startProcessInstanceByKey(PROCESS_KEY, variables);
        // 2.申请人提交
        doProposerTask(proposerId, OperationEnum.SUBMIT, null);
        // 3.A审核成功
        doAuditorTask(GROUP_A, OperationEnum.AUDIT_SUCCESS, null);
        // 4.B审核成功
        // TODO 系统任务的执行应该异步执行,不应该同步执行,这里未验证
        try {
            doAuditorTask(GROUP_B, OperationEnum.AUDIT_SUCCESS, null);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof BpmnError);
        }
        // 5.流程结束(查看控制台日志可以看到LogErrorDelegate.class打印的日志,表示已进入异常处理的service task)
        Assert.assertNull(
            runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId())
                .singleResult());
    }

    /**
     * 申请人处理任务
     *
     * @param proposerId proposer id
     * @param operationEnum operation
     * @param newVariables variables
     */
    private void doProposerTask(String proposerId, OperationEnum operationEnum,
        Map<String, Object> newVariables) {
        Task task = taskService.createTaskQuery().taskAssignee(proposerId).singleResult();
        Assert.assertNotNull(task);
        Map<String, Object> variables = new HashMap<>();
        variables.put(VariableConstant.OPERATION, operationEnum.getType());
        Optional.ofNullable(newVariables).ifPresent(map -> variables.putAll(newVariables));
        // operation comment
        FlowUtils.setComment(taskService, task, CommentEnum.OPERATION.getType(),
            operationEnum.getType());
        taskService.complete(task.getId(), variables);
    }

    /**
     * 审批组下随机用户处理任务
     *
     * @param groupId group id
     * @param operationEnum operation
     * @param newVariables variables
     */
    private void doAuditorTask(String groupId, OperationEnum operationEnum,
        Map<String, Object> newVariables) {
        Task task = taskService.createTaskQuery().taskCandidateGroup(groupId).singleResult();
        Assert.assertNotNull(task);
        User user = getRandomUser(groupId);
        // 该组下随机用户接收任务
        if (StringUtils.isEmpty(task.getAssignee())) {
            taskService.claim(task.getId(), user.getId());
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put(VariableConstant.OPERATION, operationEnum.getType());
        Optional.ofNullable(newVariables).ifPresent(map -> variables.putAll(newVariables));
        // 添加评论和完成任务(先评论和完成)必须为一个事务,要么同时成功,要么同时失败
        // 添加评论(操作类型,备注信息),评论可以给各个task绑定相同参数
        // operation comment
        FlowUtils.setComment(taskService, task, CommentEnum.OPERATION.getType(),
            operationEnum.getType());
        // remark comment
        FlowUtils.setComment(taskService, task, CommentEnum.REMARK.getType(),
            user.getName() + ":" + operationEnum.getType());
        // 完成任务
        taskService.complete(task.getId(), variables);
    }

    /**
     * 获取应用组下随机用户
     *
     * @param groupId groupId
     * @return User
     */
    private User getRandomUser(String groupId) {
        Set<User> users = UserGroup.query(groupId);
        return users.stream().collect(Collectors.toList()).get(RANDOM.nextInt(users.size()));
    }
}
