package com.blog_app_apis.service;

import com.blog_app_apis.dtos.PostDTO;
import com.blog_app_apis.dtos.PostResponce;

import java.util.List;

public interface PostService {

    // Create

    PostDTO createPost(PostDTO postDto, Integer userId, Integer categoryId);
    // Update

    PostDTO updatePost(PostDTO postDto, Integer postId);

    // Delete

    String deletePost(Integer postId);

    // Get all Post

    PostResponce getAllPost(Integer pageNumber, Integer pageSize, String sortBy, String sortDir);

    // get Single Post

    PostDTO getPostById(Integer postId);

    // Get all post by category

    List<PostDTO> getPostByCategory(Integer categoryId);

    // Get all post by User

    List<PostDTO> getPostByUser(Integer userId);

    // Search Post
    List<PostDTO> searchPosts(String keyword);

    // Update Post Image with binary data
    PostDTO updatePostImage(Integer postId, String imageName, byte[] imageData);

    // Get Post Image binary data by imageName
    byte[] getPostImage(String imageName) throws java.io.FileNotFoundException;
}
