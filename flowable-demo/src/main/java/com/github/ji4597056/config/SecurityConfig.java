package com.github.ji4597056.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * spring security config
 *
 * @author Jeffrey
 * @since 2017/4/26 11:13
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 禁用csrf防御
        http.csrf()
            .disable()
            .authorizeRequests()
            .antMatchers("/admin/**", "/flowable/**")
            .hasRole("ADMIN")
            .anyRequest()
            .permitAll()
            .and()
            .httpBasic()
            .and()
            .logout()
            .deleteCookies("JSESSIONID")
            .permitAll();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
            .withUser("admin")
            .password("admin")
            .roles("ADMIN")
            .and()
            .withUser("test")
            .password("test")
            .roles("USER");
    }
}
