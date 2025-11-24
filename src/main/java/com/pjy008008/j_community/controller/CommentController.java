package com.pjy008008.j_community.controller;

import com.pjy008008.j_community.controller.dto.CommentCreateRequest;
import com.pjy008008.j_community.controller.dto.CommentResponse;
import com.pjy008008.j_community.controller.dto.CommentUpdateRequest;
import com.pjy008008.j_community.controller.dto.ErrorResponse;
import com.pjy008008.j_community.model.VoteType;
import com.pjy008008.j_community.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "게시글의 댓글 전체 조회", description = "특정 게시글의 모든 댓글과 대댓글을 조회합니다. (로그인 시 본인 투표 상태 포함)")
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getCommentsByPost(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String username = (userDetails != null) ? userDetails.getUsername() : null;

        List<CommentResponse> comments = commentService.getCommentsByPost(postId, username);
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

    @Operation(summary = "댓글 수정", description = "자신이 작성한 댓글을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (작성자 아님)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable("commentId") Long commentId,
            @Valid @RequestBody CommentUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CommentResponse response = commentService.updateComment(commentId, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "댓글 삭제", description = "자신이 작성한 댓글을 삭제합니다. (대댓글이 있다면 함께 삭제됩니다)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (작성자 아님)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable("commentId") Long commentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        commentService.deleteComment(commentId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "댓글 추천", description = "댓글을 추천(+1)하거나 취소합니다.")
    @PostMapping("/comments/{commentId}/upvote")
    public ResponseEntity<Integer> upvoteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        int currentVotes = commentService.voteComment(commentId, VoteType.UP, userDetails.getUsername());
        return ResponseEntity.ok(currentVotes);
    }

    @Operation(summary = "댓글 비추천", description = "댓글을 비추천(-1)하거나 취소합니다.")
    @PostMapping("/comments/{commentId}/downvote")
    public ResponseEntity<Integer> downvoteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        int currentVotes = commentService.voteComment(commentId, VoteType.DOWN, userDetails.getUsername());
        return ResponseEntity.ok(currentVotes);
    }
}