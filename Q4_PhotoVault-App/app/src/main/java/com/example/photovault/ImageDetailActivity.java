package com.example.photovault;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.documentfile.provider.DocumentFile;

import com.bumptech.glide.Glide;
import com.example.photovault.databinding.ActivityImageDetailBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ImageDetailActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URI = "extra_image_uri";
    public static final String EXTRA_TREE_URI = "extra_tree_uri";

    private ActivityImageDetailBinding binding;
    private Uri imageUri;
    @Nullable
    private Uri treeUri;

    public static Intent newIntent(Context context, Uri imageUri, Uri treeUri) {
        Intent i = new Intent(context, ImageDetailActivity.class);
        i.setData(imageUri);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        i.putExtra(EXTRA_IMAGE_URI, imageUri.toString());
        i.putExtra(EXTRA_TREE_URI, treeUri.toString());
        return i;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityImageDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String uriStr = getIntent().getStringExtra(EXTRA_IMAGE_URI);
        if (uriStr == null && getIntent().getData() != null) {
            uriStr = getIntent().getData().toString();
        }
        if (uriStr == null) {
            finish();
            return;
        }
        imageUri = Uri.parse(uriStr);
        String treeStr = getIntent().getStringExtra(EXTRA_TREE_URI);
        if (treeStr == null) {
            treeStr = PhotoVaultPrefs.getTreeUri(this);
        }
        treeUri = treeStr != null ? Uri.parse(treeStr) : null;

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        Glide.with(this)
                .load(imageUri)
                .centerCrop()
                .into(binding.previewImage);

        DocumentUriHelper.Details details = DocumentUriHelper.loadDetails(this, imageUri);
        String name = details.displayName;
        binding.textFileName.setText(!TextUtils.isEmpty(name) ? name : getString(R.string.unknown));

        String pathText = buildPathDisplay(treeStr, name);
        binding.textFilePath.setText(pathText);

        if (details.sizeBytes >= 0) {
            binding.textFileSize.setText(ImageMetadataHelper.formatFileSize(details.sizeBytes));
        } else {
            binding.textFileSize.setText(getString(R.string.unknown));
        }

        long modified = details.lastModifiedMs;
        String dateTaken = ImageMetadataHelper.formatDateTaken(this, imageUri, modified);
        if (TextUtils.isEmpty(dateTaken)) {
            dateTaken = getString(R.string.unknown);
        }
        binding.textDateTaken.setText(dateTaken);

        String cameraSummary = ImageMetadataHelper.readCameraSummary(this, imageUri);
        if (TextUtils.isEmpty(cameraSummary)) {
            binding.rowCameraMeta.setVisibility(View.GONE);
        } else {
            binding.rowCameraMeta.setVisibility(View.VISIBLE);
            binding.textCameraMeta.setText(cameraSummary);
        }

        binding.btnDelete.setOnClickListener(v -> showDeleteConfirm());
    }

    private String buildPathDisplay(@Nullable String treeStr, @Nullable String fileName) {
        StringBuilder sb = new StringBuilder();
        if (treeStr != null) {
            DocumentFile root = DocumentFile.fromTreeUri(this, Uri.parse(treeStr));
            String folderLabel = root != null ? root.getName() : null;
            if (!TextUtils.isEmpty(folderLabel)) {
                sb.append(folderLabel);
                if (!TextUtils.isEmpty(fileName)) {
                    sb.append(" / ").append(fileName);
                }
                sb.append("\n\n");
            }
        }
        sb.append(imageUri.toString());
        return sb.toString();
    }

    private void showDeleteConfirm() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_dialog_title)
                .setMessage(R.string.delete_dialog_message)
                .setNegativeButton(R.string.cancel, (d, w) -> d.dismiss())
                .setPositiveButton(R.string.delete, (d, w) -> deleteImage())
                .show();
    }

    private void deleteImage() {
        Glide.with(this).clear(binding.previewImage);
        boolean ok = DocumentUriHelper.deleteDocument(this, treeUri, imageUri);
        if (!ok) {
            Toast.makeText(this, R.string.delete_failed, Toast.LENGTH_LONG).show();
            return;
        }
        finish();
    }
}
