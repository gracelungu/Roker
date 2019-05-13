package com.bulksmskivubox.broker;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import android.Manifest;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private FirebaseAuth mAuth;
    private Menu menu;

    private GoogleMap mMap;
    private View mapView;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private float DEFAULT_ZOOM = 16;
    private LatLng DEFAULT_LOCATION = new LatLng(0.347596,32.582520);

    private Toolbar toolbar;
    
    private FirebaseUser user;
    
    private boolean LOGGEDIN;

    private DatabaseReference mDatabase;RecyclerView itemsRecycler;
    private FirebaseRecyclerAdapter adapter;

    private LinearLayoutManager linearLayoutManager;



    static {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        mAuth = FirebaseAuth.getInstance();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mapView = mapFragment.getView();

        mFusedLocationProviderClient = LocationServices
                .getFusedLocationProviderClient(this);

        // Items recycler view
        itemsRecycler = findViewById(R.id.items_recycler);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        itemsRecycler.setLayoutManager(linearLayoutManager);
        itemsRecycler.setHasFixedSize(true);


        //Initialize the variables
        initVars();

        // Display all the properties on the bottom sheet
        fetch();

    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        user = mAuth.getCurrentUser();

        if (user != null) {

            // User is signed in
            LOGGEDIN = true;

        } else {

            // No user is signed in
            LOGGEDIN = false;

        }
        adapter.startListening();


    }


    public class viewHolder extends RecyclerView.ViewHolder {

        RelativeLayout root;
        TextView item_price;
        TextView capacity;
        ImageView image, locate;
        TextView type;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
            item_price = itemView.findViewById(R.id.price);
            capacity = itemView.findViewById(R.id.capacity);
            image = itemView.findViewById(R.id.item_image);
            locate = itemView.findViewById(R.id.locate);
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


        adapter = new FirebaseRecyclerAdapter<House, MapsActivity.viewHolder>(options) {
            @Override
            public MapsActivity.viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item, parent, false);

                return new MapsActivity.viewHolder(view);
            }

            @Override
            protected void onBindViewHolder(MapsActivity.viewHolder holder, final int position, final House model) {

                if(!model.getActive()){
                    holder.root.setVisibility(View.GONE);
                }

                int icon ;

                if(model.getLand()){
                    icon = R.drawable.land_icon;
                }else{
                    icon = R.drawable.house_icon;
                }


                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(model.getLatitude(), model.getLongitude()))
                        .snippet(model.getPrice()+" UGX")
                        .icon(BitmapDescriptorFactory.fromResource(icon))
                        .title(model.getPrice()+" UGX")

                );

                marker.setTag(adapter.getRef(position).getKey());

                holder.setCapacity(model.getCapacity(), model.getLand());
                holder.setItem_price(model.getPrice());
                holder.setType(model.getLand());
                holder.setImage(model.getImages());

                holder.root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(MapsActivity.this, Item.class).putExtra("key",adapter.getRef(position).getKey()));
                    }
                });

                holder.locate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(model.getLatitude(), model.getLongitude())));
                    }
                });

            }

        };
        itemsRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();



    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        String itemKey = (String) marker.getTag();
        startActivity(new Intent(MapsActivity.this, Item.class).putExtra("key",itemKey));


        return true;
    }

    public void initVars(){

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ViewCompat.setElevation(findViewById(R.id.toolbar),2.0f);
        toolbar.setTitle("");

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;


        //Relocate the position button
        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 300);
        }


        mMap.setMinZoomPreference(6.0f);
        mMap.setMaxZoomPreference(16.0f);

        requestLocationPermission();

        // Place autocomplete search
        placeSearch();

        // Get all the properties and show them on the map
        getProperties();

        // Set a listener for marker click.
        mMap.setOnMarkerClickListener(this);


    }

    public void getProperties(){

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child("properties")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        House house = dataSnapshot.getValue(House.class);
                        showPropertyOnMap(house);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                        Toast.makeText(MapsActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    public void showPropertyOnMap(House house){

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(house.getLatitude(), house.getLongitude()))
                .title(house.getPrice()+" UGX"));


    }

    public void placeSearch(){

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);


        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                LatLng coordinate = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                        coordinate, 15);
                mMap.animateCamera(location);

            }

            @Override
            public void onError(Status status) {

                Toast.makeText(MapsActivity.this, "Try again", Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void requestLocationPermission() {

        //Check if the permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //Request for the location permission if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);

        }//If the permission is granted
        else if (mMap != null) {

            mMap.setMyLocationEnabled(true);

            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {

                        // Set the map's camera position to the current location of the device.
                        Location location = task.getResult();

                        if(location != null){
                            LatLng currentLatLng = new LatLng(location.getLatitude(),
                                    location.getLongitude());
                            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLatLng,
                                    DEFAULT_ZOOM);
                            mMap.animateCamera(update);
                        }

                        else{
                            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION,
                                    DEFAULT_ZOOM);
                            mMap.animateCamera(update);
                        }

                    }
                }
            });

        }
    }

    private void showDefaultLocation() {
        Toast.makeText(this, "Please enable your location", Toast.LENGTH_SHORT).show();
        LatLng kampala = new LatLng(0.347596, 32.582520);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(kampala));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLocationPermission();
                } else {
                    showDefaultLocation();
                }
                return;
            }

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.account){
            
            startActivity(new Intent(this,Account.class));
        
        }

        if(item.getItemId() == R.id.log){

            if(LOGGEDIN){

                FirebaseAuth.getInstance().signOut();

                startActivity(new Intent(this,MapsActivity.class));

                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();

            }else{
                startActivity(new Intent(this,PhoneVerify.class));
            }


        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        this.menu = menu;

        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if(LOGGEDIN){

            menu.findItem(R.id.account).setVisible(true);
            menu.findItem(R.id.log).setTitle("Logout");

        }
        else{

            menu.findItem(R.id.account).setVisible(false);
            menu.findItem(R.id.log).setTitle("Login");

        }

        return super.onPrepareOptionsMenu(menu);
    }
}
