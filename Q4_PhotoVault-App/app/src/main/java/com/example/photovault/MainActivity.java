package com.example.photovault;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.documentfile.provider.DocumentFile;

import com.example.photovault.databinding.ActivityMainBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CAMERA_PERMISSION = 1001;
    private ActivityMainBinding binding;
    private Uri pendingPhotoUri;

    private final ActivityResultLauncher<Uri> openTreeLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocumentTree(),
            this::onFolderPicked);

    private final ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != RESULT_OK && pendingPhotoUri != null) {
                    DocumentFile f = DocumentFile.fromSingleUri(this, pendingPhotoUri);
                    if (f != null && f.exists()) {
                        try {
                            f.delete();
                        } catch (Exception ignored) {}
                    }
                } else if (result.getResultCode() == RESULT_OK) {
                    Toast.makeText(this, R.string.photo_saved, Toast.LENGTH_SHORT).show();
                }
                pendingPhotoUri = null;
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbar);
        binding.btnBrowseFolder.setOnClickListener(v -> openTreeLauncher.launch(null));
        binding.btnTakePhoto.setOnClickListener(v -> tryTakePhoto());
    }

    private void onFolderPicked(Uri treeUri) {
        if (treeUri == null) return;

        // FIX: Ensure both READ and WRITE permissions are taken and persisted
        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        try {
            getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        PhotoVaultPrefs.saveTreeUri(this, treeUri.toString());
        startActivity(GalleryActivity.newIntent(this, treeUri));
    }

    private void tryTakePhoto() {
        String tree = PhotoVaultPrefs.getTreeUri(this);
        if (tree == null || tree.isEmpty()) {
            Toast.makeText(this, R.string.choose_folder_first, Toast.LENGTH_LONG).show();
            return;
        }

        Uri treeUri = Uri.parse(tree);
        DocumentFile root = DocumentFile.fromTreeUri(this, treeUri);

        if (root == null || !root.canWrite()) {
            Toast.makeText(this, R.string.choose_folder_first, Toast.LENGTH_LONG).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQ_CAMERA_PERMISSION);
            return;
        }
        launchCamera(root);
    }

    private void launchCamera(@NonNull DocumentFile root) {
        String name = "PHOTO_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".jpg";
        DocumentFile photo = root.createFile("image/jpeg", name);
        if (photo == null) return;

        pendingPhotoUri = photo.getUri();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pendingPhotoUri);

        // Grant permissions for the specific photo URI to the camera app
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setClipData(ClipData.newUri(getContentResolver(), "Photo", pendingPhotoUri));

        if (intent.resolveActivity(getPackageManager()) != null) {
            takePictureLauncher.launch(intent);
        }
    }
}