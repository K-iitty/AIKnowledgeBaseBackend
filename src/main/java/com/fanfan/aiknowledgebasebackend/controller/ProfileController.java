package com.fanfan.aiknowledgebasebackend.controller;

import com.fanfan.aiknowledgebasebackend.dto.ProfileItemRequest;
import com.fanfan.aiknowledgebasebackend.entity.Profile;
import com.fanfan.aiknowledgebasebackend.entity.ProfileItem;
import com.fanfan.aiknowledgebasebackend.entity.User;
import com.fanfan.aiknowledgebasebackend.service.ProfileService;
import com.fanfan.aiknowledgebasebackend.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final UserService userService;

    public ProfileController(ProfileService profileService, UserService userService) {
        this.profileService = profileService;
        this.userService = userService;
    }

    @GetMapping
    public Profile get(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User u = userService.findByUsername(principal.getUsername());
        return profileService.getOrCreate(u.getId());
    }

    @PutMapping
    public Profile update(@RequestBody Profile profile) {
        return profileService.update(profile);
    }

    @PostMapping("/items")
    public ProfileItem addItem(@RequestBody ProfileItemRequest req) {
        return profileService.addItem(req.getProfileId(), req.getType(), req.getTitle(), req.getContent(), 
                req.getStartDate(), req.getEndDate(), req.getOrderIndex());
    }
    
    @PutMapping("/items/{id}")
    public ProfileItem updateItem(@PathVariable Long id, @RequestBody ProfileItemRequest req) {
        return profileService.updateItem(id, req.getType(), req.getTitle(), req.getContent(), 
                req.getStartDate(), req.getEndDate(), req.getOrderIndex());
    }

    @GetMapping("/items")
    public List<ProfileItem> listItems(@RequestParam Long profileId) {
        return profileService.listItems(profileId);
    }

    @DeleteMapping("/items/{id}")
    public void deleteItem(@PathVariable Long id) {
        profileService.deleteItem(id);
    }

}
