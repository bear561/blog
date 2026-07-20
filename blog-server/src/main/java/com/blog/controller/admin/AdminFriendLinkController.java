package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.dto.FriendLinkDTO;
import com.blog.service.FriendLinkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/friend-links")
@RequiredArgsConstructor
public class AdminFriendLinkController {

    private final FriendLinkService friendLinkService;

    @GetMapping
    public Result<?> getAll() {
        return Result.success(friendLinkService.getAllLinks());
    }

    @PostMapping
    public Result<?> create(@Valid @RequestBody FriendLinkDTO dto) {
        return Result.success(friendLinkService.createLink(dto));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody FriendLinkDTO dto) {
        return Result.success(friendLinkService.updateLink(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        friendLinkService.deleteLink(id);
        return Result.success();
    }
}
