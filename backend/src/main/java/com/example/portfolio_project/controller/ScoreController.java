package com.example.portfolio_project.controller;

import com.example.portfolio_project.entity.PlayerScore;
import com.example.portfolio_project.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://sparkling-granita-e2ed7d.netlify.app"
})
@RestController
@RequestMapping("/api/scores")
public class ScoreController {

    @Autowired
    private ScoreService service;

    @PostMapping
    public ResponseEntity<PlayerScore> submitScore(@RequestBody Map<String, Object> payload) {
        String username = (String) payload.get("username");
        int score = (int) payload.get("score");
        return ResponseEntity.ok(service.saveScore(username, score));
    }

    @GetMapping
    public ResponseEntity<List<PlayerScore>> getLeaderboard() {
        return ResponseEntity.ok(service.getTopScores());
    }

    @DeleteMapping
    public ResponseEntity<String> clearScores() {
        service.clearAllScores();
        return ResponseEntity.ok("All scores cleared.");
    }
}
