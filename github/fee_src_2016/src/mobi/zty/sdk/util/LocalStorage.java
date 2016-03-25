package mobi.zty.sdk.util;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalStorage {

    public static final String STORAGE_NAME = "ZTY";

    private SharedPreferences sharedPreferences;

    private LocalStorage(Context context) {
        sharedPreferences = context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
    }

    public static LocalStorage getInstance(Context context) {
        return new LocalStorage(context);
    }

    public boolean hasKey(String key) {
        return !Helper.isEmpty(key)
                && sharedPreferences.contains(key);
    }

    public void putString(String key, String value) {

        if (Helper.isEmpty(key)
        		/* || StringUtil.isEmpty(value)*/)
            return;
        sharedPreferences.edit().putString(key, value).commit();

    }
    
    public void putInt(String key,  int value) {

        if (Helper.isEmpty(key)
        		/* || StringUtil.isEmpty(value)*/)
            return;
        sharedPreferences.edit().putInt(key, value).commit();

    }

    public String getString(String key, String... defaultValues) {

        if (Helper.isEmpty(key)) return null;
        String defaultValue =
                defaultValues.length >= 1 ?
                        defaultValues[0] : "";
        return sharedPreferences.getString(key, defaultValue);

    }
    
    public int getInt(String key, int defaultValue) {
        if (Helper.isEmpty(key)) return 0;
        return sharedPreferences.getInt(key, defaultValue);
    }

}
