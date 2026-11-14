package com.doubao.calendar2026;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 2026 年中国法定节假日数据（依据 国办发明电〔2025〕7号）。
 * 数据一次性构建好，供月视图 / 年视图共用。
 */
public final class HolidayData {

    public static final int YEAR = 2026;

    public static final int TYPE_NORMAL = 0;   // 普通工作日
    public static final int TYPE_HOLIDAY = 1;  // 法定节假日放假
    public static final int TYPE_MAKEUP = 2;   // 调休上班日（周末上班）

    private HolidayData() {
    }

    /** 一天的完整信息 */
    public static class DayInfo {
        public int month;
        public int day;
        public int type = TYPE_NORMAL;
        public String holidayName = null; // 节假日名称，调休日为 "调休"
        public boolean weekend = false;   // 自然周日/周六
        public int weekIndex = 0;         // 0=周一 ... 6=周日
    }

    /** 一个假期区间 */
    public static class HolidayBlock {
        public final String name;
        public final int startMonth, startDay, endMonth, endDay;
        public final int days;
        public final String rangeText;

        HolidayBlock(String name, int sm, int sd, int em, int ed, int days, String rangeText) {
            this.name = name;
            this.startMonth = sm;
            this.startDay = sd;
            this.endMonth = em;
            this.endDay = ed;
            this.days = days;
            this.rangeText = rangeText;
        }
    }

    /** 七个法定节假日安排 */
    public static final List<HolidayBlock> BLOCKS = new ArrayList<>();

    static {
        BLOCKS.add(new HolidayBlock("元旦", 1, 1, 1, 3, 3, "1月1日–1月3日"));
        BLOCKS.add(new HolidayBlock("春节", 2, 15, 2, 23, 9, "2月15日–2月23日"));
        BLOCKS.add(new HolidayBlock("清明节", 4, 4, 4, 6, 3, "4月4日–4月6日"));
        BLOCKS.add(new HolidayBlock("劳动节", 5, 1, 5, 5, 5, "5月1日–5月5日"));
        BLOCKS.add(new HolidayBlock("端午节", 6, 19, 6, 21, 3, "6月19日–6月21日"));
        BLOCKS.add(new HolidayBlock("中秋节", 9, 25, 9, 27, 3, "9月25日–9月27日"));
        BLOCKS.add(new HolidayBlock("国庆节", 10, 1, 10, 7, 7, "10月1日–10月7日"));
    }

    /** 调休上班日（MMDD） */
    private static final int[] MAKEUP_DAYS = {104, 214, 228, 509, 920, 1010};

    private static final LinkedHashMap<Integer, DayInfo> MAP = new LinkedHashMap<>();

    static {
        // 1. 先按自然日填充整年
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(YEAR, Calendar.JANUARY, 1);
        int total = c.getActualMaximum(Calendar.DAY_OF_YEAR);
        for (int i = 0; i < total; i++) {
            DayInfo info = new DayInfo();
            info.month = c.get(Calendar.MONTH) + 1;
            info.day = c.get(Calendar.DAY_OF_MONTH);
            info.weekIndex = c.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY < 0
                    ? 6 : c.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
            info.weekend = info.weekIndex >= 5;
            MAP.put(key(info.month, info.day), info);
            c.add(Calendar.DAY_OF_MONTH, 1);
        }

        // 2. 打上法定节假日标记
        for (HolidayBlock b : BLOCKS) {
            Calendar s = Calendar.getInstance();
            s.clear();
            s.set(YEAR, b.startMonth - 1, b.startDay);
            Calendar e = Calendar.getInstance();
            e.clear();
            e.set(YEAR, b.endMonth - 1, b.endDay);
            while (!s.after(e)) {
                DayInfo info = MAP.get(key(s.get(Calendar.MONTH) + 1, s.get(Calendar.DAY_OF_MONTH)));
                if (info != null) {
                    info.type = TYPE_HOLIDAY;
                    info.holidayName = b.name;
                }
                s.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        // 3. 打上调休上班标记（覆盖周末属性）
        for (int mmdd : MAKEUP_DAYS) {
            DayInfo info = MAP.get(mmdd);
            if (info != null) {
                info.type = TYPE_MAKEUP;
                info.holidayName = "调休";
            }
        }
    }

    private static int key(int month, int day) {
        return month * 100 + day;
    }

    public static DayInfo get(int month, int day) {
        return MAP.get(key(month, day));
    }

    /** 某月有几天 */
    public static int daysInMonth(int month) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(YEAR, month - 1, 1);
        return c.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /** 某月 1 号是周几：0=周一 ... 6=周日 */
    public static int firstWeekIndex(int month) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(YEAR, month - 1, 1);
        int dow = c.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
        return dow < 0 ? 6 : dow;
    }

    /** 与某月相交的假期 */
    public static List<HolidayBlock> blocksInMonth(int month) {
        List<HolidayBlock> list = new ArrayList<>();
        for (HolidayBlock b : BLOCKS) {
            if (month >= b.startMonth && month <= b.endMonth) {
                list.add(b);
            }
        }
        return list;
    }

    /** 某月内的假期天数（含调休上班倒扣不影响，仅统计放假日） */
    public static int holidayCountInMonth(int month) {
        int n = 0;
        for (DayInfo info : MAP.values()) {
            if (info.month == month && info.type == TYPE_HOLIDAY) {
                n++;
            }
        }
        return n;
    }

    /** 全年放假总天数 */
    public static int totalHolidayDays() {
        int n = 0;
        for (DayInfo info : MAP.values()) {
            if (info.type == TYPE_HOLIDAY) {
                n++;
            }
        }
        return n;
    }

    /** 全年调休上班天数 */
    public static int totalMakeupDays() {
        return MAKEUP_DAYS.length;
    }

    /**
     * 从指定日期（含当天）向后找下一个包含该日期或之后的假期。
     */
    public static HolidayBlock nextBlockFrom(int month, int day) {
        for (HolidayBlock b : BLOCKS) {
            if (b.endMonth > month || (b.endMonth == month && b.endDay >= day)) {
                return b;
            }
        }
        return null;
    }

    /**
     * 距离下一个假期开始还有几天；若已在假期中返回 0-1 之间的约定值（0 表示今天在假期内）。
     * 若之后没有假期返回 -1。
     */
    public static int daysUntilNextHoliday(int month, int day) {
        for (HolidayBlock b : BLOCKS) {
            int start = b.startMonth * 100 + b.startDay;
            int now = month * 100 + day;
            int end = b.endMonth * 100 + b.endDay;
            if (now >= start && now <= end) {
                return 0;
            }
            if (start > now) {
                Calendar nowC = Calendar.getInstance();
                nowC.clear();
                nowC.set(YEAR, month - 1, day);
                Calendar tarC = Calendar.getInstance();
                tarC.clear();
                tarC.set(YEAR, b.startMonth - 1, b.startDay);
                long diff = (tarC.getTimeInMillis() - nowC.getTimeInMillis()) / (24L * 3600 * 1000);
                return (int) diff;
            }
        }
        return -1;
    }
}
