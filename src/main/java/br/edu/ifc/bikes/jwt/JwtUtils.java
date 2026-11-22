package br.edu.ifc.bikes.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class JwtUtils {

    public static final String JWT_BEARER = "Bearer ";
    public static final String JWT_AUTHORIZATION = "Authorization";
    public static final String SECRET_KEY = "0123456789-0123456789-0123456789";
    public static final long EXPIRE_DAYS = 0;
    public static final long EXPIRE_HOURS = 12;
    public static final long EXPIRE_MINUTES = 0;

    private JwtUtils(){}

    //criação do token
    private static SecretKey generateKey(){
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    private static Date toExpireDate(Date start){
        LocalDateTime dateTime = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime end = dateTime.plusDays(EXPIRE_DAYS).plusHours(EXPIRE_HOURS).plusMinutes(EXPIRE_MINUTES);
        return Date.from(end.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static JwtToken createToken(String username, String role){
        Date issuedAt = new Date();
        Date limit = toExpireDate(issuedAt);

        String token = Jwts.builder()
                .header().add("typ", "JWT")
                .and()
                .subject(username)
                .issuedAt(issuedAt)
                .expiration(limit)
                .signWith(generateKey())
                .claim("role", role)
                .compact();

        return new JwtToken(token);
    }

    private static String refactorToken(String token){
        String normalized = token == null ? "" : token.trim();
        while (normalized.toLowerCase(java.util.Locale.ROOT).startsWith("bearer ")) {
            normalized = normalized.substring(JWT_BEARER.length()).trim();
        }
        return normalized;
    }

    private static Claims getClaimsFromToken(String token){
        try{
            return Jwts.parser()
                    .verifyWith(generateKey())
                    .build()
                    .parseSignedClaims(refactorToken(token)).getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public static String getUsernameFromToken(String token){
        Claims claims = getClaimsFromToken(token);
        return claims == null ? null : claims.getSubject();
    }

    public static boolean isTokenValid(String token){
        Claims claims = getClaimsFromToken(token);
        return claims != null && claims.getSubject() != null && !claims.getSubject().isBlank();
    }
}