package com.neuerordner.main.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

public class Divider {
    public static View divider(Context ctx, int marginTopDp) {
        float d = ctx.getResources().getDisplayMetrics().density;
        int hPx   = (int) (2.5 * d + 0.5f);        // 1 dp
        int mTop  = (int) (marginTopDp * d + 0.5f);

        View v = new View(ctx);
        ViewGroup.MarginLayoutParams lp =
                new ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, hPx);
        lp.setMargins(0, mTop, 0, 0);
        v.setLayoutParams(lp);
        v.setBackgroundColor(Color.GRAY);
        return v;
    }

}
