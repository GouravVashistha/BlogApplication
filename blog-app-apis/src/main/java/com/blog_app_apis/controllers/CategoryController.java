package com.blog_app_apis.controllers;

import com.blog_app_apis.dtos.ApiResponse;
import com.blog_app_apis.dtos.CategroyDTO;
import com.blog_app_apis.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // CREATE
    @PostMapping
    public ResponseEntity<CategroyDTO> createCategory(@Valid @RequestBody CategroyDTO categroyDTO) {
        CategroyDTO createdCategory = categoryService.createCategory(categroyDTO);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    // UPDATE
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategroyDTO> updateCategory(
            @Valid @RequestBody CategroyDTO categroyDTO,
            @PathVariable("categoryId") Integer categoryId) {

        CategroyDTO updatedCategory = categoryService.updateCategory(categroyDTO, categoryId);
        return ResponseEntity.ok(updatedCategory);
    }

    // DELETE
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable("categoryId") Integer categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(
                new ApiResponse("Category deleted successfully!", true));
    }

    // GET BY ID
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategroyDTO> getCategory(@PathVariable("categoryId") Integer categoryId) {
        return ResponseEntity.ok(categoryService.getCategory(categoryId));
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<CategroyDTO>> getAllCategory() {
        return ResponseEntity.ok(categoryService.getCategories());
    }
}