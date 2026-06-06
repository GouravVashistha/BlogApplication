package com.blog_app_apis.serviceImpl;

import com.blog_app_apis.Entity.Role;
import com.blog_app_apis.Entity.User;
import com.blog_app_apis.config.AppConstants;
import com.blog_app_apis.dtos.UserDTO;
import com.blog_app_apis.exceptions.InvalidMailException;
import com.blog_app_apis.exceptions.ResourceNotFoundException;
import com.blog_app_apis.repository.RoleRepository;
import com.blog_app_apis.repository.UserRepo;
import com.blog_app_apis.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * UserServiceImpl - User management service with comprehensive logging
 * 
 * Purpose: Handle user CRUD operations, registration, and user-related business logic
 * 
 * Logs every operation for debugging and monitoring purposes:
 * - All user operations (create, read, update, delete)
 * - User registration with password encryption
 * - Error cases with detailed context
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;


    @Override
    public UserDTO createUser(UserDTO userDTO) throws InvalidMailException {
        log.info("=== Starting User Creation Process ===");
        log.info("Creating user with email: {}", userDTO.getEmail());
        log.debug("User details - Name: {}, About: {}", userDTO.getName(), userDTO.getAbout());

        User user = modelMapper.map(userDTO, User.class);
        log.debug("UserDTO mapped to User entity successfully");
        
        try {
            log.debug("Attempting to save user to database");
            User savedUser = userRepo.save(user);
            log.info("User created successfully with ID: {}", savedUser.getId());
            log.debug("User creation completed for email: {}", userDTO.getEmail());
            
            return modelMapper.map(user, UserDTO.class);
        } catch (DataIntegrityViolationException ex) {
            log.error("❌ User creation failed - Email already exists: {}", userDTO.getEmail());
            log.debug("Exception details: {}", ex.getMessage());
            throw new InvalidMailException("Email already exists");
        } catch (Exception ex) {
            log.error("❌ Unexpected error during user creation: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public UserDTO updateUser(UserDTO userDTO, Integer userId) throws InvalidMailException {
        log.info("=== Starting User Update Process ===");
        log.info("Updating user with ID: {}", userId);
        log.debug("Update data - Email: {}, Name: {}", userDTO.getEmail(), userDTO.getName());

        try {
            // Check if email is already in use by another user
            if (userRepo.findByEmail(userDTO.getEmail())
                    .filter(u -> !u.getId().equals(userId))
                    .isPresent()) {
                log.warn("❌ Update failed - Email already in use: {} (user ID: {})", userDTO.getEmail(), userId);
                throw new InvalidMailException("Email already in use");
            }

            User user = getUserOrThrow(userId);
            log.debug("User found in database: {}", user.getEmail());

            // Update fields
            if (userDTO.getName() != null) {
                log.debug("Updating name from '{}' to '{}'", user.getName(), userDTO.getName());
                user.setName(userDTO.getName());
            }

            if (userDTO.getEmail() != null) {
                log.debug("Updating email from '{}' to '{}'", user.getEmail(), userDTO.getEmail());
                user.setEmail(userDTO.getEmail());
            }

            if (userDTO.getPassword() != null) {
                log.debug("Updating password for user: {}", userId);
                user.setPassword(userDTO.getPassword());
            }

            if (userDTO.getAbout() != null) {
                log.debug("Updating about from '{}' to '{}'", user.getAbout(), userDTO.getAbout());
                user.setAbout(userDTO.getAbout());
            }

            log.debug("Saving updated user to database");
            User savedUser = userRepo.save(user);
            log.info("✅ User updated successfully - ID: {}, Email: {}", userId, savedUser.getEmail());
            
            return modelMapper.map(savedUser, UserDTO.class);
        } catch (DataIntegrityViolationException ex) {
            log.error("❌ Update failed due to data integrity violation for user ID: {}", userId);
            log.debug("Exception: {}", ex.getMessage());
            throw new InvalidMailException("Email already in use");
        } catch (ResourceNotFoundException ex) {
            log.error("❌ Update failed - User not found with ID: {}", userId);
            throw ex;
        } catch (Exception ex) {
            log.error("❌ Unexpected error during user update - User ID: {}, Error: {}", userId, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public UserDTO getUserById(Integer userId) {
        log.debug("Fetching user with ID: {}", userId);
        try {
            User user = getUserOrThrow(userId);
            log.debug("✅ User found - ID: {}, Email: {}", user.getId(), user.getEmail());
            return modelMapper.map(user, UserDTO.class);
        } catch (ResourceNotFoundException ex) {
            log.error("❌ User not found with ID: {}", userId);
            throw ex;
        }
    }

    @Override
    public List<UserDTO> getAllUsers() {
        log.info("Fetching all users from database");
        try {
            List<UserDTO> users = userRepo.findAll()
                    .stream()
                    .map(u -> modelMapper.map(u, UserDTO.class))
                    .toList();
            log.info("✅ Successfully fetched {} users", users.size());
            log.debug("User count breakdown - Total: {}", users.size());
            return users;
        } catch (Exception ex) {
            log.error("❌ Error fetching all users: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public void deleteUser(Integer userId) {
        log.info("=== Starting User Deletion Process ===");
        log.info("Deleting user with ID: {}", userId);
        
        try {
            User user = getUserOrThrow(userId);
            log.debug("User found for deletion - ID: {}, Email: {}", user.getId(), user.getEmail());
            
            this.userRepo.delete(user);
            log.info("✅ User deleted successfully - ID: {}, Email: {}", userId, user.getEmail());
        } catch (ResourceNotFoundException ex) {
            log.error("❌ Deletion failed - User not found with ID: {}", userId);
            throw ex;
        } catch (Exception ex) {
            log.error("❌ Unexpected error during user deletion - ID: {}, Error: {}", userId, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public UserDTO registerNewUser(UserDTO userDto) {
        User user = this.modelMapper.map(userDto, User.class);
        //encoded password
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        //roles
        Role role = this.roleRepository.findById(AppConstants.NORMAL_USER).get();

        user.getRoles().add(role);

        User newuser = this.userRepo.save(user);
        return this.modelMapper.map(newuser, UserDTO.class);
    }
    @Override
    public UserDTO assignAdminRole(Integer userId) {
        log.info("Promoting user with ID: {} to Admin role", userId);
        User user = getUserOrThrow(userId);
        
        Role adminRole = this.roleRepository.findById(AppConstants.ADMIN_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", AppConstants.ADMIN_USER));
        
        user.getRoles().add(adminRole);
        User updatedUser = this.userRepo.save(user);
        
        log.info("✅ User with ID: {} successfully promoted to Admin", userId);
        return this.modelMapper.map(updatedUser, UserDTO.class);
    }


    private User getUserOrThrow(Integer userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
}
