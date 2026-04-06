package com.example.photovault;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.photovault.databinding.ActivityGalleryBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GalleryActivity extends AppCompatActivity implements GalleryAdapter.OnImageClickListener {

    public static final String EXTRA_TREE_URI = "extra_tree_uri";

    private ActivityGalleryBinding binding;
    private Uri treeUri;
    private GalleryAdapter adapter;

    public static Intent newIntent(Context context, Uri treeUri) {
        Intent i = new Intent(context, GalleryActivity.class);
        i.putExtra(EXTRA_TREE_URI, treeUri.toString());
        return i;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String extra = getIntent().getStringExtra(EXTRA_TREE_URI);
        if (extra == null) {
            extra = PhotoVaultPrefs.getTreeUri(this);
        }
        if (extra == null) {
            finish();
            return;
        }
        treeUri = Uri.parse(extra);

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        int span = 3;
        GridLayoutManager glm = new GridLayoutManager(this, span);
        binding.recyclerGallery.setLayoutManager(glm);
        adapter = new GalleryAdapter(this);
        binding.recyclerGallery.setAdapter(adapter);

        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.applyFilter(s != null ? s.toString() : "");
                updateCountBadge();
                updateEmptyState();
            }
        });

        loadImages();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadImages();
    }

    private void loadImages() {
        DocumentFile root = DocumentFile.fromTreeUri(this, treeUri);
        List<GalleryItem> list = new ArrayList<>();
        if (root != null) {
            DocumentFile[] files = root.listFiles();
            if (files != null) {
                for (DocumentFile f : files) {
                    if (f.isFile()) {
                        String type = f.getType();
                        if (type != null && type.startsWith("image/")) {
                            Uri u = f.getUri();
                            if (DocumentUriHelper.isTrashedDocument(this, u)) {
                                continue;
                            }
                            list.add(new GalleryItem(u, f.getName(), f.lastModified()));
                        }
                    }
                }
            }
        }
        Collections.sort(list, Comparator.comparingLong((GalleryItem a) -> a.lastModified).reversed());
        adapter.setItems(list);
        String q = binding.searchInput.getText() != null ? binding.searchInput.getText().toString() : "";
        adapter.applyFilter(q);
        updateCountBadge();
        updateEmptyState();
    }

    private void updateCountBadge() {
        int n = adapter.getFilteredCount();
        if (n == 1) {
            binding.imageCountBadge.setText(R.string.image_count_single);
        } else {
            binding.imageCountBadge.setText(getString(R.string.image_count_format, n));
        }
    }

    private void updateEmptyState() {
        boolean empty = adapter.getFilteredCount() == 0;
        binding.emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.recyclerGallery.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onImageClick(GalleryItem item) {
        startActivity(ImageDetailActivity.newIntent(this, item.uri, treeUri));
    }
}
