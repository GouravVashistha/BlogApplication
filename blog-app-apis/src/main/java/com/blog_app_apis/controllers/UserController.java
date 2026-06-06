package com.blog_app_apis.controllers;

import com.blog_app_apis.dtos.ApiResponse;
import com.blog_app_apis.dtos.UserDTO;
import com.blog_app_apis.exceptions.InvalidMailException;
import com.blog_app_apis.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping(path = "/createUser")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) throws Exception {
        UserDTO createUserDTO = this.userService.createUser(userDTO);
        return new ResponseEntity<>(createUserDTO, HttpStatus.CREATED);
    }

    @PutMapping("/updateUser/{userId}")
    public ResponseEntity<UserDTO> updateUser(@Valid @RequestBody UserDTO userDTO,
            @PathVariable("userId") Integer userId) throws InvalidMailException {
        UserDTO updateUser = this.userService.updateUser(userDTO, userId);
        return ResponseEntity.ok(updateUser);
    }

    // @PreAuthorize("hasRole('ADMIN_USER')")
    @DeleteMapping("/deleteUser/{userId}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable("userId") Integer userId) {
        this.userService.deleteUser(userId);
        return new ResponseEntity<ApiResponse>(new ApiResponse("User Deleted Successfully", true), HttpStatus.OK);
    }

    // @PreAuthorize("hasRole('NORMAL_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping("/AllUsers")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(this.userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getSingleUser(@PathVariable("userId") Integer userId) {
        return ResponseEntity.ok(this.userService.getUserById(userId));
    }

}
