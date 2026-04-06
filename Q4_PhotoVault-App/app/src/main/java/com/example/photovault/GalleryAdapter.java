package com.example.photovault;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.photovault.databinding.ItemGalleryImageBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.VH> {

    public interface OnImageClickListener {
        void onImageClick(GalleryItem item);
    }

    private final List<GalleryItem> items = new ArrayList<>();
    private final List<GalleryItem> filtered = new ArrayList<>();
    private final OnImageClickListener listener;

    public GalleryAdapter(OnImageClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<GalleryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        applyFilter("");
    }

    public void applyFilter(String query) {
        filtered.clear();
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        for (GalleryItem item : items) {
            if (q.isEmpty() || item.displayName.toLowerCase(Locale.ROOT).contains(q)) {
                filtered.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public int getFilteredCount() {
        return filtered.size();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGalleryImageBinding binding = ItemGalleryImageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        GalleryItem item = filtered.get(position);
        holder.bind(item.uri, listener, item);
    }

    @Override
    public int getItemCount() {
        return filtered.size();
    }

    static final class VH extends RecyclerView.ViewHolder {

        private final ItemGalleryImageBinding binding;

        VH(ItemGalleryImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Uri uri, OnImageClickListener listener, GalleryItem item) {
            Glide.with(binding.thumbnail.getContext())
                    .load(uri)
                    .centerCrop()
                    .placeholder(R.drawable.shape_thumb_rounded)
                    .into(binding.thumbnail);
            binding.getRoot().setOnClickListener(v -> listener.onImageClick(item));
        }
    }
}
