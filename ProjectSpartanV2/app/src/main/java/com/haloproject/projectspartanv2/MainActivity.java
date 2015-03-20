package com.haloproject.projectspartanv2;

import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.haloproject.bluetooth.AndroidBlue;

public class MainActivity extends ActionBarActivity {
    static private FragmentManager mFragmentManager;
    static private AndroidBlue mAndroidBlue;
    final int TOTAL_SWIPE_FRAGMENTS = 4;
    static private int currentFragment; //-1 means its at main menu
    private float x1, x2, y1, y2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            mFragmentManager.beginTransaction()
                    .add(R.id.container, new MainFragment())
                    .commit();
        }
        AndroidBlue.setContext(getApplicationContext());
        AndroidBlue.setActivity(this);
        mAndroidBlue = AndroidBlue.getInstance();
        currentFragment = -1;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                y2 = event.getY();
                if (x2 - x1 > 600) {
                    if (currentFragment != -1 && currentFragment > 0) {
                        currentFragment -= 1;
                        mFragmentManager.beginTransaction()
                                .replace(R.id.container, swipeFragment(currentFragment))
                                .commit();
                    }
                } else if (x1 - x2 > 600) {
                    if (currentFragment != -1 && currentFragment < TOTAL_SWIPE_FRAGMENTS) {
                        currentFragment += 1;
                        mFragmentManager.beginTransaction()
                                .replace(R.id.container, swipeFragment(currentFragment))
                                .commit();
                    }
                } else if (y2 - y1 > 400) {
                    if (mFragmentManager.getBackStackEntryCount() != 0) {
                        currentFragment = -1;
                        mFragmentManager.popBackStack();
                        mFragmentManager.beginTransaction()
                                .replace(R.id.container, new MainFragment())
                                .commit();
                    }
                }
                break;
        }
        return true;
    }

    private Fragment swipeFragment(int fragment) {
        switch (fragment) {
            case 0:
                return new CoolingFragment();
            case 1:
                return new LightingFragment();
            case 2:
                return new RadarFragment();
            case 3:
                return new SettingsFragment();
            default:
                return new MainFragment();
        }
    }

    public void tempCool(View view) {
        currentFragment = 0;
        mFragmentManager.beginTransaction()
                .replace(R.id.container, swipeFragment(currentFragment))
                .addToBackStack("test").commit();
    }

    public void settings(View view) {
        currentFragment = 3;
        mFragmentManager.beginTransaction()
                .replace(R.id.container, swipeFragment(currentFragment))
                .addToBackStack("test").commit();
    }

    public void lighting(View view) {
        currentFragment = 1;
        mFragmentManager.beginTransaction()
                .replace(R.id.container, swipeFragment(currentFragment))
                .addToBackStack("test").commit();
    }

    public void radar(View view) {
        currentFragment = 2;
        mFragmentManager.beginTransaction()
                .replace(R.id.container, swipeFragment(currentFragment))
                .addToBackStack("test").commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    static public class LightingFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_lighting, container, false);
        }
    }

    static public class RadarFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_radar, container, false);
        }
    }


    static public class MainFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            currentFragment = -1;
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_main, container, false);
        }
    }

    static public class CoolingFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            final View view = inflater.inflate(R.layout.fragment_cooling, container, false);

            mAndroidBlue.setOnReceive(new Runnable() {
                @Override
                public void run() {
                    TextView headtemp = (TextView) view.findViewById(R.id.headtemp);
                    headtemp.setText(String.format("%.2f", mAndroidBlue.headTemperature.getValue()));
                }
            });
            return view;
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mAndroidBlue.destroyOnReceive();
        }
    }

    static public class SettingsFragment extends Fragment {
        private ListView btdevices;
        //private ArrayAdapter<BluetoothDevice> mArrayAdapter;
        private Switch switch1;
        private Button discover;
        private Button configure;
        private View view;
        private RadioButton connected;

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            view = inflater.inflate(R.layout.fragment_settings, container, false);
            switch1 = (Switch) view.findViewById(R.id.switch1);
            switch1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean on = ((Switch)v).isChecked();
                    if (on) {
                        mAndroidBlue.enableBluetooth();
                    } else {
                        mAndroidBlue.disableBluetooth();
                    }
                }
            });
            btdevices = (ListView) view.findViewById(R.id.btdevices);
            btdevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    RadioGroup group = ((RadioGroup) view.findViewById(R.id.radioGroup));
                    RadioButton bb   = ((RadioButton) group.findViewById(R.id.setbeaglebone));
                    RadioButton gg   = ((RadioButton) group.findViewById(R.id.setgoogleglass));
                    if (bb.isChecked()) {
                        mAndroidBlue.setBeagleBone(position);
                        ((TextView)view.findViewById(R.id.beaglebone)).setText(mAndroidBlue.getBeagleBone().getAddress());
                        if (mAndroidBlue.setDevice(position)) {
                            mAndroidBlue.connect();
                        }
                    } else if (gg.isChecked()) {
                        mAndroidBlue.setGoogleGlass(position);
                        ((TextView)view.findViewById(R.id.googleglass)).setText(mAndroidBlue.getGoogleGlass().getAddress());
                    }
                }
            });
            discover = (Button) view.findViewById(R.id.discover);
            discover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btdevices.setAdapter(mAndroidBlue.getDeviceStrings());
                    mAndroidBlue.startDiscovery();
                }
            });
            connected = (RadioButton) view.findViewById(R.id.connected);
            if (mAndroidBlue.isConnected()) {
                connected.setChecked(true);
            } else {
                connected.setChecked(false);
            }
            connected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (mAndroidBlue.isConnected()) {
                        ((RadioButton) buttonView).setChecked(true);
                    } else {
                        ((RadioButton) buttonView).setChecked(false);
                    }
                }
            });
            mAndroidBlue.setOnConnect(new Runnable() {
                @Override
                public void run() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Connected", Toast.LENGTH_LONG).show();
                            connected.setChecked(true);
                        }
                    });
                }
            });
            configure = (Button) view.findViewById(R.id.configure);
            configure.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAndroidBlue.sendConfiguration();
                }
            });

            return view;
        }

        @Override
        public void onStart() {
            super.onStart();
            if (mAndroidBlue.isEnabled()) {
                switch1.setChecked(true);
            } else {
                switch1.setChecked(false);
            }
        }
    }


}