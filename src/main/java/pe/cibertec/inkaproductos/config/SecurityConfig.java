package pe.cibertec.inkaproductos.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import pe.cibertec.inkaproductos.security.JwtFilter;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:4200"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .csrf(c -> c.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Público
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/stock/eventos").permitAll()

                        // --- REGLAS DE PRODUCTOS REFACTORIZADAS ---
                        // 1. Lectura para todos los usuarios autenticados
                        .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/**").hasAnyRole("ADMIN", "SUPERVISOR", "USUARIO")
                        // 2. Escritura (POST, PUT, DELETE) solo para ADMIN
                        .requestMatchers("/api/productos/**").hasRole("ADMIN")

                        // --- OTRAS REGLAS ---
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/traslados").hasAnyRole("ADMIN", "SUPERVISOR")
                        .requestMatchers("/api/solicitudes/*/aprobar").hasAnyRole("ADMIN", "SUPERVISOR")
                        .requestMatchers("/api/solicitudes/*/rechazar").hasAnyRole("ADMIN", "SUPERVISOR")
                        .requestMatchers("/api/solicitudes/pendientes").hasAnyRole("ADMIN", "SUPERVISOR")
                        .requestMatchers(HttpMethod.POST, "/api/solicitudes").hasAnyRole("ADMIN", "SUPERVISOR", "USUARIO")
                        .requestMatchers("/api/solicitudes/mis").hasAnyRole("ADMIN", "SUPERVISOR", "USUARIO")
                        
                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}