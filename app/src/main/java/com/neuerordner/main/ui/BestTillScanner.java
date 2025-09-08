package com.neuerordner.main.ui;

import static android.view.View.VISIBLE;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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
import androidx.navigation.Navigation;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.neuerordner.main.R;
import com.neuerordner.main.data.GlobalViewModel;
import com.neuerordner.main.data.PermissionException;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BestTillScanner extends Fragment {

    private TextRecognizer recognizer =
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    private PreviewView previewView;
    private LifecycleCameraController lifeCycleCameraController;
    private boolean torchEnable = false;
    private GlobalViewModel vm;
    private static final String CAMERAPERMISSION = Manifest.permission.CAMERA;
    private ImageAnalysis imageAnalysis;
    public String recognizedText;
    private View root;
    private TextView displayText;
    private ProgressBar progressBar;
    private boolean showDialog;

    public BestTillScanner() {};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        root = inflater.inflate(R.layout.fragment_camera_scan, container, false);
        previewView = root.findViewById(R.id.scannerPreview);
        lifeCycleCameraController = new LifecycleCameraController(getActivity().getBaseContext());
        vm = new ViewModelProvider(requireActivity()).get(GlobalViewModel.class);
        ImageButton torchButton = root.findViewById(R.id.torchBtn);
        progressBar = root.findViewById(R.id.textProgressBar);
        displayText = root.findViewById(R.id.textDisplay);

        torchButton.setOnClickListener(click ->  {
            torchEnable = !torchEnable;
            lifeCycleCameraController.enableTorch(torchEnable);
        });

        previewView.setOnClickListener(l -> {
            showDialog = false;
            progressBar.setVisibility(VISIBLE);
        });

        // Permission ActivityResult Skeleton
        ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (!isGranted) {
                        Toast.makeText(requireContext(), "Permission Not Granted", Toast.LENGTH_SHORT).show();
                    }
                });

        if (ContextCompat.checkSelfPermission(requireContext(), CAMERAPERMISSION) == PackageManager.PERMISSION_GRANTED) {
            setupCamera();
        } else {
            if (shouldShowRequestPermissionRationale(CAMERAPERMISSION)) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Kamera-Zugriff notwendig")
                        .setMessage("Ohne Kamera-Zugriff kann kein QR-Code gescannt werden. " +
                                "Bitte erlaube den Zugriff.")
                        .setPositiveButton("Erlauben", (dialog, which) -> {
                            permissionLauncher.launch(CAMERAPERMISSION);
                        })
                        .setNegativeButton("Abbrechen", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();

            } else {
                try {
                    permissionLauncher.launch(CAMERAPERMISSION);
                } catch (PermissionException ex) {
                    Toast.makeText(requireContext(), ex.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        return root;

    }
    public void setupCamera() {
        try {
            lifeCycleCameraController.bindToLifecycle(this);

            CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
            ResolutionSelector resolutionSelector = lifeCycleCameraController.getImageCaptureResolutionSelector();

            lifeCycleCameraController.setCameraSelector(cameraSelector);

            previewView.setController(lifeCycleCameraController);


            imageAnalysis = new ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setResolutionSelector(resolutionSelector).build();

            Executor scanExecutor = Executors.newSingleThreadExecutor();

            lifeCycleCameraController.setImageAnalysisAnalyzer(scanExecutor, new ImageAnalysis.Analyzer() {
                @ExperimentalGetImage
                @Override
                public void analyze(@NonNull ImageProxy imageProxy) {

                    Image mediaImage = imageProxy.getImage();

                    if (mediaImage != null) {
                        InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                        recognizer.process(image).addOnSuccessListener(result -> {

                            String resultString = result.getText();
                            if (resultString.isEmpty()) {
                                return;
                            }

                            displayText.post(() -> {
                                displayText.setVisibility(VISIBLE);
                                displayText.setText(result.getText());
                                progressBar.post(() -> {
                                    progressBar.setVisibility(View.INVISIBLE);
                                });
                            });

                            if (!showDialog) {
                                showDialog = true;
                                String textInDialog = result.getText();
                                requireActivity().runOnUiThread(() -> {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireContext());
                                    alertDialog.setTitle("Text");
                                    alertDialog.setMessage(resultString);
                                    alertDialog.setPositiveButton("Use", (dialog, which) -> {
                                        vm.setTextMlScanned(textInDialog);
                                        lifeCycleCameraController.unbind();
                                        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main).popBackStack();
                                    });
                                    alertDialog.setNegativeButton("Rescan", (dialog, which) -> {
                                        showDialog = false;
                                        progressBar.setVisibility(View.VISIBLE);
                                        displayText.setVisibility(View.INVISIBLE);
                                        return;
                                    });
                                    alertDialog.show();
                                });
                            }



                        }).addOnFailureListener(ex -> {
                            if (progressBar.getVisibility() != VISIBLE) {
                                progressBar.post(() -> {
                                    progressBar.setVisibility(VISIBLE);
                                });
                            }


                        }).addOnCompleteListener((l) -> {
                            imageProxy.close();
                        });

                    }
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


}
