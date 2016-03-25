package mobi.zty.sdk.util;

import android.content.Context;
import android.util.TypedValue;

public class MetricUtil {

    public static int getDip(Context context, float param) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, param, context.getResources().getDisplayMetrics());
    }

}
