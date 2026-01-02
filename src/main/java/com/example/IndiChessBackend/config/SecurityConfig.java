package com.example.IndiChessBackend.config;

import com.example.IndiChessBackend.service.UserDetailService;
import org.hibernate.Session;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailService userDetailService;

    public SecurityConfig(UserDetailService userDetailService){
        this.userDetailService = userDetailService;
    }

    @Bean
    public SecurityFilterChain newSpringSecurityFilterChain(HttpSecurity http) throws Exception{

        // RULE 1: Who goes where?
        return http
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/hello").permitAll()
//                .requestMatchers("/world").permitAll()
//                .requestMatchers("/all").permitAll()
//                .requestMatchers("/hidden-resource").denyAll()
                                .anyRequest().authenticated()
                )
                // RULE 2: How to login?
                .formLogin(Customizer.withDefaults())
//                form -> form
//                .loginPage("/login")
//                .defaultSuccessUrl("/hello", true)
//                .permitAll()
                //)// RULE 3: How to logout?
//                .logout(logout -> logout
//                        .logoutUrl("/logout")    // exit door
//                        .logoutSuccessUrl("/") // Wave goodbye page
//                        .permitAll()
//                )
                // RULE 4: Stop dragon spies!
                .csrf(csrf -> csrf.disable())//protection (usually ON!)

                .build();  // BUILD THE SECURITY SYSTEM!
    }



    @Bean
    public PasswordEncoder passwordEncoder() {
        return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance(); // Use BCryptPasswordEncoder for password hashing
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        // Set the UserDetailsService and password encoder
        authenticationManagerBuilder
                .userDetailsService(userDetailService);


        // Return the AuthenticationManager object
        return authenticationManagerBuilder.build();
    }

}