package stasiek.wojcik.wordletrainingproject.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtTokenService {

    private final String ENCRYPTION_KEY;

    public JwtTokenService(final EncryptionKeyLoader encryptionKeyLoader) {
        this.ENCRYPTION_KEY = encryptionKeyLoader.loadClientSecret();
    }

    public String generateToken(final String username) {
        return generateToken(new HashMap<>(), username);
    }

    public String generateToken(final Map<String, Object> claims,
                                final String username) {
        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getEncryptionKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(final String token,
                                final UserDetails userDetails) {
        final var username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(final String token) {
        return extractExpiration(token).before(new Date());
    }

    public String extractUsername(final String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (final MalformedJwtException | SignatureException e) {
            return null;
        }
    }

    public <T> T extractClaim(final String token,
                              final Function<Claims, T> claimsResolver) {
        final var claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Date extractExpiration(final String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(final String token) {
        try {
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getEncryptionKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException ex) {
            return ex.getClaims();
        }
    }

    private Key getEncryptionKey() {
        final var keyBytes = Decoders.BASE64.decode(ENCRYPTION_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
