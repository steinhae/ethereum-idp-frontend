package de.tum.repairchain;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.web3j.abi.datatypes.Bool;
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



    private Boolean fixedFlag;
    private Boolean enoughFixConfirmations;
    private String fixPictureHash;
    private  Boolean enouoghConfirmationsFlag;
    private Integer confirmationCount;
    private Integer fixConfirmationCount;


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

        try {
            Bool fixedReport = repairchain.getFixedReport(new Utf8String(city), new Bytes20(id.getBytes())).get();
            fixedFlag = fixedReport.getValue();
        } catch (Exception e) {
            Log.d(TAG, "Could not get Report Fixed Flag from Blockchain");
            e.printStackTrace();
        }

        try {
            Bool enoughFixes = repairchain.getEnoughFixConfirmations(new Utf8String(city), new Bytes20(id.getBytes())).get();
            enoughFixConfirmations = enoughFixes.getValue();
        } catch (Exception e) {
            Log.d(TAG, "Could not get Enough Fix Confirmation Flag from Blockchain");
            e.printStackTrace();
        }

        try {
            Utf8String fixPictureHash1 = repairchain.getFixedPictureHash1(new Utf8String(city), new Bytes20(id.getBytes())).get();
            Utf8String fixPictureHash2 = repairchain.getFixedPictureHash2(new Utf8String(city), new Bytes20(id.getBytes())).get();
            fixPictureHash = fixPictureHash1.toString() + fixPictureHash2.toString();
        } catch (Exception e) {
            Log.d(TAG, "Could not get Fix Picture Hash from Blockchain");
            e.printStackTrace();
        }

        try {
            Bool enoughConfirmations = repairchain.getEnoughConfirmations(new Utf8String(city), new Bytes20(id.getBytes())).get();
            enouoghConfirmationsFlag = enoughConfirmations.getValue();
        } catch (Exception e) {
            Log.d(TAG, "Could not get Enough Confirmation Flag from Blockchain");
            e.printStackTrace();
        }

        try {
            Uint256 confCount = repairchain.getConfirmationCount(new Utf8String(city), new Bytes20(id.getBytes())).get();
            confirmationCount = Integer.parseInt(confCount.toString());
        } catch (Exception e) {
            Log.d(TAG, "Could not get Confirmation Count from Blockchain");
            e.printStackTrace();
        }

        try {
            Uint256 fixConfCount = repairchain.getFixConfirmationCount(new Utf8String(city), new Bytes20(id.getBytes())).get();
            fixConfirmationCount = Integer.parseInt(fixConfCount.toString());
        } catch (Exception e) {
            Log.d(TAG, "Could not get fix Confirmation Count from Blockchain");
            e.printStackTrace();
        }


    }

    public String getPictureHash() {
        return pictureHash;
    }

    public String getDescription() {
        return description;
    }

    public LatLng getLocation() {
        return location;
    }

    public String getCity() {
        return city;
    }

    public String getId() {
        return id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Boolean getFixedFlag() {
        return fixedFlag;
    }

    public Boolean getEnoughFixConfirmations() {
        return enoughFixConfirmations;
    }

    public String getFixPictureHash() {
        return fixPictureHash;
    }

    public Boolean getEnouoghConfirmationsFlag() {
        return enouoghConfirmationsFlag;
    }

    public Integer getConfirmationCount() {
        return confirmationCount;
    }

    public Integer getFixConfirmationCount() {
        return fixConfirmationCount;
    }
}
