package icar.a5i4s.com.cashierb.helper;

import android.util.Log;

/**
 * Created by light on 2017/2/27.
 */

public class Logg {
    public static boolean POWER = false;
    public static void i(String title, String text){
        if (POWER){
            Log.i(title, text);
        } else {
            return;
        }
    }
}
