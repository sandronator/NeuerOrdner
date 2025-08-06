package com.example.neuerordner;

import android.os.Bundle;

import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.neuerordner.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MobileAds.initialize(this, initializationStatus -> {});

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.locationListContainerFragment, R.id.itemContainerFragment, R.id.cameraScanFragment)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        navView.setOnItemSelectedListener(item -> {
            int dest = item.getItemId();

            // Alte Instanz (falls vorhanden) inkl. Unter-Stack entfernen
            NavOptions opts = new NavOptions.Builder()
                    .setPopUpTo(dest, /* inclusive = */ true)
                    .setLaunchSingleTop(false)      // erzwingt Neubau
                    .build();

            navController.navigate(dest, null, opts);
            return true;                            // Event verbraucht
        });

        /* ───────────── 2. Re-Select des aktiven Tabs neu laden ────────── */
        navView.setOnItemReselectedListener(item -> {
            int dest = item.getItemId();
            navController.popBackStack(dest, /* inclusive = */ true);   // aktuelle Instanz weg
            navController.navigate(dest);                               // neue erzeugen
        });

    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }


}