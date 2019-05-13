package com.bulksmskivubox.broker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Edit extends AppCompatActivity {

    Toolbar toolbar;
    String key ;

    TextInputEditText price, num, desc;
    TextInputLayout numLayout;
    Button next;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Edit");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Edit.this, Account.class));
            }
        });

        //Getting the house object from the previous activity
        key = getIntent().getStringExtra("key");

        formDetails();

    }

    public void formDetails(){

        price = findViewById(R.id.price);

        num = findViewById(R.id.number);
        numLayout = findViewById(R.id.numLayout);
        numLayout.setHint("Size");


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

                DatabaseReference ref =  mDatabase.child("properties/"+key);
                Map<String, Object> updates = new HashMap<String,Object>();
                updates.put("price", Double.parseDouble(price.getText().toString()));
                updates.put("capacity", num.getText().toString());
                updates.put("description", desc.getText().toString());
                ref.updateChildren(updates)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Edit.this, "Property edited", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Edit.this, "Could not edit the item \n "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });


            }
        });


    }

}
