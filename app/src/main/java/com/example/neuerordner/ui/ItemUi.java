package com.example.neuerordner.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.room.Room;

import com.example.neuerordner.data.AppDatabase;
import com.example.neuerordner.data.GlobalViewModel;
import com.example.neuerordner.data.Item;
import com.example.neuerordner.data.Location;
import com.example.neuerordner.R;
import com.github.ayvazj.hashadapter.LinkedHashMapAdapter;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemUi extends Fragment {
    private String locationId = "";
    private GlobalViewModel vm;
    private EditText name;
    private String recognizedText;
    public ItemUi() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        // Inflate das Layout
        View root = inflater.inflate(R.layout.fragment_add_item, viewGroup, false);

        // Initialisiere DB (optional, falls noch nicht gesetzt)
        var db = Room.databaseBuilder(requireContext(), AppDatabase.class, "app-db").allowMainThreadQueries().build();

        vm = new ViewModelProvider(requireActivity()).get(GlobalViewModel.class);
        recognizedText = vm.getTextMlScanned().getValue();
        // Views binden
        LinearLayout itemContainer = root.findViewById(R.id.itemAddLinearLayout);
        name = itemContainer.findViewById(R.id.itemname);
        EditText itemQuantity = itemContainer.findViewById(R.id.itemquantity);
        Spinner spinner = itemContainer.findViewById(R.id.locationlist);
        Button saveButton = itemContainer.findViewById(R.id.additembutton);
        Button recognizeText = itemContainer.findViewById(R.id.recognizeItemText);

        // Lade Locations aus DB
        List<Location> locations = db.locationDAO().getAll();
        LinkedHashMap<String, String> locationMap = new LinkedHashMap<>();
        for (Location loc : locations) {
            locationMap.put(loc.Id, loc.Name);
        }

        // Adapter setzen
        LinkedHashMapAdapter<String, String> adapter = new LinkedHashMapAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item, // Neutraler Spinner-Layout
                locationMap
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        if (getArguments() != null) {
            String bundleName = getArguments().getString("name");
            name.setText(bundleName != null ? bundleName : "");
            Integer bundleQuantity = getArguments().getInt("quantity");
            itemQuantity.setText(bundleQuantity != null ? Integer.toString(bundleQuantity) : "0" );
            String bundleLocationId = getArguments().getString("locId");
            int counter = 0;
            for (Map.Entry<String, String> entry: adapter) {
                if (entry.getKey().equals(bundleLocationId))  {
                    spinner.setSelection(counter);
                }
                counter++;
            }
        }

        if (recognizedText != null && !recognizedText.isEmpty()) {
            name.setText(recognizedText);
        }


        //########################################################################//

        // Spinner-Auswahl behandeln
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Map.Entry<String, String> selectedEntry = (Map.Entry<String, String>) spinner.getSelectedItem();
                    if (selectedEntry != null) {
                        locationId = selectedEntry.getKey();
                    }
                } catch (Exception exception) {
                    Log.e("Error Selecting Location on Item Creationg with :", exception.toString());
                    Toast.makeText(requireContext(), "Error on selecting location", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                locationId = ""; // Fallback
            }
        });

        // Speichern-Button
        saveButton.setOnClickListener(v -> {
            String nameString = name.getText().toString().trim();
            String quantityText = itemQuantity.getText().toString().trim();

            // Validierung
            if (nameString.isEmpty() || quantityText.isEmpty() || locationId.isEmpty()) {
                Toast.makeText(requireContext(), "Required Items missing", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int quantityInt = Integer.parseInt(quantityText);
                OffsetDateTime now = OffsetDateTime.now();
                String itemId = UUID.randomUUID().toString();
                Item item = new Item(itemId, locationId, nameString, quantityInt, now);
                db.itemDao().insert(item);
                Toast.makeText(requireContext(), "Created Item", Toast.LENGTH_SHORT).show();
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                navController.navigate(R.id.locationListContainerFragment);

            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Parsing Error Try Again", Toast.LENGTH_SHORT).show();
            }
        });

        recognizeText.setOnClickListener(l -> {
            vm.setTextMlScanned(null);
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.camerTextScannerFragment);
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (vm == null) {
            vm = new ViewModelProvider(requireActivity()).get(GlobalViewModel.class);
        }
        recognizedText = vm.getTextMlScanned().getValue();
        if (recognizedText != null && !recognizedText.isEmpty() && name != null) {
            name.setText(recognizedText);
        }

        vm.setTextMlScanned(null);
    }
}
