package com.neuerordner.main.ui;

import static android.view.View.VISIBLE;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.neuerordner.main.R;
import com.neuerordner.main.data.BestTillAlgorithm;
import com.neuerordner.main.data.GlobalViewModel;
import com.neuerordner.main.data.PermissionException;
import com.neuerordner.main.utility.ItemUtility;
import com.neuerordner.main.utility.NavigationUtility;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class BestTillScanner extends Fragment {

    private static final String CAMERAPERMISSION = Manifest.permission.CAMERA;

    // ML Kit
    private final TextRecognizer recognizer =
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

    // CameraX
    private PreviewView previewView;
    private LifecycleCameraController lifeCycleCameraController;
    private ImageAnalysis imageAnalysis;
    private boolean torchEnable = false;

    // UI
    private View root;
    private TextView displayText;
    private ProgressBar progressBar;

    // App
    private GlobalViewModel vm;
    private ItemUtility itemUtility;
    private NavigationUtility navUtil;

    // State
    private final AtomicBoolean showingDialog = new AtomicBoolean(false);
    private Set<LocalDate> dates = new LinkedHashSet<>();

    public BestTillScanner() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        root = inflater.inflate(R.layout.fragment_camera_scan, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View root, Bundle saveInstanceState) {
        super.onViewCreated(root, saveInstanceState);
        previewView = root.findViewById(R.id.scannerPreview);
        lifeCycleCameraController = new LifecycleCameraController(requireContext());
        vm = new ViewModelProvider(requireActivity()).get(GlobalViewModel.class);
        ImageButton torchButton = root.findViewById(R.id.torchBtn);
        progressBar = root.findViewById(R.id.textProgressBar);
        displayText = root.findViewById(R.id.textDisplay);
        itemUtility = new ItemUtility(root);
        navUtil = new NavigationUtility(root);

        torchButton.setOnClickListener(click ->  {
            torchEnable = !torchEnable;
            lifeCycleCameraController.enableTorch(torchEnable);
        });

        previewView.setOnClickListener(l -> {
            // allow re-scanning UX hint
            progressBar.setVisibility(VISIBLE);
        });

        ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        setupCamera();
                    } else {
                        Toast.makeText(requireContext(), "Permission Not Granted", Toast.LENGTH_SHORT).show();
                    }
                });

        if (ContextCompat.checkSelfPermission(requireContext(), CAMERAPERMISSION) == PackageManager.PERMISSION_GRANTED) {
            setupCamera();
        } else {
            if (shouldShowRequestPermissionRationale(CAMERAPERMISSION)) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Kamera-Zugriff notwendig")
                        .setMessage("Ohne Kamera-Zugriff kann kein Text erkannt werden. Bitte erlaube den Zugriff.")
                        .setPositiveButton("Erlauben", (dialog, which) -> permissionLauncher.launch(CAMERAPERMISSION))
                        .setNegativeButton("Abbrechen", (dialog, which) -> dialog.dismiss())
                        .show();
            } else {
                try {
                    permissionLauncher.launch(CAMERAPERMISSION);
                } catch (PermissionException ex) {
                    Toast.makeText(requireContext(), ex.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void setupCamera() {
        try {
            lifeCycleCameraController.bindToLifecycle(this);
            lifeCycleCameraController.setCameraSelector(CameraSelector.DEFAULT_BACK_CAMERA);
            previewView.setController(lifeCycleCameraController);

            ResolutionSelector resolutionSelector = lifeCycleCameraController.getImageCaptureResolutionSelector();

            imageAnalysis = new ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setResolutionSelector(resolutionSelector)
                    .build();

            Executor scanExecutor = Executors.newSingleThreadExecutor();

            lifeCycleCameraController.setImageAnalysisAnalyzer(scanExecutor, new ImageAnalysis.Analyzer() {
                @ExperimentalGetImage
                @Override
                public void analyze(@NonNull ImageProxy imageProxy) {
                    Image mediaImage = imageProxy.getImage();
                    if (mediaImage == null) {
                        imageProxy.close();
                        return;
                    }

                    InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

                    recognizer.process(image)
                            .addOnSuccessListener(result -> {
                                String resultString = result.getText().replaceAll("\\s+", "");
                                if (resultString == null || resultString.isEmpty()) return;

                                displayText.post(() -> {
                                    displayText.setVisibility(VISIBLE);
                                    displayText.setText(resultString);
                                    progressBar.post(() -> {
                                        progressBar.setVisibility(View.INVISIBLE);
                                    });
                                });

                                dates = BestTillAlgorithm.run(resultString);
                                if (dates == null || dates.isEmpty()) return;

                                Log.d("Found Dates", dates.toString());

                                // show single dialog (once), on UI thread
                                requireActivity().runOnUiThread(() -> {
                                    if (!isAdded()) return;

                                    // Acquire dialog "lock"
                                    if (!showingDialog.compareAndSet(false, true)) {
                                        return; // already showing one
                                    }

                                    Iterator<LocalDate> it = dates.iterator();
                                    final int total = dates.size();
                                    final int[] index = {0};
                                    final LocalDate[] current = { it.next() };

                                    AlertDialog dialog = new AlertDialog.Builder(requireContext())
                                            .setTitle("Date 1 / " + total)
                                            .setMessage("Date: " + current[0])
                                            .setCancelable(false)
                                            .setPositiveButton("Confirm", (d, w) -> {
                                                vm.setDate(current[0]);
                                                navUtil.navigateWithoutBundle(R.id.itemContainerFragment);
                                                d.dismiss();
                                            })
                                            .setNegativeButton("Cancel", (d, w) -> {
                                                d.dismiss();
                                                navUtil.navigateWithoutBundle(R.id.itemContainerFragment);
                                            })
                                            // Placeholder: override after show() so neutral doesn't auto-dismiss
                                            .setNeutralButton(it.hasNext() ? "Next" : null, null)
                                            .create();

                                    dialog.setOnShowListener(dlg -> {
                                        Button nextBtn = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                                        if (nextBtn != null) {
                                            nextBtn.setOnClickListener(v -> {
                                                if (!it.hasNext()) return;

                                                index[0]++;
                                                current[0] = it.next();

                                                dialog.setTitle("Date " + (index[0] + 1) + " / " + total);
                                                dialog.setMessage("Date: " + current[0]);

                                                if (!it.hasNext()) {
                                                    nextBtn.setVisibility(View.GONE);
                                                }
                                            });
                                        }
                                    });

                                    dialog.setOnDismissListener(d -> showingDialog.set(false));

                                    if (Looper.myLooper() == Looper.getMainLooper()) {
                                        dialog.show();
                                    } else {
                                        requireActivity().runOnUiThread(dialog::show);
                                    }
                                });
                            })
                            .addOnFailureListener(e -> {
                                // Optional: log or show a toast
                            })
                            .addOnCompleteListener(task -> imageProxy.close());
                }
            });

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Issues Setup Camera", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        lifeCycleCameraController.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(requireContext(), CAMERAPERMISSION) == PackageManager.PERMISSION_GRANTED) {
            setupCamera();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        lifeCycleCameraController.unbind();
    }
}
