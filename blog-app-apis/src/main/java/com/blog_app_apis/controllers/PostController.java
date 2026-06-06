package com.blog_app_apis.controllers;

import com.blog_app_apis.config.AppConstants;
import com.blog_app_apis.dtos.ApiResponse;
import com.blog_app_apis.dtos.PostDTO;
import com.blog_app_apis.dtos.PostResponce;
import com.blog_app_apis.service.FileService;
import com.blog_app_apis.service.PostService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String path;
    // ======================================= Create Post
    // ==============================================

    @PostMapping("/user/{userId}/category/{categoryId}/posts")
    public ResponseEntity<PostDTO> createPost(@RequestBody PostDTO postDTO,
            @PathVariable("userId") Integer userId,
            @PathVariable("categoryId") Integer categoryId) {

        PostDTO createPost = this.postService.createPost(postDTO, userId, categoryId);
        return new ResponseEntity<PostDTO>(createPost, HttpStatus.CREATED);
    }
    // ===================================== Get Post By Category
    // ======================================

    @GetMapping("/category/{categoryId}/posts")
    public ResponseEntity<List<PostDTO>> getPostByCategory(@PathVariable("categoryId") Integer categoryId) {
        List<PostDTO> posts = this.postService.getPostByCategory(categoryId);
        return new ResponseEntity<List<PostDTO>>(posts, HttpStatus.OK);
    }
    // ======================================= Get Post By User
    // =======x=================================

    @GetMapping("/user/{userId}/posts")
    public ResponseEntity<List<PostDTO>> getPostByUser(@PathVariable("userId") Integer userId) {
        List<PostDTO> posts = this.postService.getPostByUser(userId);
        return new ResponseEntity<List<PostDTO>>(posts, HttpStatus.OK);
    }

    // ======================================= Update Post
    // ===========================================

    @PutMapping("/posts/{postId}")
    public ResponseEntity<PostDTO> UpdatePost(@RequestBody PostDTO postDto, @PathVariable("postId") Integer postId) {
        PostDTO updatePost = this.postService.updatePost(postDto, postId);
        return new ResponseEntity<PostDTO>(updatePost, HttpStatus.OK);
    }

    // ======================================= Get Post By Id
    // ==========================================
    @GetMapping("/GetPost/{postId}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable("postId") Integer postId) {
        return ResponseEntity.ok(this.postService.getPostById(postId));
    }
    // ======================================= Delete Post By Id
    // ========================================

    @DeleteMapping("/deletePost/{postId}")
    public ResponseEntity<ApiResponse> deletePost(@PathVariable("postId") Integer postId) {
        this.postService.deletePost(postId);
        return new ResponseEntity<ApiResponse>(new ApiResponse("Post Deleted Successfully !!", true), HttpStatus.OK);
    }

    // ======================================= Search
    // ====================================================

    @GetMapping("/post/search/{keywords}")
    public ResponseEntity<List<PostDTO>> searchPostByTitile(@PathVariable("keywords") String keywords) {
        List<PostDTO> result = this.postService.searchPosts(keywords);
        return new ResponseEntity<List<PostDTO>>(result, HttpStatus.OK);
    }

    // ======================================= Get ALL Post
    // =================================================

    @GetMapping("/allposts")
    public ResponseEntity<PostResponce> getAllPost(
            @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_DIR, required = false) String sortDir) {
        PostResponce postResponce = this.postService.getAllPost(pageNumber, pageSize, sortBy, sortDir);
        return new ResponseEntity<PostResponce>(postResponce, HttpStatus.OK);
    }

    // =======================================Post Image
    // Upload===========================================

    @PostMapping("/post/image/upload/{postId}")
    public ResponseEntity<PostDTO> uploadPostImage(@RequestParam("image") MultipartFile image,
            @PathVariable("postId") Integer postId) throws IOException {
        PostDTO postDto = this.postService.getPostById(postId);
        String fileName = this.fileService.uploadImage(path, image);
        postDto.setImageName(fileName);
        PostDTO updatePost = this.postService.updatePost(postDto, postId);
        return new ResponseEntity<PostDTO>(updatePost, HttpStatus.OK);
    }

    // ========================================method to serve
    // files===================================

    @GetMapping(value = "/post/image/{imageName}", produces = MediaType.IMAGE_JPEG_VALUE)
    public void downloadImage(@PathVariable("imageName") String imageName, HttpServletResponse response)
            throws IOException {
        InputStream resource = this.fileService.getResource(path, imageName);
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        StreamUtils.copy(resource, response.getOutputStream());

    }

}
