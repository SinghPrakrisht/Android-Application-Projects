package com.example.photovault;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.util.Locale;

public final class DocumentUriHelper {

    private static final String COLUMN_ORIGINAL_RELATIVE_PATH = "original_relative_path";

    public static final class Details {
        public String displayName;
        public long sizeBytes = -1;
        public long lastModifiedMs = -1;
    }

    private DocumentUriHelper() {}

    public static boolean deleteDocument(Context context, @Nullable Uri treeUri, Uri documentUri) {
        if (context == null || documentUri == null) return false;

        ContentResolver resolver = context.getContentResolver();
        boolean ok = false;

        try {
            // BEST METHOD: Build a Document URI using the tree and delete directly
            if (treeUri != null) {
                String docId = DocumentsContract.getDocumentId(documentUri);
                Uri deletableUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId);
                ok = DocumentsContract.deleteDocument(resolver, deletableUri);
            }
        } catch (Exception e) {
            // Fallback: Use DocumentFile if the direct contract delete fails
            DocumentFile file = DocumentFile.fromSingleUri(context, documentUri);
            if (file != null && file.exists()) {
                ok = file.delete();
            }
        }

        if (ok) {
            // Refresh MediaStore so the image disappears from other gallery apps immediately
            notifyMediaStore(context, documentUri);
        }
        return ok;
    }

    private static void notifyMediaStore(Context context, Uri uri) {
        try {
            context.getContentResolver().notifyChange(uri, null);
            context.getContentResolver().notifyChange(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null);
        } catch (Exception ignored) {}
    }

    public static Details loadDetails(Context context, Uri uri) {
        Details d = new Details();
        try (Cursor c = context.getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int nameIdx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIdx = c.getColumnIndex(OpenableColumns.SIZE);
                if (nameIdx >= 0) d.displayName = c.getString(nameIdx);
                if (sizeIdx >= 0) d.sizeBytes = c.getLong(sizeIdx);
            }
        } catch (Exception ignored) {}

        DocumentFile df = DocumentFile.fromSingleUri(context, uri);
        if (df != null) {
            if (d.displayName == null) d.displayName = df.getName();
            d.lastModifiedMs = df.lastModified();
        }
        return d;
    }

    public static boolean isTrashedDocument(Context context, Uri documentUri) {
        try (Cursor c = context.getContentResolver().query(documentUri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(COLUMN_ORIGINAL_RELATIVE_PATH);
                return idx >= 0 && !c.isNull(idx);
            }
        } catch (Exception ignored) {}
        return false;
    }
}