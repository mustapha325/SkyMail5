package com.example.skymail.Data;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import com.example.skymail.R;

import java.util.Locale;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.example.skymail.Data.io.getLocale;

public class GetTimeAgo extends Application {

    /*
     * Copyright 2012 Google Inc.
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *      http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public static String getTimeAgo(long time, Context ctx,String locale) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }
        //Localization
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return getStringByLocal(ctx,R.string.just_now,locale);
        } else if (diff < 2 * MINUTE_MILLIS) {
            return getStringByLocal(ctx,R.string.a_minute_ago,locale);
        } else if (diff < 50 * MINUTE_MILLIS) {
            if (diff < 11 * MINUTE_MILLIS && locale.equals("ar")) return getStringByLocal(ctx,R.string.time_arabic,locale)+" "+diff / MINUTE_MILLIS+" "+getStringByLocal(ctx,R.string.minutes_ar,locale);
            else if(locale.equals("ar")) return getStringByLocal(ctx,R.string.time_arabic,locale)+" "+diff / MINUTE_MILLIS+" "+getStringByLocal(ctx,R.string.minutes_ago,locale);
            else return diff / MINUTE_MILLIS + " "+getStringByLocal(ctx,R.string.minutes_ago,locale);
        } else if (diff < 90 * MINUTE_MILLIS) {
            return getStringByLocal(ctx,R.string.an_hour_ago,locale);
        } else if (diff < 24 * HOUR_MILLIS) {
            if (diff < 11 * HOUR_MILLIS && locale.equals("ar")) return getStringByLocal(ctx,R.string.time_arabic,locale)+" "+diff / HOUR_MILLIS+" "+getStringByLocal(ctx,R.string.hours_ar,locale);
            else if(locale.equals("ar")) return getStringByLocal(ctx,R.string.time_arabic,locale)+" "+diff / HOUR_MILLIS + " "+getStringByLocal(ctx,R.string.hours_ago,locale);
            else return diff / HOUR_MILLIS + " "+getStringByLocal(ctx,R.string.hours_ago,locale);
        } else if (diff < 48 * HOUR_MILLIS) {
            return getStringByLocal(ctx,R.string.yesterday,locale);
        } else {
            if(locale.equals("ar")) return getStringByLocal(ctx,R.string.time_arabic,locale)+" "+diff / DAY_MILLIS + " "+getStringByLocal(ctx,R.string.days_ago,locale);
            return diff / DAY_MILLIS + " "+getStringByLocal(ctx,R.string.days_ago,locale);
        }
    }
    public static String getStringByLocal(Context context, int id, String locale) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(new Locale(locale));
        return context.createConfigurationContext(configuration).getResources().getString(id);
    }
}