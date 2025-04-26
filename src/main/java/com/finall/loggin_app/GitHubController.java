package com.finall.loggin_app;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/github")
public class GitHubController {

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    private RestTemplate restTemplate = new RestTemplate();


    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getGithubInfo(Authentication auth) {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) auth;
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getName()
        );

        String accessToken = client.getAccessToken().getTokenValue();


        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        Map<String, Object> result = new HashMap<>();

        try {
            ResponseEntity<Map> userResponse = restTemplate.exchange(
                    "https://api.github.com/user",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            result.put("user", userResponse.getBody());

            ResponseEntity<List> resposResponse = restTemplate.exchange(
                    "https://api.github.com/user/repos",
                    HttpMethod.GET,
                    entity,
                    List.class
            );
            List<Map<String, Object>> repos = resposResponse.getBody();
            result.put("repos", repos);

            ResponseEntity<List> orgsResponse = restTemplate.exchange(
                    "https://api.github.com/user/orgs",
                    HttpMethod.GET,
                    entity,
                    List.class
            );
            result.put("orgs", orgsResponse.getBody());


            Map<String, Object> repoDetails = new HashMap<>();

            for (Map<String, Object> repo : repos) {
                String repoName = repo.get("name").toString();
                Map<String, Object> repoDetail = new HashMap<>();

                ResponseEntity<List> prsResp = restTemplate.exchange(
                        "https://api.github.com/repos/" + userResponse.getBody().get("login") + "/" + repoName + "/pulls",
                        HttpMethod.GET,
                        entity,
                        List.class
                );
                repoDetail.put("pulls", prsResp.getBody());


                //Commits
                ResponseEntity<List> commitsResp = null;

                try {

                    commitsResp = restTemplate.exchange(
                            "https://api.github.com/repos/" + userResponse.getBody().get("login") + "/" + repoName + "/commits",
                            HttpMethod.GET,
                            entity,
                            List.class
                    );
                    if (commitsResp.getBody() != null && commitsResp.getStatusCode() == HttpStatus.OK) {
                        repoDetail.put("commits", commitsResp.getBody());

                    } else {
                        repoDetail.put("commits", "No Commits Found ");
                    }


                } catch (HttpClientErrorException e) {
                    if (e.getStatusCode() == HttpStatus.CONFLICT) {
                        repoDetail.put("Commits", "No Commits found (Respository is empty)");
                    } else {
                        repoDetail.put("commits", "Error fetching commits: " + e.getMessage());
                    }
                }
                repoDetails.put(repoName, repoDetail);

            }


            result.put("repoDetails", repoDetails);


            return ResponseEntity.ok(result);


        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("Error", "Erro ao buscar informacoes do Github"));
        }


    }


}
