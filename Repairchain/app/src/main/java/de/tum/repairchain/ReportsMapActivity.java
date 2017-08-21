package de.tum.repairchain;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Button;

import android.widget.ProgressBar;
import butterknife.ButterKnife;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;
import org.web3j.abi.datatypes.generated.Bytes20;

import static de.tum.repairchain.Constants.*;
import static de.tum.repairchain.Helpers.getAllReportIdsFromCity;

public class ReportsMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location currentLocation;
    private List<Report> reportList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports_map);

        ButterKnife.bind(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton newReport = new FloatingActionButton(this);

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
    }

    @OnClick({R.id.btn_add_report})
    public void addNewReport(FloatingActionButton btn){
        startActivity(new Intent(ReportsMapActivity.this, ReportActivity.class));
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, LOCATION_PERMISSIONS, 1);
        } else {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnInfoWindowClickListener(this);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading reports...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // FixMe create function to fetch current city
                List<Bytes20> reportIds = getAllReportIdsFromCity(CITY);
                reportList = new ArrayList<Report>();

                //FixMe well redundancy n shit
                for (Bytes20 reportId : reportIds) {
                    Report currentReport = new Report(CITY, reportId);
                    reportList.add(currentReport);
                }

                for (final Report report : reportList) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (report.getFixedFlag()) {
                                if (!report.getEnoughFixConfirmations())
                                    mMap.addMarker(new MarkerOptions().position(report.getLocation())
                                            .title("Fix")
                                            .snippet(report.getDescription()))
                                            .setTag(report.getId());
                            } else {
                                if (!report.getEnouoghConfirmationsFlag())
                                    mMap.addMarker(new MarkerOptions().position(report.getLocation())
                                            .title("Report")
                                            .snippet(report.getDescription()))
                                            .setTag(report.getId());
                            }
                        }
                    });
                }
                progressDialog.dismiss();
            }
        }).start();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Bytes20 reportId = (Bytes20)marker.getTag();
        Intent showReport = new Intent(ReportsMapActivity.this, ShowReportActivity.class);
        showReport.putExtra(REPORT_ID, reportId.getValue());
        startActivity(showReport);
    }
}
