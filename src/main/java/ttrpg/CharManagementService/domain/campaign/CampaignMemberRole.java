package ttrpg.CharManagementService.domain.campaign;

public enum CampaignMemberRole {
    GM,
    PLAYER;

    public static CampaignMemberRole fromDatabaseValue(String value) {
        return CampaignMemberRole.valueOf(value.trim().toUpperCase());
    }

    public String toDatabaseValue() {
        return name();
    }
}
