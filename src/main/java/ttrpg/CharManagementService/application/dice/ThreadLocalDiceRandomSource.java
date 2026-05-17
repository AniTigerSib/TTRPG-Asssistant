package ttrpg.CharManagementService.application.dice;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

@Component
public class ThreadLocalDiceRandomSource implements DiceRandomSource {

    @Override
    public int nextInt(int minInclusive, int maxInclusive) {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
    }
}
