package com.petsalud.backendpetsalud.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationProvider authenticationProvider,
                                                   RegistroAccesoDenegadoHandler accessDeniedHandler) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        // --- Publico ---
                        .requestMatchers(HttpMethod.POST, "/api/usuarios/registro").permitAll()

                        // --- Solo ADMIN ---
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/usuarios").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/ventas").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/productos/stock-bajo").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/productos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/recompensas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/recompensas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/notificaciones/**").hasRole("ADMIN")

                        // --- VETERINARIO y ADMIN ---
                        .requestMatchers("/api/veterinario/**").hasAnyRole("VETERINARIO", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/notificaciones/**").hasAnyRole("VETERINARIO", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/citas/*/estado").hasAnyRole("VETERINARIO", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/citas/veterinario/**").hasAnyRole("VETERINARIO", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/citas/entre-fechas").hasAnyRole("VETERINARIO", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/expedientes/**").hasAnyRole("VETERINARIO", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/expedientes/**").hasAnyRole("VETERINARIO", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/expedientes/*").hasAnyRole("VETERINARIO", "ADMIN")

                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'"))
                        .frameOptions(frame -> frame.deny())
                )
                .httpBasic(withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UsuarioDetailsService usuarioDetailsService,
                                                         PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
