package com.neuerordner.main.utility;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.text.InputType;
import android.text.method.TransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.neuerordner.main.data.DatabaseService;
import com.neuerordner.main.data.Item;
import com.neuerordner.main.R;
import com.neuerordner.main.ui.Divider;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.Supplier;

public class ItemUtility {
    private final NavigationUtility _navUtil;
    private final DatabaseService _dbService;
    private final Context _context;
    private static final Integer NORMAL_TEXT_SIZE = 15;
    private static final Integer LABEL_TEXT_SIZE = 20;
    private static final Integer[] PADDING_TEXT_VIEW = {10, 15, 0, 0};
    public ItemUtility(DatabaseService dbService, Context context, View root) {
        _dbService = dbService;
        _context = context;
        _navUtil = new NavigationUtility(root);
    }
    public ItemUtility(View root) {
        _context = root.getContext();
        _dbService = new DatabaseService(_context);
        _navUtil = new NavigationUtility(root);
    }

    //WRAPPER FOR LOCATION DISPLAY
    public void makeItemShowCase(Item item, LinearLayout itemHolder, LinearLayout superHolder) {

        if (itemHolder.getChildCount() > 0) {
            return;
        }
        // Item Name with Label
        TextView lblName = textViewCreate("Name: ", LABEL_TEXT_SIZE, Typeface.BOLD, PADDING_TEXT_VIEW);
        TextView name    = textViewCreate(item.Name, NORMAL_TEXT_SIZE, Typeface.NORMAL, PADDING_TEXT_VIEW);

        // Item Quantity with Label
        TextView lblQty  = textViewCreate("Quanitiy: ", LABEL_TEXT_SIZE, Typeface.BOLD, PADDING_TEXT_VIEW);
        TextView qty     = textViewCreate(String.valueOf(item.Quantity), NORMAL_TEXT_SIZE, Typeface.NORMAL, PADDING_TEXT_VIEW);

        // Item Best Till Date with Label
        TextView textViewBestTillDate = null;
        TextView lbBestTillDate = null;

        if (item.bestTillDate != null) {
            lbBestTillDate = textViewCreate("Best Before: ", LABEL_TEXT_SIZE, Typeface.BOLD, PADDING_TEXT_VIEW);
            try {
                String eeee = item.bestTillDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
                String year = String.valueOf(item.bestTillDate.getYear());
                String month = item.bestTillDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
                String day = String.valueOf(item.bestTillDate.getDayOfMonth());
                String bestTillDate = eeee + ", " + day + " " + month + " " + year;

                textViewBestTillDate = textViewCreate(bestTillDate, NORMAL_TEXT_SIZE, Typeface.NORMAL, PADDING_TEXT_VIEW, InputType.TYPE_DATETIME_VARIATION_DATE);
            } catch (IllegalArgumentException illegalArgumentException) {
                Log.e("ItemUtility", "Error parsing Date", illegalArgumentException);
            }
        }

        // Edit Button
        Button editBtn = new Button(_context);
        editBtn.setText("Change");
        final String bestTillDateFinal = textViewBestTillDate != null ? textViewBestTillDate.toString() : LocalDate.now(ZoneId.systemDefault()).toString();
        editBtn.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putString("name", item.Name);
            b.putString("id", item.Id);
            b.putString("locationId", item.LocationId);
            b.putInt("quantity", item.Quantity);
            b.putString("date", bestTillDateFinal);
            _navUtil.navigateWithBundle(R.id.action_global_itemContainerFragment, b);
        });

        // Delete Button
        ImageButton delBtn = new ImageButton(_context);
        Drawable trashBin = ContextCompat.getDrawable(_context, R.drawable.ic_trash_32).mutate();
        delBtn.setImageDrawable(trashBin);

        //New Linear Layout
        LinearLayout opts = new LinearLayout(_context);
        opts.setOrientation(LinearLayout.HORIZONTAL);

        opts.addView(editBtn);
        opts.addView(delBtn);
        itemHolder.addView(lblName);
        itemHolder.addView(name);
        itemHolder.addView(lblQty);
        itemHolder.addView(qty);

        if (lbBestTillDate != null && textViewBestTillDate != null) {
            itemHolder.addView(lbBestTillDate);
            itemHolder.addView(textViewBestTillDate);
        }
        itemHolder.addView(opts);
        itemHolder.addView(Divider.divider(_context, 2));
        superHolder.addView(itemHolder);
        delBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(_context).setTitle("Delete")
                    .setMessage("Deletion cant be unchanged")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        _dbService.deleteItem(item);
                        itemHolder.removeAllViews();

                    }).setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    }).show();


        });
    }

    public TextView textViewCreate(String text, Integer textSize, Integer style, Integer[] padding) {
        return textViewCreate(text, textSize, style, padding, InputType.TYPE_CLASS_TEXT);
    }


        public TextView textViewCreate(String text, Integer textSize, Integer style, Integer[] padding, int inputType) {
        TextView tv = new TextView(_context);
        tv.setText(text);
        tv.setTextSize(textSize);
        tv.setTypeface(null, style);
        tv.setInputType(inputType);


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
