package com.bulksmskivubox.broker;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import android.Manifest;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

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


        //Initialize the variables
        initVars();

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
            layoutParams.setMargins(0, 0, 30, 100);
        }


        mMap.setMinZoomPreference(6.0f);
        mMap.setMaxZoomPreference(16.0f);

        requestLocationPermission();

        // Place autocomplete search
        placeSearch();

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
        mMap.moveCamera(CameraUpdateFactory.newLatLng(kampala));
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
