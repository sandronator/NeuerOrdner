package com.example.neuerordner.ui;


import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.neuerordner.data.ActiveLocation;
import com.example.neuerordner.data.GlobalViewModel;
import com.example.neuerordner.R;
import com.example.neuerordner.utility.NavigationUtility;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;


public class QrCodeGenerator extends Fragment {
    private Bitmap qrBitmap;
    private BitMatrix matrix;
    private GlobalViewModel vm;
    private static final Integer QR_SIZE = 160;
    private static final Integer CANVAS_HEIGHT = 700;
    private static final Integer CANVAS_WIDTH = 1000;
    private NavigationUtility _navUtil;

    public QrCodeGenerator() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState){
        return inflater.inflate(R.layout.fragment_qr_display, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View root, Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);
        _navUtil = new NavigationUtility(root);
        ImageView imageView = root.findViewById(R.id.qrImage);
        vm = new ViewModelProvider(requireActivity()).get(GlobalViewModel.class);
        try {
            ActiveLocation activeLocation = vm.getActionLocation().getValue();
            if (activeLocation == null || activeLocation.Name == null || activeLocation.Id == null || activeLocation.Name.isEmpty() || activeLocation.Id.isEmpty()) {
                _navUtil.navigateWithoutBundle(R.id.locationListContainerFragment);
                return;
            }
            System.out.println("active location in QR: " + activeLocation.Name + activeLocation.Id);
            qrBitmap = generateQrBitmap(String.format("locationId: %s, name: %s", activeLocation.Id, activeLocation.Name), QR_SIZE);
            imageView.setImageBitmap(qrBitmap);
            askAndSafe(activeLocation.Name);
        } catch (WriterException e) {
            System.out.println(e);
            Toast.makeText(requireContext(), "Cant create QR Code", Toast.LENGTH_SHORT).show();

        }

        return;
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

    private void saveQrToDownloads(String filename) {
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
            int canvasWidth = CANVAS_WIDTH;
            int canvasHeight = CANVAS_HEIGHT;
            Bitmap canvas = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.RGB_565);
            for (int x = 0; x < canvasWidth; x++) {
                for (int y = 0; y < canvasHeight; y++) {
                    if (x > QR_SIZE || y > QR_SIZE) {
                        canvas.setPixel(x, y, Color.WHITE);
                        continue;
                    }

                    boolean isBlack = false;
                        try {
                            isBlack = matrix.get(x, y);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            isBlack = false;
                        }

                    canvas.setPixel(x, y, isBlack ? Color.BLACK : Color.WHITE);
                }
            }

            canvas.compress(Bitmap.CompressFormat.JPEG, 100, out);

        } catch (IOException e) {
            Toast.makeText(requireContext(), "Speichern fehlgeschlagen", Toast.LENGTH_SHORT).show();
            return;
        }
        values.clear();
        values.put(MediaStore.Downloads.IS_PENDING, 0);
        resolver.update(itemUri, values, null, null);
        Toast.makeText(requireContext(), "QR-Code in Downloads gespeichert", Toast.LENGTH_SHORT).show();

    }

    private void askAndSafe(String defaultName) {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        EditText name = new EditText(requireContext());
        name.setText(defaultName);
        new AlertDialog.Builder(requireContext())
                .setTitle("Dateiname:")
                .setView(name)
                .setPositiveButton("Speichern", (dialog, which) -> {
                    String filename = name.getText().toString().trim();
                    if (!filename.toLowerCase().endsWith(".jpeg")) filename += ".jpeg";
                    saveQrToDownloads( filename);
                    dialog.dismiss();
                    navController.navigate(R.id.locationListContainerFragment);

                })
                .setNegativeButton("Abbrechen", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
    @Override
    public void onPause() {
        super.onPause();
        vm.setActionLocation(null);
    }


}
