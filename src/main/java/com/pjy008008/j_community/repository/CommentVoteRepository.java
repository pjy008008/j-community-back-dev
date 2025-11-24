package com.pjy008008.j_community.repository;

import com.pjy008008.j_community.entity.CommentVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentVoteRepository extends JpaRepository<CommentVote, Long> {
    Optional<CommentVote> findByUserIdAndCommentId(Long userId, Long commentId);
    List<CommentVote> findByUserIdAndComment_Post_Id(Long userId, Long postId);
}