package com.example.neuerordner.ui;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.beust.ah.A;
import com.example.neuerordner.R;
import com.example.neuerordner.data.AppDatabase;
import com.example.neuerordner.data.CreateDatabase;
import com.example.neuerordner.data.DatabaseService;
import com.example.neuerordner.data.Location;
import com.example.neuerordner.utility.ItemUtility;
import com.example.neuerordner.utility.NavigationUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.Inflater;

public class QrMassSelect extends Fragment {

    private DatabaseService _dbService;
    private ItemUtility _itemUtility;
    private ArrayList<Location> locationSelected;
    private List<Location> locationList;
    private NavigationUtility _navUtil;
    private GridLayout checkBoxLayout;
    private TextView selectCounterView;
    private int counter = 0;
    public QrMassSelect() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        View root = inflater.inflate(R.layout.fragment_mass_select, container, false);
        selectCounterView = root.findViewById(R.id.selectCounter);
        checkBoxLayout = root.findViewById(R.id.layout_mass_select);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View root, Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        _dbService = new DatabaseService(requireContext());
        _itemUtility = new ItemUtility(_dbService, requireContext(), root);
        _navUtil = new NavigationUtility(root);
        locationList = _dbService.getAllLocations();
        locationSelected = new ArrayList<Location>();

        makeCheckBox(root, false);

        Button saveButton = root.findViewById(R.id.saveButton);
        Button selectAllButton = root.findViewById(R.id.selectAllButton);


        saveButton.setOnClickListener(l -> {
            if (locationList == null || locationList.isEmpty() || locationList.size() < 1) {
                Toast.makeText(requireContext(), "Nothing Selceted", Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle locationBundle = new Bundle();
            locationBundle.putParcelableArrayList("KEY", locationSelected);

            _navUtil.navigateWithBundle(R.id.qrCodeDisplayContainerFragment, locationBundle);
        });

        selectAllButton.setOnClickListener(l -> {
            if (locationSelected.size() == locationList.size()) {
                locationSelected = new ArrayList<>();
                counter = 0;
                checkBoxLayout.removeAllViews();
                makeCheckBox(root, false);
            } else {
                locationSelected = new ArrayList<>(locationList);
                checkBoxLayout.removeAllViews();
                makeCheckBox(root, true);
            }
        });
    }

    private void updateCounterState() {
        if (counter > 0) {
            selectCounterView.setVisibility(View.VISIBLE);
        } else {
            selectCounterView.setVisibility(View.GONE);
        }
        selectCounterView.setText(String.valueOf(counter));
    }

    private void makeCheckBox(View root, boolean allClicked)  {

        for (Location loc: locationList) {
            CheckBox checkBox = new CheckBox(requireContext());
            checkBox.setText(loc.Name);
            checkBox.setTag(loc);
            checkBox.setChecked(allClicked);
            if (allClicked) counter = locationList.size();
            updateCounterState();
            //If clicked we check the current state and add or remove the location based on the state of checkbox
            checkBox.setOnClickListener(l -> {
                if (checkBox.isChecked()) {
                    locationSelected.add(loc);
                    counter++;
                } else {
                    if (locationSelected.contains(loc)) {
                        locationSelected.remove(loc);
                        counter--;
                    }
                }
                updateCounterState();
            });

            checkBoxLayout.addView(checkBox);
        }


    }
}
