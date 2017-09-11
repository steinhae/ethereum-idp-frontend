package de.tum.repairchain;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.ImageView;

import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes20;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import de.tum.repairchain.contracts.Report_sol_Repairchain;

import static android.content.Context.LOCATION_SERVICE;
import static de.tum.repairchain.Constants.LOCATION_PERMISSIONS;

/**
 * Helper class
 *
 * Contains various support methods.
 *
 */
public class Helpers {

    private static final String TAG = Helpers.class.getSimpleName();

    public static String getConfigValue(Context context, String name) {
        Resources resources = context.getResources();

        try {
            InputStream rawResource = resources.openRawResource(R.raw.local);
            Properties properties = new Properties();
            properties.load(rawResource);
            return properties.getProperty(name);
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Unable to find the local file: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Failed to open local file.");
        }
        return null;
    }

    public static String getUrlFromHash(String hash) {
        return "https://gateway.ipfs.io/ipfs/" + hash;
    }

    public static List<Bytes20> getAllReportIdsFromCity(String city) {
        Report_sol_Repairchain repairchain = Web3jManager.getInstance().getRepairchain();
        List<Bytes20> resultList = new ArrayList<>();
        try {
            DynamicArray<Bytes20> reportIds = repairchain.getReportIdsFromCity(new Utf8String(city)).get();
            resultList.addAll(reportIds.getValue());
        } catch (Exception e) {
            Log.d(TAG, "Could not get Report Ids from Blockchain");
            e.printStackTrace();
        }
        return resultList;
    }

    public static Location getLastKnownLocation(Context context, Activity activity, LocationManager locationManager) {
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, LOCATION_PERMISSIONS, 1);
            }
            Location l = locationManager.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                    // Found best last known location: %s", l);
                    bestLocation = l;
                }
        }
        return bestLocation;
    }

    public static class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {
        ImageView imageView;
        ProgressDialog progressDialog;
        public DownloadImageTask(Context context, ImageView imageView){
            this.imageView = imageView;
            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle("Downloading image");
            progressDialog.setIndeterminate(true);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Bitmap doInBackground (String... urls){
            Log.i("DownloadImageTask", "started working");
            Bitmap result = null;
            String url = getUrlFromHash(urls[0]);
            try {
                InputStream in = new URL(url).openStream();
                result = BitmapFactory.decodeStream(in);
                in.close();
            } catch (Exception e) {
                Log.e("UploadImage", "Failed loading IPFS image. ", e);
                e.printStackTrace();
            }
            Log.i("DownloadImageTask", "finished working");
            return result;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(100);
            progressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            progressDialog.dismiss();
            Log.i("DownloadImageTask", "postExecute");
            imageView.setImageBitmap(result);
        }
    }
}
