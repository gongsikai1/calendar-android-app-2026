package com.doubao.calendar2026;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

/**
 * 2026 日历：一屏看全年国假，月视图 + 年视图双模式。
 */
public class MainActivity extends Activity {

    private LinearLayout monthContainer;
    private LinearLayout yearContainer;
    private TextView titleMonth;
    private TextView btnPrev;
    private TextView btnNext;
    private TextView segMonth;
    private TextView segYear;
    private MonthView monthView;
    private YearView yearView;
    private ScrollView monthScrollRef;

    private TextView infoTags;
    private TextView infoBody;
    private TextView infoCountdown;
    private TextView infoSelected;
    private TextView cardTitle;
    private TextView cardRight;

    private int month = 1;
    private boolean monthMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applySystemBar();
        setContentView(buildRoot());
        updateSegment(true);
        goMonth(initialMonth());
    }

    private int initialMonth() {
        Calendar c = Calendar.getInstance();
        if (c.get(Calendar.YEAR) == HolidayData.YEAR) {
            return c.get(Calendar.MONTH) + 1;
        }
        return 1;
    }

    private void applySystemBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
            getWindow().setNavigationBarColor(Color.WHITE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    // ---------------- 界面搭建 ----------------

    private View buildRoot() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.PAGE_BG);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            root.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                    int top = insets.getSystemWindowInsetTop();
                    v.setPadding(0, top, 0, 0);
                    return insets;
                }
            });
        }

        root.addView(buildHeader());

        FrameLayout content = new FrameLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        root.addView(content, lp);

        monthContainer = buildMonthContainer();
        monthScrollRef = new ScrollView(this);
        monthScrollRef.setFillViewport(false);
        monthScrollRef.setOverScrollMode(View.OVER_SCROLL_NEVER);
        monthScrollRef.addView(monthContainer, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        content.addView(monthScrollRef, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        yearContainer = buildYearContainer();
        ScrollView yearScroll = new ScrollView(this);
        yearScroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        yearScroll.addView(yearContainer);
        content.addView(yearScroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        yearScroll.setVisibility(View.GONE);

        return root;
    }

    private View buildHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(20), dp(14), dp(20), dp(10));

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout titleCol = new LinearLayout(this);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        TextView title = text("2026 日历", 25, Ui.TEXT, true);
        title.setLetterSpacing(0.03f);
        TextView sub = text("中国法定节假日 · 全年放假安排", 12, Ui.TEXT_SUB, false);
        sub.setPadding(0, dp(4), 0, 0);
        titleCol.addView(title);
        titleCol.addView(sub);
        titleRow.addView(titleCol, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView todayBtn = text("今天", 12, Ui.TEXT, true);
        todayBtn.setGravity(Gravity.CENTER);
        todayBtn.setPadding(dp(14), dp(7), dp(14), dp(7));
        GradientDrawable pill = new GradientDrawable();
        pill.setColor(Ui.TRACK);
        pill.setCornerRadius(dp(14));
        todayBtn.setBackground(pill);
        todayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                int m = c.get(Calendar.YEAR) == HolidayData.YEAR ? c.get(Calendar.MONTH) + 1 : 1;
                updateSegment(true);
                goMonth(m);
                if (c.get(Calendar.YEAR) == HolidayData.YEAR) {
                    monthView.setSelectedDay(c.get(Calendar.DAY_OF_MONTH));
                }
            }
        });
        titleRow.addView(todayBtn, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        header.addView(titleRow);

        // 分段控件：月 / 年
        LinearLayout seg = new LinearLayout(this);
        seg.setOrientation(LinearLayout.HORIZONTAL);
        GradientDrawable track = new GradientDrawable();
        track.setColor(Ui.TRACK);
        track.setCornerRadius(dp(19));
        seg.setBackground(track);
        seg.setPadding(dp(3), dp(3), dp(3), dp(3));

        segMonth = text("月视图", 13, Ui.TEXT, true);
        segYear = text("年视图", 13, Ui.TEXT, true);
        segMonth.setGravity(Gravity.CENTER);
        segYear.setGravity(Gravity.CENTER);
        segMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSegment(true);
            }
        });
        segYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSegment(false);
            }
        });
        seg.addView(segMonth, new LinearLayout.LayoutParams(0, dp(34), 1f));
        seg.addView(segYear, new LinearLayout.LayoutParams(0, dp(34), 1f));

        LinearLayout.LayoutParams segLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        segLp.topMargin = dp(12);
        header.addView(seg, segLp);
        return header;
    }

    private void updateSegment(boolean monthMode) {
        this.monthMode = monthMode;
        segMonth.setBackground(monthMode ? segPill(true) : null);
        segYear.setBackground(monthMode ? null : segPill(false));
        segMonth.setTextColor(monthMode ? Ui.TEXT : Ui.TEXT_SUB);
        segYear.setTextColor(monthMode ? Ui.TEXT_SUB : Ui.TEXT);
        monthScrollRef.setVisibility(monthMode ? View.VISIBLE : View.GONE);
        yearContainer.setVisibility(monthMode ? View.GONE : View.VISIBLE);
        ScrollView ys = (ScrollView) yearContainer.getParent();
        ys.setVisibility(monthMode ? View.GONE : View.VISIBLE);
        if (!monthMode) {
            yearView.setHighlightMonth(month);
            ys.scrollTo(0, 0);
        }
    }

    private GradientDrawable segPill(boolean left) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(Color.WHITE);
        d.setCornerRadius(dp(16));
        return d;
    }

    private LinearLayout buildMonthContainer() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);

        // 月份导航
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER_VERTICAL);
        nav.setPadding(dp(20), dp(6), dp(20), dp(2));

        btnPrev = text("‹", 24, Ui.TEXT, false);
        btnNext = text("›", 24, Ui.TEXT, false);
        btnPrev.setGravity(Gravity.CENTER);
        btnNext.setGravity(Gravity.CENTER);
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goMonth(month - 1);
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goMonth(month + 1);
            }
        });

        titleMonth = text("2026年1月", 17, Ui.TEXT, true);
        titleMonth.setGravity(Gravity.CENTER);

        nav.addView(btnPrev, new LinearLayout.LayoutParams(dp(34), dp(34)));
        nav.addView(titleMonth, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        nav.addView(btnNext, new LinearLayout.LayoutParams(dp(34), dp(34)));
        container.addView(nav);

        // 月历本体
        monthView = new MonthView(this);
        monthView.setListener(new MonthView.Listener() {
            @Override
            public void onDaySelected(HolidayData.DayInfo info, int m) {
                showSelected(info);
            }

            @Override
            public void onMonthChanged(int delta) {
                goMonth(month + delta);
            }
        });
        LinearLayout.LayoutParams mvLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mvLp.leftMargin = dp(12);
        mvLp.rightMargin = dp(12);
        container.addView(monthView, mvLp);

        // 图例
        container.addView(buildLegend());

        // 信息卡
        container.addView(buildInfoCard());
        return container;
    }

    private View buildLegend() {
        LinearLayout legend = new LinearLayout(this);
        legend.setOrientation(LinearLayout.HORIZONTAL);
        legend.setGravity(Gravity.CENTER);
        legend.setPadding(dp(16), dp(2), dp(16), dp(8));
        legend.addView(legendItem(Ui.RED, Ui.RED_BG, "法定节假日"));
        legend.addView(legendItem(Ui.BLUE, Ui.BLUE_BG, "调休上班"));
        legend.addView(legendItem(Ui.TEXT_SUB, 0, "周末"));
        return legend;
    }

    private View legendItem(int dotColor, int bgColor, String label) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.CENTER_VERTICAL);
        View dot = new View(this);
        GradientDrawable d = new GradientDrawable();
        d.setColor(bgColor != 0 ? bgColor : 0x00000000);
        d.setCornerRadius(dp(4));
        d.setStroke(dp(1.2f), dotColor);
        dot.setBackground(d);
        LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dp(9), dp(9));
        LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        itemLp.rightMargin = dp(16);
        item.addView(dot, dotLp);
        TextView tv = text(label, 11, Ui.TEXT_SUB, false);
        tv.setPadding(dp(5), 0, 0, 0);
        item.addView(tv);
        item.setLayoutParams(itemLp);
        return item;
    }

    private View buildInfoCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFFF7F8FA);
        bg.setCornerRadius(dp(16));
        card.setBackground(bg);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));

        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardLp.leftMargin = dp(16);
        cardLp.rightMargin = dp(16);
        cardLp.topMargin = dp(4);
        cardLp.bottomMargin = dp(20);
        card.setLayoutParams(cardLp);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        cardTitle = text("本月假期", 11, Ui.TEXT_SUB, false);
        cardRight = text("", 11, Ui.TEXT_SUB, false);
        cardRight.setGravity(Gravity.RIGHT);
        row.addView(cardTitle, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(cardRight);
        card.addView(row);

        infoTags = text("", 17, Ui.TEXT, true);
        infoTags.setPadding(0, dp(6), 0, 0);
        card.addView(infoTags);

        infoBody = text("", 12, Ui.TEXT_SUB, false);
        infoBody.setPadding(0, dp(3), 0, 0);
        card.addView(infoBody);

        infoSelected = text("", 12, Ui.BLUE, false);
        infoSelected.setPadding(0, dp(8), 0, 0);
        infoSelected.setVisibility(View.GONE);
        card.addView(infoSelected);

        infoCountdown = text("", 12, Ui.RED, true);
        infoCountdown.setPadding(0, dp(9), 0, 0);
        card.addView(infoCountdown);
        return card;
    }

    private LinearLayout buildYearContainer() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);

        // 概览卡
        LinearLayout summary = new LinearLayout(this);
        summary.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFFF7F8FA);
        bg.setCornerRadius(dp(16));
        summary.setBackground(bg);
        summary.setPadding(dp(16), dp(14), dp(16), dp(14));
        LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sLp.leftMargin = dp(16);
        sLp.rightMargin = dp(16);
        sLp.topMargin = dp(4);
        summary.setLayoutParams(sLp);

        LinearLayout numbers = new LinearLayout(this);
        numbers.setOrientation(LinearLayout.HORIZONTAL);
        numbers.setGravity(Gravity.CENTER_VERTICAL);
        numbers.addView(metric(String.valueOf(HolidayData.totalHolidayDays()), "天放假"),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        View sep = new View(this);
        sep.setBackgroundColor(Ui.DIVIDER);
        numbers.addView(sep, new LinearLayout.LayoutParams(dp(1), dp(28)));
        numbers.addView(metric(String.valueOf(HolidayData.BLOCKS.size()), "个节假日"),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        View sep2 = new View(this);
        sep2.setBackgroundColor(Ui.DIVIDER);
        numbers.addView(sep2, new LinearLayout.LayoutParams(dp(1), dp(28)));
        numbers.addView(metric(String.valueOf(HolidayData.totalMakeupDays()), "天调休"),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        summary.addView(numbers);
        TextView sumTip = text("2026 年放假安排依据国办发明电〔2025〕7号", 10, Ui.TEXT_SUB, false);
        sumTip.setPadding(0, dp(10), 0, 0);
        summary.addView(sumTip);
        container.addView(summary);

        TextView hint = text("红色为法定节假日　蓝色为调休上班　点任意月份可查看月历", 10, Ui.TEXT_SUB, false);
        hint.setPadding(dp(20), dp(9), dp(20), 0);
        container.addView(hint);

        yearView = new YearView(this);
        yearView.setListener(new YearView.Listener() {
            @Override
            public void onMonthPicked(int m) {
                updateSegment(true);
                goMonth(m);
            }
        });
        LinearLayout.LayoutParams yvLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        yvLp.topMargin = dp(10);
        container.addView(yearView, yvLp);

        // 假期清单
        container.addView(buildHolidayList());
        return container;
    }

    private View metric(String value, String label) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER);
        TextView v = text(value, 19, Ui.RED, true);
        v.setGravity(Gravity.CENTER);
        TextView l = text(label, 10, Ui.TEXT_SUB, false);
        l.setGravity(Gravity.CENTER);
        l.setPadding(0, dp(3), 0, 0);
        col.addView(v);
        col.addView(l);
        return col;
    }

    private View buildHolidayList() {
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(16), dp(10), dp(16), dp(24));

        for (final HolidayData.HolidayBlock b : HolidayData.BLOCKS) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(12), dp(13), dp(12), dp(13));
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(0xFFF7F8FA);
            bg.setCornerRadius(dp(12));
            row.setBackground(bg);
            LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rLp.bottomMargin = dp(8);
            row.setLayoutParams(rLp);

            View bar = new View(this);
            GradientDrawable barBg = new GradientDrawable();
            barBg.setColor(Ui.RED);
            barBg.setCornerRadius(dp(2));
            bar.setBackground(barBg);
            row.addView(bar, new LinearLayout.LayoutParams(dp(3), dp(18)));

            LinearLayout cols = new LinearLayout(this);
            cols.setOrientation(LinearLayout.VERTICAL);
            cols.setPadding(dp(10), 0, 0, 0);
            TextView name = text(b.name + "  " + b.rangeText, 13, Ui.TEXT, true);
            TextView sub = text(subText(b), 11, Ui.TEXT_SUB, false);
            sub.setPadding(0, dp(3), 0, 0);
            cols.addView(name);
            cols.addView(sub);
            row.addView(cols, new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView days = text(b.days + "天", 12, Ui.RED, true);
            GradientDrawable daysBg = new GradientDrawable();
            daysBg.setColor(Ui.RED_BG);
            daysBg.setCornerRadius(dp(10));
            days.setBackground(daysBg);
            days.setPadding(dp(9), dp(4), dp(9), dp(4));
            row.addView(days);

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateSegment(true);
                    goMonth(b.startMonth);
                }
            });
            list.addView(row);
        }
        return list;
    }

    private String subText(HolidayData.HolidayBlock b) {
        if ("春节".equals(b.name)) {
            return "腊月二十八至正月初七 · 2月14日、2月28日上班";
        }
        if ("国庆节".equals(b.name)) {
            return "9月20日、10月10日上班 · 与中秋相近但分开安排";
        }
        if ("元旦".equals(b.name)) {
            return "1月4日（周日）上班";
        }
        if ("劳动节".equals(b.name)) {
            return "5月9日（周六）上班";
        }
        return "连休 " + b.days + " 天";
    }

    // ---------------- 交互逻辑 ----------------

    private void goMonth(int m) {
        if (m < 1 || m > 12) return;
        month = m;
        titleMonth.setText(HolidayData.YEAR + "年" + m + "月");
        monthView.setMonth(m);
        yearView.setHighlightMonth(m);
        btnPrev.setTextColor(m > 1 ? Ui.TEXT : 0xFFD8DCE1);
        btnNext.setTextColor(m < 12 ? Ui.TEXT : 0xFFD8DCE1);
        infoSelected.setVisibility(View.GONE);
        updateMonthInfo(m);
    }

    private void updateMonthInfo(int m) {
        List<HolidayData.HolidayBlock> blocks = HolidayData.blocksInMonth(m);
        int holidayDays = HolidayData.holidayCountInMonth(m);
        int makeupDays = makeupInMonth(m);

        cardRight.setText(makeupDays > 0
                ? "放假 " + holidayDays + " 天 · 调休 " + makeupDays + " 天"
                : "放假 " + holidayDays + " 天");

        SpannableStringBuilder ssb = new SpannableStringBuilder();
        if (blocks.isEmpty()) {
            ssb.append("本月无法定节假日");
            infoTags.setTextColor(Ui.TEXT_SUB);
            infoTags.setTextSize(15);
            infoTags.setText(ssb);
            infoBody.setText("正常上班 · 周末双休");
        } else {
            infoTags.setTextColor(Ui.TEXT);
            infoTags.setTextSize(17);
            for (int i = 0; i < blocks.size(); i++) {
                HolidayData.HolidayBlock b = blocks.get(i);
                if (i > 0) ssb.append("、");
                int start = ssb.length();
                ssb.append(b.name);
                ssb.setSpan(new ForegroundColorSpan(Ui.RED), start, ssb.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new StyleSpan(Typeface.BOLD), start, ssb.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (!blocks.isEmpty()) {
                ssb.append(" 假期");
                infoBody.setText(blocks.size() == 1
                        ? blocks.get(0).rangeText + " · 放假 " + blocks.get(0).days + " 天"
                        : "共 " + holidayDays + " 天假期");
            }
            infoTags.setText(ssb);
        }

        // 倒计时
        Calendar today = Calendar.getInstance();
        if (today.get(Calendar.YEAR) == HolidayData.YEAR) {
            int mo = today.get(Calendar.MONTH) + 1;
            int da = today.get(Calendar.DAY_OF_MONTH);
            HolidayData.HolidayBlock next = HolidayData.nextBlockFrom(mo, da);
            int left = HolidayData.daysUntilNextHoliday(mo, da);
            if (next == null) {
                infoCountdown.setText("2026 年的法定节假日已全部结束 · 全年共 "
                        + HolidayData.totalHolidayDays() + " 天假期");
            } else if (left == 0) {
                infoCountdown.setText("今天正在放「" + next.name + "」· 共 " + next.days + " 天");
            } else {
                infoCountdown.setText("距「" + next.name + "」还有 " + left + " 天 · "
                        + next.rangeText);
            }
        } else {
            infoCountdown.setText("全年共 " + HolidayData.totalHolidayDays() + " 天假期 · "
                    + HolidayData.totalMakeupDays() + " 天调休上班");
        }
    }

    private int makeupInMonth(int m) {
        int n = 0;
        for (int d = 1; d <= HolidayData.daysInMonth(m); d++) {
            HolidayData.DayInfo info = HolidayData.get(m, d);
            if (info != null && info.type == HolidayData.TYPE_MAKEUP) n++;
        }
        return n;
    }

    private void showSelected(HolidayData.DayInfo info) {
        if (info == null) return;
        String week = weekName(info.weekIndex);
        String tag;
        if (info.type == HolidayData.TYPE_HOLIDAY) {
            tag = info.holidayName + "假期 · 放假";
            infoSelected.setTextColor(Ui.RED);
        } else if (info.type == HolidayData.TYPE_MAKEUP) {
            tag = "调休上班日（补班）";
            infoSelected.setTextColor(Ui.BLUE);
        } else if (info.weekend) {
            tag = "周末 · 休息";
            infoSelected.setTextColor(Ui.TEXT_SUB);
        } else {
            tag = "工作日";
            infoSelected.setTextColor(Ui.TEXT_SUB);
        }
        infoSelected.setText("已选：" + info.month + "月" + info.day + "日 " + week + " · " + tag);
        infoSelected.setVisibility(View.VISIBLE);
    }

    private String weekName(int idx) {
        String[] names = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        return names[idx];
    }

    // ---------------- 小工具 ----------------

    private TextView text(String s, float spSize, int color, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(s);
        tv.setTextSize(spSize);
        tv.setTextColor(color);
        tv.setIncludeFontPadding(false);
        if (bold) {
            tv.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        }
        return tv;
    }

    private int dp(float v) {
        return Ui.dpi(this, v);
    }
}
