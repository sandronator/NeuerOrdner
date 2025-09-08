package com.neuerordner.main.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.neuerordner.main.data.ActiveLocation;
import com.neuerordner.main.data.DatabaseService;
import com.neuerordner.main.data.GlobalViewModel;
import com.neuerordner.main.data.Location;
import com.neuerordner.main.R;
import com.neuerordner.main.utility.NavigationUtility;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

public class AddLocationUi extends Fragment {
    private GlobalViewModel vm;
    private View root;
    private NavigationUtility _navUtil;
    private DatabaseService _dbService;
    private EditText nameElement;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_add_location,
                container, false);
        return root;
    }
    @Override
    public void onViewCreated(@NonNull View root, Bundle savedStateInstance) {
        super.onViewCreated(root, savedStateInstance);

        _navUtil = new NavigationUtility(root);
        _dbService = new DatabaseService(requireContext());

        LinearLayout linearLayout =
                root.findViewById(R.id.locationAddLinearLayout);
        Button saveButton =
                linearLayout.findViewById(R.id.createlocationbutton);
        View scanTextButton =
                linearLayout.findViewById(R.id.scanTextButton);

        vm = new ViewModelProvider(requireActivity()).get(GlobalViewModel.class);

        String locationId = "";

        nameElement =
                linearLayout.findViewById(R.id.locationname);


        Location updateLocation = vm.getUpdateLocation().getValue();

        if (updateLocation != null) {
            nameElement.setText(updateLocation.Name);
            locationId = updateLocation.Id;
            saveButton.setText("Update Location");
        } else {
            locationId = UUID.randomUUID().toString();
        }

        String finalLocationId = locationId;
        saveButton.setOnClickListener(v -> {
            String name = getName();

            if (name == null || name.isEmpty()) {
                return;
            }
            String parsedId;
            if (finalLocationId == null || finalLocationId.isEmpty()) {
                parsedId = UUID.randomUUID().toString();
            } else {
                parsedId = finalLocationId;
            }
            Location location = new Location();
            location.Id = parsedId;
            location.Name = name;
            location.CreationDate = OffsetDateTime.now(ZoneId.systemDefault());


            if (updateLocation != null && !updateLocation.Name.isEmpty() && !updateLocation.Id.isEmpty()) {
                _dbService.updateLocation(location);
                vm.setUpdateLocation(null);
                Toast.makeText(requireContext(), "Updated Succesfully", Toast.LENGTH_SHORT).show();
                _navUtil.navigateWithoutBundle(R.id.locationListContainerFragment);
            } else {
                _dbService.addLocation(location);
                setActiveAndNavigateQr(location);
            }

        });

        scanTextButton.setOnClickListener(l -> _navUtil.navigateWithoutBundle(R.id.camerTextScannerFragment));

    }

    private void setActiveAndNavigateQr(Location location) {
        ActiveLocation activeLocation = new ActiveLocation();
        activeLocation.Id = location.Id;
        activeLocation.Name = location.Name;

        vm.setActionLocation(activeLocation);
        vm.setTextMlScanned(null);

        _navUtil.navigateWithoutBundle(R.id.qrCodeDisplayContainerFragment);
    }

    private String getName() {
        String name = nameElement.getText().toString();
        if (name.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Name darf nicht leer sein",
                    Toast.LENGTH_SHORT).show();
            return null;
        } else {
            return name;
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        String locationName = vm.getTextMlScanned().getValue();
        if (locationName == null || locationName.isEmpty()) return;
        EditText locationNameLabel = root.findViewById(R.id.locationname);
        locationNameLabel.setText(locationName);
        vm.setTextMlScanned(null);
    }
    @Override
    public void onPause() {
        super.onPause();
        vm.setUpdateLocation(null);
    }
}