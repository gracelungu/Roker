package com.bulksmskivubox.broker;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Details extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private DatabaseReference mDatabase;

    Toolbar toolbar;
    TextInputEditText price, num, desc;
    TextInputLayout numLayout;
    Button next;
    boolean land;
    House house;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mAuth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Details");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Getting the new house object from the previous activity
        house = (House) getIntent().getSerializableExtra("House");

        land = house.getLand();

        //Validation and submition
        formDetails();

        //Getting the new house object from the previous activity
        house = (House) getIntent().getSerializableExtra("House");


    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        user = mAuth.getCurrentUser();


    }

    public void formDetails(){

        price = findViewById(R.id.price);

        num = findViewById(R.id.number);
        numLayout = findViewById(R.id.numLayout);

        if(land){
            numLayout.setHint("Acres");
        }


        desc = findViewById(R.id.description);

        next = findViewById(R.id.next);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(price.getText().toString().isEmpty()){
                    price.setError("Required");
                    return;
                }

                if(num.getText().toString().isEmpty()){
                    num.setError("Required");
                    return;
                }



                //Setting data to save
                house.setPrice(Double.parseDouble(price.getText().toString()));
                house.setCapacity(num.getText().toString());
                house.setDescription(desc.getText().toString());


                startActivity(new Intent(Details.this, Images.class).putExtra("House",house));


            }
        });

    }


}
