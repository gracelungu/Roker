package com.bulksmskivubox.broker;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Item extends AppCompatActivity {

    Toolbar toolbar;
    String key ;

    private FirebaseAuth mAuth;

    TextView price, room, land, description;
    Button get;
    boolean LOGGEDIN = false;
    ViewPager viewPager;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        mDatabase = FirebaseDatabase.getInstance().getReference();

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

        //Getting the house object from the previous activity
        key = getIntent().getStringExtra("key");

        // Initialize
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        // Initialize the variables
        initVars();

        try{
            //Get the house
            getHouse();
        }catch(Exception e){

        }

    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {

            // User is signed in
            LOGGEDIN = true;

        } else {

            // No user is signed in
            LOGGEDIN = false;

        }


    }

    public void initVars(){

        price = findViewById(R.id.price);
        room = findViewById(R.id.room);
        land = findViewById(R.id.land);
        description = findViewById(R.id.description);

        get = findViewById(R.id.get);



    }

    public void getHouse(){

        mDatabase.child("properties/"+key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                final House house = dataSnapshot.getValue(House.class);
                viewPager.setAdapter(new ImagePageAdapter(Item.this, house.getImages()));

                price.setText(house.getPrice()+" UGX");

                if(!house.land){
                    land.setText("HOUSE");
                    room.setText(house.getCapacity()+" Rooms");
                }else{
                    land.setText("LAND");
                    room.setText(house.getCapacity()+" Acres");
                }

                description.setText(house.getDescription());

                get.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getIntouch(house.getOwner());
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Toast.makeText(Item.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void getIntouch (String owner){
        mDatabase.child("users/"+owner).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Uri number = Uri.parse("tel:"+user.getPhone());
                Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                startActivity(callIntent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void deleteItem(){
       DatabaseReference ref =  mDatabase.child("properties/"+key);
        Map<String, Object> updates = new HashMap<String,Object>();
        updates.put("active", false);
        ref.updateChildren(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Item.this, "Property deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Item.this, "Could not delete the item \n "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if(LOGGEDIN){
            inflater.inflate(R.menu.item_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.delete){
            deleteItem();
            finish();
        }

        if(item.getItemId() == R.id.edit){
            startActivity(new Intent(this, Edit.class).putExtra("key",key));
        }
        return super.onOptionsItemSelected(item);
    }
}
