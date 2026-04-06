package com.example.photovault;

import android.content.Context;

public final class PhotoVaultPrefs {

    private static final String PREFS = "photovault_prefs";
    private static final String KEY_TREE_URI = "tree_uri";

    private PhotoVaultPrefs() {
    }

    public static void saveTreeUri(Context context, String uriString) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_TREE_URI, uriString)
                .apply();
    }

    public static String getTreeUri(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_TREE_URI, null);
    }
}
