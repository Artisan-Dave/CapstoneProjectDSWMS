package com.example.capstone_dswms;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class uploadImage extends AppCompatActivity {

    private ImageView ivChoosePic;
    private Button btnUpload;

    private Uri imageUri;

    LoadingDialog DialogBar;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://caps-dswms-default-rtdb.firebaseio.com/");
    StorageReference storageReference = FirebaseStorage.getInstance().getReference("images");

    private static final int REQUEST_IMAGE_PERMISSION = 1001;

    private boolean isImagePermissionGranted = false;

    // ActivityResultLauncher to handle image selection
    private ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        ivChoosePic.setImageURI(imageUri);
                    }
                }
            }
    );

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);


        btnUpload = findViewById(R.id.btnupload);
        ivChoosePic = findViewById(R.id.ivchoosepic);

        DialogBar = new LoadingDialog(this);


        ivChoosePic.setOnClickListener(v -> {
                openImagePicker();
        });

        btnUpload.setOnClickListener(v -> uploadImageToFirebase());

    }

    // Instead of accessing the imageUri directly, use MediaStore to get image information
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        imagePickerLauncher.launch(intent);
    }

    // Instead of accessing the imageUri directly, use MediaStore to get the image data
//    private void uploadImageToFirebase() {
//        showLoadingDialog();
//        if (imageUri == null) {
//            Toast.makeText(this, "Please choose an image to upload", Toast.LENGTH_SHORT).show();
//            dismissLoadingDialog();
//            return;
//        }
//
//        // Check if a caption is provided
//        String caption = etCaption.getText().toString().trim();
//        if (caption.isEmpty()) {
//            Toast.makeText(this, "Please provide a caption", Toast.LENGTH_SHORT).show();
//            dismissLoadingDialog();
//            return;
//        }
//
//        // Use the MediaStore API to get image data
//        String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME};
//        Cursor cursor = getContentResolver().query(imageUri, projection, null, null, null);
//
//        if (cursor != null && cursor.moveToFirst()) {
//            String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
//            // Get the image data and process it as needed
//            // ...
//
//            cursor.close();
//        } else {
//            // Failed to retrieve image data from MediaStore
//            Toast.makeText(this, "Failed to retrieve image data", Toast.LENGTH_SHORT).show();
//        }
//
//        // Get a reference to store the image in Firebase Storage
//        StorageReference imageRef = storageReference.child(System.currentTimeMillis() + ".jpg");
//
//        // Upload the file to Firebase Storage
//        UploadTask uploadTask = imageRef.putFile(imageUri);
//
//        // Optional: Add a listener to track the upload progress
//        uploadTask.addOnProgressListener(snapshot -> {
//            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
//            // Update progress if needed
//        });
//
//        // Optional: Handle success or failure of the upload
//        uploadTask.continueWithTask(task -> {
//            if (!task.isSuccessful()) {
//                throw task.getException();
//            }
//            // Continue with the task to get the download URL
//            return imageRef.getDownloadUrl();
//        }).addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                // Image upload is successful, now save the download URL and caption in the Realtime Database
//                String downloadUrl = task.getResult().toString();
//
//                // Save download URL and caption in the Realtime Database
//                databaseReference.child("location").setValue(downloadUrl);
//                databaseReference.child("caption").setValue(caption);
//
//                Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
//                // Clear the fields and reset the flag
//                etCaption.setText("");
//                isImagePermissionGranted = false;
//                dismissLoadingDialog();
//                recreate();
//            } else {
//                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private void uploadImageToFirebase() {
        DialogBar.ShowDialog();
        if (imageUri == null) {
            Toast.makeText(this, "Please choose an image to upload", Toast.LENGTH_SHORT).show();
            DialogBar.HideDialog();
            return;
        }

//        // Check if a caption is provided
//        String caption = etCaption.getText().toString().trim();
//        if (caption.isEmpty()) {
//            Toast.makeText(this, "Please provide a caption", Toast.LENGTH_SHORT).show();
//            dismissLoadingDialog();
//            return;
//        }

        // Get a reference to store the image in Firebase Storage
        StorageReference imageRef = storageReference.child(System.currentTimeMillis() + ".jpg");

        // Upload the file to Firebase Storage
        UploadTask uploadTask = imageRef.putFile(imageUri);

        // Optional: Add a listener to track the upload progress
        uploadTask.addOnProgressListener(snapshot -> {
            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
            // Update progress if needed
        });

        // Optional: Handle success or failure of the upload
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            // Continue with the task to get the download URL
            return imageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Image upload is successful, now save the download URL and caption in the Realtime Database
                String downloadUrl = task.getResult().toString();

//                Spinner spinner = findViewById(R.id.spinner);
//
//                // Obtain the selected item from the spinner
//                String selectedItem = spinner.getSelectedItem().toString();
//                DatabaseReference itemsRef = databaseReference.child("trashbin1");
//
//                if(selectedItem.equals("Select Floor")){
//                    Toast.makeText(uploadImage.this, "Please Select Floor", Toast.LENGTH_SHORT).show();
//                    recreate();
//                }

                Spinner spinner = findViewById(R.id.spinner);

                // Obtain the selected item position from the spinner
                int selectedPosition = spinner.getSelectedItemPosition();
                DatabaseReference itemsRef = databaseReference.child("trashbin1");

                if (selectedPosition == 0) {
                    Toast.makeText(uploadImage.this, "Please Select Floor", Toast.LENGTH_SHORT).show();
                    recreate();
                } else {
                    // Obtain the selected item from the spinner
                    String selectedItem = spinner.getSelectedItem().toString();
                    // Do something with the selected item
                    // Set the values for "caption" and "location"
                    itemsRef.child("location").setValue(downloadUrl);
                    itemsRef.child("caption").setValue(selectedItem);

                    // Rest of the code to clear fields, show messages, etc.
                    Toast.makeText(uploadImage.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                    spinner.setSelection(0);
                    isImagePermissionGranted = false;
                    DialogBar.HideDialog();
                    recreate();
                }
            } else {
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
