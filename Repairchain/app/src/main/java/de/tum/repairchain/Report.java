package de.tum.repairchain;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes20;
import org.web3j.abi.datatypes.generated.Uint256;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import de.tum.repairchain.contracts.Report_sol_Repairchain;

/**
 * Created by anton on 19.08.2017.
 */

public class Report {

    private static final String TAG = Report.class.getSimpleName();

    private String pictureHash;
    private String description;
    private LatLng location;
    private String city;
    private String id;
    private Date creationDate;

    public Report(String city, String id) {
        Report_sol_Repairchain repairchain = Web3jManager.getInstance().getRepairchain();

        this.city = city;
        this.id = id;

        try {
            Utf8String pictureHash1 = repairchain.getPictureHash1(new Utf8String(city), new Bytes20(id.getBytes())).get();
            Utf8String pictureHash2 = repairchain.getPictureHash2(new Utf8String(city), new Bytes20(id.getBytes())).get();
            pictureHash = pictureHash1.toString() + pictureHash2.toString();
        } catch (Exception e) {
            Log.d(TAG, "Could not get Picture Hash from Blockchain");
            e.printStackTrace();
        }

        try {
            Utf8String desc1 = repairchain.getDescription1(new Utf8String(city), new Bytes20(id.getBytes())).get();
            Utf8String desc2 = repairchain.getDescription2(new Utf8String(city), new Bytes20(id.getBytes())).get();
            Utf8String desc3 = repairchain.getDescription3(new Utf8String(city), new Bytes20(id.getBytes())).get();
            Utf8String desc4 = repairchain.getDescription4(new Utf8String(city), new Bytes20(id.getBytes())).get();
            description = desc1.toString() + desc2.toString() + desc3.toString() + desc4.toString();
        } catch (Exception e) {
            Log.d(TAG, "Could not get Description from Blockchain");
            e.printStackTrace();
        }

        try {
            Utf8String latitude = repairchain.getLatitude(new Utf8String(city), new Bytes20(id.getBytes())).get();
            Utf8String longitude = repairchain.getLongitude(new Utf8String(city), new Bytes20(id.getBytes())).get();
            location = new LatLng(Double.parseDouble(latitude.toString()), Double.parseDouble(longitude.toString()));
        } catch (Exception e) {
            Log.d(TAG, "Could not get Location from Blockchain");
            e.printStackTrace();
        }

        try{
            Uint256 timestamp = repairchain.getTimestamp(new Utf8String(city), new Bytes20(id.getBytes())).get();
            creationDate = new Date(Long.parseLong(timestamp.toString()));
        } catch (Exception e) {
            Log.d(TAG, "Could not get Timestamp from Blockchain");
            e.printStackTrace();
        }

    }
}
