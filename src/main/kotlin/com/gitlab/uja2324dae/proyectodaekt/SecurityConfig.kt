package com.gitlab.uja2324dae.proyectodaekt

import com.gitlab.uja2324dae.proyectodaekt.service.UserDetailsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher

@Configuration
@EnableWebSecurity
class SecurityConfig(private val userDetailsService: UserDetailsService) {
    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager =
        authenticationConfiguration.authenticationManager

    @Bean
    fun bCryptPasswordEncoder(): BCryptPasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun filterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        val authenticationManagerBuilder = httpSecurity.getSharedObject(AuthenticationManagerBuilder::class.java)
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder())
        val authenticationManager = authenticationManagerBuilder.build()
        httpSecurity
            .authorizeHttpRequests {
                it
                    .requestMatchers(antMatcher(HttpMethod.POST, "/user/")).permitAll()
                    .requestMatchers(antMatcher(HttpMethod.GET, "/sharedRides/")).permitAll()
                    .requestMatchers(antMatcher("/**")).authenticated()
            }
            .authenticationManager(authenticationManager)
            .csrf { it.disable() }
            .cors { it.disable() }
            .httpBasic { it.realmName("carpooling") }

        return httpSecurity.build()
    }
}