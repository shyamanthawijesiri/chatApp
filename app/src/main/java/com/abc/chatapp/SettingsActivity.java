package com.abc.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {
    private DatabaseReference mUserDatabase;
    private StorageReference mImageStorage;
    private FirebaseUser mCurrentUser;

    private CircleImageView mImage;
    private TextView mDisplayName;
    private TextView mStatus;
    private Button mChangeStatus;
    private Button mChangeImage;

    private Toolbar mToolbar;

    private ProgressDialog mProgressDialog;
    private Bitmap thumb_img;

    private static final int GELLARY_PICK = 1;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mImage = (CircleImageView)findViewById(R.id.settings_image);
        mDisplayName = (TextView)findViewById(R.id.settings_displayName);
        mStatus = (TextView)findViewById(R.id.settings_status);
        mChangeImage = (Button)findViewById(R.id.setting_image_btn);
        mChangeStatus = (Button)findViewById(R.id.settings_status_btn);
        mImageStorage = FirebaseStorage.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        String current_uid = mCurrentUser.getUid();

        mToolbar = (Toolbar)findViewById(R.id.setting_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               // Toast.makeText(SettingsActivity.this, dataSnapshot.toString(), Toast.LENGTH_LONG).show();
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mDisplayName.setText(name);
                mStatus.setText(status);
                if (!image.equals("default")){

                Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.avatar).into(mImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {

                     Picasso.get().load(image).placeholder(R.drawable.avatar).into(mImage);
                    }
                });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status_val = mStatus.getText().toString();
                Intent status_intent = new Intent(SettingsActivity.this, StatusActivity.class);
                status_intent.putExtra("status_val", status_val);
                startActivity(status_intent);
            }
        });

        mChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                CropImage.activity()
//                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .start(SettingsActivity.this);
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"),GELLARY_PICK);
            }
        });





    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       // Toast.makeText(SettingsActivity.this, requestCode +" "+ requestCode + " "+RESULT_OK, Toast.LENGTH_LONG).show();
        if(requestCode == GELLARY_PICK && resultCode == RESULT_OK){
            Uri imageuri = data.getData();
            CropImage.activity(imageuri)
                    .setAspectRatio(1,1)
                    .start(this);

          }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle("Uploading Image");
                mProgressDialog.setMessage("Please Wait Until Uploading is Finished");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();
                File thumb_image = new File(resultUri.getPath());

                try {
                    thumb_img = new Compressor(this)
                                .setMaxWidth(200)
                                .setMaxHeight(200)
                                .setQuality(75)
                                .compressToBitmap(thumb_image);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_img.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();

                String user_id = mCurrentUser.getUid();
                StorageReference filePath = mImageStorage.child("profile_images").child(user_id+".jpg");
                final StorageReference thumb_file_path = mImageStorage.child("profile_images").child("thumbs").child(user_id+ ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                                firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String imageurl = uri.toString();
                                        UploadTask uploadTask = thumb_file_path.putBytes(thumb_byte);

                                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot tasktThumbSnapshot) {
                                                final Task<Uri> firebaseThumbUri = tasktThumbSnapshot.getStorage().getDownloadUrl();
                                                firebaseThumbUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        final String thumburl = uri.toString();
                                                        Map image_hashmap = new HashMap<>();
                                                        image_hashmap.put("image", imageurl);
                                                        image_hashmap.put("thumb_image", thumburl);
                                                            mUserDatabase.updateChildren(image_hashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        mProgressDialog.dismiss();
                                                                        Toast.makeText(SettingsActivity.this,"url Upload", Toast.LENGTH_LONG).show();
                                                                    }
                                                                }
                                                            });

                                                    }
                                                });

                                            }
                                        });

                                    }
                                });

                            }
                        });
//                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                        if(task.isSuccessful()){
//                            String imagerl = task.getResult().toString();
//                            Toast.makeText(SettingsActivity.this,imagerl, Toast.LENGTH_LONG).show();
//                            mUserDatabase.child("image").setValue(imagerl).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    if(task.isSuccessful()){
//                                        mProgressDialog.dismiss();
//                                        Toast.makeText(SettingsActivity.this,"url Upload", Toast.LENGTH_LONG).show();
//                                    }
//                                }
//                            });
//                        }else{
//                            Toast.makeText(SettingsActivity.this,"uploaded failed", Toast.LENGTH_LONG).show();
//                            mProgressDialog.dismiss();
//                        }
//                    }
//                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
    }
