package ttrpg.CharManagementService.domain.auth.RefreshToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import ttrpg.CharManagementService.domain.exception.InvalidTokenException;
import ttrpg.CharManagementService.domain.user.UserId;

class RefreshTokenTest {

    @Test
    void issuesTokenThatCanBeMatchedAndParsed() {
        var userId = UserId.newId();

        var issued = RefreshToken.issue(
            userId,
            "JUnit",
            InetAddress.getLoopbackAddress(),
            Instant.now().plusSeconds(60)
        );

        assertTrue(issued.refreshToken().matches(issued.rawToken()));
        assertEquals(issued.refreshToken().getId(), RefreshToken.extractTokenId(issued.rawToken()));
        assertEquals(userId, issued.refreshToken().getUserId());
        assertTrue(issued.refreshToken().isValid());
    }

    @Test
    void rejectsMalformedTokenOnExtraction() {
        assertThrows(InvalidTokenException.class, () -> RefreshToken.extractTokenId("broken-token"));
    }

    @Test
    void revokesExpiredTokenOnValidation() {
        var issued = RefreshToken.issue(
            UserId.newId(),
            "JUnit",
            InetAddress.getLoopbackAddress(),
            Instant.now().minusSeconds(1)
        );

        assertFalse(issued.refreshToken().isValid());
        assertTrue(issued.refreshToken().isRevoked());
    }
}
