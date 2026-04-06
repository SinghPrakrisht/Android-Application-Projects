package com.example.photovault;

import android.net.Uri;

public final class GalleryItem {

    public final Uri uri;
    public final String displayName;
    public final long lastModified;

    public GalleryItem(Uri uri, String displayName, long lastModified) {
        this.uri = uri;
        this.displayName = displayName != null ? displayName : "";
        this.lastModified = lastModified;
    }
}
