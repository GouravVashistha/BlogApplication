package com.blog_app_apis.serviceImpl;

import com.blog_app_apis.Entity.Category;
import com.blog_app_apis.Entity.Post;
import com.blog_app_apis.Entity.User;
import com.blog_app_apis.dtos.PostDTO;
import com.blog_app_apis.dtos.PostResponce;
import com.blog_app_apis.exceptions.ResourceNotFoundException;
import com.blog_app_apis.repository.CategoryRepository;
import com.blog_app_apis.repository.PostRepo;
import com.blog_app_apis.repository.UserRepo;
import com.blog_app_apis.service.PostService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostServiceImple implements PostService {
    @Autowired
    private PostRepo postRepo;
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private CategoryRepository categoryRepo;

    @Override
    public PostDTO createPost(PostDTO postDto, Integer userId, Integer categoryId) {

        User user = this.userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "UserId", userId));
        Category category = this.categoryRepo.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "CategoryId", categoryId));

        Post post = this.modelMapper.map(postDto, Post.class);
        post.setImageName("default.png");
        post.setAddDate(new Date());
        post.setUser(user);
        post.setCategory(category);

        Post newPost = this.postRepo.save(post);
        return this.modelMapper.map(newPost, PostDTO.class);
    }
    //===================================== UpDate Post=================================================

    @Override
    public PostDTO updatePost(PostDTO postDto, Integer postId) {

        Post post = this.postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postId));

        // Get currently authenticated user details
        String currentUserEmail = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(auth -> auth.getAuthority().equals("ADMIN_USER"));

        if (!post.getUser().getEmail().equals(currentUserEmail) && !isAdmin) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to update this post");
        }

        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setImageName(postDto.getImageName());

        // now we save all in post-repository
        Post updatepost = this.postRepo.save(post);
        return this.modelMapper.map(updatepost, PostDTO.class);
    }
//======================================= Delete post===============================================

    @Override
    public String deletePost(Integer postId) {
        Post post = this.postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postId));

        // Get currently authenticated user details
        String currentUserEmail = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(auth -> auth.getAuthority().equals("ADMIN_USER"));

        if (!post.getUser().getEmail().equals(currentUserEmail) && !isAdmin) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to delete this post");
        }

        postRepo.deleteById(postId);
        return "Post delete Successfully";
    }

//========================================= Get All the Post =============================================

    @Override
    public PostResponce getAllPost(Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {

        Sort sort = null;
        if (sortDir.equalsIgnoreCase("asc")) {
            sort = sort.by(sortBy).ascending();

        } else {
            sort = sort.by(sortBy).descending();
        }

        Pageable p = PageRequest.of(pageNumber, pageSize, sort);

        Page<Post> pagePost = this.postRepo.findAll(p);
        List<Post> allPosts = pagePost.getContent();

        List<PostDTO> postDtos = allPosts.stream().map((post) -> this.modelMapper.map(post, PostDTO.class))
                .collect(Collectors.toList());

        PostResponce postResponce = new PostResponce();
        postResponce.setContent(postDtos);
        postResponce.setPageNumber(pagePost.getNumber());
        postResponce.setPageSize(pagePost.getSize());
        postResponce.setTotalElements(pagePost.getTotalElements());
        postResponce.setTotalPages(pagePost.getTotalPages());
        postResponce.setLastpage(pagePost.isLast());


        return postResponce;
    }

    //======================================= Get Post By Id===============================================

    @Override
    public PostDTO getPostById(Integer postId) {
        Post post = this.postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "PostId", postId));
        return this.modelMapper.map(post, PostDTO.class);
    }

    //==================================== Get Post by catagory ==================================================

    @Override
    public List<PostDTO> getPostByCategory(Integer categoryId) {

        Category cat = this.categoryRepo.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "CategoryId", categoryId));

        return this.postRepo.findByCategory(cat)
                .stream()
                .map((post) -> this.modelMapper.map(post, PostDTO.class))
                .toList();
    }
//==================================== Get Post by User ===========================================================

    @Override
    public List<PostDTO> getPostByUser(Integer userId) {
        User users = this.userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userid", userId));

        return this.postRepo.findByUser(users)
                .stream()
                .map((post) -> this.modelMapper.map(post, PostDTO.class))
                .toList();
    }

    //====================================== Search Post ======================================================

    @Override
    public List<PostDTO> searchPosts(String keyword) {
        List<Post> posts = this.postRepo.findByTitleContaining(keyword);
        List<PostDTO> postDtos = posts.stream()
                .map((post) -> this.modelMapper.map(post, PostDTO.class))
                .collect(Collectors.toList());
        return postDtos;
    }

    @Override
    public PostDTO updatePostImage(Integer postId, String imageName, byte[] imageData) {
        Post post = this.postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postId));
        post.setImageName(imageName);
        post.setImageData(imageData);
        Post updatedPost = this.postRepo.save(post);
        return this.modelMapper.map(updatedPost, PostDTO.class);
    }

    @Override
    public byte[] getPostImage(String imageName) throws java.io.FileNotFoundException {
        List<Post> posts = this.postRepo.findByImageName(imageName);
        if (!posts.isEmpty()) {
            Post post = posts.get(0);
            if (post.getImageData() != null) {
                return post.getImageData();
            }
        }
        throw new java.io.FileNotFoundException("Image not found in database: " + imageName);
    }
}
