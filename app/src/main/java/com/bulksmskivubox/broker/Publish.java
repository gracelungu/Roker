package com.bulksmskivubox.broker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Publish {

    Context context;
    House house;
    FirebaseUser user;
    private StorageReference storage;
    List<Bitmap> images;
    AlertDialog.Builder builder;
    List<String> imageUrls = new ArrayList<>();

    public Publish(Context context, House house, FirebaseUser user, List<Bitmap> images) {
        this.context = context;
        this.house = house;
        this.user = user;
        this.images = images;

        builder = new AlertDialog.Builder(context);

        // Add the images
        addImages();


    }

    public void addImages(){

        upload(images.get(0));

        // Showing the loader
        load();

    }

    public void upload(Bitmap bitmap){

        // The toast
        Toast.makeText(context, "Start uploading", Toast.LENGTH_SHORT).show();

        Log.i("UPLOADING", images.size()+"  "+imageUrls.size());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        //Generate a random id
        String id = UUID.randomUUID().toString();

        storage = FirebaseStorage.getInstance().getReference("Images/"+this.user.getUid()+"/"+id);

        UploadTask task = storage.putBytes(data);

        task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                // Getting the image url
                storage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        // Adding the image url
                        imageUrls.add(uri.toString());

                        if(imageUrls.size() >= images.size()){

                            // Push to firebase
                            push();

                        }else{

                            // Upload the following image
                            upload(images.get(imageUrls.size()));

                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });



            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }

    public void push(){

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        String key = db.getReference("users/"+this.user.getUid()+"/properties").push().getKey();

        DatabaseReference userRef = db.getReference("properties/"+key);

        // Adding the paths to the house object
        this.house.setImages(imageUrls);

        userRef.setValue(this.house)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(context, "Published", Toast.LENGTH_SHORT).show();
                        context.startActivity(new Intent(context, Account.class));

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    public void load(){

        builder.setTitle("Uploading...")
                .setMessage("Please wait while the images are being uploaded... ")
                .show();

    }

}
