package com.jarjarblinkz.piLauncherNext;

import static com.jarjarblinkz.piLauncherNext.MainActivity.sharedPreferences;
import static com.jarjarblinkz.piLauncherNext.SettingsProvider.KEY_AUTORUN;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            boolean autorunEnabled = sharedPreferences.getBoolean(KEY_AUTORUN, true);
            if (autorunEnabled) {
                Intent i = new Intent(context, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        }
    }
}