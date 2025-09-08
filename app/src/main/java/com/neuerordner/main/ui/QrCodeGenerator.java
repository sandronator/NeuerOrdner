package com.neuerordner.main.ui;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.neuerordner.main.data.DatabaseService;
import com.neuerordner.main.data.GlobalViewModel;
import com.neuerordner.main.R;
import com.neuerordner.main.data.Location;
import com.neuerordner.main.utility.ItemUtility;
import com.neuerordner.main.utility.NavigationUtility;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class QrCodeGenerator extends Fragment {
    private Bitmap qrBitmap;
    private BitMatrix matrix;
    private GlobalViewModel vm;
    private static final Integer QR_SIZE = 120;
    private static final Integer CANVAS_HEIGHT = 700;
    private static final Integer CANVAS_WIDTH = 1000;
    private List<Bitmap> bitmaps = new ArrayList<>();
    private NavigationUtility _navUtil;
    private ItemUtility _itemUtility;
    private DatabaseService _dbService;
    private List<Location> locations = new ArrayList<>();
    private final int QR_CODE_MARGIN = 20;
    private final int QR_SIZE_PLUS_MARGIN = QR_SIZE + QR_CODE_MARGIN;
    private final int MAX_QR_EACH_ROW = CANVAS_WIDTH / QR_SIZE_PLUS_MARGIN;
    private final int MAX_ROWS = CANVAS_HEIGHT / QR_SIZE_PLUS_MARGIN;
    private final int MAX_QR_CODES = MAX_QR_EACH_ROW * MAX_ROWS - 1;


    public QrCodeGenerator() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState){
        return inflater.inflate(R.layout.fragment_qr_display, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View root, Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);
        Log.d("Max QR: ", String.valueOf(MAX_QR_CODES));
        _navUtil = new NavigationUtility(root);
        _dbService = new DatabaseService(requireContext());
        _itemUtility = new ItemUtility(_dbService, requireContext(), root);
        ImageView imageView = root.findViewById(R.id.qrImage);
        vm = new ViewModelProvider(requireActivity()).get(GlobalViewModel.class);

        Bundle locationBundle = getArguments();
        if (locationBundle == null || locationBundle.isEmpty()) {
            _navUtil.navigateWithoutBundle(R.id.locationListContainerFragment);
            return;
        }

        try {
            locations = locationBundle.getParcelableArrayList("KEY");
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Parcable Error", Toast.LENGTH_SHORT).show();
        }

        if (locations == null || locations.isEmpty() || locations.size() < 1) {
            Toast.makeText(requireContext(), "Nothing Parsed", Toast.LENGTH_SHORT).show();
            _navUtil.navigateWithoutBundle(R.id.locationListContainerFragment);
        }

        for (int i = 0; i < locations.size(); i++) {

            Location location = locations.get(i);

            if (location.Id == null || location.Id.isEmpty() || location.Name == null || location.Name.isEmpty()) {
                continue;
            }
            try {
                Bitmap map = generateQrBitmap(location.Id, QR_SIZE);
                bitmaps.add(map);
            } catch (WriterException we) {
                Toast.makeText(requireContext(), "Error generating QR-CODE", Toast.LENGTH_SHORT).show();
                _navUtil.navigateWithoutBundle(R.id.locationListContainerFragment);
                return;
            }

        }

        if (bitmaps == null || bitmaps.isEmpty()) {
            _navUtil.navigateWithoutBundle(R.id.locationListContainerFragment);
            return;
        }
        imageView.setImageBitmap(bitmaps.get(0));
        askAndSafe();
    }

    private Bitmap generateQrBitmap(String text, int size) throws WriterException {
        Map<EncodeHintType,Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        matrix = new QRCodeWriter()
                .encode(text, BarcodeFormat.QR_CODE, size, size, hints);

        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bmp;
    }
    private Pair<Integer, Integer> getHeightWidth(int position) {
        if (position == 0) {
            return new Pair<Integer, Integer>(0, 0);
        }
        if (position > MAX_QR_CODES) {
            return null;
        }
        int lengthOfQR = QR_SIZE_PLUS_MARGIN;
        if (MAX_QR_EACH_ROW < 1) {
            return null;
        }
        int findRow = position / MAX_QR_EACH_ROW;
        int positionInRow = position % MAX_QR_EACH_ROW;

        int width = (positionInRow * lengthOfQR);
        if (positionInRow == 0) width = 0;
        Integer height =  (findRow * lengthOfQR);
        return new Pair<Integer, Integer>(height, width);
    }
    Bitmap makeBottomLabel(String text) {
        int w = QR_SIZE, h = QR_CODE_MARGIN;
        Bitmap label = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(label);
        c.drawColor(Color.WHITE);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.BLACK);
        p.setTextAlign(Paint.Align.CENTER);

        // start size ~60% of band height
        float baseSize = Math.max(10f, h * 0.6f);
        p.setTextSize(baseSize);

        int padding = 4;                    // left/right padding in px
        int maxWidth = w - 2 * padding;

        // 1) shrink text size (bounded)
        while (p.measureText(text) > maxWidth && p.getTextSize() > 8f) {
            p.setTextSize(p.getTextSize() - 1f);
        }

        // 2) if still too wide, ellipsize manually
        if (p.measureText(text) > maxWidth) {
            String ell = "…";
            int end = text.length();
            while (end > 0 && p.measureText(text, 0, end) + p.measureText(ell) > maxWidth) {
                end--;
            }
            text = (end > 0 ? text.substring(0, end) + ell : ell);
        }

        // vertical centering (y is baseline)
        Paint.FontMetrics fm = p.getFontMetrics();
        float cx = w / 2f;
        float cy = h / 2f - (fm.ascent + fm.descent) / 2f;

        c.drawText(text, cx, cy, p);
        return label;
    }

    private void saveQrToDownloads() {
        String filename = locations.size() == 1 ? locations.get(0).Name : "QRCodeDump.jpeg";

        ContentResolver resolver = requireContext().getContentResolver();
        ContentValues values = new ContentValues();

        values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
        values.put(MediaStore.Downloads.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/NeuerOrdner");
        values.put(MediaStore.Downloads.IS_PENDING, 1);

        Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Uri itemUri = resolver.insert(collection, values);

        if (itemUri == null) {
            Toast.makeText(requireContext(), "Konnte Datei nicht anlegen", Toast.LENGTH_SHORT).show();
            return;
        }

        try (OutputStream out = resolver.openOutputStream(itemUri)) {
            Bitmap canvas = Bitmap.createBitmap(CANVAS_WIDTH, CANVAS_HEIGHT, Bitmap.Config.ARGB_8888);
            canvas.eraseColor(Color.WHITE);
            for (Integer i = 0; i < locations.size(); i++) {
                Log.d("Current Iteration: ", String.valueOf(i));
                if (i > MAX_QR_CODES) {
                    Log.d("Exceeding Max QR-Codes", String.valueOf(i));
                    break;
                }
                Pair<Integer, Integer> heightWidth = getHeightWidth(i);

                Bitmap bitmap = bitmaps.get(i);

                Bitmap label = makeBottomLabel(locations.get(i).Name);

                if (heightWidth == null || heightWidth.first == null || heightWidth.second == null) {
                    Log.d("Cant create QR Code", locations.get(i).getid());
                    continue;
                }
                Integer start_height = heightWidth.first;
                Integer start_width = heightWidth.second;

                try {
                    Integer stop_height = QR_SIZE_PLUS_MARGIN + start_height;
                    Integer stop_width = QR_SIZE_PLUS_MARGIN + start_width;
                    int white = Color.WHITE;



                    for (int x = 0; x < CANVAS_WIDTH; x++) {
                        for (int y = 0; y < CANVAS_HEIGHT; y++) {
                            //In range of correct QR-Code position
                            if (x < stop_width && x > start_width && y < stop_height && y > start_height) {
                                int qr_code_width_position = x - start_width;
                                int qr_code_hight_position = y - start_height;

                                try {
                                    if (qr_code_hight_position > QR_SIZE) {
                                        int position_label_y = qr_code_hight_position - QR_SIZE;
                                        int position_label_x = qr_code_width_position;

                                        int pixel_label = label.getPixel(position_label_x, position_label_y);
                                        canvas.setPixel(x, y, pixel_label);


                                    }

                                } catch (IllegalArgumentException exception) {
                                }

                                try {

                                    white = bitmaps.get(i).getPixel(qr_code_width_position, qr_code_hight_position);
                                    canvas.setPixel(x, y, white);

                                } catch (IllegalArgumentException illegalArgumentException) {
                                }

                            }
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    Log.d("Out of Bound on parsing QR Codes on Canvas", e.toString());
                    return;
                }
            }
            canvas.compress(Bitmap.CompressFormat.PNG, 100, out);

        } catch (IOException e) {
            Toast.makeText(requireContext(), "Speichern fehlgeschlagen", Toast.LENGTH_SHORT).show();
            return;
        }
        values.clear();
        values.put(MediaStore.Downloads.IS_PENDING, 0);
        resolver.update(itemUri, values, null, null);
        Toast.makeText(requireContext(), "QR-Code in Downloads gespeichert", Toast.LENGTH_SHORT).show();

    }

    private void askAndSafe() {
        _itemUtility.textViewCreate(String.format("Do you want save %d Locations?", locations.size()), R.dimen.NORMAL_TEXT_SIZE, 0, new Integer[0]);
        _itemUtility.alertDialogCreation("Do you want to create QR-Code", "Can use some space", "Speichern", "Abbrechen", () -> {
            saveQrToDownloads();

            return null;
        }, () -> {
            _navUtil.navigateWithoutBundle(R.id.locationListContainerFragment);
            return null;
        }).show();
    }
    @Override
    public void onPause() {
        super.onPause();
        vm.setActionLocation(null);
    }


}
