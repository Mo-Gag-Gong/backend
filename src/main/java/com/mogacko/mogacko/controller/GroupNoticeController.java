package com.mogacko.mogacko.controller;

import com.mogacko.mogacko.dto.GroupNoticeCreateRequest;
import com.mogacko.mogacko.dto.GroupNoticeDto;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.service.AuthService;
import com.mogacko.mogacko.service.GroupNoticeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups/{groupId}/notices")
@RequiredArgsConstructor
@Tag(name = "스터디 그룹 공지", description = "스터디 그룹 공지 API")
public class GroupNoticeController {

    private final GroupNoticeService noticeService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<Page<GroupNoticeDto>> getGroupNotices(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<GroupNoticeDto> notices = noticeService.getGroupNotices(groupId, page, size);
        if (notices == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(notices);
    }

    @GetMapping("/{noticeId}")
    public ResponseEntity<GroupNoticeDto> getNoticeDetails(
            @PathVariable Long groupId,
            @PathVariable Long noticeId) {

        GroupNoticeDto notice = noticeService.getNoticeDetails(groupId, noticeId);
        if (notice == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(notice);
    }

    @PostMapping
    public ResponseEntity<GroupNoticeDto> createNotice(
            @PathVariable Long groupId,
            @RequestBody GroupNoticeCreateRequest request) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        GroupNoticeDto createdNotice = noticeService.createNotice(currentUser, groupId, request);
        if (createdNotice == null) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(createdNotice);
    }

    @PutMapping("/{noticeId}")
    public ResponseEntity<GroupNoticeDto> updateNotice(
            @PathVariable Long groupId,
            @PathVariable Long noticeId,
            @RequestBody GroupNoticeCreateRequest request) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        GroupNoticeDto updatedNotice = noticeService.updateNotice(currentUser, groupId, noticeId, request);
        if (updatedNotice == null) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(updatedNotice);
    }

    @DeleteMapping("/{noticeId}")
    public ResponseEntity<?> deleteNotice(
            @PathVariable Long groupId,
            @PathVariable Long noticeId) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        boolean success = noticeService.deleteNotice(currentUser, groupId, noticeId);
        if (!success) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok().build();
    }
}