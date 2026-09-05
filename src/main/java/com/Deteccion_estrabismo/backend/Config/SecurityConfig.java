package com.Deteccion_estrabismo.backend.Config;

import com.Deteccion_estrabismo.backend.Jwt.JwtAuthenticationFilter;
import com.Deteccion_estrabismo.backend.Service.UsuariosService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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
    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;
    @Autowired
    private com.Deteccion_estrabismo.backend.Service.CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())// Desactivar CSRF(Cross-Site Request Forgery) para pruebas con POSTMAN
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll() // publico
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/responsable/**").hasRole("RESPONSABLE")
                        .requestMatchers("/api/evaluaciones/**").permitAll()
                        .requestMatchers("/api/pdf/**").permitAll()
                        .requestMatchers("/paciente/**").hasRole("PACIENTE")
                        .anyRequest().authenticated()// el resto pide login
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(login -> login.disable())// quitar el login por formulario
                .httpBasic(httpBasic -> httpBasic.disable());// Quitar el basic Auth mientras se usa
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
