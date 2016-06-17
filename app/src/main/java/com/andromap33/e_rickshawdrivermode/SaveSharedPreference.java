package com.andromap33.e_rickshawdrivermode;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SaveSharedPreference {
    static final String PREF_DRIVER_ID= "DriverID";

    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setDriverID(Context ctx, String d_id)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_DRIVER_ID, d_id);
        editor.commit();
    }

    public static String getDriverID(Context ctx)
    {
        return getSharedPreferences(ctx).getString(PREF_DRIVER_ID, "");
    }
}