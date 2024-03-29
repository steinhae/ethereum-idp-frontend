package de.tum.repairchain;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes20;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigInteger;
import java.util.Date;

import de.tum.repairchain.contracts.Report_sol_Repairchain;

/**
 * Report
 *
 * This class is an object wrapper for the report defined in the smart contract. It uses autogenerated methods
 * in Report_sol_Repairchain to access report properties in the Blockchain.
 *
 */
public class Report {

    private static final String TAG = Report.class.getSimpleName();

    private String pictureHash;
    private String description;
    private LatLng location;
    private String city;
    private Bytes20 id;
    private Date creationDate;
    private Boolean fixedFlag;
    private Boolean enoughFixConfirmations;
    private String fixPictureHash;
    private Boolean enouoghConfirmationsFlag;
    private int confirmationCount;
    private int fixConfirmationCount;

    /**
     * Constructor that reads all report propertiers from the Blockchain.
     * The city argument must specify the city of the report. The id argument
     * contains the id of the report to be loaded.
     * <p>
     * This method reads the properties sequential from the Blockchain which can result in a delay.
     * Further optimizations as reading in parallel are conceivable.
     *
     * @param  city  city of the report
     * @param  id id of the report to be loaded
     */
    public Report(String city, Bytes20 id) {
        Report_sol_Repairchain repairchain = Web3jManager.getInstance().getRepairchain();

        this.city = city;
        this.id = id;

        try {
            Utf8String pictureHash1 = repairchain.getPictureHash1(new Utf8String(city), id).get();
            Utf8String pictureHash2 = repairchain.getPictureHash2(new Utf8String(city), id).get();
            pictureHash = pictureHash1.toString() + pictureHash2.toString();
        } catch (Exception e) {
            Log.d(TAG, "Could not get Picture Hash from Blockchain");
            e.printStackTrace();
        }

        try {
            Utf8String desc1 = repairchain.getDescription1(new Utf8String(city), id).get();
            Utf8String desc2 = repairchain.getDescription2(new Utf8String(city), id).get();
            Utf8String desc3 = repairchain.getDescription3(new Utf8String(city), id).get();
            Utf8String desc4 = repairchain.getDescription4(new Utf8String(city), id).get();
            description = desc1.toString() + desc2.toString() + desc3.toString() + desc4.toString();
        } catch (Exception e) {
            Log.d(TAG, "Could not get Description from Blockchain");
            e.printStackTrace();
        }

        try {
            Utf8String latitude = repairchain.getLatitude(new Utf8String(city), id).get();
            Utf8String longitude = repairchain.getLongitude(new Utf8String(city), id).get();
            location = new LatLng(Double.parseDouble(latitude.toString()), Double.parseDouble(longitude.toString()));
        } catch (Exception e) {
            Log.d(TAG, "Could not get Location from Blockchain");
            e.printStackTrace();
        }

        try {
            Uint256 timestamp = repairchain.getTimestamp(new Utf8String(city), id).get();
            creationDate = new Date(timestamp.getValue().longValue()*1000);
        } catch (Exception e) {
            Log.d(TAG, "Could not get Timestamp from Blockchain");
            e.printStackTrace();
        }

        try {
            Bool fixedReport = repairchain.getFixedReport(new Utf8String(city), id).get();
            fixedFlag = fixedReport.getValue();
        } catch (Exception e) {
            Log.d(TAG, "Could not get Report Fixed Flag from Blockchain");
            e.printStackTrace();
        }

        try {
            Bool enoughFixes = repairchain.getEnoughFixConfirmations(new Utf8String(city), id).get();
            enoughFixConfirmations = enoughFixes.getValue();
        } catch (Exception e) {
            Log.d(TAG, "Could not get Enough Fix Confirmation Flag from Blockchain");
            e.printStackTrace();
        }

        try {
            Utf8String fixPictureHash1 = repairchain.getFixedPictureHash1(new Utf8String(city), id).get();
            Utf8String fixPictureHash2 = repairchain.getFixedPictureHash2(new Utf8String(city), id).get();
            fixPictureHash = fixPictureHash1.toString() + fixPictureHash2.toString();
        } catch (Exception e) {
            Log.d(TAG, "Could not get Fix Picture Hash from Blockchain");
            e.printStackTrace();
        }

        try {
            Bool enoughConfirmations = repairchain.getEnoughConfirmations(new Utf8String(city), id).get();
            enouoghConfirmationsFlag = enoughConfirmations.getValue();
        } catch (Exception e) {
            Log.d(TAG, "Could not get Enough Confirmation Flag from Blockchain");
            e.printStackTrace();
        }

        try {
            Uint256 confCount = repairchain.getConfirmationCount(new Utf8String(city), id).get();
            confirmationCount = confCount.getValue().intValue();
        } catch (Exception e) {
            Log.d(TAG, "Could not get Confirmation Count from Blockchain");
            e.printStackTrace();
        }

        try {
            Uint256 fixConfCount = repairchain.getFixConfirmationCount(new Utf8String(city), id).get();
            fixConfirmationCount = fixConfCount.getValue().intValue();
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

    public Bytes20 getId() {
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
