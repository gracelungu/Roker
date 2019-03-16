package com.bulksmskivubox.broker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Property extends AppCompatActivity {

    FirebaseAuth mAuth;

    Toolbar toolbar;
    ImageView house, land;

    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property);

        mAuth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Property");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        house = findViewById(R.id.house);
        house.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                forward(false);

            }
        });

        land = findViewById(R.id.land);
        land.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                forward(true);

            }
        });



    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();

    }

    public void forward(Boolean isLand){

        House house = new House(
                0,
                null,
                null,
                null,
                null,
                isLand,
                false,
                true,
                this.currentUser.getUid()
        );

        startActivity(new Intent(Property.this, location.class).putExtra("House",house));

    }




}
