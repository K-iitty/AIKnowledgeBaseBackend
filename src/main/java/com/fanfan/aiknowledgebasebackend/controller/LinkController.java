package com.fanfan.aiknowledgebasebackend.controller;

import com.fanfan.aiknowledgebasebackend.entity.Link;
import com.fanfan.aiknowledgebasebackend.entity.User;
import com.fanfan.aiknowledgebasebackend.service.LinkService;
import com.fanfan.aiknowledgebasebackend.service.UserService;
import lombok.Data;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/links")
public class LinkController {

    private final LinkService linkService;
    private final UserService userService;

    public LinkController(LinkService linkService, UserService userService) {
        this.linkService = linkService;
        this.userService = userService;
    }

    @PostMapping
    public Link create(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal, @RequestBody CreateReq req) {
        User u = userService.findByUsername(principal.getUsername());
        return linkService.create(u.getId(), req.getCategoryId(), req.getTitle(), req.getUrl(), req.getRemark(), req.getIcon(), req.getOrderIndex());
    }

    @GetMapping("/list")
    public List<Link> list(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal, @RequestParam(required = false) Long categoryId) {
        User u = userService.findByUsername(principal.getUsername());
        return linkService.list(u.getId(), categoryId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        linkService.delete(id);
    }
    
    @PutMapping("/{id}")
    public Link update(@PathVariable Long id, @RequestBody CreateReq req) {
        return linkService.update(id, req.getCategoryId(), req.getTitle(), req.getUrl(), req.getRemark(), req.getIcon(), req.getOrderIndex());
    }

    @Data
    public static class CreateReq {
        private Long categoryId;
        private String title;
        private String url;
        private String remark;
        private String icon;
        private Integer orderIndex;
    }
}
