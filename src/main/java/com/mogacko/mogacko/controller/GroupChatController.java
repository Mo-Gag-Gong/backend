package com.mogacko.mogacko.controller;

import com.mogacko.mogacko.dto.GroupChatCreateRequest;
import com.mogacko.mogacko.dto.GroupChatDto;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.service.AuthService;
import com.mogacko.mogacko.service.GroupChatService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups/{groupId}/chats")
@RequiredArgsConstructor
@Tag(name = "스터디 그룹 채팅", description = "스터디 그룹 채팅 API")
public class GroupChatController {

    private final GroupChatService chatService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<Page<GroupChatDto>> getGroupChats(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<GroupChatDto> chats = chatService.getGroupChats(groupId, page, size);
        if (chats == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(chats);
    }

    @PostMapping
    public ResponseEntity<GroupChatDto> sendMessage(
            @PathVariable Long groupId,
            @RequestBody GroupChatCreateRequest request) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        GroupChatDto sentMessage = chatService.sendMessage(currentUser, groupId, request);
        if (sentMessage == null) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(sentMessage);
    }
}