package com.mogacko.mogacko.dto;

public enum KickMemberResult {
    SUCCESS,
    GROUP_NOT_FOUND,
    USER_NOT_FOUND,
    NOT_GROUP_OWNER,
    CANNOT_KICK_OWNER,
    MEMBER_NOT_FOUND,
    FAILED
}