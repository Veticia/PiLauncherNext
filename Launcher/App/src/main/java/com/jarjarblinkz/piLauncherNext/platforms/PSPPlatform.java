package com.jarjarblinkz.piLauncherNext.platforms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import com.jarjarblinkz.piLauncherNext.MainActivity;
import com.jarjarblinkz.piLauncherNext.SettingsProvider;

import net.didion.loopy.iso9660.ISO9660FileEntry;
import net.didion.loopy.iso9660.ISO9660FileSystem;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Scanner;

public class PSPPlatform  extends AbstractPlatform {

    private static final String CONFIG_FILE = Environment.getExternalStorageDirectory().getPath() + "/PSP/SYSTEM/ppssppvr.ini";
    private static final String FILENAME_PREFIX = "FileName";
    private static final String RECENT_TAG = "[Recent]";
    public static final String PACKAGE_PREFIX = "psp/";

    public ArrayList<ApplicationInfo> getInstalledApps(Context context) {
        ArrayList<ApplicationInfo> output = new ArrayList<>();
        if (!isSupported(context)) {
            return output;
        }

        for (String path : locateGames()) {
            ApplicationInfo app = new ApplicationInfo();
            app.name = path.substring(path.lastIndexOf('/') + 1);
            app.packageName = PACKAGE_PREFIX + path;
            if(!SettingsProvider.installDates.containsKey(app.packageName)) {
                File file = new File(path);
                SettingsProvider.installDates.put(app.packageName, file.lastModified());
            }
            output.add(app);
        }
        return output;
    }

    public boolean isSupported(Context context) {
        for (ApplicationInfo app : new VRPlatform().getInstalledApps(context)) {
            if (app.packageName.startsWith(MainActivity.EMULATOR_PACKAGE)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void loadIcon(Activity activity, ImageView icon, ApplicationInfo app, String name) {
        final File file = pkg2path(activity, app.packageName);
        if (file.exists()) {
            if (AbstractPlatform.updateIcon(icon, file, app.packageName)) {
                return;
            }
        }

        new Thread(() -> {
            try {
                //noinspection ResultOfMethodCallIgnored
                Objects.requireNonNull(file.getParentFile()).mkdirs();
                String isoToRead = app.packageName.substring(PACKAGE_PREFIX.length());
                ISO9660FileSystem discFs = new ISO9660FileSystem(new File(isoToRead), true);

                //noinspection rawtypes
                Enumeration es = discFs.getEntries();
                while (es.hasMoreElements()) {
                    ISO9660FileEntry fileEntry = (ISO9660FileEntry) es.nextElement();
                    if (fileEntry.getName().contains("ICON0.PNG")) {
                        if (saveStream(discFs.getInputStream(fileEntry), file)) {
                            activity.runOnUiThread(() -> AbstractPlatform.updateIcon(icon, file, app.packageName));
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public boolean runApp(Context context, ApplicationInfo app, boolean multiwindow) {
        if (context == null || app == null || app.packageName == null) {
            Log.e("runApp", "Failed to launch");
            return false;
        }

        try {
            String path = app.packageName.substring(PACKAGE_PREFIX.length());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + path), "*/*");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setPackage(MainActivity.EMULATOR_PACKAGE);
            context.getApplicationContext().startActivity(intent);
            return true;
        } catch (Exception e) {
            Log.e("runApp", "Failed to launch");
            return false;
        }
    }

    private ArrayList<String> locateGames() {
        ArrayList<String> output = new ArrayList<>();
        try {
            boolean enabled = false;
            FileInputStream fis = new FileInputStream(CONFIG_FILE);
            Scanner sc = new Scanner(fis);
            while (sc.hasNext()) {
                String line = sc.nextLine();
                if (enabled && line.startsWith(FILENAME_PREFIX)) {
                    output.add(line.substring(line.indexOf('/')));
                }

                if (line.startsWith(RECENT_TAG)) {
                    enabled = true;
                } else if (line.startsWith("[")) {
                    enabled = false;
                }
            }
            sc.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }
}
