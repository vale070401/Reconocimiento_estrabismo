package com.Deteccion_estrabismo.backend.Jwt;

import com.Deteccion_estrabismo.backend.Repository.UsuariosRepository;
import com.Deteccion_estrabismo.backend.Service.JwtService;
import com.Deteccion_estrabismo.backend.Entities.Usuarios;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.http.HttpHeaders;

import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuariosRepository usuariosRepository;

    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService,
            UsuariosRepository usuariosRepository) {
        this.jwtService = jwtService;
        this.usuariosRepository = usuariosRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, java.io.IOException {

        String path = request.getServletPath();

        // Saltar validación para endpoints públicos
        if (path.startsWith("/auth/") ||
                path.startsWith("/api/pdf/") ||
                path.startsWith("/api/evaluaciones/")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = getTokenFromRequest(request);

        if (token != null) {
            // Obtener correo o username del token
            String correo = jwtService.extractUsername(token);

            // Recuperar usuario de la base de datos
            Usuarios usuario = usuariosRepository.findByCorreo(correo)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Crear Authentication y ponerlo en el contexto
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    usuario.getCorreo(), null,
                    List.of(new SimpleGrantedAuthority(usuario.getRol().name())));

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
