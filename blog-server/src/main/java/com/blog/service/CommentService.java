package com.blog.service;

import com.blog.common.BusinessException;
import com.blog.common.PageResult;
import com.blog.dto.CommentDTO;
import com.blog.entity.Comment;
import com.blog.mapper.CommentMapper;
import com.blog.vo.CommentVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentMapper commentMapper;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    public CommentService(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ===== 公开接口 =====

    public List<CommentVO> getCommentsByArticleId(Long articleId) {
        List<Comment> comments = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getArticleId, articleId)
                        .eq(Comment::getIsReviewed, 1)
                        .orderByAsc(Comment::getCreatedAt));

        return buildCommentTree(comments);
    }

    public void submitComment(CommentDTO dto, HttpServletRequest request) {
        String ip = getClientIp(request);

        // 限流检查（Redis 不可用时跳过）
        if (redisTemplate != null) {
            String rateKey = "rate:comment:" + ip;
            Long count = redisTemplate.opsForValue().increment(rateKey);
            if (count != null && count == 1) {
                redisTemplate.expire(rateKey, 1, TimeUnit.MINUTES);
            }
            if (count != null && count > 5) {
                throw new BusinessException(429, "评论太频繁，请稍后再试");
            }
        }

        Comment comment = new Comment();
        comment.setArticleId(dto.getArticleId());
        comment.setParentId(dto.getParentId());
        comment.setReplyToId(dto.getReplyToId());
        comment.setNickname(dto.getNickname());
        comment.setEmail(dto.getEmail());
        comment.setWebsite(dto.getWebsite());
        comment.setContent(dto.getContent());
        comment.setIsReviewed(0); // 默认未审核
        comment.setIp(ip);
        comment.setUserAgent(request.getHeader("User-Agent"));

        commentMapper.insert(comment);
    }

    // ===== 管理接口 =====

    public PageResult<CommentVO> adminGetComments(int page, int size, Integer isReviewed) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .orderByDesc(Comment::getCreatedAt);
        if (isReviewed != null) {
            wrapper.eq(Comment::getIsReviewed, isReviewed);
        }

        Page<Comment> p = new Page<>(page, size);
        IPage<Comment> result = commentMapper.selectPage(p, wrapper);

        List<CommentVO> records = result.getRecords().stream()
                .map(this::toCommentVO)
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), page, size);
    }

    public void reviewComment(Long id, boolean approved) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(404, "评论不存在");
        }
        comment.setIsReviewed(approved ? 1 : 0);
        commentMapper.updateById(comment);
    }

    public void deleteComment(Long id) {
        commentMapper.deleteById(id);
    }

    // ===== 私有方法 =====

    private List<CommentVO> buildCommentTree(List<Comment> comments) {
        Map<Long, CommentVO> map = new HashMap<>();
        List<CommentVO> roots = new ArrayList<>();

        for (Comment c : comments) {
            CommentVO vo = toCommentVO(c);
            vo.setCreatedAtFmt(c.getCreatedAt() != null ? c.getCreatedAt().format(FMT) : "");
            map.put(c.getId(), vo);
        }

        for (Comment c : comments) {
            CommentVO vo = map.get(c.getId());
            if (c.getParentId() != null && map.containsKey(c.getParentId())) {
                CommentVO parent = map.get(c.getParentId());
                if (parent.getReplies() == null) {
                    parent.setReplies(new ArrayList<>());
                }
                parent.getReplies().add(vo);
            } else if (c.getParentId() == null) {
                roots.add(vo);
            }
        }

        return roots;
    }

    private CommentVO toCommentVO(Comment c) {
        CommentVO vo = new CommentVO();
        vo.setId(c.getId());
        vo.setArticleId(c.getArticleId());
        vo.setParentId(c.getParentId());
        vo.setReplyToId(c.getReplyToId());
        vo.setNickname(c.getNickname());
        vo.setEmail(c.getEmail());
        vo.setWebsite(c.getWebsite());
        vo.setContent(c.getContent());
        vo.setIsReviewed(c.getIsReviewed());
        vo.setCreatedAt(c.getCreatedAt());
        return vo;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
