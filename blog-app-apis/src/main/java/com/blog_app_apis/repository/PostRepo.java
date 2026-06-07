package com.blog_app_apis.repository;

import com.blog_app_apis.Entity.Category;
import com.blog_app_apis.Entity.Post;
import com.blog_app_apis.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepo extends JpaRepository<Post, Integer> {
    List<Post> findByUser(User user);

    List<Post> findByCategory(Category category);

    List<Post> findByTitleContaining(String title);

    List<Post> findByImageName(String imageName);

    @Query("select p.imageData from Post p where p.imageName = :imageName")
    List<byte[]> findImageDataByImageName(@Param("imageName") String imageName);
}
