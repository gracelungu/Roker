package com.bulksmskivubox.broker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.util.List;

public class Account extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    Toolbar toolbar;
    ImageView reload;
    TextView publish, telephone;
    LinearLayout add;

    RecyclerView itemsRecycler;
    private FirebaseRecyclerAdapter adapter;

    private LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mAuth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Account");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Account.this, MapsActivity.class));
            }
        });

        // Items recycler view
        itemsRecycler = findViewById(R.id.items_recycler);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        itemsRecycler.setLayoutManager(linearLayoutManager);
        itemsRecycler.setHasFixedSize(true);

        //Initialize variables
        initVars();


    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();

        //Displaying the user telephone number
        telephone.setText(currentUser.getPhoneNumber());

        if(currentUser != null){
            fetch();
        }

        adapter.startListening();



    }


    public void initVars() {

        reload = findViewById(R.id.reload);
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Account.this, PhoneVerify.class));
            }
        });

        publish = findViewById(R.id.publish);
        publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Account.this, Property.class));
            }
        });

        telephone = findViewById(R.id.telephone);

        add = findViewById(R.id.add);




    }

    public class viewHolder extends RecyclerView.ViewHolder {

        RelativeLayout root;
        TextView item_price;
        TextView capacity;
        ImageView image;
        TextView type;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
            item_price = itemView.findViewById(R.id.price);
            capacity = itemView.findViewById(R.id.capacity);
            image = itemView.findViewById(R.id.item_image);
            type = itemView.findViewById(R.id.type);
        }

        public void setItem_price(double price) {
            this.item_price.setText(price+" ugx");
        }

        public void setCapacity(String capacity, boolean land) {

            if(land){
                this.capacity.setText(capacity+" Acres");
            }else{
                this.capacity.setText(capacity+" Rooms");
            }

        }

        public void setType(Boolean land){
            if(land){
                this.type.setText("LAND");
            }else{
                this.type.setText("HOUSE");
            }
        }

        public void setImage(List<String> images) {
            Picasso.get().load(images.get(0)).into(this.image);
            Log.i("IMAGE PIC", images.get(0));
        }
    }

    public void fetch() {

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("properties");


        FirebaseRecyclerOptions<House> options =
                new FirebaseRecyclerOptions.Builder<House>()
                        .setQuery(query, House.class)
                        .build();


        adapter = new FirebaseRecyclerAdapter<House, viewHolder>(options) {
            @Override
            public viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item, parent, false);

                return new viewHolder(view);
            }

            @Override
            protected void onBindViewHolder(viewHolder holder, final int position, final House model) {

                if(!model.getOwner().equals(currentUser.getUid()) && model.getActive() ){
                   holder.root.setVisibility(View.GONE);
                }

                holder.setCapacity(model.getCapacity(), model.getLand());
                holder.setItem_price(model.getPrice());
                holder.setType(model.getLand());
                holder.setImage(model.getImages());

                holder.root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(Account.this, Item.class).putExtra("key",adapter.getRef(position).getKey()));
                    }
                });

            }

        };
        itemsRecycler.setAdapter(adapter);


    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(Account.this, MapsActivity.class));
        super.onBackPressed();
    }
}
