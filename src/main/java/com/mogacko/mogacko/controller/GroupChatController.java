package com.mogacko.mogacko.controller;

import com.mogacko.mogacko.dto.GroupChatCreateRequest;
import com.mogacko.mogacko.dto.GroupChatDto;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.service.AuthService;
import com.mogacko.mogacko.service.GroupChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups/{groupId}/chats")
@RequiredArgsConstructor
@Tag(name = "스터디 그룹 채팅", description = "스터디 그룹 채팅 관리 API - 그룹 내 채팅 메시지를 관리합니다.")
public class GroupChatController {

    private final GroupChatService chatService;
    private final AuthService authService;

    /**
     * 스터디 그룹의 채팅 메시지 목록을 페이지 단위로 조회합니다.
     *
     * @param groupId 스터디 그룹 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 채팅 메시지 목록
     */
    @Operation(summary = "그룹 채팅 목록 조회", description = "스터디 그룹의 채팅 메시지 목록을 페이지 단위로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅 메시지 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "스터디 그룹을 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<Page<GroupChatDto>> getGroupChats(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {

        Page<GroupChatDto> chats = chatService.getGroupChats(groupId, page, size);
        if (chats == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(chats);
    }

    /**
     * 스터디 그룹에 새 채팅 메시지를 전송합니다.
     *
     * @param groupId 스터디 그룹 ID
     * @param request 채팅 메시지 내용
     * @return 전송된 채팅 메시지 정보
     */
    @Operation(summary = "채팅 메시지 전송", description = "스터디 그룹에 새 채팅 메시지를 전송합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메시지 전송 성공",
                    content = @Content(schema = @Schema(implementation = GroupChatDto.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "메시지 전송 권한 없음")
    })
    @PostMapping
    public ResponseEntity<GroupChatDto> sendMessage(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "채팅 메시지 내용") @RequestBody GroupChatCreateRequest request) {

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
