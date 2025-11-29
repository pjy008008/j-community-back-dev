package com.pjy008008.j_community.config;

import com.pjy008008.j_community.security.OAuth2SuccessHandler;
import com.pjy008008.j_community.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        String[] swaggerPaths = {
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html"
        };

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(swaggerPaths).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/posts").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/posts/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/posts/*/upvote").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/posts/*/downvote").authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/communities/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/communities").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/communities/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/communities/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/posts/{postId}/comments").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/posts/{postId}/comments").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/comments/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/comments/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/comments/{commentId}/replies").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/comments/*/upvote").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/comments/*/downvote").authenticated()

                        .requestMatchers("/api/notifications/**").authenticated()

                        .requestMatchers(HttpMethod.DELETE, "/api/auth/withdraw").authenticated()
                        .requestMatchers("/api/users/**").authenticated()
                        .anyRequest().authenticated()
                )

                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                )

                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}