package maderski.bluetoothautoplaymusic.UI;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

import maderski.bluetoothautoplaymusic.R;
import maderski.bluetoothautoplaymusic.SharedPrefs.BAPMPreferences;

public class MapsFragment extends Fragment {
    private static final String TAG = "MapsFragment";

    public MapsFragment() {
        // Required empty public constructor
    }

    public static MapsFragment newInstance() {
        MapsFragment fragment = new MapsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);

        Typeface typeface_bold = Typeface.createFromAsset(getContext().getAssets(), "fonts/TitilliumText600wt.otf");
        TextView textView = (TextView)rootView.findViewById(R.id.daysToLaunchLabel);
        textView.setTypeface(typeface_bold);

        setDaysToLaunchLabel(rootView);
        setCheckBoxes(rootView);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void setDaysToLaunchLabel(View view){
        String mapChoice = BAPMPreferences.getMapsChoice(getContext());
        PackageManager packageManager = getActivity().getPackageManager();
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(mapChoice, 0);
            mapChoice = packageManager.getApplicationLabel(appInfo).toString();
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
            mapChoice = "Maps";
        }

        TextView textView = (TextView)view.findViewById(R.id.daysToLaunchLabel);
        textView.setText("DAYS to launch " + mapChoice);
    }

    private void setCheckBoxes(View view){
        CheckBox checkBox;
        String[] entireWeek = {"1", "2", "3", "4", "5", "6", "7"};
        Set<String> daysToLaunchSet = BAPMPreferences.getDaysToLaunchMaps(getActivity());

        LinearLayout daysToLaunchChkBoxLL = (LinearLayout) view.findViewById(R.id.daysChkBoxLL);
        daysToLaunchChkBoxLL.removeAllViews();

        for(String day : entireWeek){
            checkBox = new CheckBox(getActivity());
            checkBox.setText(getNameOfDay(day));
            checkBox.setTextColor(getResources().getColor(R.color.colorPrimary));
            checkBox.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/TitilliumText400wt.otf"));
            checkBox.setChecked(daysToLaunchSet.contains(day));
            checkboxListener(checkBox, day);
            daysToLaunchChkBoxLL.addView(checkBox);
        }
    }

    private void checkboxListener(CheckBox checkBox, String dayNumber){
        final Context ctx = getActivity();
        final CheckBox cb = checkBox;
        final String dn = dayNumber;

        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<String> daysToLaunch = new HashSet<>(BAPMPreferences.getDaysToLaunchMaps(ctx));
                if(cb.isChecked()){
                    daysToLaunch.add(dn);
                    BAPMPreferences.setDaysToLaunchMaps(ctx, daysToLaunch);
                }else{
                    daysToLaunch.remove(dn);
                    BAPMPreferences.setDaysToLaunchMaps(ctx, daysToLaunch);
                }
            }
        });
    }

    private String getNameOfDay(String dayNumber){
        switch(dayNumber){
            case "1":
                return "Sunday";
            case "2":
                return "Monday";
            case "3":
                return "Tuesday";
            case "4":
                return "Wednesday";
            case "5":
                return "Thursday";
            case "6":
                return "Friday";
            case "7":
                return "Saturday";
            default:
                return "Unknown Day Number";
        }
    }
}
