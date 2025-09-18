package com.neuerordner.main.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.neuerordner.main.R;
import com.neuerordner.main.data.DatabaseService;
import com.neuerordner.main.data.Location;
import com.neuerordner.main.utility.ItemUtility;
import com.neuerordner.main.utility.NavigationUtility;

import java.util.ArrayList;
import java.util.List;

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

    private void updateLocationSelected() {
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

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            // prohibit auto resizing
            lp.width = 0;
            // height is normal wrapped
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            // add a equal weights
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);

            checkBox.setLayoutParams(lp);
            // allow multi line
            checkBox.setSingleLine(false);
            // row limit
            checkBox.setMaxLines(3);
            // forbid "..." clipping
            checkBox.setEllipsize(null);

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
                updateLocationSelected();
            });

            checkBoxLayout.addView(checkBox);
        }

        if (allClicked) counter = locationList.size();
        updateLocationSelected();



    }
}
