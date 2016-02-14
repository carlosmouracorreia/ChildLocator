package pt.tecnico.childlocator.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * Created by carloscorreia on 28-11-2015.
 */
public class SessionManager {
    private static String TAG = SessionManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    Editor editor;
    Context _context;

    private static final String PREF_NAME = "ChildLocator";
    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";
    public static final String KEY_CHILD_ID = "childId";
    private static final String KEY_CHILD_NAME = "childName";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_PARENT_ID = "parentId";
    private static final String KEY_DANGER_CHILD_ID = "dangerChildId";


    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    public void setId(long id) {
        editor.putLong(KEY_CHILD_ID, id);
        editor.commit();
    }

    public void setDangerChildId(int id) {
        editor.putInt(KEY_DANGER_CHILD_ID, id);
        editor.commit();
    }

    public void setParentId(long id) {
        editor.putLong(KEY_PARENT_ID, id);
        editor.commit();
    }

    public void setName(String name) {
        editor.putString(KEY_CHILD_NAME, name);
        editor.commit();
    }


    public void setLastLatitude(float lat) {
        editor.putFloat(KEY_LATITUDE, lat);
        editor.commit();
    }

    public void setLastLongitude(float lon) {
        editor.putFloat(KEY_LONGITUDE, lon);
        editor.commit();
    }

    public void removeAllData() {
        editor.clear();
        editor.commit();
    }
    public String getChildName() {
        return pref.getString(KEY_CHILD_NAME,"");
    }

    public long getChildId() {
        return pref.getLong(KEY_CHILD_ID, 0);
    }
    public int getDangerChildId() {
        return pref.getInt(KEY_DANGER_CHILD_ID, 0);
    }
    public long getParentId() {
        return pref.getLong(KEY_PARENT_ID, 0);
    }


    public float getLastLatitude() {
        return pref.getFloat(KEY_LATITUDE,0);
    }
    public float getLastLongitude() {
        return pref.getFloat(KEY_LONGITUDE,0);
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }
}