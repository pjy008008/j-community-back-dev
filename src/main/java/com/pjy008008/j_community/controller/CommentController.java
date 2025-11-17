package com.pjy008008.j_community.controller;

import com.pjy008008.j_community.controller.dto.CommentCreateRequest;
import com.pjy008008.j_community.controller.dto.CommentResponse;
import com.pjy008008.j_community.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Comment API", description = "댓글 및 대댓글 API")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "게시글의 댓글 전체 조회", description = "특정 게시글의 모든 댓글과 대댓글을 계층 구조로 조회합니다.")
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getCommentsByPost(
            @Parameter(description = "조회할 댓글의 게시글 ID", required = true)
            @PathVariable("postId") Long postId
    ) {
        List<CommentResponse> comments = commentService.getCommentsByPost(postId);
        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "최상위 댓글 작성", description = "특정 게시글에 새 댓글을 작성합니다. (인증 필요)")
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createTopLevelComment(
            @Parameter(description = "댓글을 작성할 게시글 ID", required = true)
            @PathVariable("postId") Long postId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CommentResponse comment = commentService.createTopLevelComment(postId, request, userDetails.getUsername());
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    @Operation(summary = "대댓글 작성", description = "특정 댓글에 대댓글(답글)을 작성합니다. (인증 필요)")
    @PostMapping("/comments/{commentId}/replies")
    public ResponseEntity<CommentResponse> createReply(
            @Parameter(description = "대댓글을 작성할 댓글 ID", required = true)
            @PathVariable("commentId") Long commentId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CommentResponse reply = commentService.createReply(commentId, request, userDetails.getUsername());
        return new ResponseEntity<>(reply, HttpStatus.CREATED);
    }
}