package com.pjy008008.j_community.repository;

import com.pjy008008.j_community.entity.UserCommunity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserCommunityRepository extends JpaRepository<UserCommunity, Long> {
    boolean existsByUserIdAndCommunityId(Long userId, Long communityId);
    Optional<UserCommunity> findByUserIdAndCommunityId(Long userId, Long communityId);
    List<UserCommunity> findAllByUserId(Long userId);
}