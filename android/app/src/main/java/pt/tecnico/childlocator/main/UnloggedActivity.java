package pt.tecnico.childlocator.main;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;


import pt.tecnico.childlocator.external.SlidingTabLayout;
import pt.tecnico.childlocator.helper.SessionManager;
import pt.tecnico.childlocator.main.unlogged.ChildFrag;
import pt.tecnico.childlocator.main.unlogged.LoginFrag;
import pt.tecnico.childlocator.main.unlogged.RegisterFrag;
import pt.tecnico.childlocator.services.CoordsIntervalReceiver;


public class UnloggedActivity extends ActionBarActivity {


    private ViewPager pager;
    private TextView normal,warning;
    private TabsPagerAdapter adapter;
    private SlidingTabLayout tabs;
    private static UnloggedActivity mInstance;
    private SessionManager session;
    public static final String TAG = "UnloggedActivity";


    public static synchronized UnloggedActivity getInstance() {
        return mInstance;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //In case the child has logged out (by deleting app data) we stop current alarms (that request location) set by this app
        cancelCoordsAlarm();
        // In case we have login data, we go to child mode, which is the only one that persists data
        session = new SessionManager(getApplicationContext());
        if (session.isLoggedIn()) {
            Intent intent = new Intent(UnloggedActivity.this,
                    ChildActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_unlogged);

        normal = (TextView) findViewById(R.id.normalT);
        warning = (TextView) findViewById(R.id.warningT);
        if(session.getDangerChildId()!=0) {
            normal.setVisibility(View.GONE);
            warning.setVisibility(View.VISIBLE);
        }

        pager = (ViewPager) findViewById(R.id.pager);
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        adapter = new TabsPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);

        tabs.setDistributeEvenly(true);
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });
        tabs.setViewPager(pager);
        mInstance = this;
    }

    public void cancelCoordsAlarm() {
        Intent intent = new Intent(getApplicationContext(), CoordsIntervalReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, 0,
                intent, 0);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }


    public ViewPager getPager() { return pager; }

    public class TabsPagerAdapter extends FragmentStatePagerAdapter {

        public TabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment frag;
            switch (position) {
                case 0:
                    frag = new LoginFrag();
                    return frag;
                case 1:
                    frag = new ChildFrag();
                    return frag;
                case 2:
                    frag = new RegisterFrag();
                    return frag;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = "";
            switch (position) {
                case 0:
                    title = getString(R.string.unlogged_tab_parent);
                    break;
                case 1:
                    title = getString(R.string.unlogged_tab_child);
                    break;
                case 2:
                    title = getString(R.string.unlogged_tab_register);
                    break;
            }

            return title;
        }
    }
}
