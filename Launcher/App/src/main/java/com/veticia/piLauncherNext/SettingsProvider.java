package com.veticia.piLauncherNext;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import com.veticia.piLauncherNext.platforms.AndroidPlatform;
import com.veticia.piLauncherNext.platforms.PSPPlatform;
import com.veticia.piLauncherNext.platforms.VRPlatform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SettingsProvider
{
    public static final String KEY_CUSTOM_NAMES = "KEY_CUSTOM_NAMES";
    public static final String KEY_CUSTOM_OPACITY = "KEY_CUSTOM_OPACITY";
    public static final String KEY_CUSTOM_SCALE = "KEY_CUSTOM_SCALE";
    public static final String KEY_CUSTOM_THEME = "KEY_CUSTOM_THEME";
    public static final String KEY_CUSTOM_STYLE = "KEY_CUSTOM_STYLE";
    public static final String KEY_EDITMODE = "KEY_EDITMODE";
    public static final String KEY_AUTORUN = "KEY_AUTORUN";
    public static final String KEY_PLATFORM_ANDROID = "KEY_PLATFORM_ANDROID";
    public static final String KEY_PLATFORM_PSP = "KEY_PLATFORM_PSP";
    public static final String KEY_PLATFORM_VR = "KEY_PLATFORM_VR";

    private final String KEY_APP_GROUPS = "prefAppGroups";
    private final String KEY_APP_LIST = "prefAppList";
    private final String KEY_SELECTED_GROUPS = "prefSelectedGroups";
    private final String SEPARATOR = "#@%";

    private static SettingsProvider instance;
    private static Context context;

    public static synchronized SettingsProvider getInstance (Context context)
    {
        if (SettingsProvider.instance == null) {
            SettingsProvider.instance = new SettingsProvider(context);
        }
        return SettingsProvider.instance;
    }

    //storage
    private final SharedPreferences mPreferences;
    private Map<String, String> mAppList = new HashMap<>();
    private Set<String> mAppGroups = new HashSet<>();
    private Set<String> mSelectedGroups = new HashSet<>();

    private SettingsProvider(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SettingsProvider.context = context;
    }

    public void setAppList(Map<String, String> appList)
    {
        mAppList = appList;
        storeValues();
    }

    public Map<String, String> getAppList()
    {
        readValues();
        return mAppList;
    }

    public ArrayList<ApplicationInfo> getInstalledApps(Context context, List<String> selected, boolean first) {

        // Get list of installed apps
        Map<String, String> apps = getAppList();
        ArrayList<ApplicationInfo> installedApplications = new ArrayList<>();
        if (isPlatformEnabled(KEY_PLATFORM_ANDROID)) {
            List<ApplicationInfo> androidApps = new AndroidPlatform().getInstalledApps(context);
            for (ApplicationInfo app : androidApps) {
                if (!mAppList.containsKey(app.packageName)) {
                    mAppList.put(app.packageName, context.getString(R.string.default_tools_group));
                }
            }

            installedApplications.addAll(androidApps);
        }
        if (isPlatformEnabled(KEY_PLATFORM_PSP) && new PSPPlatform().isSupported(context)) {
            // only add PSP apps if the platform is supported
            List<ApplicationInfo> pspApps = new PSPPlatform().getInstalledApps(context);
            for (ApplicationInfo app : pspApps) {
                if (!mAppList.containsKey(app.packageName)) {
                    mAppList.put(app.packageName, "PSP");
                }
            }
            installedApplications.addAll(pspApps);
        }
        if (isPlatformEnabled(KEY_PLATFORM_VR)) {
            installedApplications.addAll(new VRPlatform().getInstalledApps(context));
        }

        // Save changes to app list
        setAppList(mAppList);

        // Put them into a map with package name as keyword for faster handling
        String ownPackageName = context.getApplicationContext().getPackageName();
        Map<String, ApplicationInfo> appMap = new LinkedHashMap<>();
        for(ApplicationInfo installedApplication : installedApplications)
        {
            String pkg = installedApplication.packageName;
            boolean showAll = selected.isEmpty();
            boolean isNotAssigned = !apps.containsKey(pkg) && first;
            boolean isInGroup = apps.containsKey(pkg) && selected.contains(apps.get(pkg));
            boolean isVr = hasMetadata(installedApplication, "com.samsung.android.vr.application.mode");
            boolean isEnvironment = !isVr && hasMetadata(installedApplication, "com.oculus.environmentVersion");
            if(showAll || isNotAssigned || isInGroup)
            {
                // Check for system app
                boolean isSystemApp = (installedApplication.flags & ApplicationInfo.FLAG_SYSTEM) == 1;
                if (pkg.startsWith("com.oculus.browser")) isSystemApp = false;
                if (pkg.startsWith("metapwa")) isSystemApp = true;
                if (pkg.startsWith("oculuspwa")) isSystemApp = true;
                if (pkg.startsWith("com.facebook.arvr")) isSystemApp = true;
                if (pkg.startsWith("com.meta.environment")) isSystemApp = true;
                if (pkg.startsWith("com.oculus.avatar2")) isSystemApp = true;
                if (pkg.startsWith("com.oculus.environment")) isSystemApp = true;
                if (pkg.startsWith("com.oculus.helpcenter")) isSystemApp = true;
                if (pkg.startsWith("com.oculus.systemutilities")) isSystemApp = true;
                if (pkg.startsWith("com.meta.AccountsCenter.pwa")) isSystemApp = true;
                if (pkg.startsWith("com.pico")) isSystemApp = true;
                if (pkg.startsWith("com.pico.playsys")) isSystemApp = false;
                if (pkg.startsWith("com.pico.browser")) isSystemApp = false;
                if (pkg.startsWith("com.pico.metricstool")) isSystemApp = false;
                if (pkg.startsWith("com.pico4.settings")) isSystemApp = false;
                if (pkg.startsWith("com.picovr.assistantphone")) isSystemApp = false;
                if (pkg.startsWith("com.picovr.picostreamassistant")) isSystemApp = false;
                if (pkg.startsWith("com.pvr")) isSystemApp = true;
                if (pkg.startsWith("com.ss.android.ttvr")) isSystemApp = false;
                if (pkg.startsWith("com.monotype.android.font")) isSystemApp = true;
                if (pkg.startsWith("com.samsung.android.filter")) isSystemApp = true;
                if (pkg.startsWith("com.samsung.SMT.lang")) isSystemApp = true;
                if (pkg.startsWith("com.candycamera.android.filter")) isSystemApp = true;
                if (pkg.startsWith("eu.kanade.tachiyomi.extension")) isSystemApp = true;
                if (pkg.startsWith("com.mixplorer.addon")) isSystemApp = true;
                if (pkg.startsWith("com.ghisler.tcplugins")) isSystemApp = true;

                if (!isSystemApp && !isEnvironment) {
                    if(!installedApplication.packageName.equals(ownPackageName)) {
                        appMap.put(installedApplication.packageName, installedApplication);
                    }
                }
            }
        }

        // Create new list of apps
        PackageManager pm = context.getPackageManager();
        ArrayList<ApplicationInfo> output = new ArrayList<>(appMap.values());
        output.sort((a, b) -> {
            String na = getAppDisplayName(context, a.packageName, a.loadLabel(pm)).toUpperCase();
            String nb = getAppDisplayName(context, b.packageName, b.loadLabel(pm)).toUpperCase();
            return na.compareTo(nb);
        });
        return output;
    }

    public boolean hasMetadata(ApplicationInfo app, String metadata)
    {
        if (app.metaData != null)
        {
            for (String key : app.metaData.keySet())
            {
                if (metadata.compareTo(key) == 0)
                {
                    return true;
                }
            }
        }
        return false;
    }

    public void setAppGroups(Set<String> appGroups)
    {
        mAppGroups = appGroups;
        storeValues();
    }

    public Set<String> getAppGroups()
    {
        readValues();
        return mAppGroups;
    }

    public void setSelectedGroups(Set<String> appGroups)
    {
        mSelectedGroups = appGroups;
        storeValues();
    }

    public Set<String> getSelectedGroups()
    {
        readValues();
        return mSelectedGroups;
    }

    public ArrayList<String> getAppGroupsSorted(boolean selected)
    {
        readValues();
        ArrayList<String> output = new ArrayList<>(selected ? mSelectedGroups : mAppGroups);
        output.sort((a, b) -> {
            String name1 = simplifyName(a.toUpperCase());
            String name2 = simplifyName(b.toUpperCase());
            return name1.compareTo(name2);
        });
        return output;
    }

    private synchronized void readValues()
    {
        try
        {
            Set<String> def = new HashSet<>();
            def.add(context.getString(R.string.default_apps_group));
            Set<String> defClone = new HashSet<>();
            defClone.add(context.getString(R.string.default_apps_group));
            mSelectedGroups = mPreferences.getStringSet(KEY_SELECTED_GROUPS, defClone);
            def.add(context.getString(R.string.default_tools_group));
            if (new PSPPlatform().isSupported(context)) {
                def.add("PSP");
            }
            mAppGroups = mPreferences.getStringSet(KEY_APP_GROUPS, def);

            mAppList.clear();
            Set<String> apps = new HashSet<>();
            apps = mPreferences.getStringSet(KEY_APP_LIST, apps);
            for (String s : apps) {
                String[] data = s.split(SEPARATOR);
                mAppList.put(data[0], data[1]);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private synchronized void storeValues()
    {
        try
        {
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putStringSet(KEY_APP_GROUPS, mAppGroups);
            editor.putStringSet(KEY_SELECTED_GROUPS, mSelectedGroups);

            Set<String> apps = new HashSet<>();
            for (String pkg : mAppList.keySet()) {
                apps.add(pkg + SEPARATOR + mAppList.get(pkg));
            }
            editor.putStringSet(KEY_APP_LIST, apps);

            editor.apply();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public String addGroup() {
        String name = "XXX";
        List<String> groups = getAppGroupsSorted(false);
        if (groups.contains(name)) {
            int index = 1;
            while (groups.contains(name + index)) {
                index++;
            }
            name = name + index;
        }
        groups.add(name);
        setAppGroups(new HashSet<>(groups));
        return name;
    }

    public void selectGroup(String name) {
        Set<String> selectFirst = new HashSet<>();
        selectFirst.add(name);
        setSelectedGroups(selectFirst);
    }

    public static String getAppDisplayName(Context context, String pkg, CharSequence label)
    {
        String name = PreferenceManager.getDefaultSharedPreferences(context).getString(pkg, "");
        if (!name.isEmpty()) {
            return name;
        }

        String retVal = label.toString();
        if (retVal == null || retVal.equals("")) {
            retVal = pkg;
        }
        return retVal;
    }

    public void setAppDisplayName(Context context, ApplicationInfo appInfo, String newName)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(appInfo.packageName, newName);
        editor.apply();
    }

    public String simplifyName(String name) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if ((c >= 'A') && (c <= 'Z')) output.append(c);
            if ((c >= '0') && (c <= '9')) output.append(c);
        }
        return output.toString();
    }

    public boolean isPlatformEnabled(String key) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, true);
    }
}
