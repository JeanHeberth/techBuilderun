package com.br.techbuilderun.controller;

import com.br.techbuilderun.dto.CreateTweetRequestDto;
import com.br.techbuilderun.dto.FeedDto;
import com.br.techbuilderun.dto.FeedItemDto;
import com.br.techbuilderun.entity.Role;
import com.br.techbuilderun.entity.Tweet;
import com.br.techbuilderun.repository.TweetRepository;
import com.br.techbuilderun.repository.UserRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
public class TweetController {

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/tweets")
    public ResponseEntity<Void> createTweet(@RequestBody CreateTweetRequestDto createTweetRequestDto,
                                            JwtAuthenticationToken token) {

        var user = userRepository.findById(UUID.fromString(token.getName()));

        var tweet = new Tweet();
        tweet.setUser(user.get());
        tweet.setContext(createTweetRequestDto.content());
        tweetRepository.save(tweet);
        return ResponseEntity.ok().build();

    }

    @DeleteMapping("/tweets/{id}")
    public ResponseEntity<Void> deleteTweet(@PathVariable("id") Long tweetId, JwtAuthenticationToken token) {

        var user = userRepository.findById(UUID.fromString(token.getName()));
        var tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var isAdmin = user.get().getRoles()
                .stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        if (isAdmin || tweet.getUser().getUserId().equals(UUID.fromString(token.getName()))) {
            tweetRepository.delete(tweet);

        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/feed")
    public ResponseEntity<List<FeedDto>> feed(@RequestParam(value = "page", defaultValue = "0") int page,
                                              @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
      var tweets =  tweetRepository.findAll(PageRequest.of(page, pageSize, Sort.Direction.DESC, "createTimeInstant"))
              .map(tweet -> new FeedItemDto(tweet.getTweetId(), tweet.getContext(), tweet.getUser().getUsername()));
      return ResponseEntity.ok(Collections.singletonList(new FeedDto(tweets.getContent(), page, pageSize, tweets.getTotalPages(), tweets.getTotalElements())));
    }
}
