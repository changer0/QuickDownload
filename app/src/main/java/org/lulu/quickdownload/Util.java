package org.lulu.quickdownload;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * author: changer0
 * date: 2022/6/27
 */
public class Util {
    public static int getScreenWidth() {
        Resources resources = Resources.getSystem();
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int dp2px(float dipValue) {
        Resources resources = Resources.getSystem();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dipValue
                , resources.getDisplayMetrics());
    }
}
