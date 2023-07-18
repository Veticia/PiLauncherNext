package com.jarjarblinkz.piLauncherNext.ui;

import static com.jarjarblinkz.piLauncherNext.MainActivity.DEFAULT_SCALE;
import static com.jarjarblinkz.piLauncherNext.MainActivity.DEFAULT_STYLE;
import static com.jarjarblinkz.piLauncherNext.MainActivity.STYLES;
import static com.jarjarblinkz.piLauncherNext.MainActivity.sharedPreferences;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jarjarblinkz.piLauncherNext.ImageUtils;
import com.jarjarblinkz.piLauncherNext.MainActivity;
import com.jarjarblinkz.piLauncherNext.R;
import com.jarjarblinkz.piLauncherNext.SettingsProvider;
import com.jarjarblinkz.piLauncherNext.platforms.AbstractPlatform;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AppsAdapter extends BaseAdapter
{
    final int style = sharedPreferences.getInt(SettingsProvider.KEY_CUSTOM_STYLE, DEFAULT_STYLE);
    private static Drawable iconDrawable;
    private static File iconFile;
    private static String packageName;
    private static long lastClickTime;
    private final MainActivity mainActivityContext;
    private final List<ApplicationInfo> appList;
    private final boolean isEditMode;
    private final boolean showTextLabels;
    private final int itemScale;
    private final SettingsProvider settingsProvider;

    public enum SORT_FIELD { APP_NAME, RECENT_DATE, INSTALL_DATE }
    public enum SORT_ORDER { ASCENDING, DESCENDING }

    public AppsAdapter(MainActivity context, boolean editMode, int scale, boolean names)
    {
        mainActivityContext = context;
        isEditMode = editMode;
        showTextLabels = names;
        itemScale = scale;
        settingsProvider = SettingsProvider.getInstance(mainActivityContext);

        ArrayList<String> sortedGroups = settingsProvider.getAppGroupsSorted(false);
        ArrayList<String> sortedSelectedGroups = settingsProvider.getAppGroupsSorted(true);
        boolean isFirstGroupSelected = !sortedSelectedGroups.isEmpty() && !sortedGroups.isEmpty() && sortedSelectedGroups.get(0).compareTo(sortedGroups.get(0)) == 0;
        appList = settingsProvider.getInstalledApps(context, sortedSelectedGroups, isFirstGroupSelected);
        sharedPreferences = mainActivityContext.getSharedPreferences(mainActivityContext.getPackageName() + "_preferences", Context.MODE_PRIVATE);
        SORT_FIELD sortField = SORT_FIELD.values()[sharedPreferences.getInt(SettingsProvider.KEY_SORT_FIELD, 0)];
        SORT_ORDER sortOrder = SORT_ORDER.values()[sharedPreferences.getInt(SettingsProvider.KEY_SORT_ORDER, 0)];
        this.sort(sortField, sortOrder);
    }

    private static class ViewHolder {
        LinearLayout layout;
        ImageView imageView;
        TextView textView;
        ImageView progressBar;
    }

    public int getCount()
    {
        return appList.size();
    }

    public Object getItem(int position)
    {
        return appList.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    private final Handler handler = new Handler();
    @SuppressLint("NewApi")
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        final ApplicationInfo currentApp = appList.get(position);
        LayoutInflater inflater = (LayoutInflater) mainActivityContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            // Create a new ViewHolder and inflate the layout
            convertView = inflater.inflate(R.layout.lv_app, parent, false);
            holder = new ViewHolder();
            holder.layout = convertView.findViewById(R.id.layout);
            holder.imageView = convertView.findViewById(R.id.imageLabel);
            holder.textView = convertView.findViewById(R.id.textLabel);
            holder.progressBar = convertView.findViewById(R.id.progress_bar);
            convertView.setTag(holder);

            // Set size of items
            int kScale = sharedPreferences.getInt(SettingsProvider.KEY_CUSTOM_SCALE, DEFAULT_SCALE) + 1;
            float textSize = holder.textView.getTextSize();
            float textSizeScaled = Math.max(10, textSize / 5 * kScale);
            holder.textView.setTextSize(textSizeScaled);

            //Calculate text height
            holder.textView.measure(0, 0);
            int textHeight = holder.textView.getMeasuredHeight();

            ViewGroup.LayoutParams params = holder.layout.getLayoutParams();

            params.width = itemScale;
            if (style == 0) {
                if(showTextLabels) {
                    params.height = (int) ((itemScale) * 0.5625) + textHeight;
                }else{
                    params.height = (int) ((itemScale) * 0.5625);
                }
            } else {
                if(showTextLabels) {
                    params.height = itemScale + textHeight;
                }else{
                    params.height = itemScale;
                }
            }
            holder.layout.setLayoutParams(params);
        } else {
            // ViewHolder already exists, reuse it
            holder = (ViewHolder) convertView.getTag();
        }

        // set value into textview
        PackageManager pm = mainActivityContext.getPackageManager();
        String name = SettingsProvider.getAppDisplayName(mainActivityContext, currentApp.packageName, currentApp.loadLabel(pm));
        holder.textView.setText(name);
        holder.textView.setVisibility(showTextLabels ? View.VISIBLE : View.GONE);

        if (isEditMode) {
            // short click for app details, long click to activate drag and drop
            holder.layout.setOnTouchListener((view, motionEvent) -> {
                if (AbstractPlatform.isOculusHeadset()) {
                    boolean selected = mainActivityContext.selectApp(currentApp.packageName);
                    view.setAlpha(selected? 0.5F : 1.0F);
                } else {
                    if ((motionEvent.getAction() == MotionEvent.ACTION_DOWN) ||
                            (motionEvent.getAction() == MotionEvent.ACTION_POINTER_DOWN)) {
                        packageName = currentApp.packageName;
                        lastClickTime = System.currentTimeMillis();
                        ClipData dragData = ClipData.newPlainText(name, name);
                        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            view.startDragAndDrop(dragData, shadowBuilder, view, 0);
                        } else {
                            view.startDrag(dragData, shadowBuilder, view, 0);
                        }
                    }
                }
                return false;
            });

            // drag and drop
            holder.layout.setOnDragListener((view, event) -> {
                if (currentApp.packageName.compareTo(packageName) == 0) {
                    if (event.getAction() == DragEvent.ACTION_DRAG_STARTED) {
                        view.setVisibility(View.INVISIBLE);
                    } else if (event.getAction() == DragEvent.ACTION_DRAG_ENDED) {
                        mainActivityContext.reloadUI();
                    } else if (event.getAction() == DragEvent.ACTION_DROP) {
                        if (System.currentTimeMillis() - lastClickTime < 250) {
                            showAppDetails(currentApp);
                        } else {
                            mainActivityContext.reloadUI();
                        }
                    }
                    return event.getAction() != DragEvent.ACTION_DROP;
                }
                return true;
            });
        } else {
            holder.layout.setOnClickListener(view -> {
                holder.progressBar.setVisibility(View.VISIBLE);
                RotateAnimation rotateAnimation = new RotateAnimation(
                        0, 360,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f
                );
                rotateAnimation.setDuration(1000);
                rotateAnimation.setRepeatCount(Animation.INFINITE);
                rotateAnimation.setInterpolator(new LinearInterpolator());
                holder.progressBar.startAnimation(rotateAnimation);
                if(!mainActivityContext.openApp(currentApp)) {
                    holder.progressBar.setVisibility(View.GONE);
                    holder.progressBar.clearAnimation();
                }

                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> {
                    AlphaAnimation fadeOut = new AlphaAnimation(1, 0);
                    fadeOut.setDuration(1000);
                    fadeOut.setFillAfter(true);
                    holder.progressBar.startAnimation(fadeOut);
                }, 2000);
                handler.postDelayed(() -> {
                    holder.progressBar.setVisibility(View.GONE);
                    holder.progressBar.clearAnimation();
                }, 3000);
            });
            holder.layout.setOnLongClickListener(view -> {
                showAppDetails(currentApp);
                return false;
            });
        }

        // set application icon
        AbstractPlatform platform = AbstractPlatform.getPlatform(currentApp);
        try {
            platform.loadIcon(mainActivityContext, holder.imageView, currentApp, name);
        } catch (Resources.NotFoundException e) {
            Log.e("loadIcon", "Error loading icon for app: " + currentApp.packageName, e);
        }
        return convertView;
    }

    public void onImageSelected(String path, ImageView selectedImageView) {
        AbstractPlatform.clearIconCache();
        if (path != null) {
            Bitmap bitmap = ImageUtils.getResizedBitmap(BitmapFactory.decodeFile(path), 512);
            ImageUtils.saveBitmap(bitmap, iconFile);
            selectedImageView.setImageBitmap(bitmap);
        } else {
            selectedImageView.setImageDrawable(iconDrawable);
            AbstractPlatform.updateIcon(selectedImageView, iconFile, STYLES[style]+"."+ packageName);
        }
        mainActivityContext.reloadUI();
        this.notifyDataSetChanged(); // for real time updates
    }

    private Long getInstallDate(ApplicationInfo applicationInfo) {
        if(SettingsProvider.installDates.containsKey(applicationInfo.packageName)) {
            return SettingsProvider.installDates.get(applicationInfo.packageName);
        }else{
            return 0L;
        }
    }

    public void sort(SORT_FIELD field, SORT_ORDER order) {
        final PackageManager pm = mainActivityContext.getPackageManager();
        final Map<String, Long> recents = settingsProvider.getRecents();

        Collections.sort(appList, (a, b) -> {
            String na;
            String nb;
            long naL;
            long nbL;
            int result;
            switch (field) {
                case RECENT_DATE:
                    if (recents.containsKey(a.packageName)) {
                        naL = recents.get(a.packageName);
                    } else {
                        naL = getInstallDate(a);
                    }
                    if (recents.containsKey(b.packageName)) {
                        nbL = recents.get(b.packageName);
                    } else {
                        nbL = getInstallDate(b);
                    }
                    result = Long.compare(naL, nbL);
                    break;

                case INSTALL_DATE:
                    naL = getInstallDate(a);
                    nbL = getInstallDate(b);
                    result = Long.compare(naL, nbL);
                    break;

                default: //by APP_NAME
                    na = SettingsProvider.getAppDisplayName(mainActivityContext, a.packageName, a.loadLabel(pm)).toUpperCase();
                    nb = SettingsProvider.getAppDisplayName(mainActivityContext, b.packageName, b.loadLabel(pm)).toUpperCase();
                    result = na.compareTo(nb);
                    break;
            }

            return order == SORT_ORDER.ASCENDING ? result : -result;
        });
        this.notifyDataSetChanged();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void showAppDetails(ApplicationInfo actApp) {

        //set layout
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivityContext);
        builder.setView(R.layout.dialog_app_details);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bkg_dialog);
        dialog.show();

        //info action
        dialog.findViewById(R.id.info).setOnClickListener(view13 -> mainActivityContext.openAppDetails(actApp.packageName));

        //set name
        PackageManager pm = mainActivityContext.getPackageManager();
        String name = SettingsProvider.getAppDisplayName(mainActivityContext, actApp.packageName, actApp.loadLabel(pm));
        final EditText input = dialog.findViewById(R.id.app_name);
        input.setText(name);
        dialog.findViewById(R.id.ok).setOnClickListener(view12 -> {
            settingsProvider.setAppDisplayName(actApp, input.getText().toString());
            mainActivityContext.reloadUI();
            dialog.dismiss();
        });

        // load icon
        ImageView tempImage = dialog.findViewById(R.id.app_icon);
        AbstractPlatform platform = AbstractPlatform.getPlatform(actApp);
        platform.loadIcon(mainActivityContext, tempImage, actApp, name);

        tempImage.setOnClickListener(view1 -> {
            iconDrawable = actApp.loadIcon(pm);
            packageName = actApp.packageName;
            iconFile = AbstractPlatform.pkg2path(mainActivityContext, STYLES[style]+"."+actApp.packageName);
            if (iconFile.exists()) {
                iconFile.delete();
            }
            mainActivityContext.setSelectedImageView(tempImage);
            ImageUtils.showImagePicker(mainActivityContext, MainActivity.PICK_ICON_CODE);
        });
    }

    public String getSelectedPackage() {
        return packageName;
    }
}
