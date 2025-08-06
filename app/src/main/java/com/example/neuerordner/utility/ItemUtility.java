package com.example.neuerordner.utility;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.neuerordner.data.AppDatabase;
import com.example.neuerordner.data.Item;
import com.example.neuerordner.R;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ItemUtility {
    private final NavigationUtility _navUtil;
    private final AppDatabase _db;
    private final Context _context;
    private static final Integer NORMAL_TEXT_SIZE = 15;
    private static final Integer LABEL_TEXT_SIZE = 20;
    private static final Integer[] PADDING_TEXT_VIEW = {10, 15, 0, 0};

    public ItemUtility(AppDatabase db, Context context, View root) {
        _db = db;
        _context = context;
        _navUtil = new NavigationUtility(root);
    }

    public void makeItemShowCase(Item item, LinearLayout itemHolder, LinearLayout superHolder) {

        if (itemHolder.getChildCount() > 0) {
            return;
        }

        TextView lblName = textViewCreate("Name: ", LABEL_TEXT_SIZE, Typeface.BOLD, PADDING_TEXT_VIEW);
        TextView name    = textViewCreate(item.Name, NORMAL_TEXT_SIZE, Typeface.NORMAL, PADDING_TEXT_VIEW);

        TextView lblQty  = textViewCreate("Quanitiy: ", LABEL_TEXT_SIZE, Typeface.BOLD, PADDING_TEXT_VIEW);
        TextView qty     = textViewCreate(String.valueOf(item.Quantity), NORMAL_TEXT_SIZE, Typeface.NORMAL, PADDING_TEXT_VIEW);

        Button editBtn = new Button(_context);
        editBtn.setText("Change");
        editBtn.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putString("name", item.Name);
            b.putString("itemid", item.Id);
            b.putString("locId", item.LocationId);
            b.putInt("quantity", item.Quantity);
            _navUtil.navigateWithBundle(R.id.action_global_itemContainerFragment, b);
        });

        ImageButton delBtn = new ImageButton(_context);
        Drawable trashBin = ContextCompat.getDrawable(_context, R.drawable.ic_trash_32).mutate();
        delBtn.setImageDrawable(trashBin);


        LinearLayout opts = new LinearLayout(_context);
        opts.setOrientation(LinearLayout.HORIZONTAL);
        opts.addView(editBtn);
        opts.addView(delBtn);

        itemHolder.addView(lblName);
        itemHolder.addView(name);
        itemHolder.addView(lblQty);
        itemHolder.addView(qty);
        itemHolder.addView(opts);
        superHolder.addView(itemHolder);
        delBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(_context).setTitle("Delete")
                    .setMessage("Deletion cant be unchanged")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        _db.itemDao().delete(item);
                        itemHolder.removeAllViews();

                    }).setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    }).show();


        });
    }

    public TextView textViewCreate(String text, Integer textSize, Integer style, Integer[] padding) {
        TextView tv = new TextView(_context);
        tv.setText(text);
        tv.setTextSize(textSize);
        tv.setTypeface(null, style);

        if (padding != null && padding.length == 4) {
            tv.setPadding(padding[0], padding[1], padding[2], padding[3]);
        }

        return tv;
    }

    public <T> AlertDialog alertDialogCreation(String title, String message, String positiveButtonName, String negativeButtonName, Supplier<T> callbackPositive, Supplier<T> callbackNegative) {
        return new AlertDialog.Builder(_context).setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonName, (dialog, which) -> {
                    callbackPositive.get();
                }).setNegativeButton(negativeButtonName, ((dialog, which) -> {
                    callbackNegative.get();
                    dialog.dismiss();
                })).create();



    }

}
