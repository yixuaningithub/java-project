package com.example.portfolio_project.service;

import com.example.portfolio_project.entity.PlayerScore;
import com.example.portfolio_project.repository.PlayerScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Optional;
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
            Optional<PlayerScore> existing = repository.findByUsername(username);
            if (existing.isPresent()) {
                PlayerScore oldScore = existing.get();
                if (score > oldScore.getScore()) {
                    oldScore.setScore(score);
                    oldScore.setTimestamp(LocalDateTime.now());
                    return repository.save(oldScore);
                } else {
                    return oldScore;
                }
            } else {
                List<PlayerScore> topScores = repository.findTop10ByOrderByScoreDescTimestampAsc();

                if (topScores.size() < 10 || score > topScores.get(topScores.size() - 1).getScore()) {
                    PlayerScore newScore = new PlayerScore();
                    newScore.setUsername(username);
                    newScore.setScore(score);
                    newScore.setTimestamp(LocalDateTime.now());
                    return repository.save(newScore);
                }

                if (score == topScores.get(topScores.size() - 1).getScore()) {
                    return null;
                }
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

    public List<PlayerScore> getTopScores() {
        return repository.findTop10ByOrderByScoreDescTimestampAsc();
    }

    public void clearAllScores() {
        repository.deleteAll();
    }
}
