package com.jarjarblinkz.piLauncherNext.ui;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.jarjarblinkz.piLauncherNext.MainActivity;
import com.jarjarblinkz.piLauncherNext.R;
import com.jarjarblinkz.piLauncherNext.SettingsProvider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroupsAdapter extends BaseAdapter {
    public static final int MAX_GROUPS = 12;
    public static final String HIDDEN_GROUP = "HIDDEN!";

    private final MainActivity mainActivity;
    private final List<String> appGroups;
    private final Set<String> selectedGroups;
    private final SettingsProvider settingsProvider;
    private final boolean isEditMode;

    /** Create new adapter */
    public GroupsAdapter(MainActivity activity, boolean editMode) {
        mainActivity = activity;
        isEditMode = editMode;
        settingsProvider = SettingsProvider.getInstance(activity);

        SettingsProvider settings = SettingsProvider.getInstance(mainActivity);
        appGroups = settings.getAppGroupsSorted(false);
        if (editMode) {
            appGroups.add(HIDDEN_GROUP);
            appGroups.add("+ " + mainActivity.getString(R.string.add_group));
        }
        selectedGroups = settings.getSelectedGroups();
    }

    public int getCount() {
        return appGroups.size();
    }

    public String getItem(int position) {
        return appGroups.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        @SuppressWarnings("unused")
        final TextView textView;
        final View menu;

        ViewHolder(View itemView) {
            textView = itemView.findViewById(R.id.textLabel);
            menu = itemView.findViewById(R.id.menu);
        }
    }

    public void setGroup(String packageName, int position) {
        // add group or hidden group selection
        String name = appGroups.get(position);
        List<String> appGroupsList = settingsProvider.getAppGroupsSorted(false);
        if (appGroupsList.size() + 1 == position) {
            name = settingsProvider.addGroup();
        } else if (appGroupsList.size() == position) {
            name = HIDDEN_GROUP;
        }

        // move app into group
        Map<String, String> apps = settingsProvider.getAppList();
        apps.remove(packageName);
        apps.put(packageName, name);
        settingsProvider.setAppList(apps);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.lv_group, parent, false);

            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (position >= MAX_GROUPS - 1) {
            convertView.setVisibility(View.GONE);
        }

        // set menu action
        holder.menu.setOnClickListener(view -> {

            final Map<String, String> apps = settingsProvider.getAppList();
            final Set<String> appGroupsList = settingsProvider.getAppGroups();
            final String oldGroupName = settingsProvider.getAppGroupsSorted(false).get(position);

            AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
            builder.setView(R.layout.dialog_group_details);

            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bkg_dialog);
            dialog.show();

            final EditText groupNameInput = dialog.findViewById(R.id.group_name);
            groupNameInput.setText(oldGroupName);

            dialog.findViewById(R.id.ok).setOnClickListener(view1 -> {
                String newGroupName = groupNameInput.getText().toString();
                if (newGroupName.length() > 0) {
                    appGroupsList.remove(oldGroupName);
                    appGroupsList.add(newGroupName);
                    Map<String, String> updatedAppList = new HashMap<>();
                    for (String packageName : apps.keySet()) {
                        if (apps.get(packageName).compareTo(oldGroupName) == 0) {
                            updatedAppList.put(packageName, newGroupName);
                        } else {
                            updatedAppList.put(packageName, apps.get(packageName));
                        }
                    }
                    HashSet<String> selectedGroup = new HashSet<>();
                    selectedGroup.add(newGroupName);
                    settingsProvider.setSelectedGroups(selectedGroup);
                    settingsProvider.setAppGroups(appGroupsList);
                    settingsProvider.setAppList(updatedAppList);
                    mainActivity.reloadUI();
                }
                dialog.dismiss();
            });

            dialog.findViewById(R.id.group_delete).setOnClickListener(view12 -> {
                HashMap<String, String> newAppList = new HashMap<>();
                for (String packageName : apps.keySet()) {
                    if (oldGroupName.equals(apps.get(packageName))) {
                        newAppList.put(packageName, HIDDEN_GROUP);
                    } else {
                        newAppList.put(packageName, apps.get(packageName));
                    }
                }
                settingsProvider.setAppList(newAppList);

                appGroupsList.remove(oldGroupName);
                settingsProvider.setAppGroups(appGroupsList);

                Set<String> firstSelectedGroup = new HashSet<>();
                try{
                    firstSelectedGroup.add(settingsProvider.getAppGroupsSorted(false).get(0));
                    settingsProvider.setSelectedGroups(firstSelectedGroup);
                }catch(IndexOutOfBoundsException e){
                    settingsProvider.resetGroups();
                }
                mainActivity.reloadUI();
                dialog.dismiss();
            });
        });

        // set the look
        setLook(position, convertView, holder.menu);

        // set drag and drop
        final View finalConvertView = convertView;
        convertView.setOnDragListener((view, event) -> {
            if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                int[] colors = new int[]{Color.argb(192, 128, 128, 255), Color.TRANSPARENT};
                GradientDrawable.Orientation orientation = GradientDrawable.Orientation.LEFT_RIGHT;
                finalConvertView.setBackground(new GradientDrawable(orientation, colors));
            } else if (event.getAction() == DragEvent.ACTION_DRAG_EXITED) {
                setLook(position, finalConvertView, holder.menu);
            } else if (event.getAction() == DragEvent.ACTION_DROP) {
                // add group or hidden group selection
                String packageName = mainActivity.getSelectedPackage();
                setGroup(mainActivity.getSelectedPackage(), position);

                // false to dragged icon fly back
                Set<String> selectedGroup = settingsProvider.getSelectedGroups();
                return !selectedGroup.contains(packageName);
            }
            return true;
        });


        // set value into textview
        TextView textView = convertView.findViewById(R.id.textLabel);
        if (HIDDEN_GROUP.equals(appGroups.get(position))) {
            String hiddenText = " -  " + mainActivity.getString(R.string.apps_hidden);
            textView.setText(hiddenText);
        } else {
            textView.setText(appGroups.get(position));
        }

        return convertView;
    }

    private void setLook(int position, View itemView, View menu) {
        View filler = mainActivity.findViewById(R.id.filler);
        View topBar = mainActivity.findViewById(R.id.topBar);
        View pi = mainActivity.findViewById(R.id.pi);
        View sort = mainActivity.findViewById(R.id.sort);
        View update = mainActivity.findViewById(R.id.update);
        int gap = (topBar.getWidth()-pi.getWidth()-sort.getWidth()-(update!=null?update.getWidth():0))%getCount();
        filler.setMinimumWidth(gap);
        boolean isSelected = selectedGroups.contains(appGroups.get(position));
        if (isSelected) {
            int[] colors = new int[] {Color.argb(192, 255, 255, 255), Color.TRANSPARENT};
            GradientDrawable.Orientation orientation = GradientDrawable.Orientation.TOP_BOTTOM;
            itemView.setBackground(new GradientDrawable(orientation, colors));
            if(position==getCount()-1){
                filler.setBackground(new GradientDrawable(orientation, colors));
            }
            if (isEditMode && (position < getCount() - 2)) {
                menu.setVisibility(View.VISIBLE);
            } else {
                menu.setVisibility(View.GONE);
            }
        } else {
            itemView.setBackgroundColor(Color.TRANSPARENT);
            menu.setVisibility(View.GONE);
            if(position==getCount()-1){
                filler.setBackgroundColor(Color.TRANSPARENT);
        }
    }
}
}
