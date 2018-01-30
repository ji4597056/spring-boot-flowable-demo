package com.github.ji4597056.task.holiday;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * @author Jeffrey
 * @since 2018/01/22 13:28
 */
public class CallExternalSystemDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        System.err.println("Calling the external system for employee "
            + execution.getVariable("employee"));
    }
}
