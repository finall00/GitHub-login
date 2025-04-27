package com.finall.loggin_app.Controller;


import com.finall.loggin_app.Services.GitHubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/github")
public class GitHubController {

    @Autowired
  private GitHubService gitHubService;


    @GetMapping("/user")
    public ResponseEntity<?> getUserInfo(Authentication auth) {
        return ResponseEntity.ok(gitHubService.getUserInfo(auth));
    }


    @GetMapping("/repos")
    public ResponseEntity<?> getRepos(Authentication auth) {
       return ResponseEntity.ok(gitHubService.getUserRepos(auth));
    }


    @GetMapping("/orgs")
    public ResponseEntity<?> getOrgs(Authentication auth) {
        return ResponseEntity.ok(gitHubService.getUserOrgs(auth));
    }


    @GetMapping("/repos/{owner}/{repo}/pulls")
    public ResponseEntity<?> getRepoPulls(Authentication auth, @PathVariable String owner, @PathVariable String repo) {
        return ResponseEntity.ok(gitHubService.getRepoPulls(auth, owner, repo));
    }


    @GetMapping("/repos/{owner}/{repo}/commits")
    public ResponseEntity<?> getRepoCommits(Authentication auth, @PathVariable String owner, @PathVariable String repo) {
        return ResponseEntity.ok(gitHubService.getRepoCommits(auth, owner, repo));
    }

    @GetMapping("/repos/{owner}/{repo}/issues")
    public ResponseEntity<?> getRepoIssues(Authentication auth, @PathVariable String owner, @PathVariable String repo) {
        return ResponseEntity.ok(gitHubService.getRepoIssues(auth, owner, repo));
    }


    @GetMapping("/issues/all")
    public ResponseEntity<?> getAllIssues(Authentication auth) {
        List<Map<String, Object>> issues = gitHubService.getAllIssues(auth);
        if (issues.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No issues found");
        }
        return ResponseEntity.ok(issues);
    }

}
