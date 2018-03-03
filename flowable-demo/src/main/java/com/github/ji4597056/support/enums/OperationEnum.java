package com.github.ji4597056.support.enums;

/**
 * operation enum(和ApplyPermission流程中操作对应)
 *
 * @author Jeffrey
 * @since 2018/03/02 14:29
 */
public enum OperationEnum {

    UPDATE("update"),

    FINISH("finish"),

    SUBMIT("submit"),

    AUDIT_SUCCESS("approval_success"),

    AUDIT_FAILURE("approval_failure");

    private String type;

    OperationEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
