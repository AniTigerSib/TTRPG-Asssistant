package ttrpg.CharManagementService.domain.campaign;

public enum CampaignVisibility {
    PRIVATE,
    INVITE_ONLY,
    PUBLIC;

    public static CampaignVisibility fromDatabaseValue(String value) {
        return CampaignVisibility.valueOf(value.trim().toUpperCase());
    }

    public String toDatabaseValue() {
        return name().toLowerCase();
    }
}
