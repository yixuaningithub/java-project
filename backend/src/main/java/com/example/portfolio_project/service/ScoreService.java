package com.example.portfolio_project.service;

import com.example.portfolio_project.entity.PlayerScore;
import com.example.portfolio_project.repository.PlayerScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.locks.ReentrantLock;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScoreService {
    @Autowired
    private PlayerScoreRepository repository;
    private final ReentrantLock lock = new ReentrantLock();

    public PlayerScore saveScore(String username, int score) {
        lock.lock();
        try {
            PlayerScore ps = new PlayerScore();
            ps.setUsername(username);
            ps.setScore(score);
            ps.setTimestamp(LocalDateTime.now());
            return repository.save(ps);
        } finally {
            lock.unlock();
        }
    }

    public List<PlayerScore> getTopScores() {
        return repository.findTop10ByOrderByScoreDesc();
    }

    public void clearAllScores() {
        repository.deleteAll();
    }
}

