package com.example.test.transapp;

import java.util.Locale;

public class Language {
    public static final Language EN = new Language("英語", "en", Locale.US);
    public static final Language ZH_CHS = new Language("中国語(簡体字)", "zh-CHS", Locale.CHINA);
    public static final Language JA = new Language("日本語", "ja", Locale.JAPAN);
    public static final Language KO = new Language("韓国語", "ko", Locale.KOREA);
    public static final Language[] LANGUAGES = {EN, ZH_CHS, JA, KO};
    private String mName;
    private String mCode;
    private Locale mLocale;

    public Language(String name, String code, Locale locale) {
        mName = name;
        mCode = code;
        mLocale = locale;
    }

    public String getName() {
        return mName;
    }

    public String getCode() {
        return mCode;
    }

    public Locale getLocale() {
        return mLocale;
    }
}
