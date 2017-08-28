package de.tum.repairchain;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import org.web3j.abi.datatypes.Utf8String;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.tum.repairchain.contracts.Report_sol_Repairchain;

import static de.tum.repairchain.Constants.*;
import static de.tum.repairchain.Helpers.*;

public class ReportActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Context context;
    private GoogleMap map;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location currentLocation;
    private String imageURL = "";
    private String time;

    @BindView(R.id.txt_time)
    TextView tvTime;
    @BindView(R.id.mv_current_location)
    MapView mapView;
    @BindView(R.id.txt_image)
    TextView tvImage;
    @BindView(R.id.et_description)
    EditText descriptionField;

    @OnClick({R.id.btn_upload})
    public void clickUploadButton(Button btn) {
        Intent intent = new Intent(ReportActivity.this, UploadImage.class);
        startActivityForResult(intent, JUST_SOME_CODE);
    }

    @OnClick({R.id.btn_submit_report})
    public void submitReport(Button btn) {
        final double latitude = currentLocation.getLatitude();
        final double longitude = currentLocation.getLongitude();
        final String description = descriptionField.getText().toString();

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Submitting report...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Report_sol_Repairchain repairchain = Web3jManager.getInstance().getRepairchain();

                try {
                    final TransactionReceipt transactionReceipt = repairchain.addReportToCity(
                            new Utf8String(CITY),
                            new Utf8String(imageURL),
                            new Utf8String(String.valueOf(longitude)),
                            new Utf8String(String.valueOf(latitude)),
                            new Utf8String(description)).get();
                    if (transactionReceipt != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Report submitted successfully, txHash = " + transactionReceipt.getTransactionHash(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Error while submitting report.", Toast.LENGTH_LONG).show();
                        }
                    });
                    e.printStackTrace();
                } finally {
                    progressDialog.dismiss();
                }
            }
        }).start();
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
        if (map != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, LOCATION_PERMISSIONS, 1);
            } else {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
                currentLocation = getLastKnownLocation(context, ReportActivity.this, locationManager);
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

        setTitle("Create Report");

        ButterKnife.bind(this);

        context = getApplicationContext();
        Log.i("ReportActivity", "activity has been opened");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        mapView.onCreate(savedInstanceState);
        // needed to initialize GoogleMap object
        mapView.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, LOCATION_PERMISSIONS, 1);
        } else {
            setMapPosition();
        }

        time = dateFormat.format(new Date());
        tvTime.setText(time);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("ReportActivity", "onActivityResult opened");
        if (resultCode == IMAGE_ADDED) {
            imageURL = data.getStringExtra(IPFS_UPLOAD_DONE);
            Log.i("ReportActivity", "Added image: " + imageURL);
            tvImage.setText(R.string.image_added);
        }
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
            try {
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
