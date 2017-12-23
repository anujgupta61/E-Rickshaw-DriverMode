package com.andromap33.e_rickshawdrivermode;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

class SaveSharedPreference {
    private static final String PREF_DRIVER_ID = "DriverID";

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    static void setDriverID(Context ctx, String d_id) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_DRIVER_ID, d_id);
        //editor.commit();
        editor.apply();
    }

    static String getDriverID(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_DRIVER_ID, "");
    }
}