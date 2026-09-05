package com.Deteccion_estrabismo.backend.Service;

import com.Deteccion_estrabismo.backend.Entities.Responsable;
import com.Deteccion_estrabismo.backend.Entities.Usuarios;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Generar token simple
    public String generateToken(Usuarios userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    // Generar token con claims extra
    public String generateToken(Map<String, Object> extraClaims, Usuarios userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userDetails.getId());
        claims.put("correo", userDetails.getCorreo());
        claims.put("rol", userDetails.getRol().name());
        claims.put("nombres", userDetails.getNombres());
        claims.put("apellidos", userDetails.getApellidos());
        claims.put("documentoIdentidad", userDetails.getDocumentoIdentidad());
        claims.put("tipoDocumento",
                userDetails.getTipoDocumento() != null ? userDetails.getTipoDocumento().name() : null);
        claims.put("numeroTele", userDetails.getNumeroTele());

        // Claims específicos de Responsable
        if (userDetails instanceof Responsable) {
            Responsable responsable = (Responsable) userDetails;
            claims.put("parentesco", responsable.getParentesco());
            claims.put("ocupacion", responsable.getOcupacion());
            claims.put("ciudadResidencia", responsable.getCiudadResidencia());
        }

        // Agregar claims adicionales si se proporcionan
        if (extraClaims != null) {
            claims.putAll(extraClaims);
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getCorreo()) // Usamos el correo como identificador único
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Validar token
    public boolean isTokenValid(String token, Usuarios userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getCorreo())) && !isTokenExpired(token);
    }

}