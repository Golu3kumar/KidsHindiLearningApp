package com.selfhelpindia.kidsquizapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.selfhelpindia.kidsquizapp.databinding.FragmentProfileBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;


public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private static final int STORAGE_PERMISSION_CODE = 1;
    private static final int OPEN_DOCUMENT_CODE = 0;
    SharedPreferences sf2;
    SharedPreferences.Editor editor2;
    public static final String CURRENT_DATE_AND_COUNTER_SF = "reward_helper1";
    FirebaseFirestore database;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    FragmentProfileBinding profileBinding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        database = FirebaseFirestore.getInstance();
        profileBinding = FragmentProfileBinding.inflate(inflater, container, false);
        profileBinding.profileName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        //FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
        if (FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl() != null)
            Glide.with(getContext()).load(FirebaseAuth.getInstance()
                    .getCurrentUser().getPhotoUrl()).into(profileBinding.profileImage);
        profileBinding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    onSelectImageClick();
                } else {
                    requestStoragePermission();
                }
            }
        });

        profileBinding.updateProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = profileBinding.updateName.getText().toString().trim();
                final String newPass = profileBinding.updatePass.getText().toString().trim();
                if (name.isEmpty() && (newPass.isEmpty()||newPass.length()<=6) ){
                    Toast.makeText(getContext(), "Name Required! OR Password length should be 6", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!name.isEmpty()){
                    UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name).build();
                    FirebaseAuth.getInstance().getCurrentUser().updateProfile(userProfileChangeRequest);
                    database.collection("users")
                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .update("name",name).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                                Log.d(TAG, "onComplete: Name Updated!");
                            else
                                Toast.makeText(getContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                if (!newPass.isEmpty() && newPass.length()>=6){
                    FirebaseAuth.getInstance().getCurrentUser().updatePassword(newPass)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                database.collection("users")
                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .update("pass",newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                            Log.d(TAG, "onComplete: password Updated!");
                                        else
                                            Toast.makeText(getContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                }
                Toast.makeText(getContext(), "Profile Updated!", Toast.LENGTH_SHORT).show();

            }
        });

        profileBinding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                sf2 = getActivity().getSharedPreferences(CURRENT_DATE_AND_COUNTER_SF,Context.MODE_PRIVATE);
                editor2 = sf2.edit();
                editor2.clear();
                editor2.apply();
                Intent intent = new Intent(getContext(),LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getContext().startActivity(intent);
            }
        });
        return profileBinding.getRoot();
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed to choose your profile.")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                            onSelectImageClick();
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            onSelectImageClick();
        }
    }

    private void onSelectImageClick() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, OPEN_DOCUMENT_CODE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == OPEN_DOCUMENT_CODE && resultCode == RESULT_OK) {
            if (resultData != null) {
                // this is the image selected by the user
                Uri imageUri = resultData.getData();
                uploadImageToStorage(imageUri);
                profileBinding.profileImage.setImageURI(imageUri);
            }
        }
    }

    private void uploadImageToStorage(final Uri imageUri) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                StorageReference profileImageRef =
                        FirebaseStorage.getInstance().getReference("profilePics/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + ".jpg");
                if (imageUri != null) {
                    Bitmap bmp = null;
                    try {
                        bmp = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    assert bmp != null;
                    bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                    byte[] data = baos.toByteArray();
                    //uploading the image
                    UploadTask uploadTask2 = profileImageRef.putBytes(data);
                    uploadTask2.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            taskSnapshot.getMetadata().getReference().getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                                    .setPhotoUri(uri).build();
                                            FirebaseAuth.getInstance().getCurrentUser().updateProfile(userProfileChangeRequest);
                                            database.collection("users")
                                                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .update("profileUrl", uri.toString());
                                            //Log.d(TAG, "onComplete: " + FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString());
                                        }
                                    });
                        }
                    });
                }

            }
        };
        Thread thread = new Thread(runnable);
        thread.start();

    }

}