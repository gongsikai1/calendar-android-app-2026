package com.doubao.calendar2026;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Calendar;

/**
 * 月视图：手绘 7 列 × 6 行日历网格，节假日红底、调休蓝底、今天描环。
 * 支持左右滑动切换月份、点击日期回调。
 */
public class MonthView extends View {

    public interface Listener {
        void onDaySelected(HolidayData.DayInfo info, int month);

        /** delta = -1 上一个月，+1 下一个月 */
        void onMonthChanged(int delta);
    }

    private static final String[] WEEK = {"一", "二", "三", "四", "五", "六", "日"};

    private int year = HolidayData.YEAR;
    private int month = 1;
    private int selectedDay = -1;

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private float headerH, cellH, cellW;
    private float downX, downY;
    private boolean moved;

    private int todayYear, todayMonth, todayDay;

    private Listener listener;

    public MonthView(Context context) {
        this(context, null);
    }

    public MonthView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        strokePaint.setStyle(Paint.Style.STROKE);
        Calendar c = Calendar.getInstance();
        todayYear = c.get(Calendar.YEAR);
        todayMonth = c.get(Calendar.MONTH) + 1;
        todayDay = c.get(Calendar.DAY_OF_MONTH);
    }

    public void setListener(Listener l) {
        this.listener = l;
    }

    public void setMonth(int month) {
        this.month = month;
        this.selectedDay = -1;
        invalidate();
    }

    public int getMonth() {
        return month;
    }

    public void setSelectedDay(int day) {
        this.selectedDay = day;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        headerH = Ui.dp(getContext(), 30);
        cellH = Ui.dp(getContext(), 56);
        cellW = w / 7f;
        int h = (int) (headerH + cellH * 6 + Ui.dp(getContext(), 4));
        setMeasuredDimension(w, resolveSize(h, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawWeekHeader(canvas);
        drawDays(canvas);
    }

    private void drawWeekHeader(Canvas canvas) {
        textPaint.setTextSize(Ui.dp(getContext(), 12));
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        textPaint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < 7; i++) {
            boolean weekend = i >= 5;
            textPaint.setColor(weekend ? Ui.RED : Ui.TEXT_SUB);
            float cx = cellW * i + cellW / 2f;
            canvas.drawText(WEEK[i], cx, headerH * 0.72f, textPaint);
        }
    }

    private void drawDays(Canvas canvas) {
        int first = HolidayData.firstWeekIndex(month);
        int days = HolidayData.daysInMonth(month);
        Paint.FontMetrics fm = new Paint.FontMetrics();

        for (int i = 0; i < 42; i++) {
            int day = i - first + 1;
            if (day < 1 || day > days) continue;
            int col = i % 7;
            int row = i / 7;

            float left = col * cellW + Ui.dp(getContext(), 3);
            float top = headerH + row * cellH + Ui.dp(getContext(), 2);
            float right = (col + 1) * cellW - Ui.dp(getContext(), 3);
            float bottom = headerH + (row + 1) * cellH - Ui.dp(getContext(), 2);
            rect.set(left, top, right, bottom);
            float radius = Ui.dp(getContext(), 11);

            HolidayData.DayInfo info = HolidayData.get(month, day);
            boolean isHoliday = info != null && info.type == HolidayData.TYPE_HOLIDAY;
            boolean isMakeup = info != null && info.type == HolidayData.TYPE_MAKEUP;
            boolean isWeekend = info != null && info.weekend;
            boolean isToday = todayYear == year && todayMonth == month && todayDay == day;
            boolean isSelected = day == selectedDay;

            // 背景
            if (isHoliday) {
                fill.setColor(Ui.RED_BG);
                canvas.drawRoundRect(rect, radius, radius, fill);
            } else if (isMakeup) {
                fill.setColor(Ui.BLUE_BG);
                canvas.drawRoundRect(rect, radius, radius, fill);
            } else if (isSelected) {
                fill.setColor(Ui.TRACK);
                canvas.drawRoundRect(rect, radius, radius, fill);
            }

            // 日期数字
            float numberCenterY = top + (bottom - top) * 0.40f;
            textPaint.setTextAlign(Paint.Align.CENTER);
            if (isHoliday) {
                textPaint.setTextSize(Ui.dp(getContext(), 17));
                textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                textPaint.setColor(Ui.RED);
            } else if (isMakeup) {
                textPaint.setTextSize(Ui.dp(getContext(), 16));
                textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                textPaint.setColor(Ui.BLUE);
            } else {
                textPaint.setTextSize(Ui.dp(getContext(), 16));
                textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                textPaint.setColor(isWeekend ? Ui.WEEKEND : Ui.TEXT);
            }
            textPaint.getFontMetrics(fm);
            float baseline = numberCenterY - (fm.ascent + fm.descent) / 2f;
            canvas.drawText(String.valueOf(day), rect.centerX(), baseline, textPaint);

            // 假期名称 / 调休标记
            String tag = null;
            if (isHoliday) {
                tag = shortName(info.holidayName);
            } else if (isMakeup) {
                tag = "班";
            }
            if (tag != null) {
                textPaint.setTextSize(Ui.dp(getContext(), 9));
                textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                textPaint.setColor(isHoliday ? Ui.RED : Ui.BLUE);
                textPaint.getFontMetrics(fm);
                float tagBaseline = bottom - Ui.dp(getContext(), 8) - (fm.ascent + fm.descent) / 2f;
                canvas.drawText(tag, rect.centerX(), tagBaseline, textPaint);
            }

            // 今天 / 选中描边
            if (isToday || isSelected) {
                strokePaint.setStrokeWidth(Ui.dp(getContext(), isSelected ? 1.6f : 2f));
                strokePaint.setColor(isHoliday ? Ui.RED : (isSelected ? Ui.TEXT : Ui.RED));
                canvas.drawRoundRect(rect, radius, radius, strokePaint);
            }
        }
    }

    private String shortName(String name) {
        if (name == null) return "";
        if (name.startsWith("春节")) return "春节";
        String s = name.replace("节", "");
        return s.length() > 2 ? s.substring(0, 2) : s;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                moved = false;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(event.getX() - downX) > Ui.dp(getContext(), 12)) {
                    moved = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                float dx = event.getX() - downX;
                float dy = event.getY() - downY;
                if (Math.abs(dx) > Ui.dp(getContext(), 45) && Math.abs(dx) > Math.abs(dy) * 1.5f) {
                    if (listener != null) {
                        listener.onMonthChanged(dx < 0 ? 1 : -1);
                    }
                    return true;
                }
                if (!moved) {
                    handleTap(event.getX(), event.getY());
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void handleTap(float x, float y) {
        if (y < headerH) return;
        int col = (int) (x / cellW);
        int row = (int) ((y - headerH) / cellH);
        if (col < 0 || col > 6 || row < 0 || row > 5) return;
        int first = HolidayData.firstWeekIndex(month);
        int day = row * 7 + col - first + 1;
        int days = HolidayData.daysInMonth(month);
        if (day < 1 || day > days) return;
        selectedDay = day;
        invalidate();
        if (listener != null) {
            listener.onDaySelected(HolidayData.get(month, day), month);
        }
    }

    public void setYear(int year) {
        this.year = year;
    }
}
