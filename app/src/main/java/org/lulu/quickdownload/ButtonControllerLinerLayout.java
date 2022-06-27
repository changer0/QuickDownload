package org.lulu.quickdownload;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;

/**
 * author: changer0
 * date: 2022/6/27
 */
public class ButtonControllerLinerLayout extends LinearLayout {
    public ButtonControllerLinerLayout(Context context) {
        super(context);
    }

    public ButtonControllerLinerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ButtonControllerLinerLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ButtonControllerLinerLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public Button addButton(CharSequence text, View.OnClickListener clickListener) {
        Button button = new Button(getContext());
        button.setText(text);
        button.setOnClickListener(clickListener);
        addComponent(button);
        return button;
    }

    public TextView addText(CharSequence text) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setTextColor(Color.BLACK);
        addComponent(textView);
        return textView;
    }

    public void addComponent(View view) {
        view.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        addView(view);
    }

    public View addComponent(@LayoutRes int layout) {
        View inflate = LayoutInflater.from(getContext()).inflate(layout, null);
        addComponent(inflate);
        return inflate;
    }
}
