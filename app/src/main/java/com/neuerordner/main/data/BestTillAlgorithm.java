package com.neuerordner.main.data;

import android.util.Log;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BestTillAlgorithm {

    private static class NamePattern {
        public String name;
        public Pattern pattern;

        public NamePattern(String name, Pattern pattern) {
            this.name = name;
            this.pattern = pattern;

        }
    }

    private final static Pattern NUMERIC_DATE_PATTERN = Pattern.compile("(0[1-9]|[1-2][0-9]|3[01])\\.(0[1-9]|1[0-2])\\.(20\\d{2})"); //28.12.2000 DD.MM.YYYY
    private final static Pattern NUMERIC_TEXT_DATE_PATTERN = Pattern.compile("(0[1-9]|[1-2][0-9]|3[01])-([\\p{L}]{3,4})-(20\\d{2})", Pattern.CASE_INSENSITIVE); //28-Dez-2000 DD-MMM-YYYY
    private final static Pattern NUMERIC_DATE_PATTERN_2 = Pattern.compile("(0[1-9]|[1-2][0-9]|3[01])-(0[1-9]|1[0-2])-(20\\d{2})"); //28-12-2000 DD-MM-YYYY
    private final static Pattern ISO_8601_DATE_PATTERN_SMALL = Pattern.compile("(20\\d{2})-(0[1-9]|1[0-2])"); //2000-12 YYYY-MM
    private final static Pattern ISO_8601_DATE_PATTERN_BIG = Pattern.compile("(20\\d{2})(0[1-9]|1[0-2])(0[1-9]|[1-2][0-9]|3[01])"); //20001228 YYYYMMDD
    private final static Pattern ISO_8601_DATE_PATTERN_IMPERIAL = Pattern.compile("(20\\d{2})-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[01])"); //2000-12-28 YYYY-MM-DD
    private final static Pattern NUMERIC_DATE_PATTERN_3 = Pattern.compile("(0[1-9]|[1-2][0-9]|3[01])\\.(0[1-9]|1[0-2])\\.(\\d{2})"); //28.12.00
    private final static NamePattern[] DATE_PATTERNS = {
            new NamePattern("NUMERIC_DATE_PATTERN", NUMERIC_DATE_PATTERN),
            new NamePattern("NUMERIC_TEXT_DATE_PATTERN", NUMERIC_TEXT_DATE_PATTERN),
            new NamePattern("NUMERIC_DATE_PATTERN_2", NUMERIC_DATE_PATTERN_2),
            new NamePattern("ISO_8601_DATE_PATTERN_SMALL", ISO_8601_DATE_PATTERN_SMALL),
            new NamePattern("ISO_8601_DATE_PATTERN_BIG", ISO_8601_DATE_PATTERN_BIG),
            new NamePattern("ISO_8601_DATE_PATTERN_IMPERIAL", ISO_8601_DATE_PATTERN_IMPERIAL),
            new NamePattern("NUMERIC_DATE_PATTERN_3", NUMERIC_DATE_PATTERN_3)
    };
    private static final Map<String, Integer> monthMap_en = Map.ofEntries(
            Map.entry("jan", 1),
            Map.entry("feb", 2),
            Map.entry("mar", 3),
            Map.entry("apr", 4),
            Map.entry("may", 5),
            Map.entry("jun", 6),
            Map.entry("jul", 7),
            Map.entry("aug", 8),
            Map.entry("sep", 9),
            Map.entry("sept", 9),
            Map.entry("oct", 10),
            Map.entry("nov", 11),
            Map.entry("dec", 12)
    );

    private static final Map<String, Integer> monthMap_de = Map.ofEntries(
            Map.entry("jan", 1),
            Map.entry("feb", 2),
            Map.entry("mär", 3),
            Map.entry("maer", 3),
            Map.entry("maerz", 3),
            Map.entry("mrz", 3),
            Map.entry("apr", 4),
            Map.entry("mai", 5),
            Map.entry("jun", 6),
            Map.entry("jul", 7),
            Map.entry("aug", 8),
            Map.entry("sep", 9),
            Map.entry("sept", 9),
            Map.entry("okt", 10),
            Map.entry("nov", 11),
            Map.entry("dez", 12)
    );
    private BestTillAlgorithm() {}

    /**
     * Run the Algorithm, to determine all dates in a text
     * @author Sandro Fuetsch
     * @param text Input Text
     * @return Set of Dates
     * @since 1.0
     */
    public static Set<LocalDate> run(String text) {
        Set<LocalDate> dates = new LinkedHashSet<>();
        for (NamePattern namePattern : DATE_PATTERNS) {
            Matcher matcher = namePattern.pattern.matcher(text);
            while (matcher.find()) {
                switch (namePattern.name) {
                    case "NUMERIC_DATE_PATTERN":
                    case "NUMERIC_DATE_PATTERN_2":
                        try {
                            String day = matcher.group(1);
                            String month = matcher.group(2);
                            String year = matcher.group(3);
                            dates.add(LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day)));
                        } catch (NumberFormatException | DateTimeException e) {
                            Log.e("BestTillAlgorithm", "Failure on date parsing", e);
                        }
                        break;
                    case "NUMERIC_DATE_PATTERN_3":
                        try {
                            String day = matcher.group(1);
                            String month = matcher.group(2);
                            String year = matcher.group(3);
                            int fullYear = Integer.parseInt(year) + 2000;

                            dates.add(LocalDate.of(fullYear, Integer.parseInt(month), Integer.parseInt(day)));
                        } catch (NumberFormatException | DateTimeException e) {
                                Log.e("BestTillAlgorithm", "Failure on date parsing", e);
                            }
                        break;
                    case "NUMERIC_TEXT_DATE_PATTERN":
                        try {
                            String day = matcher.group(1);
                            String year = matcher.group(3);
                            String month = matcher.group(2).toLowerCase(java.util.Locale.ROOT);
                            int month_int = 0;

                            if (monthMap_de.containsKey(month)) {
                                month_int = monthMap_de.get(month);
                            } else if (monthMap_en.containsKey(month)) {
                                month_int = monthMap_en.get(month);
                            }
                            if (month_int == 0) {
                                break;
                            }
                            dates.add(LocalDate.of(Integer.parseInt(year), month_int, Integer.parseInt(day)));
                        } catch (NumberFormatException | DateTimeException e) {
                            Log.e("BestTillAlgorithm", "Failure on date parsing", e);
                        }
                        break;

                    case "ISO_8601_DATE_PATTERN_SMALL":
                        try {
                            String month = matcher.group(2);
                            String year = matcher.group(1);
                            int day = 1;

                            dates.add(LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), day));
                        } catch (NumberFormatException | DateTimeException e) {
                            Log.e("BestTillAlgorithm", "Failure on date parsing", e);
                        }
                        break;

                    case "ISO_8601_DATE_PATTERN_BIG":
                        try {
                            String year = matcher.group(1);
                            String month = matcher.group(2);
                            String day = matcher.group(3);
                            dates.add(LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day)));
                        } catch (NumberFormatException | DateTimeException e) {
                            Log.e("BestTillAlgorithm", "Failure on date parsing", e);
                        }
                        break;

                    case "ISO_8601_DATE_PATTERN_IMPERIAL":
                        try {
                            String year = matcher.group(1);
                            String month = matcher.group(2);
                            String day = matcher.group(3);
                            dates.add(LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day)));
                        } catch (NumberFormatException | DateTimeException e) {
                            Log.e("BestTillAlgorithm", "Failure on date parsing", e);
                        }
                        break;
                }
            }
        }
        return dates;
    }
}
