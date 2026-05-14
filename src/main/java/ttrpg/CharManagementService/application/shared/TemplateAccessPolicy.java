package ttrpg.CharManagementService.application.shared;

import org.springframework.stereotype.Component;

import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplate;
import ttrpg.CharManagementService.domain.chartemplate.CharacterTemplateVisibility;
import ttrpg.CharManagementService.domain.exception.AccessDeniedOperationException;
import ttrpg.CharManagementService.domain.user.User;
import ttrpg.CharManagementService.domain.user.UserRole;

@Component
public class TemplateAccessPolicy {

    public boolean canView(CharacterTemplate template, User currentUser) {
        if (template.getVisibility() == CharacterTemplateVisibility.VISIBLE) {
            return true;
        }
        return hasTemplateManagementRole(currentUser);
    }

    public void assertCanManage(User currentUser) {
        if (!hasTemplateManagementRole(currentUser)) {
            throw new AccessDeniedOperationException("Template management requires elevated role");
        }
    }

    private boolean hasTemplateManagementRole(User currentUser) {
        return currentUser != null && currentUser.hasAnyRole(UserRole.CREATOR, UserRole.ADMIN, UserRole.SUPERADMIN);
    }
}
