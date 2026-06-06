package com.blog_app_apis.controllers;

import com.blog_app_apis.dtos.ApiResponse;
import com.blog_app_apis.dtos.CommentDTO;
import com.blog_app_apis.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("./api/")
public class CommentControler {
    @Autowired
    private CommentService commentService;

    @PostMapping("/post/{post-id}/comments/{user-id}")
    public ResponseEntity<CommentDTO> createComment(@RequestBody CommentDTO comment,
            @PathVariable("post-id") Integer postId,
            @PathVariable("user-id") Integer userId) {
        CommentDTO createComment = this.commentService.createComment(comment, postId, userId);
        return new ResponseEntity<CommentDTO>(createComment, HttpStatus.CREATED);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse> deleteComment(@PathVariable("commentId") Integer commentId) {
        this.commentService.deleteComment(commentId);
        return new ResponseEntity<ApiResponse>(new ApiResponse("Deleted Comment Successfully !!", true), HttpStatus.OK);
    }
}
