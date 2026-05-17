package ttrpg.CharManagementService.infrastructure.security;

import java.io.IOException;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import ttrpg.CharManagementService.application.auth.AuthenticationTokenService;
import ttrpg.CharManagementService.domain.user.UserRepository;

@Component
@RequiredArgsConstructor
public class AccessTokenAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationTokenService authenticationTokenService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            resolveAuthentication(request);
        }

        filterChain.doFilter(request, response);
    }

    private void resolveAuthentication(HttpServletRequest request) {
        var bearerToken = extractBearerToken(request);
        if (bearerToken == null) {
            return;
        }

        try {
            var userId = authenticationTokenService.resolveUserIdByAccessToken(bearerToken);
            if (userId.isEmpty()) {
                return;
            }

            var user = userRepository.findById(userId.get()).orElse(null);
            if (user == null) {
                return;
            }

            var authorities = user.getRoles().stream()
                .map(role -> "ROLE_" + role.name())
                .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());

            var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (RuntimeException exception) {
            SecurityContextHolder.clearContext();
        }
    }

    private String extractBearerToken(HttpServletRequest request) {
        var authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank() || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7).trim();
    }
}
