package com.mogacko.mogacko.controller;

import com.mogacko.mogacko.dto.GroupCreateRequest;
import com.mogacko.mogacko.dto.GroupMemberDto;
import com.mogacko.mogacko.dto.StudyGroupDto;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.service.AuthService;
import com.mogacko.mogacko.service.StudyGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "스터디 그룹", description = "스터디 그룹 관리 API")
public class StudyGroupController {

    private final StudyGroupService studyGroupService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<Page<StudyGroupDto>> getAllGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<StudyGroupDto> groups = studyGroupService.getAllGroups(page, size);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<StudyGroupDto>> getGroupsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<StudyGroupDto> groups = studyGroupService.getGroupsByCategory(category, page, size);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<StudyGroupDto>> searchGroups(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<StudyGroupDto> groups = studyGroupService.searchGroups(keyword, page, size);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/my-groups")
    public ResponseEntity<List<StudyGroupDto>> getMyGroups() {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        List<StudyGroupDto> groups = studyGroupService.getMyGroups(currentUser);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<StudyGroupDto> getGroupDetails(@PathVariable Long groupId) {
        StudyGroupDto group = studyGroupService.getGroupDetails(groupId);
        if (group == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(group);
    }

    @PostMapping
    public ResponseEntity<StudyGroupDto> createGroup(@RequestBody GroupCreateRequest request) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        StudyGroupDto createdGroup = studyGroupService.createGroup(currentUser, request);
        return ResponseEntity.ok(createdGroup);
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<StudyGroupDto> updateGroup(
            @PathVariable Long groupId,
            @RequestBody GroupCreateRequest request) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        StudyGroupDto updatedGroup = studyGroupService.updateGroup(currentUser, groupId, request);
        if (updatedGroup == null) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(updatedGroup);
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> deactivateGroup(@PathVariable Long groupId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        boolean success = studyGroupService.deactivateGroup(currentUser, groupId);
        if (!success) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberDto>> getGroupMembers(@PathVariable Long groupId) {
        List<GroupMemberDto> members = studyGroupService.getGroupMembers(groupId);
        if (members == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(members);
    }

    @PostMapping("/{groupId}/join")
    public ResponseEntity<?> joinGroup(@PathVariable Long groupId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        boolean success = studyGroupService.joinGroup(currentUser, groupId);
        if (!success) {
            return ResponseEntity.badRequest().body("Cannot join group");
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{groupId}/leave")
    public ResponseEntity<?> leaveGroup(@PathVariable Long groupId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        boolean success = studyGroupService.leaveGroup(currentUser, groupId);
        if (!success) {
            return ResponseEntity.badRequest().body("Cannot leave group");
        }

        return ResponseEntity.ok().build();
    }
}