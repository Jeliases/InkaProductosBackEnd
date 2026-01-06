package pe.cibertec.inkaproductos.config;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
public class SecurityConfig {

    // ============================
    // USERS EN MEMORIA
    // ============================
    @Bean
    public UserDetailsService userDetailsService() {

        PasswordEncoder encoder = passwordEncoder();

        UserDetails admin = User.builder()
                .username("admin@inkaproductos.com")
                .password(encoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        UserDetails user = User.builder()
                .username("user@inkaproductos.com")
                .password(encoder.encode("user123"))
                .roles("USER")
                .build();

        UserDetails ti = User.builder()
                .username("gestionti@inkaproductos.com")
                .password(encoder.encode("ti123"))
                .roles("TI")
                .build();

        return new InMemoryUserDetailsManager(admin, user, ti);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    // AUTH PROVIDER
    @Bean
    public AuthenticationProvider authProvider(UserDetailsService uds) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http,
                                                       AuthenticationProvider provider)
            throws Exception {

        AuthenticationManagerBuilder builder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        builder.authenticationProvider(provider);

        return builder.build();
    }

    // SECURITY FILTER CHAIN

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // CORS
        http.cors(cors -> cors.configurationSource(request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(List.of("http://localhost:4200"));
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(List.of("*"));
            config.setAllowCredentials(true);
            return config;
        }));

        // CSRF OFF para SPA (Angular)
        http.csrf(csrf -> csrf.disable());

        // RULES
        http.authorizeHttpRequests(auth -> auth

                // LOGIN libre
                .requestMatchers("/api/auth/login").permitAll()

                // Preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Públicos
                .requestMatchers(HttpMethod.GET, "/api/categorias/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/almacenes/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/productos/**").permitAll()

                // USER
                .requestMatchers(HttpMethod.POST, "/api/solicitudes").hasRole("USER")
                .requestMatchers(HttpMethod.GET, "/api/solicitudes/mias").hasRole("USER")

                // ADMIN
                .requestMatchers(HttpMethod.POST, "/api/productos/transaccion").hasRole("ADMIN")
                .requestMatchers("/api/aprobaciones/**").hasRole("ADMIN")

                // TI

                .requestMatchers(HttpMethod.GET, "/api/usuarios/**").hasRole("TI")
                .requestMatchers(HttpMethod.POST, "/api/usuarios/**").hasRole("TI")
                .requestMatchers(HttpMethod.PUT, "/api/usuarios/**").hasRole("TI")
                .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasRole("TI")


                // Todo lo demás requiere login
                .anyRequest().authenticated()
        );


        http.httpBasic(Customizer.withDefaults());

        return http.build();
    }


}
