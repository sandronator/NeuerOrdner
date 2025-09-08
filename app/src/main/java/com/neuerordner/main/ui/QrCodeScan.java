package com.neuerordner.main.ui;

import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.Manifest;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;
import androidx.viewbinding.ViewBinding;


import com.neuerordner.main.data.ActiveLocation;
import com.neuerordner.main.data.AppDatabase;
import com.neuerordner.main.data.GlobalViewModel;
import com.neuerordner.main.data.PermissionException;
import com.neuerordner.main.R;
import com.neuerordner.main.utility.NavigationUtility;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class QrCodeScan extends Fragment {
    private PreviewView previewView;
    private ConstraintLayout layout;
    private AppDatabase db = null;
    private static final String CAMERAPERMISSION = Manifest.permission.CAMERA;
    private boolean isFlashOn = false;
    private ViewBinding viewBinding;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private LifecycleCameraController lifeCycleCameraController;
    private GlobalViewModel vm;
    private ImageAnalysis imageAnalysis;
    private boolean torchEnable = false;
    private NavigationUtility _navUtil;
    private ProgressBar _progressbar;
    private boolean showSpinner;



    public QrCodeScan() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        View root = inflater.inflate(R.layout.fragment_camera_scan, container, false);
        db = Room.databaseBuilder(requireContext(), AppDatabase.class, "app-db").allowMainThreadQueries().build();
        layout = root.findViewById(R.id.cameraScanFragment);
        previewView = root.findViewById(R.id.scannerPreview);
        _progressbar = root.findViewById(R.id.textProgressBar);
        _progressbar.setVisibility(View.VISIBLE);
        lifeCycleCameraController = new LifecycleCameraController(getActivity().getBaseContext());
        vm = new ViewModelProvider(requireActivity()).get(GlobalViewModel.class);
        ImageButton torchButton = root.findViewById(R.id.torchBtn);
        torchButton.setOnClickListener(click ->  {
            torchEnable = !torchEnable;
            lifeCycleCameraController.enableTorch(torchEnable);
        });

        root.post(() -> {
            _navUtil = new NavigationUtility(root);
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
            Set<String> scannedLocations = new HashSet<String>();

            CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
            ResolutionSelector resolutionSelector = lifeCycleCameraController.getImageCaptureResolutionSelector();

            lifeCycleCameraController.setCameraSelector(cameraSelector);

            previewView.setController(lifeCycleCameraController);


            imageAnalysis = new ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setResolutionSelector(resolutionSelector).build();

            BarcodeScanner scanner = BarcodeScanning.getClient();
            Executor scanExecutor = Executors.newSingleThreadExecutor();

            lifeCycleCameraController.setImageAnalysisAnalyzer(scanExecutor, new ImageAnalysis.Analyzer() {
                @ExperimentalGetImage
                @Override
                public void analyze(@NonNull ImageProxy imageProxy) {
                    Image mediaImage = imageProxy.getImage();

                    if (mediaImage == null) {
                        imageProxy.close();
                    }

                    InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

                    //##################### REMOVAL OF NAME ######## ID ONLY ###########################
                    scanner.process(image)
                            .addOnSuccessListener(barcodes -> {
                                for (Barcode barcode : barcodes) {
                                    if (barcode.getRawValue() != null) {
                                        String[] locationArray = barcode.getDisplayValue().split(",");
                                        String locationId;
                                        String locationName;

                                        if (locationArray.length == 2) {
                                            locationId = locationArray[0].split(":")[1];
                                            locationName = locationArray[1].split(":")[1];
                                            if (!scannedLocations.contains(locationId)) {
                                                scannedLocations.add(locationId);
                                                ActiveLocation activeLocation = new ActiveLocation();

                                                activeLocation.Name = locationName;
                                                activeLocation.Id = locationId;

                                                vm.setActionLocation(activeLocation);
                                                scannedLocations.clear();

                                                Toast.makeText(requireContext(), "Found: "+activeLocation.Name, Toast.LENGTH_SHORT).show();
                                                _progressbar.setVisibility(View.INVISIBLE);
                                                _navUtil.navigateWithoutBundle(R.id.locationListContainerFragment);
                                            }
                                        }

                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                _progressbar.setVisibility(View.VISIBLE);
                            })
                            .addOnCompleteListener(task -> {
                                imageProxy.close();
                            });


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
