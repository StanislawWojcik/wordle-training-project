package stasiek.wojcik.wordletrainingproject.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtTokenService {

  // TODO: move it somewhere?
  @Value("${encryption.key}")
  private String ENCRYPTION_KEY;

  public String generateToken(final UserDetails userDetails) {
    return generateToken(new HashMap<>(), userDetails);
  }

  public String generateToken(final Map<String, Object> claims,
                              final UserDetails userDetails) {
    return Jwts
        .builder()
        .setClaims(claims)
        .setSubject(userDetails.getUsername())
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
    return extractClaim(token, Claims::getSubject);
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
