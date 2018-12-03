package com.example.adamsaunders.iwantthat;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // targets the bottom nav and turns on the listener
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);

        //sets the home fragment as the default fragment
        loadFragment(new SearchFragment());
    }

    // loads in the basic fragments to the display
    public boolean loadFragment(Fragment fragment){
        if(fragment != null)
        {
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
            return true;
        }
        return false;
    }

    // when a item is selected it then loads that fragment
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        Fragment fragment = null;

        //Determines which item is selected
        switch (menuItem.getItemId()){
            case R.id.navigation_search:
                fragment = new SearchFragment();
                break;
            case R.id.navigation_list:
                fragment = new ListFragment();
                break;
            case R.id.navigation_options:
                // navigation options gets a special handler due to it not directly sharing the fragment class
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, new OptionsFragment()).commit();
                return true;
        }

        //return the fragment that has been selected
        return loadFragment(fragment);
    }
}
