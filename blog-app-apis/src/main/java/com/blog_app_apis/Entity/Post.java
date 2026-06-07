package com.blog_app_apis.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "posts")
@Getter
@Setter
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer postId;
    @Column(name = "post_title", nullable = false)
    private String title;
    @Column(name = "post_content", length = 10000, nullable = false)
    private String content;

    private String imageName;

    @Column(name = "image_data")
    private byte[] imageData;

    private Date addDate;

    @ManyToOne
    @JoinColumn(name = "category_Id")
    private Category category;

    @ManyToOne
    @JoinColumn
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private Set<Comment> comments = new HashSet<>();


}
