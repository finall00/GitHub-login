package com.finall.loggin_app.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;


@Service
public class GitHubService {

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    private RestTemplate restTemplate;

    private HttpHeaders getHeaders(Authentication auth) {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) auth;
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getName()
        );
        String accessToken = client.getAccessToken().getTokenValue();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }

    public Map<String, Object> getUserInfo(Authentication auth) {
        HttpEntity<String> entity = new HttpEntity<>(getHeaders(auth));
        ResponseEntity<Map> userResponse = restTemplate.exchange(
                "https://api.github.com/user",
                HttpMethod.GET,
                entity,
                Map.class
        );
        return userResponse.getBody();
    }

    public List<Map<String, Object>> getUserRepos(Authentication auth) {
        HttpEntity<String> entity = new HttpEntity<>(getHeaders(auth));
        ResponseEntity<List> reposResponse = restTemplate.exchange(
                "https://api.github.com/user/repos",
                HttpMethod.GET,
                entity,
                List.class
        );
        return reposResponse.getBody();
    }

    public List<Map<String, Object>> getUserOrgs(Authentication auth) {
        HttpEntity<String> entity = new HttpEntity<>(getHeaders(auth));
        ResponseEntity<List> reposResponse = restTemplate.exchange(
                "https://api.github.com/user/orgs",
                HttpMethod.GET,
                entity,
                List.class
        );
        return reposResponse.getBody();
    }

    public List<Map<String, Object>> getRepoPulls(Authentication auth, String owner, String repo) {
        HttpEntity<String> entity = new HttpEntity<>(getHeaders(auth));
        ResponseEntity<List> pullsResponse = restTemplate.exchange(
                "https://api.github.com/repos/" + owner + "/" + repo + "/pulls",
                HttpMethod.GET,
                entity,
                List.class
        );
        return pullsResponse.getBody();
    }

    public Object getRepoCommits(Authentication auth, String owner, String repo) {
        HttpEntity<String> entity = new HttpEntity<>(getHeaders(auth));
        try {
            ResponseEntity<List> commitResponse = restTemplate.exchange(
                    "https://api.github.com/repos/" + owner + "/" + repo + "/commits",
                    HttpMethod.GET,
                    entity,
                    List.class
            );
            if (commitResponse.getStatusCode() == HttpStatus.OK) {
                return commitResponse.getBody();
            } else {
                return "No commits found";
            }
        } catch (Exception e) {
            return "Error while getting repo commits" + e.getMessage();
        }

    }

    public List<Map<String, Object>> getRepoIssues(Authentication auth, String owner, String repo) {
        HttpEntity<String> entity = new HttpEntity<>(getHeaders(auth));


        ResponseEntity<List> issuesResponse = restTemplate.exchange(
                "https://api.github.com/repos/" + owner + "/" + repo + "/issues",
                HttpMethod.GET,
                entity,
                List.class
        );


        if (issuesResponse.getStatusCode() == HttpStatus.OK) {
            return issuesResponse.getBody();
        } else {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getAllIssues(Authentication auth) {
        List<Map<String, Object>> allIssues = new ArrayList<>();

        List<Map<String, Object>> repos = getUserRepos(auth);

        HttpEntity<String> entity = new HttpEntity<>(getHeaders(auth));

        if (repos == null || repos.isEmpty()) {
            return allIssues; // Sem repositórios, retorna lista vazia
        }

        // Para cada repositório, buscar as issues
        for (Map<String, Object> repo : repos) {
            String owner =  ((Map<String, Object>) repo.get("owner")).get("login").toString();

            String repoName = repo.get("name").toString();

            try {
                Object issues = getRepoIssues(auth, owner, repoName);

                if (issues instanceof List) {
                    allIssues.addAll((List<Map<String, Object>>) issues);
                }

            } catch (Exception e) {
                e.printStackTrace(); // Trata erro ao buscar as issues
            }
        }
        return allIssues;
    }


}
