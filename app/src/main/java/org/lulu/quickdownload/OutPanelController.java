package org.lulu.quickdownload;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.lifecycle.MutableLiveData;

/**
 * author: changer0
 * date: 2022/6/27
 */
public class OutPanelController {

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private TextView tvOutPanel;
    private ScrollView scrollView;
    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();


    public OutPanelController(ScrollView scrollView, TextView tvOutPanel) {
        this.tvOutPanel = tvOutPanel;
        this.scrollView = scrollView;
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

//        CharSequence oldStr = tvOutPanel.getText();
//        if (TextUtils.isEmpty(oldStr)) {
//            tvOutPanel.setText(msg);
//        } else {
//            tvOutPanel.setText(oldStr + "\n" + msg);
//        }
        tvOutPanel.setText(spannableStringBuilder);
        mainHandler.postDelayed(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN), 200);

    }

    public void clearPanel() {
        spannableStringBuilder.clear();
        tvOutPanel.setText("");
    }

    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}
