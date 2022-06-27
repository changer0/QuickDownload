package org.lulu.quickdownload;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * author: changer0
 * date: 2022/6/27
 */
public class OutPanelController implements View.OnTouchListener {

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private TextView tvOutPanel;
    private ScrollView scrollView;
    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
    private boolean canScroll = true;
    private final Runnable canScrollDelayRunnable = () -> canScroll = true;
    ;


    public OutPanelController(ScrollView scrollView, TextView tvOutPanel) {
        this.tvOutPanel = tvOutPanel;
        this.scrollView = scrollView;
        scrollView.setOnTouchListener(this);
    }

    public void printlnE(CharSequence msg) {
        SpannableString spannableString = new SpannableString(msg);
        spannableString.setSpan(
                new ForegroundColorSpan(Color.RED),
                0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        printlnI(spannableString);
    }

    @SuppressLint("SetTextI18n")
    public void printlnI(CharSequence msg) {
        if (!isMainThread()) {
            mainHandler.post(() -> printlnI(msg));
            return;
        }
        spannableStringBuilder.append(msg);
        spannableStringBuilder.append("\n————————————————————————————\n");
        tvOutPanel.setText(spannableStringBuilder);
        mainHandler.postDelayed(() -> {
            if (canScroll) {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }, 200);

    }

    public void clearPanel() {
        spannableStringBuilder.clear();
        tvOutPanel.setText("");
    }

    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mainHandler.removeCallbacks(canScrollDelayRunnable);
                canScroll = false;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mainHandler.removeCallbacks(canScrollDelayRunnable);
                mainHandler.postDelayed(canScrollDelayRunnable, 2000);

        }
        return false;
    }
}
