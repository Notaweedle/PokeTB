package edu.neumont.Pwest.finalfinal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class InputValidator {


    public static final int MAX_DEX = 1025;

    public static final int MAX_IV = 31;
    public static final int MAX_EV = 255;


    private static final Pattern SEARCH_QUERY = Pattern.compile(
            "^(?:#?0*(?<dex>[1-9]\\d{0,3})"
          + "|(?<species>[a-z][a-z0-9]+(?:-[a-z0-9]+)*))$");


    private static final Pattern DROPPED_PUNCTUATION = Pattern.compile("[.'’:]");
    private static final Pattern WORD_BREAK = Pattern.compile("[\\s_]+");
    private static final Pattern IV_EXACT = Pattern.compile("^(?:3[01]|[12]?\\d)$");
    private static final Pattern EV_EXACT = Pattern.compile("^(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$");
    private static final Pattern IV_TYPING = Pattern.compile("^0*(?:3[01]|[12]?\\d)?$");
    private static final Pattern EV_TYPING = Pattern.compile("^0*(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)?$");
    private static final Pattern NAME_WORD_START = Pattern.compile("(?:^|-)([a-z0-9])");

    private InputValidator() {
    }
    public enum QueryKind { DEX, SPECIES, INVALID }
    public static final class SearchQuery {
        private final QueryKind kind;
        private final String species;
        private final int dexNumber;
        private final String problem;

        private SearchQuery(QueryKind kind, String species, int dexNumber, String problem) {
            this.kind = kind;
            this.species = species;
            this.dexNumber = dexNumber;
            this.problem = problem;
        }

        public QueryKind getKind() { return kind; }
        public boolean isValid() { return kind != QueryKind.INVALID; }


        public String getApiQuery() { return species; }


        public int getDexNumber() { return dexNumber; }


        public String getProblem() { return problem; }
    }
    public static SearchQuery parseSearch(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return invalid("Enter a Pokemon name or Dex number.");
        }

        String cleaned = normalize(raw);
        if (cleaned.isEmpty()) {
            return invalid("\"" + raw.trim() + "\" has no letters or numbers in it.");
        }

        Matcher matcher = SEARCH_QUERY.matcher(cleaned);
        if (!matcher.matches()) {
            return invalid("\"" + raw.trim() + "\" isn't a valid name or Dex number.\n"
                    + "Names use letters and single hyphens, like \"ho-oh\" or \"deoxys-normal\".");
        }


        String dexDigits = matcher.group("dex");
        if (dexDigits != null) {
            int dex = Integer.parseInt(dexDigits);
            if (dex > MAX_DEX) {
                return invalid("Dex number " + dex + " is past the last Pokemon (" + MAX_DEX + ").");
            }
            return new SearchQuery(QueryKind.DEX, String.valueOf(dex), dex, null);
        }

        return new SearchQuery(QueryKind.SPECIES, matcher.group("species"), -1, null);
    }
    private static SearchQuery invalid(String problem) {
        return new SearchQuery(QueryKind.INVALID, null, -1, problem);
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }

        String result = raw.trim().toLowerCase();
        result = DROPPED_PUNCTUATION.matcher(result).replaceAll("");
        result = WORD_BREAK.matcher(result).replaceAll("-");
        return result;
    }
    public static String prettify(String apiName) {
        if (apiName == null || apiName.isEmpty()) {
            return "";
        }
        return NAME_WORD_START.matcher(apiName)
                .replaceAll(match -> " " + match.group(1).toUpperCase())
                .trim();
    }

    public static boolean isValidIv(String text) {
        return text != null && IV_EXACT.matcher(text).matches();
    }
    public static boolean isValidEv(String text) {
        return text != null && EV_EXACT.matcher(text).matches();
    }
    public static boolean isIvInProgress(String text) {
        return text != null && IV_TYPING.matcher(text).matches();
    }
    public static boolean isEvInProgress(String text) {
        return text != null && EV_TYPING.matcher(text).matches();
    }
    public static Pattern looseSearch(String userText) {
        String safe = Pattern.quote(userText == null ? "" : userText.trim());
        return Pattern.compile(".*" + safe + ".*", Pattern.CASE_INSENSITIVE);
    }
}
