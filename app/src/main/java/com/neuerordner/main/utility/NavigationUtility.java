package com.neuerordner.main.utility;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class NavigationUtility {
    private View _root;
    private NavController _nav;

    public NavigationUtility(View root) {
        _root = root;
        _nav = Navigation.findNavController(_root);
    }

    public NavigationUtility(NavController nav) {
        _nav = nav;
    }

    public void navigatePreviousStack() {
        _nav.popBackStack();
    }

    public void navigateWithBundle(@IdRes Integer id, Bundle bundle) {
        _nav.navigate(id, bundle);
    }

    public void navigateWithoutBundle(@IdRes Integer id) {
        _nav.navigate(id);
    }
}
