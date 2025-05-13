// src/main/java/com/mogacko/mogacko/dto/MembershipActionResult.java
package com.mogacko.mogacko.dto;

public enum MembershipActionResult {
    SUCCESS,
    GROUP_NOT_FOUND,
    USER_NOT_FOUND,
    NOT_GROUP_OWNER,
    MEMBER_NOT_PENDING,
    MAX_MEMBERS_EXCEEDED,
    FAILED
}