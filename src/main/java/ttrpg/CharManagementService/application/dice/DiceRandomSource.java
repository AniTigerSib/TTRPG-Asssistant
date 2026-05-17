package ttrpg.CharManagementService.application.dice;

public interface DiceRandomSource {

    int nextInt(int minInclusive, int maxInclusive);
}
