package com.doubao.calendar2026;

import android.content.Context;

/** 尺寸与配色统一在这里，改一处即可换肤。 */
public final class Ui {
    // 颜色
    public static final int RED = 0xFFE2342F;          // 节假日主色
    public static final int RED_BG = 0xFFFFEFEC;       // 节假日浅底
    public static final int BLUE = 0xFF2F6BD9;         // 调休上班主色
    public static final int BLUE_BG = 0xFFEDF3FF;      // 调休浅底
    public static final int TEXT = 0xFF16181C;         // 主文字
    public static final int TEXT_SUB = 0xFF8F959E;     // 次要文字
    public static final int WEEKEND = 0xFF8A9099;      // 周末文字
    public static final int PAGE_BG = 0xFFFFFFFF;
    public static final int TRACK = 0xFFF1F3F6;        // 分段控件轨道
    public static final int DIVIDER = 0xFFEDEFF2;

    private Ui() {
    }

    public static float dp(Context c, float v) {
        return v * c.getResources().getDisplayMetrics().density;
    }

    public static int dpi(Context c, float v) {
        return Math.round(dp(c, v));
    }
}
