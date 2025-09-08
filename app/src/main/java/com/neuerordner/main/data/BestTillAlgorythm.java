package com.neuerordner.main.data;

import com.neuerordner.main.ui.BestTillScanner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BestTillAlgorythm {

    private final Pattern NUMERIC_DATE_PATTERN = Pattern.compile("(0[1-9]|[1-2][0-9]|3[01]\\.(0[0-9]|1[0-2])\\.(20\\d{2}))", Pattern.CASE_INSENSITIVE); //28.12.2000 DD.MM.YYYY
    private final Pattern NUMERIC_TEXT_DATE_PATTERN = Pattern.compile("(0[1-9]|[1-2][0-9]|3[01])-([a-zA-Z]{3}-20\\d{2})"); //28-Dez-2000 DD-MMM-YYYY
    private final Pattern NUMERIC_DATE_PATTERN_2 = Pattern.compile("(0[1-9]|[1-2][0-9]|3[01]-(0[0-9]|1[0-2])-(20\\d{2}))"); //28-12-2000 DD-MM-YYYY
    private final Pattern ISO_8601_DATE_PATTERN_SMALL = Pattern.compile("(20\\d{2})-(0[0-9]|1[0-2])"); //2000-12 YYYY-MM
    private final Pattern ISO_8601_DATE_PATTERN_BIG = Pattern.compile("(20\\d{2}0[0-9]|1[0-2]0[1-9]|[1-2][0-9]|3[01])"); //20001228 YYYYMMDD
    private final Pattern ISO_8601_DATE_PATTERN_IMPERIAL = Pattern.compile("(20\\d{2})-(0[0-9]|1[0-2])-0[1-9]|[1-2][0-9]|3[01]"); //2000-12-28 YYYY-MM-DD
    private final Pattern[] DATE_PATTERNS = {NUMERIC_DATE_PATTERN, NUMERIC_TEXT_DATE_PATTERN, NUMERIC_DATE_PATTERN_2, ISO_8601_DATE_PATTERN_SMALL, ISO_8601_DATE_PATTERN_BIG, ISO_8601_DATE_PATTERN_IMPERIAL};
    public BestTillAlgorythm() {}
    public String bestTill(String text) {
        for (Pattern pattern : DATE_PATTERNS) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.group();
            }
        }
    }


}
