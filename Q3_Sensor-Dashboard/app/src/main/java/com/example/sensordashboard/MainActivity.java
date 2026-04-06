package com.example.sensordashboard;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // Sensor Manager
    private SensorManager sensorManager;

    // Sensors
    private Sensor accelerometer;
    private Sensor lightSensor;
    private Sensor proximitySensor;

    // Accelerometer TextViews
    private TextView tvAccelX, tvAccelY, tvAccelZ;
    private TextView tvAccelStatus;
    private View accelIndicator;

    // Light TextViews
    private TextView tvLightValue;
    private TextView tvLightStatus;
    private View lightIndicator;

    // Proximity TextViews
    private TextView tvProximityValue;
    private TextView tvProximityStatus;
    private View proximityIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        sensorManager   = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer   = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lightSensor     = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        checkSensorAvailability();
    }

    private void initViews() {
        tvAccelX           = findViewById(R.id.tv_accel_x);
        tvAccelY           = findViewById(R.id.tv_accel_y);
        tvAccelZ           = findViewById(R.id.tv_accel_z);
        tvAccelStatus      = findViewById(R.id.tv_accel_status);
        accelIndicator     = findViewById(R.id.accel_indicator);

        tvLightValue       = findViewById(R.id.tv_light_value);
        tvLightStatus      = findViewById(R.id.tv_light_status);
        lightIndicator     = findViewById(R.id.light_indicator);

        tvProximityValue   = findViewById(R.id.tv_proximity_value);
        tvProximityStatus  = findViewById(R.id.tv_proximity_status);
        proximityIndicator = findViewById(R.id.proximity_indicator);
    }

    private void checkSensorAvailability() {
        if (accelerometer == null) {
            tvAccelStatus.setText("Not Available");
            tvAccelX.setText("N/A");
            tvAccelY.setText("N/A");
            tvAccelZ.setText("N/A");
            accelIndicator.setBackgroundResource(R.drawable.indicator_inactive);
        }
        if (lightSensor == null) {
            tvLightStatus.setText("Not Available");
            tvLightValue.setText("N/A");
            lightIndicator.setBackgroundResource(R.drawable.indicator_inactive);
        }
        if (proximitySensor == null) {
            tvProximityStatus.setText("Not Available");
            tvProximityValue.setText("N/A");
            proximityIndicator.setBackgroundResource(R.drawable.indicator_inactive);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null)
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        if (lightSensor != null)
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_UI);
        if (proximitySensor != null)
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                handleAccelerometer(event);
                break;
            case Sensor.TYPE_LIGHT:
                handleLight(event);
                break;
            case Sensor.TYPE_PROXIMITY:
                handleProximity(event);
                break;
        }
    }

    private void handleAccelerometer(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        tvAccelX.setText(String.format("%.3f m/s²", x));
        tvAccelY.setText(String.format("%.3f m/s²", y));
        tvAccelZ.setText(String.format("%.3f m/s²", z));
        accelIndicator.setBackgroundResource(R.drawable.indicator_active);

        double magnitude = Math.sqrt(x * x + y * y + z * z);
        if (magnitude > 12) {
            tvAccelStatus.setText("Moving Fast");
        } else if (magnitude > 10.5) {
            tvAccelStatus.setText("Moving");
        } else {
            tvAccelStatus.setText("Still");
        }
    }

    private void handleLight(SensorEvent event) {
        float lux = event.values[0];
        tvLightValue.setText(String.format("%.1f lx", lux));
        lightIndicator.setBackgroundResource(R.drawable.indicator_active);

        if (lux < 10) {
            tvLightStatus.setText("Dark");
        } else if (lux < 200) {
            tvLightStatus.setText("Dim");
        } else if (lux < 1000) {
            tvLightStatus.setText("Indoor Light");
        } else if (lux < 10000) {
            tvLightStatus.setText("Bright");
        } else {
            tvLightStatus.setText("Sunlight");
        }
    }

    private void handleProximity(SensorEvent event) {
        float distance  = event.values[0];
        float maxRange  = proximitySensor.getMaximumRange();
        tvProximityValue.setText(String.format("%.1f cm", distance));
        proximityIndicator.setBackgroundResource(R.drawable.indicator_active);
        tvProximityStatus.setText(distance < maxRange ? "Object Nearby" : "Clear");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}