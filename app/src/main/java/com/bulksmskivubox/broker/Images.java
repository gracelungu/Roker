package com.bulksmskivubox.broker;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.events.Publisher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Images extends AppCompatActivity {

    Toolbar toolbar;
    TextView vr;
    AlertDialog.Builder builder;
    House house;
    TextView chooseGallery;
    Button next;
    List<Bitmap> images = new ArrayList<>();
    static final int PICK_IMAGE_REQUEST = 1;
    private String vrPackageName = "com.google.vr.cyclops";

    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Images");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // mAuth
        mAuth = FirebaseAuth.getInstance();

        //Handles the 360 image selection
        vrCamera();

        //Getting the new house object from the previous activity
        house = (House) getIntent().getSerializableExtra("House");


        // Get the image from the gallery
        chooseGallery = findViewById(R.id.gallery);
        chooseGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check it the images do not exceed 10
                if(images.size() <= 10 ){
                    selectImage();
                }else{
                    Toast.makeText(Images.this, "You can only add 10 images", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // Move to the next step
        next = findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check if the user selected an image
                if(images.size() == 0){

                    Toast.makeText(Images.this, "You must select an image", Toast.LENGTH_SHORT).show();

                }else{

                    // Publish
                    new Publish(Images.this,house,currentUser,images);

                }

            }
        });

    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();
    }


    public void selectImage(){

        Intent getImage = new Intent();
        getImage.setType("image/*");
        getImage.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(getImage, "Select Image"), PICK_IMAGE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));

                // Adding the selected image to the view
                addImage(bitmap);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addImage(Bitmap bitmap){

        // Adding the bitmap image to the images list
        images.add(bitmap);

        LinearLayout parent  = findViewById(R.id.parent);

        // fill in the image
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        imageView.setLayoutParams(layoutParams);
        layoutParams.setMargins(0, 0, 0, 20);
        imageView.setImageBitmap(bitmap);

        // insert into main view
        parent.addView(imageView);

    }

    public void vrCamera(){

        vr = findViewById(R.id.vr);
        vr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isAppInstalled(Images.this,vrPackageName)){

                    alertMessage(Images.this,true);

                }else{

                    alertMessage(Images.this,false);

                }

            }
        });

    }

    public void alertMessage(Context context, final boolean found){

        builder = new AlertDialog.Builder(context);

        builder.setTitle("360 Image")
                .setMessage("Use the Cardboard camera app to capture the 360 image, then select the image from the gallery.")
                .setPositiveButton("Open", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete

                        if(found){

                            launchApp(vrPackageName);

                        }else{

                            launchGoogle("com.google.vr.cyclops");

                        }

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();

    }

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void launchApp(String packageName){

        Toast.makeText(this, "Openning Cardboard camera", Toast.LENGTH_SHORT).show();

        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            startActivity(launchIntent);//null pointer check in case package name was not found
        }


    }

    public void launchGoogle(String packageName){

        Toast.makeText(this, "Launching Google play", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id="+packageName));
        startActivity(intent);

    }

}
