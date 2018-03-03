package com.github.ji4597056.support.enums;

/**
 * comment enum
 *
 * @author Jeffrey
 * @since 2018/03/03 10:54
 */
public enum CommentEnum {

    OPERATION("operation"),

    REMARK("remark");

    private String type;

    CommentEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
