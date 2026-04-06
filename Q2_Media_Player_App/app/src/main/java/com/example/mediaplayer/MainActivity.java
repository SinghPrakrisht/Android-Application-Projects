package com.example.mediaplayerapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // ── Views ──────────────────────────────────────────────────────────────────
    private VideoView videoView;
    private FrameLayout videoContainer;
    private LinearLayout audioCard, videoCard;
    private ImageButton btnPlay, btnPause, btnStop, btnRestart, btnFullscreen;
    private ImageButton audioBtnPlay, audioBtnPause, audioBtnStop, audioBtnRestart;
    private ImageButton btnOpenFile, btnOpenUrl;
    private SeekBar audioSeekBar;
    private TextView tvCurrentTime, tvTotalTime, tvNowPlaying, tvPlayerMode;
    private ProgressBar videoProgress;

    // ── State ──────────────────────────────────────────────────────────────────
    private MediaPlayer mediaPlayer;
    private Uri currentUri;
    private boolean isFullscreen = false;
    private boolean isAudioMode = false;
    private boolean mediaReady = false;

    private final Handler seekHandler = new Handler();
    private final Runnable seekUpdater = new Runnable() {
        @Override public void run() {
            if (isAudioMode && mediaPlayer != null && mediaReady) {
                try {
                    int pos   = mediaPlayer.getCurrentPosition();
                    int total = mediaPlayer.getDuration();
                    if (total > 0) {
                        audioSeekBar.setMax(total);
                        audioSeekBar.setProgress(pos);
                        tvCurrentTime.setText(formatTime(pos));
                        tvTotalTime.setText(formatTime(total));
                    }
                } catch (Exception ignored) {}
            }
            seekHandler.postDelayed(this, 500);
        }
    };

    // ── Launchers ──────────────────────────────────────────────────────────────
    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            currentUri = result.getData().getData();
                            String name = getFileName(currentUri);
                            loadMedia(currentUri, name != null ? name : "File");
                        }
                    });

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        boolean granted = result.containsValue(Boolean.TRUE);
                        if (granted) launchFilePicker();
                        else toast("Storage permission denied");
                    });

    // ── Lifecycle ──────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        setupListeners();
        seekHandler.post(seekUpdater);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        seekHandler.removeCallbacks(seekUpdater);
        releaseAudio();
    }

    @Override
    public void onBackPressed() {
        if (isFullscreen) exitFullscreen();
        else super.onBackPressed();
    }

    // ── View binding ───────────────────────────────────────────────────────────
    private void bindViews() {
        audioCard       = findViewById(R.id.audioCard);
        videoCard       = findViewById(R.id.videoCard);
        videoView       = findViewById(R.id.videoView);
        videoContainer  = findViewById(R.id.videoContainer);
        btnPlay         = findViewById(R.id.btnPlay);
        btnPause        = findViewById(R.id.btnPause);
        btnStop         = findViewById(R.id.btnStop);
        btnRestart      = findViewById(R.id.btnRestart);
        btnFullscreen   = findViewById(R.id.btnFullscreen);
        audioBtnPlay    = findViewById(R.id.audioBtnPlay);
        audioBtnPause   = findViewById(R.id.audioBtnPause);
        audioBtnStop    = findViewById(R.id.audioBtnStop);
        audioBtnRestart = findViewById(R.id.audioBtnRestart);
        btnOpenFile     = findViewById(R.id.btnOpenFile);
        btnOpenUrl      = findViewById(R.id.btnOpenUrl);
        audioSeekBar    = findViewById(R.id.audioSeekBar);
        tvCurrentTime   = findViewById(R.id.tvCurrentTime);
        tvTotalTime     = findViewById(R.id.tvTotalTime);
        tvNowPlaying    = findViewById(R.id.tvNowPlaying);
        tvPlayerMode    = findViewById(R.id.tvPlayerMode);
        videoProgress   = findViewById(R.id.videoProgress);
    }

    // ── Listeners ──────────────────────────────────────────────────────────────
    private void setupListeners() {
        btnOpenFile.setOnClickListener(v -> requestPermissionsAndOpen());
        btnOpenUrl.setOnClickListener(v -> showUrlDialog());

        // Video controls
        btnPlay.setOnClickListener(v -> doPlay());
        btnPause.setOnClickListener(v -> doPause());
        btnStop.setOnClickListener(v -> doStop());
        btnRestart.setOnClickListener(v -> doRestart());
        btnFullscreen.setOnClickListener(v -> toggleFullscreen());

        // Audio controls
        audioBtnPlay.setOnClickListener(v -> doPlay());
        audioBtnPause.setOnClickListener(v -> doPause());
        audioBtnStop.setOnClickListener(v -> doStop());
        audioBtnRestart.setOnClickListener(v -> doRestart());

        audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null && mediaReady) mediaPlayer.seekTo(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            toast("Video error – check the URL or file format");
            videoProgress.setVisibility(View.GONE);
            return true;
        });
        videoView.setOnCompletionListener(mp -> toast("Playback complete"));
    }

    // ── Open File ──────────────────────────────────────────────────────────────
    private void requestPermissionsAndOpen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean audioOk = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)  == PackageManager.PERMISSION_GRANTED;
            boolean videoOk = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;
            if (audioOk && videoOk) launchFilePicker();
            else permissionLauncher.launch(new String[]{Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_VIDEO});
        } else {
            boolean ok = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            if (ok) launchFilePicker();
            else permissionLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
        }
    }

    private void launchFilePicker() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"audio/*", "video/*"});
        filePickerLauncher.launch(i);
    }

    // ── Open URL ───────────────────────────────────────────────────────────────
    private void showUrlDialog() {
        EditText input = new EditText(this);
        input.setHint("https://...");
        input.setPadding(48, 24, 48, 24);
        input.setText("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");

        new AlertDialog.Builder(this)
                .setTitle("Enter Media URL")
                .setView(input)
                .setPositiveButton("Load", (d, w) -> {
                    String url = input.getText().toString().trim();
                    if (!url.isEmpty()) {
                        currentUri = Uri.parse(url);
                        String name = url.substring(url.lastIndexOf('/') + 1);
                        loadMedia(currentUri, name);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Load media: detect audio vs video ─────────────────────────────────────
    private void loadMedia(Uri uri, String displayName) {
        // Detect type from MIME or extension
        String mime = getContentResolver().getType(uri);
        String path = uri.toString().toLowerCase();
        boolean isAudio;
        if (mime != null) {
            isAudio = mime.startsWith("audio");
        } else {
            isAudio = path.endsWith(".mp3") || path.endsWith(".wav")
                    || path.endsWith(".ogg") || path.endsWith(".aac")
                    || path.endsWith(".flac") || path.endsWith(".m4a");
        }

        if (isAudio) {
            isAudioMode = true;
            audioCard.setVisibility(View.VISIBLE);
            videoCard.setVisibility(View.GONE);
            tvPlayerMode.setText("AUDIO");
            tvNowPlaying.setText(displayName);
            loadAudio(uri);
        } else {
            isAudioMode = false;
            videoCard.setVisibility(View.VISIBLE);
            audioCard.setVisibility(View.GONE);
            tvPlayerMode.setText("VIDEO");
            loadVideo(uri);
        }
    }

    // ── Audio ──────────────────────────────────────────────────────────────────
    private void loadAudio(Uri uri) {
        mediaReady = false;
        releaseAudio();
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                mediaReady = true;
                audioSeekBar.setMax(mp.getDuration());
                tvTotalTime.setText(formatTime(mp.getDuration()));
                mp.start();
                toast("Playing audio");
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                toast("Audio complete");
                audioSeekBar.setProgress(0);
                tvCurrentTime.setText("0:00");
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                toast("Audio error (code " + what + ")");
                mediaReady = false;
                return true;
            });
        } catch (IOException e) {
            toast("Cannot load: " + e.getMessage());
        }
    }

    private void releaseAudio() {
        if (mediaPlayer != null) {
            try { mediaPlayer.stop(); } catch (Exception ignored) {}
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaReady = false;
    }

    // ── Video ──────────────────────────────────────────────────────────────────
    private void loadVideo(Uri uri) {
        videoProgress.setVisibility(View.VISIBLE);
        videoView.setVideoURI(uri);
        videoView.setOnPreparedListener(mp -> {
            videoProgress.setVisibility(View.GONE);
            mp.start();
            toast("Playing video");
        });
    }

    // ── Playback controls ──────────────────────────────────────────────────────
    private void doPlay() {
        if (currentUri == null) { toast("Open a file or URL first"); return; }
        if (isAudioMode) {
            if (mediaPlayer == null) {
                loadAudio(currentUri); // restart after stop
                return;
            }
            if (mediaReady && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                toast("Playing");
            } else if (!mediaReady) {
                toast("Still loading…");
            }
        } else {
            if (!videoView.isPlaying()) {
                videoView.start();
                toast("Playing");
            }
        }
    }

    private void doPause() {
        if (currentUri == null) { toast("Nothing is loaded"); return; }
        if (isAudioMode) {
            if (mediaPlayer != null && mediaReady && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                toast("Paused");
            }
        } else {
            if (videoView.isPlaying()) {
                videoView.pause();
                toast("Paused");
            }
        }
    }

    private void doStop() {
        if (currentUri == null) { toast("Nothing is loaded"); return; }
        if (isAudioMode) {
            releaseAudio();   // fully stop & release; doPlay will reload
            audioSeekBar.setProgress(0);
            tvCurrentTime.setText("0:00");
            toast("Stopped");
        } else {
            videoView.stopPlayback();
            toast("Stopped");
        }
    }

    private void doRestart() {
        if (currentUri == null) { toast("Open a file or URL first"); return; }
        if (isAudioMode) {
            if (mediaPlayer != null && mediaReady) {
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
                toast("Restarted");
            } else {
                loadAudio(currentUri); // reload if stopped
            }
        } else {
            videoView.seekTo(0);
            videoView.start();
            toast("Restarted");
        }
    }

    // ── Fullscreen ─────────────────────────────────────────────────────────────
    private void toggleFullscreen() {
        if (isFullscreen) exitFullscreen();
        else enterFullscreen();
    }

    private void enterFullscreen() {
        isFullscreen = true;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
        // Resize video container using LinearLayout.LayoutParams (it lives inside LinearLayout)
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        videoContainer.setLayoutParams(lp);
        btnFullscreen.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
    }

    private void exitFullscreen() {
        isFullscreen = false;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        // Restore to 220dp
        int height220dp = (int)(220 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                height220dp
        );
        videoContainer.setLayoutParams(lp);
        btnFullscreen.setImageResource(android.R.drawable.ic_menu_zoom);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private String formatTime(int ms) {
        int s = ms / 1000;
        return String.format("%d:%02d", s / 60, s % 60);
    }

    private String getFileName(Uri uri) {
        try {
            String path = uri.getLastPathSegment();
            return (path != null) ? path : "media";
        } catch (Exception e) { return "media"; }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}