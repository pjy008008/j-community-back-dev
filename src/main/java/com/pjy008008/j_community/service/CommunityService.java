package com.pjy008008.j_community.service;

import com.pjy008008.j_community.controller.dto.CommunityCreateRequest;
import com.pjy008008.j_community.controller.dto.CommunityResponse;
import com.pjy008008.j_community.controller.dto.CommunityUpdateRequest;
import com.pjy008008.j_community.entity.Community;
import com.pjy008008.j_community.entity.User;
import com.pjy008008.j_community.exception.DuplicateResourceException;
import com.pjy008008.j_community.exception.ResourceNotFoundException;
import com.pjy008008.j_community.repository.CommunityRepository;
import com.pjy008008.j_community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;

    // 커뮤니티 생성, 관리자 전용
    @Transactional
    public CommunityResponse createCommunity(CommunityCreateRequest request, String creatorUsername) {

        if (communityRepository.findByName(request.name()).isPresent()) {
            throw new DuplicateResourceException("Community name is already taken: " + request.name());
        }

        User creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Creator not found: " + creatorUsername));

        Community newCommunity = Community.builder()
                .name(request.name())
                .description(request.description())
                .colorTheme(request.colorTheme())
                .creator(creator)
                .build();

        Community savedCommunity = communityRepository.save(newCommunity);
        return CommunityResponse.from(savedCommunity);
    }

    // 커뮤니티 수정, 관리자 전용
    @Transactional
    public CommunityResponse updateCommunity(Long communityId, CommunityUpdateRequest request) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found with id: " + communityId));

        if (!community.getName().equals(request.name()) && communityRepository.findByName(request.name()).isPresent()) {
            throw new DuplicateResourceException("Community name is already taken: " + request.name());
        }

        community.update(request.name(), request.description(), request.colorTheme());

        return CommunityResponse.from(community);
    }

    // 커뮤니티 삭제, 관리자 전용
    @Transactional
    public void deleteCommunity(Long communityId) {
        if (!communityRepository.existsById(communityId)) {
            throw new ResourceNotFoundException("Community not found with id: " + communityId);
        }
        communityRepository.deleteById(communityId);
    }

    public List<CommunityResponse> getAllCommunities() {
        return communityRepository.findAll().stream()
                .map(CommunityResponse::from)
                .collect(Collectors.toList());
    }

    public CommunityResponse getCommunityByName(String name) {
        Community community = communityRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found with name: " + name));
        return CommunityResponse.from(community);
    }
}