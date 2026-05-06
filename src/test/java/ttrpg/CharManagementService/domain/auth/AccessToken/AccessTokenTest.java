package ttrpg.CharManagementService.domain.auth.AccessToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import ttrpg.CharManagementService.domain.exception.InvalidTokenException;
import ttrpg.CharManagementService.domain.user.UserId;

class AccessTokenTest {

    @Test
    void issuesTokenThatCanBeMatchedAndParsed() {
        var userId = UserId.newId();

        var issued = AccessToken.issue(userId, Instant.now().plusSeconds(60));

        assertTrue(issued.accessToken().matches(issued.rawToken()));
        assertEquals(issued.accessToken().id(), AccessToken.extractTokenId(issued.rawToken()));
        assertEquals(userId, issued.accessToken().userId());
    }

    @Test
    void rejectsMalformedTokenOnExtraction() {
        assertThrows(InvalidTokenException.class, () -> AccessToken.extractTokenId("broken-token"));
    }

    @Test
    void reportsExpiredToken() {
        var issued = AccessToken.issue(UserId.newId(), Instant.now().minusSeconds(1));

        assertTrue(issued.accessToken().isExpired());
        assertFalse(issued.accessToken().matches("another-token"));
    }
}
