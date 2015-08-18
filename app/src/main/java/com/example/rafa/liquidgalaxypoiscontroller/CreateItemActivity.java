package com.example.rafa.liquidgalaxypoiscontroller;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;


public class CreateItemActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_item);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new CreateItemFragment())
                    .commit();
        }
    }
}
