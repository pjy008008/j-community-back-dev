package com.pjy008008.j_community.controller;

import com.pjy008008.j_community.controller.dto.CommunityCreateRequest;
import com.pjy008008.j_community.controller.dto.CommunityResponse;
import com.pjy008008.j_community.controller.dto.CommunityUpdateRequest;
import com.pjy008008.j_community.controller.dto.ErrorResponse;
import com.pjy008008.j_community.service.CommunityService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/communities")
@RequiredArgsConstructor
@Tag(name = "Community API", description = "커뮤니티 생성, 조회 API")
public class CommunityController {

    private final CommunityService communityService;

    @Operation(summary = "커뮤니티 생성 (관리자 전용)", description = "새로운 커뮤니티를 생성합니다. (ADMIN 권한 필요)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "커뮤니티 생성 성공",
                    content = @Content(schema = @Schema(implementation = CommunityResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력값 유효성 검사 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "409", description = "커뮤니티 이름 중복",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CommunityResponse> createCommunity(
            @Valid @RequestBody CommunityCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String username = userDetails.getUsername();
        CommunityResponse response = communityService.createCommunity(request, username);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "모든 커뮤니티 조회", description = "모든 커뮤니티 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<CommunityResponse>> getAllCommunities() {
        List<CommunityResponse> communities = communityService.getAllCommunities();
        return ResponseEntity.ok(communities);
    }

    @Operation(summary = "커뮤니티 이름으로 조회", description = "특정 커뮤니티의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CommunityResponse.class))),
            @ApiResponse(responseCode = "404", description = "커뮤니티를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{name}")
    public ResponseEntity<CommunityResponse> getCommunityByName(
            @PathVariable("name") String name
    ) {
        CommunityResponse community = communityService.getCommunityByName(name);
        return ResponseEntity.ok(community);
    }

    @Operation(summary = "커뮤니티 수정 (관리자 전용)", description = "커뮤니티 정보를 수정합니다. (ADMIN 권한 필요)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = CommunityResponse.class))),
            @ApiResponse(responseCode = "404", description = "커뮤니티를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이름 중복",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<CommunityResponse> updateCommunity(
            @PathVariable("id") Long id,
            @Valid @RequestBody CommunityUpdateRequest request
    ) {
        CommunityResponse response = communityService.updateCommunity(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "커뮤니티 삭제 (관리자 전용)", description = "커뮤니티를 삭제합니다. (ADMIN 권한 필요)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "커뮤니티를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCommunity(
            @PathVariable("id") Long id
    ) {
        communityService.deleteCommunity(id);
        return ResponseEntity.noContent().build();
    }
}