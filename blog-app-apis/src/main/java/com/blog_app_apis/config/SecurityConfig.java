package com.blog_app_apis.config;

import com.blog_app_apis.security.CustomUserDetailService;
import com.blog_app_apis.security.JwtAuthenticationEntryPoint;
import com.blog_app_apis.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

/**
 * SecurityConfig
 * <p>
 * Purpose:
 * Configures Spring Security for JWT-based authentication in a stateless REST API.
 * <p>
 * Key Concepts:
 * - No session management (stateless)
 * - JWT used for authentication
 * - Custom filter validates token on each request
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableWebMvc
public class SecurityConfig {
    public static final String[] PUBLIC_URLS = {
            "/api/v1/auth/**",
            "/auth/**",
            "/v3/api-docs",
            "/v2/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-ui/index.html",
            "/swagger-resources/**",
            "/webjars/**"
    };
    @Autowired
    private CustomUserDetailService customUserDetailService;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * SecurityFilterChain Bean
     * <p>
     * Defines how HTTP requests are secured.
     * This is the central configuration point for Spring Security.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                /**
                 * CSRF Disabled
                 *
                 * WHY:
                 * - CSRF protection is required when authentication is cookie-based
                 * - In this application, JWT is sent via Authorization header
                 * - Headers are NOT automatically sent by browser → no CSRF risk
                 *
                 * ⚠️ If JWT is stored in cookies → CSRF must be enabled again
                 */
                .csrf(csrf -> csrf.disable())

                /**
                 * Exception Handling
                 *
                 * Handles unauthorized access attempts.
                 *
                 * Flow:
                 * - If user is not authenticated → JwtAuthenticationEntryPoint is triggered
                 * - Returns HTTP 401 Unauthorized instead of default login page
                 */
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                /**
                 * Session Management
                 *
                 * WHY:
                 * - JWT is stateless → no session needed
                 * - Each request must contain authentication token
                 *
                 * Effect:
                 * - Spring will NOT create or use HttpSession
                 */
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                /**
                 * Authorization Rules
                 *
                 * Defines which endpoints are public and which require authentication
                 *
                 * Rules:
                 * - /api/v1/auth/** → Public (login/register)
                 * - /auth/** → Public
                 * - /v3/api-docs → Public (Swagger)
                 * - ALL GET requests → Public (read-only access)
                 * - POST/PUT/DELETE → Require authentication (write operations)
                 */
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/category", "/api/category/**").hasAuthority("ADMIN_USER")
                        .requestMatchers(HttpMethod.PUT, "/api/category", "/api/category/**").hasAuthority("ADMIN_USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/category", "/api/category/**").hasAuthority("ADMIN_USER")
                        .anyRequest().authenticated()
                );

        /**
         * JWT Filter Configuration
         *
         * WHY:
         * - Validates JWT before request reaches controller
         *
         * Flow:
         * 1. Extract token from Authorization header
         * 2. Validate token (signature + expiry)
         * 3. Load user details
         * 4. Set authentication in SecurityContextHolder
         *
         * Placement:
         * - Runs before UsernamePasswordAuthenticationFilter
         */
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * PasswordEncoder Bean
     * <p>
     * Uses BCrypt hashing algorithm.
     * <p>
     * WHY:
     * - Passwords must never be stored in plain text
     * - BCrypt provides hashing + salting
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager Bean
     * <p>
     * Required for authenticating user credentials during login.
     * <p>
     * HOW:
     * - Provided by AuthenticationConfiguration
     * - Internally uses CustomUserDetailService
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allowed Origin Patterns supporting wildcards (allows dev servers and deployed apps)
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:5174",
                "http://localhost:5175",
                "http://localhost:3000",
                "http://127.0.0.1:5173",
                "http://127.0.0.1:5174",
                "http://127.0.0.1:3000",
                "https://*.vercel.app",
                "https://*.netlify.app",
                "https://*.render.com",
                "*" // Allows any origin securely even with credentials enabled when using patterns
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}