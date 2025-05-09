package com.example.portfolio_project.repository;

import com.example.portfolio_project.entity.PlayerScore;
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

import java.util.List;

public interface PlayerScoreRepository extends JpaRepository<PlayerScore, Long> {
    // @Query(value = "SELECT * FROM player_score ORDER BY score DESC LIMIT 10",
    // nativeQuery = true)
    // List<PlayerScore> findTop10ScoresNative();
    List<PlayerScore> findTop10ByOrderByScoreDescTimestampAsc();

    Optional<PlayerScore> findByUsername(String username);
}
