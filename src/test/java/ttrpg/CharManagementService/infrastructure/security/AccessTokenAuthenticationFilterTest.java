package ttrpg.CharManagementService.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import ttrpg.CharManagementService.application.auth.AuthenticationTokenService;
import ttrpg.CharManagementService.domain.user.User;
import ttrpg.CharManagementService.domain.user.UserRepository;

class AccessTokenAuthenticationFilterTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesRequestWhenBearerTokenIsValid() throws Exception {
        var tokenService = mock(AuthenticationTokenService.class);
        var userRepository = mock(UserRepository.class);
        var user = User.create("test@example.com", "hero", "hashed:Password1");

        when(tokenService.resolveUserIdByAccessToken("access-token")).thenReturn(Optional.of(user.getId()));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        var filter = new AccessTokenAuthenticationFilter(tokenService, userRepository);
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer access-token");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertSame(user, authentication.getPrincipal());
        assertEquals("ROLE_USER", authentication.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void leavesRequestUnauthenticatedWhenBearerTokenIsMissing() throws Exception {
        var filter = new AccessTokenAuthenticationFilter(
            mock(AuthenticationTokenService.class),
            mock(UserRepository.class)
        );

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void leavesRequestUnauthenticatedWhenTokenCannotBeResolved() throws Exception {
        var tokenService = mock(AuthenticationTokenService.class);
        when(tokenService.resolveUserIdByAccessToken("bad-token")).thenReturn(Optional.empty());

        var filter = new AccessTokenAuthenticationFilter(tokenService, mock(UserRepository.class));
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad-token");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
