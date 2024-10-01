package com.ecommerce.config;

import com.ecommerce.security.DatabaseAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration  // Indicates this class is a configuration class for Spring's IoC container.
@EnableWebSecurity  // Enables Spring Security for the application.
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private DatabaseAuthenticationProvider authenticationProvider;
    // This injects the custom authentication provider which uses database-backed authentication.

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // Configures authentication to use the custom DatabaseAuthenticationProvider
        auth.authenticationProvider(authenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Configures HTTP security for various endpoints and security features.
        http
                .authorizeRequests()
                // Allows GET requests to the root, and static resources like JS, CSS, and images without authentication.
                .antMatchers(HttpMethod.GET, "/", "/js/**", "/css/**", "/images/**").permitAll()
                // Allows unrestricted access to the home, login, registration, and password recovery pages.
                .antMatchers("/", "/login", "/register", "/resources/**", "/recover-password/**").permitAll()
                // Any other request must be authenticated.
                .anyRequest().authenticated()
                .and()
                // Enables form-based login and specifies the login page and default success URL after login.
                .formLogin()
                .loginPage("/login")  // Custom login page.
                .defaultSuccessUrl("/home", true)  // Redirect to "/home" upon successful login.
                .permitAll()
                .and()
                // Enables "Remember Me" functionality for login sessions with a custom parameter and validity period.
                .rememberMe()
                .rememberMeParameter("rememberMe")  // Parameter name for "remember me" functionality.
                .tokenValiditySeconds(86400)  // Token is valid for one day (86400 seconds).
                .key("uniqueAndSecret")  // Key used to secure the "remember me" token.
                .and()
                // Configures logout behavior, allowing anyone to access the logout page.
                .logout()
                .permitAll()
                .and()
                // Disables CSRF protection, as it may not be needed (for example, if using stateless authentication like JWT).
                .csrf().disable()
                // Allows the use of iframes from the same origin (needed in cases like using embedded content).
                .headers().frameOptions().sameOrigin();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Defines a password encoder bean using BCrypt to hash passwords.
        return new BCryptPasswordEncoder();
    }
}