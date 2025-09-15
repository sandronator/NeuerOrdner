package com.neuerordner.main.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.neuerordner.main.data.ActiveLocation;
import com.neuerordner.main.data.DatabaseService;
import com.neuerordner.main.data.FileService;
import com.neuerordner.main.data.GlobalViewModel;
import com.neuerordner.main.data.Item;
import com.neuerordner.main.data.Location;
import com.neuerordner.main.data.NameAccess;
import com.neuerordner.main.data.RegexSearch;
import com.neuerordner.main.R;
import com.neuerordner.main.utility.NavigationUtility;
import com.google.gson.reflect.TypeToken;
import com.neuerordner.main.utility.ItemUtility;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class HomeDisplayUi<E> extends Fragment {
    private GlobalViewModel vm;
    private boolean isSetLocation = true;
    private List<Location> locations = Collections.emptyList();
    private RegexSearch regexSearch;
    private boolean searchLocations = false;
    private String uri = null;
    private static final String WRITEPERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private ActivityResultLauncher<String> permissionLauncher;

    private ActivityResultLauncher<String> folderPickerLauncher;
    private ItemUtility _itemUtility = null;
    private DatabaseService _dbService;
    private List<Item> itemList = new ArrayList<>();
    private NavigationUtility _navUtil;



    // ↑ Feld im Fragment
    private final ActivityResultLauncher<String[]> selectUpdateFile =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    uri -> {                         // Callback NACH Dateiauswahl
                        if (uri != null) {
                            this.uri = uri.toString();
                            showDecisionDialog();    // dein Popup mit 4 Optionen
                        } else {
                            Toast.makeText(requireContext(),
                                    "Keine Datei gewählt!", Toast.LENGTH_SHORT).show();
                        }
                    });


    /**
     * Static content (header, switch, search field, add-location button) lives in
     * the XML file. All dynamic "body" views (location rows + their item blocks)
     * are rendered into this layout so that we can wipe and rebuild it at will.
     */
    private LinearLayout bodyLayout;
    private LinearLayout containerLayout;
    boolean activeLoc = false;
    public HomeDisplayUi() { /* required empty ctor */ }

    private void startChooseDirectoryIntent() {
        Intent folderDumpingIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        folderDumpingIntent.addCategory(Intent.CATEGORY_DEFAULT);
        startActivity(Intent.createChooser(folderDumpingIntent, "Choose directory"));
    }

    private void showDecisionDialog(){
        String[] mergeOptions = {
                "Insert", //Insert Data skipping existing
                "Update", //Upadting existing
                "Update Insert", //Updating Existing and Inserting new
                "Erase And Setup New", //Delete and Setup new
        };

        new AlertDialog.Builder(requireContext())
                .setTitle("Select Action")
                .setItems(mergeOptions, (dialog, which) -> {
                    try {
                        if (uri == null || uri.isEmpty()) {
                            Toast.makeText(requireContext(), "Set Filepath first", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Uri convertedUri = Uri.parse(uri);
                        InputStream inputStream = getContext().getContentResolver().openInputStream(convertedUri);
                        FileService fileService = new FileService(inputStream);
                        TypeToken<Map<String, List<Item>>> ttoken = new TypeToken<Map<String, List<Item>>>() {
                        };
                        Object fetchedObject = fileService.fetchJson(ttoken);
                        Map<String, List<Item>> locationItems = (Map<String, List<Item>>) fetchedObject;
                        DatabaseService databaseService = new DatabaseService(requireContext());

                        switch (which) {
                            case (0):
                                databaseService.InsertHashMap(locationItems);
                                break;
                            case (1):
                                databaseService.UpdateDatabase(locationItems);
                                break;
                            case (2):
                                databaseService.UpdateAndInsert(locationItems);
                                break;
                            case (3):
                                databaseService.EraseAndSetupNew(locationItems);
                                break;
                            default:
                                break;
                        }

                    } catch (IOException ioExc) {
                        Toast.makeText(requireContext(), "Error in Filestream", Toast.LENGTH_LONG).show();
                    } finally {
                        makeRefresh();
                    }
                }).show();


    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (!isGranted) {
                        Toast.makeText(requireContext(), "Not Granted", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // ----- inflate & basic setup ----------------------------------------------------------
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        containerLayout = root.findViewById(R.id.wrapperLinear);
        // create a dedicated body-holder that sits *after* the static header
        bodyLayout = new LinearLayout(requireContext());
        bodyLayout.setOrientation(LinearLayout.VERTICAL);
        containerLayout.addView(bodyLayout);
        vm = new ViewModelProvider(requireActivity()).get(GlobalViewModel.class);
        _dbService = new DatabaseService(requireContext());
        regexSearch = new RegexSearch(_dbService);

        root.post(() -> {
            _itemUtility = new ItemUtility(_dbService, requireContext(), root);
        });

        locations = _dbService.getAllLocations();

        folderPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/json"),
                uri -> {
                    if (uri == null) {
                        return;
                    }
                    try {
                        OutputStream outputStream = getContext().getContentResolver().openOutputStream(uri);
                        FileService fileService = new FileService(outputStream);
                        List<Location> allLocation = _dbService.getAllLocations();
                        Map<String, List<Item>> locationItems = new HashMap<>();
                        for (Location location: allLocation) {
                            List<Item> itemList = _dbService.getAllItemsFromLocation(location.Id);
                            locationItems.put(location.Name, itemList);
                        }
                        fileService.dump(locationItems, outputStream);

                    } catch (IOException ioException) {
                        Toast.makeText(requireContext(), "Cant Create File on desired Location", Toast.LENGTH_SHORT).show();
                    }
                });

        // ----- header widgets ---------------------------------------------------------------
        TextView switchView   = root.findViewById(R.id.switchView);
        SwitchCompat switchCompat = root.findViewById(R.id.searchSwitch);
        EditText searchQuery  = root.findViewById(R.id.searchQuery);
        ImageButton searchIcon = root.findViewById(R.id.searchIcon);
        Button addLocationBtn = root.findViewById(R.id.addLocationButton);
        ImageButton refreshButton = root.findViewById(R.id.refreshView);
        ImageButton dBResourceButton = root.findViewById(R.id.db_menu);
        ImageButton ascendingButton = root.findViewById(R.id.sortAscending);
        ImageButton descendingButton = root.findViewById(R.id.sortDescending);

        ToggleButton dateSort = root.findViewById(R.id.SortByDatetimeOffset);
        Comparator<Location> compareByDate = (l1, l2) -> l1.CreationDate.compareTo(l2.CreationDate);
        descendingButton.setOnClickListener(l -> {
            if (dateSort.isChecked()) {

                Collections.sort(locations, compareByDate);
            } else {
                Collections.sort(locations, Collections.reverseOrder());
            }
            renderBody();
        });

        ascendingButton.setOnClickListener(l -> {
            if (dateSort.isChecked()) {
                Collections.sort(locations, compareByDate.reversed());
            } else {
                Collections.sort(locations);
            }
            renderBody();
        });

        // refreshView on Click
        refreshButton.setOnClickListener(click -> {
            makeRefresh();
        });

        // switch between location / item search
        switchCompat.setOnCheckedChangeListener((btn, checked) -> {
            searchLocations = checked;
            String itemString = getString(R.string.itemset);
            String locationString = getString(R.string.locationset);
            switchView.setText(checked ? locationString : itemString);
        });

        dBResourceButton.setOnClickListener(l -> {
            PopupMenu popUpMenu = new PopupMenu(requireContext(), l);
            popUpMenu.getMenuInflater().inflate(R.menu.db_resource_menu, popUpMenu.getMenu());
            popUpMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.UpdateDatabase) {
                    selectUpdateFile.launch(new String[] { "application/json" });
                    return true;
                } else if (menuItem.getItemId() == R.id.DumpDatabase) {
                    folderPickerLauncher.launch("DatabaseSafe_NeuerOrdner");
                    return true;
                } else {
                    Toast.makeText(requireContext(), "Nothing clicked", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
            popUpMenu.show();
        });
        // search action
        searchIcon.setOnClickListener(v -> {
            String query = searchQuery.getText().toString();
            if (query.isEmpty()) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Can't search with Empty Query")
                        .setPositiveButton("OK", (d, w) -> d.dismiss())
                        .setNegativeButton("Abbrechen", (d, w) -> d.dismiss())
                        .show();
                return; // 🚪 bail out – no query
            }

            List<? extends NameAccess> results = regexSearch.search(query, searchLocations);
            if (results.isEmpty()) {
                Toast.makeText(requireContext(), "Nothing Found", Toast.LENGTH_SHORT).show();
                return; // nothing found
            }


            // --- rebuild location list based on results -----------------------------------
            locations = new ArrayList<>();

            if (searchLocations) {
                for (var result : results) {
                    Location l = new Location();
                    l.Id = result.getid();
                    l.Name = result.getname();
                    locations.add(l);
                    itemList = new ArrayList<>();
                }

            } else { // searching items – collect unique location IDs
                Set<String> locationIds = new HashSet<>();
                locations = new ArrayList<>();
                for (var result: results) {
                    locationIds.add(result.getLocationId());
                    Item item = _dbService.getItem(result.getid());
                    itemList.add(item);
                    }
                for (String locationId: locationIds) {
                    locations.add(_dbService.getLocation(locationId));
                }
            }


            vm.setActiveLocation(null);
            vm.setGlobalItems(null);
            renderBody();
        });

        // add-location navigation
        addLocationBtn.setOnClickListener(v -> {
            _navUtil.navigateWithoutBundle(R.id.locationContainerFragment);
        });

        // style tweaks
        searchQuery.setBackgroundColor(Color.GRAY);
        searchIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_search_32));

        Drawable refreshDraw = ContextCompat.getDrawable(requireContext(), R.drawable.ic_refresh_24).mutate();
        refreshButton.setImageDrawable(refreshDraw);

        renderBody();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View root, Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);
        _navUtil = new NavigationUtility(root);
    }
    /**
     * Regenerates the list of location rows + nested item views inside {@link #bodyLayout}.
     */
    private void renderBody() {
        bodyLayout.removeAllViews();
        removeAdChildrenNow();

        // if we navigated in with an "active" location, show only that
        ActiveLocation active = vm.getActionLocation().getValue();
        if (active != null) {
            Location l = new Location();
            l.Id = active.Id;
            l.Name = active.Name;
            locations.clear();
            locations = new ArrayList<>();
            locations.add(l);

        }
        if (bodyLayout.getChildCount() > 0) {
            return;
        }
        for (int i = 0; i < locations.size(); i++) {
            Location loc = locations.get(i);
            loc.Id = loc.Id.trim().toLowerCase();
            final List<Item> itemForLocation;
            if (itemList == null || itemList.isEmpty()) {
                itemForLocation = _dbService.getAllItemsFromLocation(loc.Id);
            } else {
                itemForLocation = itemList.stream().filter(item -> item.LocationId.equals(loc.getid())).collect(Collectors.toList());
            }
            LinearLayout superHolder = new LinearLayout(requireContext());

            superHolder.setVisibility(View.GONE);
            superHolder.setOrientation(LinearLayout.VERTICAL);
            superHolder.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            // Header View
            LinearLayout headerView = new LinearLayout(requireContext());
            headerView.setOrientation(LinearLayout.HORIZONTAL);

            headerView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            // divider
            bodyLayout.addView(Divider.divider(requireContext(), 2));

            LinearLayout.LayoutParams blanc = new LinearLayout.LayoutParams(
                    0, // width = 0, so weight is respected
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f // take all available space
            );
            LinearLayout.LayoutParams wrappedTextParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            ImageButton locationMenuButton = new ImageButton(requireContext());
            Drawable locationMenuParentDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.outline_arrow_drop_down_24);
            locationMenuButton.setLayoutParams(wrappedTextParams);
            locationMenuButton.setImageDrawable(locationMenuParentDrawable);

            locationMenuButton.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(requireContext(), v);
                popupMenu.getMenuInflater().inflate(R.menu.location_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    if (menuItem.getItemId() == R.id.qr_code) {

                        ArrayList<Location> singleLocationsArray = new ArrayList<>();
                        singleLocationsArray.add(loc);

                        Bundle singleBundle = new Bundle();
                        singleBundle.putParcelableArrayList("KEY", singleLocationsArray);

                        _navUtil.navigateWithBundle(R.id.action_global_qrCodeShowFragment, singleBundle);
                        return true;
                    }

                    if (menuItem.getItemId() == R.id.delete_location) {
                        _itemUtility.alertDialogCreation("Delete Location" + loc.Name, "Changes cant be undone", "Delete", "Cancel", () -> {
                            _dbService.deleteLocation(loc);
                            headerView.removeAllViews();
                            superHolder.removeAllViews();
                            return null;
                        }, () -> {
                            return null;
                        }).show();
                        return true;
                    }

                    if (menuItem.getItemId() == R.id.update_location) {
                        vm.setUpdateLocation(loc);
                        _navUtil.navigateWithoutBundle(R.id.locationContainerFragment);
                        return true;
                    }
                    if (menuItem.getItemId() == R.id.button_mass_select) {
                        _navUtil.navigateWithoutBundle(R.id.action_global_qr_mass_scan);
                        return true;
                    }
                    return false;
                });
                popupMenu.show();
            });

            // location label
            TextView displayLoc = new TextView(requireContext());
            displayLoc.setLayoutParams(blanc);
            displayLoc.setText(loc.Name);
            displayLoc.setTextSize(20);
            displayLoc.setPadding(0, 20, 0, 10);
            headerView.addView(displayLoc);



            headerView.addView(locationMenuButton);
            // expandable item holder – starts hidden
            displayLoc.setOnClickListener(click -> {

                if (superHolder.getVisibility() == View.GONE) {
                    superHolder.setVisibility(View.VISIBLE);
                } else {
                    superHolder.setVisibility(View.GONE);
                }
                if (superHolder.getChildCount() > 0) {
                    return;
                }
                if (itemForLocation == null || itemForLocation.isEmpty()) {
                    Toast.makeText(requireContext(), "No Items", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (Item it : itemForLocation) {
                    LinearLayout itemHolder = new LinearLayout(requireContext());
                    itemHolder.setOrientation(LinearLayout.VERTICAL);
                    itemHolder.setVisibility(View.VISIBLE);

                    if (itemHolder.getChildCount() > 0) {
                        return;
                    }
                    _itemUtility.makeItemShowCase(it, itemHolder, superHolder);
                }
            });

            if (i % 10 == 0) {
                int containerId = View.generateViewId();          // eindeutige ID
                FrameLayout adPlaceholder = new FrameLayout(requireContext());
                adPlaceholder.setId(containerId);
                bodyLayout.addView(adPlaceholder);

                getChildFragmentManager()
                        .beginTransaction()
                        .replace(containerId, new Ad(), "ad_" + i)
                        .commit();
            }

            bodyLayout.addView(headerView);
            bodyLayout.addView(superHolder);


        }
    }

    private void makeRefresh() {
        vm.setActiveLocation(null);
        vm.setGlobalItems(new ArrayList<>());
        locations = _dbService.getAllLocations();
        itemList = new ArrayList<>();
        renderBody();
    }

    private void removeAdChildrenNow() {
        var fm = getChildFragmentManager();
        var tx = fm.beginTransaction();
        for (var f : fm.getFragments()) {
            if (f instanceof Ad )  {
                tx.remove(f);
            }
        }
        // commit synchronously so no stale fragments remain
        tx.commitNowAllowingStateLoss();
    }


    @Override
    public void onPause() {
        super.onPause();


    }
    @Override
    public void onResume() {
        super.onResume();
    }}