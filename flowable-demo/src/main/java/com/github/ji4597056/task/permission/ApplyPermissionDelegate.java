package com.github.ji4597056.task.permission;

import com.github.ji4597056.support.constant.VariableConstant;
import com.github.ji4597056.support.enums.FlowErrorEnum;
import com.github.ji4597056.support.enums.PermissionEnum;
import com.github.ji4597056.support.util.FlowUtils;
import com.google.common.collect.Sets;
import java.util.Set;
import org.flowable.engine.delegate.BpmnError;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 申请权限,若申请权限非法则抛出异常
 *
 * @author Jeffrey
 * @since 2018/03/01 10:46
 */
public class ApplyPermissionDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplyPermissionDelegate.class);

    private static final Set<String> PERMISSION_STORE = Sets
        .newHashSet(PermissionEnum.LEGAL_PERMISSION.getName(),
            PermissionEnum.ADD_APP_PERMISSION.getName());

    @Override
    public void execute(DelegateExecution execution) {
        String permission = (String) execution.getVariable(VariableConstant.PERMISSION);
        if (permission == null) {
            throw getError(execution, FlowErrorEnum.TASK_MISS_PARAMS,
                "can't find permission to apply!");
        }
        if (!PERMISSION_STORE.contains(permission)) {
            throw getError(execution, FlowErrorEnum.TASK_ILLEGAL,
                "illegal permission to apply(" + permission + ")");
        }
        LOGGER.info("set permission({}) to proposer({}) success", permission,
            FlowUtils.getProposer(execution).getName());
    }

    private BpmnError getError(DelegateExecution execution, FlowErrorEnum errorEnum,
        String message) {
        String errorCode = errorEnum.getErrorCode();
        String errorMessage = errorEnum.getErrorMessage() + ":" + message;
        execution.setVariable(VariableConstant.ERROR_CODE, errorCode);
        execution.setVariable(VariableConstant.ERROR_MESSGE, errorMessage);
        LOGGER.error("ApplyPermissionDelegate encounters an error:{}", errorMessage);
        throw new BpmnError(errorCode, errorMessage);
    }
}
