package com.example.photovault;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class ImageMetadataHelper {

    private ImageMetadataHelper() {
    }

    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        float kb = bytes / 1024f;
        if (kb < 1024) {
            return String.format(Locale.US, "%.1f KB", kb);
        }
        float mb = kb / 1024f;
        if (mb < 1024) {
            return String.format(Locale.US, "%.1f MB", mb);
        }
        return String.format(Locale.US, "%.2f GB", mb / 1024f);
    }

    public static String formatDateTaken(Context context, Uri uri, long fallbackTime) {
        String fromExif = readExifDateTimeOriginal(context, uri);
        if (fromExif != null) {
            return fromExif;
        }
        return formatDisplayDate(fallbackTime);
    }

    public static String formatDisplayDate(long timeMillis) {
        if (timeMillis <= 0) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timeMillis));
    }

    @Nullable
    public static String readExifDateTimeOriginal(Context context, Uri uri) {
        String raw = null;
        try (ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r")) {
            if (pfd != null) {
                ExifInterface exif = new ExifInterface(pfd.getFileDescriptor());
                raw = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);
                if (raw == null || raw.isEmpty()) {
                    raw = exif.getAttribute(ExifInterface.TAG_DATETIME);
                }
            }
        } catch (IOException ignored) {
        }
        if (raw == null || raw.isEmpty()) {
            try (InputStream is = context.getContentResolver().openInputStream(uri)) {
                if (is != null) {
                    ExifInterface exif = new ExifInterface(is);
                    raw = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);
                    if (raw == null || raw.isEmpty()) {
                        raw = exif.getAttribute(ExifInterface.TAG_DATETIME);
                    }
                }
            } catch (IOException ignored) {
            }
        }
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat exifIn = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US);
            Date d = exifIn.parse(raw);
            if (d != null) {
                SimpleDateFormat out = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                return out.format(d);
            }
        } catch (ParseException ignored) {
        }
        return raw;
    }

    @Nullable
    public static String readCameraSummary(Context context, Uri uri) {
        try (ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r")) {
            if (pfd != null) {
                ExifInterface exif = new ExifInterface(pfd.getFileDescriptor());
                String summary = buildCameraSummary(exif);
                if (summary != null) {
                    return summary;
                }
            }
        } catch (IOException ignored) {
        }
        try (InputStream is = context.getContentResolver().openInputStream(uri)) {
            if (is == null) {
                return null;
            }
            ExifInterface exif = new ExifInterface(is);
            return buildCameraSummary(exif);
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    private static String buildCameraSummary(ExifInterface exif) {
        String iso = exif.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY);
        String fNumber = exif.getAttribute(ExifInterface.TAG_F_NUMBER);
        String exposure = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);

        StringBuilder sb = new StringBuilder();
        if (iso != null && !iso.isEmpty()) {
            sb.append("ISO ").append(iso);
        }
        if (fNumber != null && !fNumber.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" · ");
            }
            sb.append("f/").append(trimRational(fNumber));
        }
        if (exposure != null && !exposure.isEmpty()) {
            String expText = formatExposure(exposure);
            if (expText != null) {
                if (sb.length() > 0) {
                    sb.append(" · ");
                }
                sb.append(expText);
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private static String trimRational(String fNumber) {
        int slash = fNumber.indexOf('/');
        if (slash > 0) {
            try {
                double num = Double.parseDouble(fNumber.substring(0, slash));
                double den = Double.parseDouble(fNumber.substring(slash + 1));
                if (den != 0) {
                    return String.format(Locale.US, "%.1f", num / den);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return fNumber;
    }

    @Nullable
    private static String formatExposure(String exposureTime) {
        try {
            if (exposureTime.contains("/")) {
                String[] p = exposureTime.split("/");
                double num = Double.parseDouble(p[0]);
                double den = Double.parseDouble(p[1]);
                if (den == 0) {
                    return null;
                }
                double sec = num / den;
                if (sec >= 1) {
                    return String.format(Locale.US, "%.1fs", sec);
                }
                return String.format(Locale.US, "1/%.0fs", 1.0 / sec);
            }
            double sec = Double.parseDouble(exposureTime);
            if (sec >= 1) {
                return String.format(Locale.US, "%.1fs", sec);
            }
            return String.format(Locale.US, "1/%.0fs", 1.0 / sec);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
