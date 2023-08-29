package com.example.postgresfulltextsearch.controller;

import com.example.postgresfulltextsearch.model.Search;
import com.example.postgresfulltextsearch.repository.UserStoryRepository;
import com.example.postgresfulltextsearch.repository.entities.UserStory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user-stories")
public class UserStoryController {
    private final UserStoryRepository userStoryRepository;

    @Autowired
    public UserStoryController(UserStoryRepository userStoryRepository) {
        this.userStoryRepository = userStoryRepository;
    }

    @PostMapping
    public void save(@RequestBody UserStory userStory) {
        userStoryRepository.save(userStory);
    }

    @PostMapping("/search")
    public List<UserStory> search(@RequestBody Search search) {
        return userStoryRepository.findByFilters(search);
    }
}
