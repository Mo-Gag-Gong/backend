package com.mogacko.mogacko.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingMembersResult {
    private List<GroupMemberDto> pendingMembers;
    private PendingMembersError error;

    public static PendingMembersResult success(List<GroupMemberDto> pendingMembers) {
        return PendingMembersResult.builder()
                .pendingMembers(pendingMembers)
                .error(null)
                .build();
    }

    public static PendingMembersResult error(PendingMembersError error) {
        return PendingMembersResult.builder()
                .pendingMembers(null)
                .error(error)
                .build();
    }
}

