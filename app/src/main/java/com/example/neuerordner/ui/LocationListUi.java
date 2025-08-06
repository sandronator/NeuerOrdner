package com.example.neuerordner.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.neuerordner.data.ActiveLocation;
import com.example.neuerordner.data.AppDatabase;
import com.example.neuerordner.data.CreateDatabase;
import com.example.neuerordner.data.DatabaseService;
import com.example.neuerordner.data.FileService;
import com.example.neuerordner.data.GlobalViewModel;
import com.example.neuerordner.data.Item;
import com.example.neuerordner.data.Location;
import com.example.neuerordner.data.RegexSearch;
import com.example.neuerordner.R;
import com.google.gson.reflect.TypeToken;
import com.example.neuerordner.utility.ItemUtility;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class LocationListUi extends Fragment {
    private GlobalViewModel vm;
    private boolean isSetLocation = true;
    private AppDatabase db;
    private List<Location> locations = Collections.emptyList();
    private RegexSearch regexSearch;
    private boolean seachLocation = false;
    private String uri = null;
    private static final String WRITEPERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private ActivityResultLauncher<String> permissionLauncher;

    private ActivityResultLauncher<String> folderPickerLauncher;
    private ItemUtility _itemUtility = null;


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
    public LocationListUi() { /* required empty ctor */ }

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
                        TypeToken<Map<String, List<Item>>> ttoken = new TypeToken<Map<String, List<Item>>>() {};
                        Object fetchedObject = fileService.fetchJson(ttoken);
                        Map<String, List<Item>> locationItems = (Map<String, List<Item>>) fetchedObject;
                        DatabaseService databaseService = new DatabaseService(db);

                        switch (which) {
                            case (0):
                                databaseService.InsertIntoDatabase(locationItems);
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
                    }  catch (ClassCastException e) {
                        Toast.makeText(requireContext(), "Not Valid File Format", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Error in loading Json: " + e, Toast.LENGTH_LONG).show();
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
        View root = inflater.inflate(R.layout.fragment_location_list, container, false);
        containerLayout = root.findViewById(R.id.wrapperLinear);
        // create a dedicated body-holder that sits *after* the static header
        bodyLayout = new LinearLayout(requireContext());
        bodyLayout.setOrientation(LinearLayout.VERTICAL);
        containerLayout.addView(bodyLayout);
        db = CreateDatabase.fetch(requireContext());
        vm = new ViewModelProvider(requireActivity()).get(GlobalViewModel.class);
        regexSearch = new RegexSearch(db);

        root.post(() -> {
            _itemUtility = new ItemUtility(db, requireContext(), root);
        });

        locations = db.locationDAO().getAll();

        folderPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/json"),
                uri -> {
                    if (uri == null) {
                        return;
                    }
                    try {
                        OutputStream outputStream = getContext().getContentResolver().openOutputStream(uri);
                        FileService fileService = new FileService(outputStream);
                        fileService.dump(DatabaseService.getAllLocationsAndItems(db), outputStream);

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
        Button updateButton = root.findViewById(R.id.UpdateDatabase);
        Button dumpButton = root.findViewById(R.id.DumpDatabase);

        // refreshView on Click
        refreshButton.setOnClickListener(click -> {
            vm.setActionLocation(null);
            vm.setGlobalItems(new ArrayList<>());
            locations = db.locationDAO().getAll();
            renderBody();
        });

        // switch between location / item search
        switchCompat.setOnCheckedChangeListener((btn, checked) -> {
            seachLocation = checked;
            String itemString = getString(R.string.itemset);
            String locationString = getString(R.string.locationset);
            switchView.setText(checked ? locationString : itemString);
        });

        updateButton.setOnClickListener(l ->
            selectUpdateFile.launch(new String[] { "application/json" }));

        dumpButton.setOnClickListener(l -> {
            folderPickerLauncher.launch("DatabaseSafe_NeuerOrdner");
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

            HashMap<String, String> results = regexSearch.search(query, seachLocation);
            if (results.isEmpty()) {
                Toast.makeText(requireContext(), "Nothing Found", Toast.LENGTH_SHORT).show();
                return; // nothing found
            }


            // --- rebuild location list based on results -----------------------------------
            locations = new ArrayList<>();
            if (seachLocation) {
                for (Map.Entry<String, String> e : results.entrySet()) {
                    Location l = new Location();
                    l.Id = e.getKey();
                    l.Name = e.getValue();
                    locations.add(l);
                }
            } else { // searching items – collect unique location IDs
                HashSet<String> seenItem = new HashSet<>();
                HashSet<String> seenLocation = new HashSet<>();
                List<Item> allItems = db.itemDao().getSyncedAll();

                for (Map.Entry<String, String> e : results.entrySet()) {
                    String itemId = e.getKey();

                    if (seenItem.contains(itemId)) {
                        continue;
                    }
                    seenItem.add(itemId);

                    for (Item i: allItems) {
                        if (i.Id.equals(itemId)) {
                            if (!seenLocation.contains(i.LocationId)) {
                                seenLocation.add(i.LocationId);
                                Location found = db.locationDAO().get(i.LocationId);
                                locations.add(found);
                            }
                        }
                    }
                }
            }
            vm.setActionLocation(null);
            vm.setGlobalItems(null);
            renderBody();
        });

        // add-location navigation
        addLocationBtn.setOnClickListener(v -> {
            NavController nav = Navigation.findNavController(v);
            nav.navigate(R.id.locationContainerFragment);
        });

        // style tweaks
        searchQuery.setBackgroundColor(Color.GRAY);
        searchIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_search_32));

        Drawable refreshDraw = ContextCompat.getDrawable(requireContext(), R.drawable.ic_refresh_24).mutate();
        refreshButton.setImageDrawable(refreshDraw);

        renderBody();
        return root;
    }

    /**
     * Regenerates the list of location rows + nested item views inside {@link #bodyLayout}.
     */
    private void renderBody() {
        bodyLayout.removeAllViews();

        // if we navigated in with an "active" location, show only that
        ActiveLocation active = vm.getActionLocation().getValue();
        if (active != null) {
            Location l = new Location();
            l.Id = active.Id; l.Name = active.Name;
            locations.clear();
            locations = new ArrayList<>();
            locations.add(l);
            try {
            } catch (NoSuchElementException e) {
                Toast.makeText(requireContext(), "List is empty", Toast.LENGTH_SHORT).show();
            }
        }
        if (bodyLayout.getChildCount() > 0) {
            return;
        }
        for (int i = 0; i < locations.size(); i++) {
            Location loc = locations.get(i);

            loc.Id = loc.Id.trim().toLowerCase();
            List<Item> items = db.itemDao().getAllFromLocationSynced(loc.Id);
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
            //Delete Location Button
            ImageButton deleteLocButton = new ImageButton(requireContext());
            Drawable trashBin = ContextCompat.getDrawable(requireContext(), R.drawable.ic_trash_32).mutate();
            deleteLocButton.setImageDrawable(trashBin);
            deleteLocButton.setLayoutParams(wrappedTextParams);

            // location label
            TextView displayLoc = new TextView(requireContext());
            displayLoc.setLayoutParams(blanc);
            displayLoc.setText(loc.Name);
            displayLoc.setTextSize(20);
            displayLoc.setPadding(0, 20, 0, 10);
            headerView.addView(displayLoc);


            //QR-Code
            ImageButton showQr = new ImageButton(requireContext());
            Drawable qrIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_qrcode_24).mutate();
            showQr.setImageDrawable(qrIcon);
            showQr.setBackground(null);
            ViewGroup.MarginLayoutParams mp = new ViewGroup.MarginLayoutParams(200, 200);
            mp.setMargins(35, 0, 0, 0);
            showQr.setLayoutParams(mp);
            showQr.setScaleType(ImageView.ScaleType.FIT_XY);
            showQr.setForegroundGravity(Gravity.TOP);
            showQr.setOnClickListener(qv -> {
                ActiveLocation al = new ActiveLocation();
                al.Id = loc.Id;
                al.Name = loc.Name;
                vm.setActionLocation(al);

                NavController nav = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                nav.navigate(R.id.action_global_qrCodeShowFragment);
            });

            headerView.addView(deleteLocButton);
            headerView.addView(showQr);
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
                if (items == null || items.isEmpty()) {
                    Toast.makeText(requireContext(), "No Items", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (Item it : items) {
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

            deleteLocButton.setOnClickListener(click -> {
                _itemUtility.alertDialogCreation("Delete Location", "Changes cant be undone", "Delete", "Cancel", () -> {
                    db.locationDAO().delete(loc);
                    headerView.removeAllViews();
                    superHolder.removeAllViews();
                    return null;
                }, () -> {
                    return null;
                }).show();

            });


        }
    }

    @Override
    public void onPause() {
        super.onPause();
        locations = db.locationDAO().getAll();
        renderBody();

    }
    @Override
    public void onResume() {
        super.onResume();
    }}