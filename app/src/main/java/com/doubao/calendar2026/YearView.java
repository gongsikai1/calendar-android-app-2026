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
 * 年视图：12 个迷你月历，3 列 × 4 行，全年放假安排一屏看全。
 * 点月份可跳转到月视图。
 */
public class YearView extends View {

    private static final int COLS = 3;
    private static final int ROWS = 4;

    public interface Listener {
        void onMonthPicked(int month);
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private float itemW, itemH, dayCell, titleH, padIn;
    private int highlightMonth = 1;
    private Listener listener;

    private int todayYear, todayMonth, todayDay;

    public YearView(Context context) {
        this(context, null);
    }

    public YearView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Calendar c = Calendar.getInstance();
        todayYear = c.get(Calendar.YEAR);
        todayMonth = c.get(Calendar.MONTH) + 1;
        todayDay = c.get(Calendar.DAY_OF_MONTH);
    }

    public void setListener(Listener l) {
        this.listener = l;
    }

    public void setHighlightMonth(int month) {
        this.highlightMonth = month;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        itemW = w / (float) COLS;
        padIn = Ui.dp(getContext(), 5);
        titleH = Ui.dp(getContext(), 20);
        dayCell = (itemW - padIn * 2) / 7f;
        itemH = titleH + dayCell * 6 + Ui.dp(getContext(), 8);
        int h = (int) (itemH * ROWS + Ui.dp(getContext(), 2));
        setMeasuredDimension(w, resolveSize(h, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int m = 1; m <= 12; m++) {
            int idx = m - 1;
            int col = idx % COLS;
            int row = idx / COLS;
            drawMiniMonth(canvas, m, col * itemW, row * itemH);
        }
    }

    private void drawMiniMonth(Canvas canvas, int month, float ox, float oy) {
        Paint.FontMetrics fm = new Paint.FontMetrics();

        // 月份标题（当前查看的月份用红色 + 浅底强调）
        if (month == highlightMonth) {
            fillRound(canvas, ox + padIn - Ui.dp(getContext(), 3), oy + Ui.dp(getContext(), 1),
                    ox + padIn + Ui.dp(getContext(), 30), oy + titleH,
                    Ui.dp(getContext(), 5), Ui.RED_BG);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(Ui.dp(getContext(), 11.5f));
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(month == highlightMonth ? Ui.RED : Ui.TEXT);
        canvas.drawText(month + "月", ox + padIn, oy + titleH * 0.82f, paint);

        int first = HolidayData.firstWeekIndex(month);
        int days = HolidayData.daysInMonth(month);
        float gridTop = oy + titleH;

        for (int i = 0; i < 42; i++) {
            int day = i - first + 1;
            if (day < 1 || day > days) continue;
            int col = i % 7;
            int row = i / 7;

            float left = ox + padIn + col * dayCell + dayCell * 0.10f;
            float top = gridTop + row * dayCell + dayCell * 0.10f;
            float right = ox + padIn + (col + 1) * dayCell - dayCell * 0.10f;
            float bottom = gridTop + (row + 1) * dayCell - dayCell * 0.10f;
            rect.set(left, top, right, bottom);

            HolidayData.DayInfo info = HolidayData.get(month, day);
            boolean isHoliday = info != null && info.type == HolidayData.TYPE_HOLIDAY;
            boolean isMakeup = info != null && info.type == HolidayData.TYPE_MAKEUP;
            boolean isWeekend = info != null && info.weekend;
            boolean isToday = todayYear == HolidayData.YEAR && todayMonth == month && todayDay == day;

            float radius = dayCell * 0.30f;
            if (isHoliday) {
                fillRound(canvas, rect.left, rect.top, rect.right, rect.bottom, radius, Ui.RED_BG);
            } else if (isMakeup) {
                fillRound(canvas, rect.left, rect.top, rect.right, rect.bottom, radius, Ui.BLUE_BG);
            }

            paint.setStyle(Paint.Style.FILL);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(Ui.dp(getContext(), 8f));
            paint.setTypeface(Typeface.create(Typeface.DEFAULT,
                    isHoliday ? Typeface.BOLD : Typeface.NORMAL));
            if (isHoliday) {
                paint.setColor(Ui.RED);
            } else if (isMakeup) {
                paint.setColor(Ui.BLUE);
            } else if (isWeekend) {
                paint.setColor(0xFFACB1B9);
            } else {
                paint.setColor(0xFF40444A);
            }
            paint.getFontMetrics(fm);
            float baseline = rect.centerY() - (fm.ascent + fm.descent) / 2f;
            canvas.drawText(String.valueOf(day), rect.centerX(), baseline, paint);

            if (isToday) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(Ui.dp(getContext(), 1));
                paint.setColor(isHoliday ? Ui.RED : Ui.TEXT_SUB);
                canvas.drawRoundRect(rect, radius, radius, paint);
                paint.setStyle(Paint.Style.FILL);
            }
        }
    }

    private void fillRound(Canvas canvas, float l, float t, float r, float b, float radius, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        rect.set(l, t, r, b);
        canvas.drawRoundRect(rect, radius, radius, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            int col = (int) (event.getX() / itemW);
            int row = (int) (event.getY() / itemH);
            if (col < 0 || col >= COLS || row < 0 || row >= ROWS) return true;
            int month = row * COLS + col + 1;
            if (month >= 1 && month <= 12 && listener != null) {
                listener.onMonthPicked(month);
            }
        }
        return true;
    }
}
