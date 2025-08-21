package com.s23010305.roadguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.ImageView;

public class ProfileImageUtils {

    private static final String PREFS = "RoadGuardPrefs";
    private static final String KEY_IMAGE = "profile_image";

    /** Loads the saved profile image into the given ImageView. Falls back to a placeholder. */
    public static void loadInto(Context context, ImageView target, int placeholderResId) {
        if (target == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String uriStr = prefs.getString(KEY_IMAGE, null);
        if (uriStr != null) {
            try {
                target.setImageURI(Uri.parse(uriStr));
            } catch (Exception e) {
                target.setImageResource(placeholderResId);
            }
        } else {
            target.setImageResource(placeholderResId);
        }
    }

    /** Returns the currently saved profile image Uri string, or null. */
    public static String getSavedUriString(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getString(KEY_IMAGE, null);
    }

    /** Saves a profile image Uri string (or clears it if null). */
    public static void saveUriString(Context context, String uriStr) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (uriStr != null) {
            editor.putString(KEY_IMAGE, uriStr);
        } else {
            editor.remove(KEY_IMAGE);
        }
        editor.apply();
    }
}
