package com.github.ji4597056.task.permission;

import com.github.ji4597056.support.constant.VariableConstant;
import com.github.ji4597056.support.util.FlowUtils;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 记录错误日志
 *
 * @author Jeffrey
 * @since 2018/3/1 10:45
 */
public class LogErrorDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogErrorDelegate.class);

    @Override
    public void execute(DelegateExecution execution) {
        // 打印error
        LOGGER.error("process error({})!user:{},process:{},error:{}",
            execution.getVariable(VariableConstant.ERROR_CODE),
            FlowUtils.getProposer(execution).getName(), execution.getProcessInstanceId(), execution
                .getVariable(VariableConstant.ERROR_MESSGE));
    }
}
