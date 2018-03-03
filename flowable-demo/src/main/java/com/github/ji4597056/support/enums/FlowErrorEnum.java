package com.github.ji4597056.support.enums;

/**
 * @author Jeffrey
 * @since 2018/03/02 9:47
 */
public enum FlowErrorEnum {

    TASK_ILLEGAL("101", "任务非法"),

    TASK_MISS_PARAMS("102", "任务缺少参数");

    private String errorCode;

    private String errorMessage;

    FlowErrorEnum(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
