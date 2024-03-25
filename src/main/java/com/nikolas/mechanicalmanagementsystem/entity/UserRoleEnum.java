package com.nikolas.mechanicalmanagementsystem.entity;

public enum UserRoleEnum {
    ADMIN("ADMIN"),
    USER("USER");

    private final String roleEnumName;

    UserRoleEnum(String roleEnumName) {
        this.roleEnumName = roleEnumName;
    }

    public String getUserRoleEnumName() {
        return roleEnumName;
    }
}

