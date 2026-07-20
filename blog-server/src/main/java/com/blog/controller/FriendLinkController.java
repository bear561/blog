package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.FriendLink;
import com.blog.service.FriendLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friend-links")
@RequiredArgsConstructor
public class FriendLinkController {

    private final FriendLinkService friendLinkService;

    @GetMapping
    public Result<List<FriendLink>> getAll() {
        return Result.success(friendLinkService.getVisibleLinks());
    }
}
