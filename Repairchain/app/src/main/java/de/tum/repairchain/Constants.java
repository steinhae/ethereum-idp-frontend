package de.tum.repairchain;

import android.Manifest;

public class Constants {
    public static final String RETURN_IMAGE_URL = "Image URL";
    public static final String RETURN_HASH = "Return Hash";
    public static final String IPFS_UPLOAD_DONE = "Upload finished";
    public static final String REPORT_ID = "Report ID";
    public static final String CITY = "minga";
    public static final int PHOTO_REQUEST = 1;
    public static final int RETURN_IMAGE_HASH = 27;
    public static final int IMAGE_ADDED = 42;
    public static final int JUST_SOME_CODE = 123;
    public static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    public static final String ETHEREUM_FOLDER = ".ethereum/";
    public static final String KEYSTORE_FOLDER = "keystore/";
    public static final String REPAIRCHAIN_ADDRESS = "0x8674135D16dA9BA1e7C336F7b768161E6724260b";
    public static final String REPAIRCHAIN_GAS_LIMIT = "800000";
}
