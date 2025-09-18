package com.neuerordner.main.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.neuerordner.main.data.DatabaseService;
import com.neuerordner.main.data.DateConverter;
import com.neuerordner.main.data.GlobalViewModel;
import com.neuerordner.main.data.Item;
import com.neuerordner.main.data.Location;
import com.neuerordner.main.R;
import com.github.ayvazj.hashadapter.LinkedHashMapAdapter;
import com.neuerordner.main.utility.NavigationUtility;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class AddItem extends Fragment {
    private String locationId = "";
    private String itemId = "";
    private GlobalViewModel vm;
    private EditText editTextName;
    private EditText editTextQuantity;
    private String recognizedText;
    private DatabaseService _dbService;
    private NavigationUtility _navutil;
    private LocalDate selectedDate;
    private CalendarView calendar;
    private Spinner spinner;
    private LinkedHashMapAdapter<String, String> adapter;


    public AddItem() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        // Inflate das Layout
        View root = inflater.inflate(R.layout.fragment_add_item, viewGroup, false);

        vm = new ViewModelProvider(requireActivity()).get(GlobalViewModel.class);
        recognizedText = vm.getTextMlScanned().getValue();
        _dbService = new DatabaseService(requireContext());

        ScrollView scrollView = root.findViewById(R.id.itemAddScrollView);
        LinearLayout itemContainer = scrollView.findViewById(R.id.itemAddLinearLayout);
        editTextName = itemContainer.findViewById(R.id.itemname);
        editTextQuantity = itemContainer.findViewById(R.id.itemquantity);
        spinner = itemContainer.findViewById(R.id.locationlist);
        calendar = itemContainer.findViewById(R.id.itemBestTillDate);

        calendar.setVisibility(View.GONE);

        // Lade Locations aus DB
        List<Location> locations = _dbService.getAllLocations();
        LinkedHashMap<String, String> locationMap = new LinkedHashMap<>();
        for (Location loc : locations) {
            locationMap.put(loc.Id, loc.Name);
        }

        // Adapter setzen
        adapter = new LinkedHashMapAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item, // Neutraler Spinner-Layout
                locationMap
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        setLatestClicked();

        // Spinner-Auswahl behandeln
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Map.Entry<String, String> selectedEntry = (Map.Entry<String, String>) spinner.getSelectedItem();
                    if (selectedEntry != null) {
                        locationId = selectedEntry.getKey();
                        vm.setLastClickedLocation(_dbService.getLocation(locationId));
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


        // fetch arguments form "Change" Button
        if (getArguments() != null) {
            String itemName = getArguments().getString("name");
            itemId = getArguments().getString("id");
            locationId = getArguments().getString("locationId");
            Integer quantity = getArguments().getInt("quantity");
            LocalDate localDate = DateConverter.parseLocalDate(getArguments().getString("date"));

            editTextName.setText(itemName);
            editTextQuantity.setText(Integer.toString(quantity));

            try {
                spinner.setSelection(findPositionInLinkedHashAdapter(locationId));
            } catch (IndexOutOfBoundsException ex) {
                Log.e("AddItem", "Error in setting dropdown Selection", ex);
            }
        }




        //Set recognized text
        if (recognizedText != null && !recognizedText.isEmpty()) {
            editTextName.setText(recognizedText);
        }



        //ShowCalendar Button

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);
        View scrollView = root.findViewById(R.id.itemAddScrollView);
        View container = scrollView.findViewById(R.id.itemAddLinearLayout);
        Button saveButton = container.findViewById(R.id.additembutton);
        CalendarView calendar = container.findViewById(R.id.itemBestTillDate);
        LinearLayout bestTillDateDropDown = container.findViewById(R.id.toogleCalendarClickTarget);
        ImageView calendarDropDownButton = container.findViewById(R.id.bestTillDateDropDown);
        Drawable arrowUp = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_arrow_upward_24);
        Drawable arrowDown = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_arrow_downward_24);
        LinearLayout itemContainer = scrollView.findViewById(R.id.itemAddLinearLayout);
        Button recognizeText = itemContainer.findViewById(R.id.recognizeItemText);
        Button scanDateButton = itemContainer.findViewById(R.id.searchBestTillDate);
        _navutil = new NavigationUtility(root);

        //besttillDateDropdown logic
        bestTillDateDropDown.setOnClickListener(l -> {
            if (calendar.getVisibility() == View.GONE) {
                calendarDropDownButton.setImageDrawable(arrowUp);
                calendar.setVisibility(View.VISIBLE);
            } else {
                calendar.setVisibility(View.GONE);
                calendarDropDownButton.setImageDrawable(arrowDown);
            }
        });

        //calendar set clicked date
        calendar.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth); // add 1
        });


        // Speichern-Button
        saveButton.setOnClickListener(v -> {
            String nameString = editTextName.getText().toString().trim();
            String quantityText = editTextQuantity.getText().toString().trim();


            // Validierung
            if (nameString.isEmpty() || quantityText.isEmpty() || locationId.isEmpty()) {
                Toast.makeText(requireContext(), "Not Completed", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int quantityInt = Integer.parseInt(quantityText);
                OffsetDateTime now = OffsetDateTime.now();
                Item item = new Item(itemId, locationId, nameString, quantityInt, now, selectedDate);
                if (!itemId.isEmpty())  {
                    _dbService.uddateItem(item);
                } else {
                    item.Id = UUID.randomUUID().toString();
                    _dbService.addItem(item);
                }
                Toast.makeText(requireContext(), "Successfull Created Item", Toast.LENGTH_SHORT).show();

                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                navController.navigate(R.id.locationListContainerFragment);

            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Parsing Error Try Again", Toast.LENGTH_SHORT).show();
            }
        });

        recognizeText.setOnClickListener(l -> {
            vm.setTextMlScanned(null);
            _navutil.navigateWithoutBundle(R.id.camerTextScannerFragment);
        });

        scanDateButton.setOnClickListener(l -> {
            vm.setDate(null);
            _navutil.navigateWithoutBundle(R.id.bestTillDateFragment);
        });


    }

    private int findPositionInLinkedHashAdapter(String locationId) throws IndexOutOfBoundsException {
        if (adapter == null) throw new IndexOutOfBoundsException("Adapter null");
        int counter = 0;
        for (Map.Entry<String, String> entry: adapter) {
            if (entry.getKey().equals(locationId))  {
                return counter;
            }
            counter++;
        }
        throw new IndexOutOfBoundsException("Id not in adapter");
    }

    public void setLatestClicked() {
        if (vm == null || spinner == null || adapter == null) return;

        Location lastClickedLocation = vm.getLastClickedLocation().getValue();
        if (lastClickedLocation != null) {
            try {
                locationId = lastClickedLocation.Id;
                spinner.setSelection(findPositionInLinkedHashAdapter(locationId));
            } catch (IndexOutOfBoundsException e) {
                Log.e("AddItem", "Error in setting dropdown Selection", e);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (vm == null) {
            vm = new ViewModelProvider(requireActivity()).get(GlobalViewModel.class);
        }
        recognizedText = vm.getTextMlScanned().getValue();
        if (recognizedText != null && !recognizedText.isEmpty() && editTextName != null) {
            editTextName.setText(recognizedText);
        }
        selectedDate = vm.getDate().getValue();
        if (calendar != null && selectedDate != null) {
            calendar.setDate(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        setLatestClicked();
        vm.setTextMlScanned(null);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


}
