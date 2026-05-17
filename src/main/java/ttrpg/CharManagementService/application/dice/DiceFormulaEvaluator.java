package ttrpg.CharManagementService.application.dice;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import ttrpg.CharManagementService.domain.exception.InvalidInputException;
import ttrpg.CharManagementService.domain.shared.Checkers;

@Component
public class DiceFormulaEvaluator {

    private final DiceRandomSource randomSource;

    public DiceFormulaEvaluator(DiceRandomSource randomSource) {
        this.randomSource = randomSource;
    }

    public DiceRollResult evaluate(String formula, java.util.UUID characterId, Function<String, Integer> modifierResolver) {
        var parsedTerms = parse(Checkers.requireStringNonBlank(formula, "formula"));
        var results = new ArrayList<DiceTermResult>(parsedTerms.size());
        var resolvedParts = new ArrayList<String>(parsedTerms.size());
        var total = 0;

        for (int index = 0; index < parsedTerms.size(); index++) {
            var term = parsedTerms.get(index);
            var evaluation = evaluateTerm(term, modifierResolver);
            results.add(evaluation);
            total += evaluation.value();
            resolvedParts.add(formatResolvedPart(evaluation, index == 0));
        }

        return new DiceRollResult(
            characterId,
            formula.trim(),
            String.join(" ", resolvedParts).trim(),
            total,
            List.copyOf(results)
        );
    }

    private List<ParsedTerm> parse(String formula) {
        var compact = formula.replaceAll("\\s+", "");
        if (compact.isBlank()) {
            throw InvalidInputException.blankField("formula");
        }

        var parsedTerms = new ArrayList<ParsedTerm>();
        var index = 0;
        var sign = 1;
        var expectingTerm = true;

        while (index < compact.length()) {
            var current = compact.charAt(index);
            if (expectingTerm) {
                if (current == '+' || current == '-') {
                    sign = current == '-' ? -1 : 1;
                    index++;
                    if (index >= compact.length()) {
                        throw InvalidInputException.invalidValue("formula", "formula must not end with an operator");
                    }
                }

                var parsed = parseTerm(compact, index);
                parsedTerms.add(parsed.withSign(sign));
                index = parsed.nextIndex();
                sign = 1;
                expectingTerm = false;
                continue;
            }

            if (current != '+' && current != '-') {
                throw InvalidInputException.invalidValue("formula", "formula must separate terms with + or -");
            }
            sign = current == '-' ? -1 : 1;
            index++;
            expectingTerm = true;
        }

        if (expectingTerm) {
            throw InvalidInputException.invalidValue("formula", "formula must not end with an operator");
        }

        return List.copyOf(parsedTerms);
    }

    private ParsedTerm parseTerm(String compact, int index) {
        var current = compact.charAt(index);
        if (current == '[') {
            var endIndex = compact.indexOf(']', index);
            if (endIndex < 0) {
                throw InvalidInputException.invalidValue("formula", "formula contains an unterminated modifier reference");
            }
            var placeholder = compact.substring(index + 1, endIndex).trim();
            if (placeholder.isBlank()) {
                throw InvalidInputException.invalidValue("formula", "formula contains an empty modifier reference");
            }
            return new ParsedTerm(ParsedTermType.PLACEHOLDER, "[" + placeholder + "]", placeholder, 0, 0, index, endIndex + 1, 1);
        }

        if (current == 'd' || current == 'D' || Character.isDigit(current)) {
            return parseNumericTerm(compact, index);
        }

        throw InvalidInputException.invalidValue("formula", "formula contains an unsupported term");
    }

    private ParsedTerm parseNumericTerm(String compact, int startIndex) {
        var index = startIndex;
        while (index < compact.length() && Character.isDigit(compact.charAt(index))) {
            index++;
        }

        var leadingDigits = compact.substring(startIndex, index);
        if (index < compact.length() && (compact.charAt(index) == 'd' || compact.charAt(index) == 'D')) {
            var count = leadingDigits.isEmpty() ? 1 : parsePositiveInt(leadingDigits, "dice count");
            index++;
            if (index >= compact.length()) {
                throw InvalidInputException.invalidValue("formula", "dice notation must include a side count");
            }

            if (compact.charAt(index) == 'f' || compact.charAt(index) == 'F') {
                return new ParsedTerm(
                    ParsedTermType.FATE_DICE,
                    (leadingDigits.isEmpty() ? "d" : leadingDigits + "d") + "F",
                    "",
                    count,
                    0,
                    startIndex,
                    index + 1,
                    1
                );
            }

            var sideStart = index;
            while (index < compact.length() && Character.isDigit(compact.charAt(index))) {
                index++;
            }
            if (sideStart == index) {
                throw InvalidInputException.invalidValue("formula", "dice notation must include a numeric side count or F");
            }

            var sides = parsePositiveInt(compact.substring(sideStart, index), "dice sides");
            if (sides < 2) {
                throw InvalidInputException.invalidValue("formula", "dice side count must be at least 2");
            }

            return new ParsedTerm(
                ParsedTermType.DICE,
                (leadingDigits.isEmpty() ? "d" : leadingDigits + "d") + sides,
                "",
                count,
                sides,
                startIndex,
                index,
                1
            );
        }

        if (leadingDigits.isEmpty()) {
            throw InvalidInputException.invalidValue("formula", "formula contains an unsupported term");
        }

        return new ParsedTerm(
            ParsedTermType.CONSTANT,
            leadingDigits,
            leadingDigits,
            0,
            0,
            startIndex,
            index,
            parsePositiveInt(leadingDigits, "modifier")
        );
    }

    private DiceTermResult evaluateTerm(ParsedTerm term, Function<String, Integer> modifierResolver) {
        return switch (term.type()) {
            case DICE -> evaluateStandardDice(term);
            case FATE_DICE -> evaluateFateDice(term);
            case CONSTANT -> new DiceTermResult(
                DiceTermKind.CONSTANT,
                term.notation(),
                term.rawValue(),
                term.sign(),
                List.of(),
                term.sign() * term.constantValue()
            );
            case PLACEHOLDER -> evaluatePlaceholder(term, modifierResolver);
        };
    }

    private DiceTermResult evaluateStandardDice(ParsedTerm term) {
        var rolls = new ArrayList<Integer>(term.count());
        var subtotal = 0;
        for (int rollIndex = 0; rollIndex < term.count(); rollIndex++) {
            var rolledValue = randomSource.nextInt(1, term.sides());
            rolls.add(rolledValue);
            subtotal += rolledValue;
        }
        return new DiceTermResult(
            DiceTermKind.DICE,
            term.notation(),
            term.notation(),
            term.sign(),
            List.copyOf(rolls),
            term.sign() * subtotal
        );
    }

    private DiceTermResult evaluateFateDice(ParsedTerm term) {
        var rolls = new ArrayList<Integer>(term.count());
        var subtotal = 0;
        for (int rollIndex = 0; rollIndex < term.count(); rollIndex++) {
            var rolledValue = randomSource.nextInt(-1, 1);
            rolls.add(rolledValue);
            subtotal += rolledValue;
        }
        return new DiceTermResult(
            DiceTermKind.DICE,
            term.notation(),
            term.notation(),
            term.sign(),
            List.copyOf(rolls),
            term.sign() * subtotal
        );
    }

    private DiceTermResult evaluatePlaceholder(ParsedTerm term, Function<String, Integer> modifierResolver) {
        if (modifierResolver == null) {
            throw InvalidInputException.invalidValue(
                "formula",
                "Character modifier " + term.notation() + " requires a bound character"
            );
        }

        var resolvedValue = modifierResolver.apply(term.rawValue().toUpperCase(Locale.ROOT));
        return new DiceTermResult(
            DiceTermKind.CHARACTER_MODIFIER,
            term.notation(),
            Integer.toString(resolvedValue),
            term.sign(),
            List.of(),
            term.sign() * resolvedValue
        );
    }

    private String formatResolvedPart(DiceTermResult term, boolean firstTerm) {
        if (firstTerm) {
            return (term.sign() < 0 ? "-" : "") + term.resolvedNotation();
        }
        return (term.sign() < 0 ? "-" : "+") + " " + term.resolvedNotation();
    }

    private int parsePositiveInt(String rawValue, String fieldName) {
        try {
            var parsed = Integer.parseInt(rawValue);
            if (parsed <= 0) {
                throw InvalidInputException.invalidValue("formula", fieldName + " must be greater than 0");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw InvalidInputException.invalidValue("formula", fieldName + " must be a valid integer");
        }
    }

    private enum ParsedTermType {
        DICE,
        FATE_DICE,
        CONSTANT,
        PLACEHOLDER
    }

    private record ParsedTerm(
        ParsedTermType type,
        String notation,
        String rawValue,
        int count,
        int sides,
        int startIndex,
        int nextIndex,
        int constantValue,
        int sign
    ) {
        private ParsedTerm(
            ParsedTermType type,
            String notation,
            String rawValue,
            int count,
            int sides,
            int startIndex,
            int nextIndex,
            int constantValue
        ) {
            this(type, notation, rawValue, count, sides, startIndex, nextIndex, constantValue, 1);
        }

        private ParsedTerm withSign(int newSign) {
            return new ParsedTerm(type, notation, rawValue, count, sides, startIndex, nextIndex, constantValue, newSign);
        }
    }
}
