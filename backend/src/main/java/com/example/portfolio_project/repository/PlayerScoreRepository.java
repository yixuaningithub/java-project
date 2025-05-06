package com.example.portfolio_project.repository;

import com.example.portfolio_project.entity.PlayerScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerScoreRepository extends JpaRepository<PlayerScore, Long> {
    List<PlayerScore> findTop10ByOrderByScoreDesc();
}
