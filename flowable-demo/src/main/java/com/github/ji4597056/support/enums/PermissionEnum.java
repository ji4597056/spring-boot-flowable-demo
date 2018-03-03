package com.github.ji4597056.support.enums;

/**
 * operation enum(和ApplyPermission流程中操作对应)
 *
 * @author Jeffrey
 * @since 2018/03/02 14:29
 */
public enum PermissionEnum {

    /**
     * 非法权限,用于测试申请权限是抛异常
     */
    ILLEGAL_PERMISSION("illegal_permission"),

    /**
     * 合法权限
     */
    LEGAL_PERMISSION("legal_permission"),

    /**
     * 添加应用权限(合法)
     */
    ADD_APP_PERMISSION("add_app_permission");

    private String name;

    PermissionEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
