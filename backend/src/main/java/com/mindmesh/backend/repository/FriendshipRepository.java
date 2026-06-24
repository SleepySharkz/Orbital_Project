package com.mindmesh.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mindmesh.backend.entity.Friendship;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    
    boolean existsByUserAIdAndUserBId(Long userAId, Long userBId);

    Optional<Friendship> findByUserAIdAndUserBId(Long userAId, Long userBId);

    @EntityGraph(attributePaths = {"userA", "userB"})
    @Query("""
        SELECT friendship
        FROM Friendship friendship
        WHERE friendship.userA.id = :userId
            OR friendship.userB.id = :userId
        ORDER BY friendship.createdAt DESC
        """)
    List<Friendship> findAllForUser(@Param("userId") Long userId);
}
