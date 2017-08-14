package de.tum.repairchain;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReportActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private MapView mapView;
    private GoogleMap map;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location currentLocation;
    private TextView tvTime;

    @OnClick({R.id.btn_upload})
    public void clickUploadButton(Button btn) {
        Intent intent = new Intent(getApplicationContext(), UploadImage.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setMapPosition();
                    Toast.makeText(getApplicationContext(), "Permission successfully set", Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(getApplicationContext(), "well fuck", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setMapPosition() {
        Log.i("setMapPosition", "has been called");
        if (map != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
            } else {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
                currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                map.setMyLocationEnabled(true);

                // Set camera to current location if it could be found
                if (currentLocation != null) {
                    LatLng positionLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(positionLatLng, 13));

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(positionLatLng)
                            .zoom(17)
                            .bearing(90)
                            .tilt(40)
                            .build();

                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        ButterKnife.bind(this);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        tvTime = (TextView) findViewById(R.id.tv_time);
        mapView = (MapView) findViewById(R.id.mv_current_location);
        mapView.onCreate(savedInstanceState);
        // needed to initialize GoogleMap object
        mapView.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        } else {
            setMapPosition();
        }

        tvTime.setText(dateFormat.format(new Date()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null)
            mapView.onResume();
    }

    @Override
    protected void onPause() {
        if (mapView != null)
            mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mapView != null) {
            try{
                mapView.onDestroy();
            } catch (NullPointerException nPE) {
                Log.e("ReportActivity", "Error when attempting MapView.onDestroy(), ignoring exception", nPE);
            }
        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null)
            mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null)
            mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        setMapPosition();
    }


}
