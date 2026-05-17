package ttrpg.CharManagementService.integration.user;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ttrpg.CharManagementService.integration.support.IntegrationTestSupport;

class UserFlowIntegrationTest extends IntegrationTestSupport {

    @Test
    void changePasswordFlowWorks() throws Exception {
        var user = newTestUser();
        var session = registerAndLogin(user);
        var changedPassword = "ChangedPass2";

        var changePasswordResponse = changePassword(session.accessToken(), user.password(), changedPassword);

        assertEquals(204, changePasswordResponse.statusCode(), changePasswordResponse.describe());

        var failedLoginResponse = login(user.email(), user.password());
        assertEquals(401, failedLoginResponse.statusCode(), failedLoginResponse.describe());
        assertEquals("INVALID_CREDENTIALS", failedLoginResponse.body().path("code").asText(), failedLoginResponse.describe());

        var reloginResponse = login(user.email(), changedPassword);
        assertEquals(200, reloginResponse.statusCode(), reloginResponse.describe());
    }

    @Test
    void changeUsernameFlowWorks() throws Exception {
        var user = newTestUser();
        var session = registerAndLogin(user);
        var changedUsername = "upd" + user.username().substring(0, Math.min(user.username().length(), 12));

        var changeUsernameResponse = changeUsername(session.accessToken(), changedUsername);

        assertEquals(200, changeUsernameResponse.statusCode(), changeUsernameResponse.describe());
        assertEquals(changedUsername, changeUsernameResponse.body().path("username").asText(),
            changeUsernameResponse.describe());

        var oldUsernameLoginResponse = login(user.username(), user.password());
        assertEquals(401, oldUsernameLoginResponse.statusCode(), oldUsernameLoginResponse.describe());
        assertEquals("INVALID_CREDENTIALS", oldUsernameLoginResponse.body().path("code").asText(),
            oldUsernameLoginResponse.describe());

        var reloginResponse = login(changedUsername, user.password());
        assertEquals(200, reloginResponse.statusCode(), reloginResponse.describe());
    }
}
