package com.example.neuerordner.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.room.Room;

import com.example.neuerordner.data.ActiveLocation;
import com.example.neuerordner.data.AppDatabase;
import com.example.neuerordner.data.GlobalViewModel;
import com.example.neuerordner.data.Location;
import com.example.neuerordner.R;
import com.example.neuerordner.utility.NavigationUtility;

import java.util.UUID;

public class LocationUi extends Fragment {
    private GlobalViewModel vm;
    private String locationName;
    private View root;
    private NavigationUtility _navUtil;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // build your DB here (ok onCreateView, since requireContext() is valid)
        var db = Room.databaseBuilder(requireContext(),
                        AppDatabase.class,
                        "app-db")
                .allowMainThreadQueries()
                .build();

        root = inflater.inflate(R.layout.fragment_add_location,
                container, false);
        LinearLayout linearLayout =
                root.findViewById(R.id.locationAddLinearLayout);
        View saveButton =
                linearLayout.findViewById(R.id.createlocationbutton);
        View scanTextButton =
                linearLayout.findViewById(R.id.scanTextButton);

        vm = new ViewModelProvider(requireActivity()).get(GlobalViewModel.class);
        root.post(() -> {
           _navUtil = new NavigationUtility(root);
        });

        saveButton.setOnClickListener(v -> {
            // 1) read input
            EditText nameElement =
                    linearLayout.findViewById(R.id.locationname);
            String name = nameElement.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(requireContext(),
                        "Name darf nicht leer sein",
                        Toast.LENGTH_SHORT).show();
                return;
            }


            // 2) create & insert Location
            Location location = new Location();
            location.Id   = UUID.randomUUID().toString();
            location.Name = name;
            db.locationDAO().insert(location);

            // 3) navigate *to the QR-display fragment*, passing the new ID
            ActiveLocation activeLocation = new ActiveLocation();
            activeLocation.Id = location.Id;
            activeLocation.Name = location.Name;

            vm.setActionLocation(activeLocation);
            vm.setTextMlScanned(null);

            _navUtil.navigateWithoutBundle(R.id.qrCodeDisplayContainerFragment);

        });

        scanTextButton.setOnClickListener(l -> _navUtil.navigateWithoutBundle(R.id.camerTextScannerFragment));

        return root;
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
}