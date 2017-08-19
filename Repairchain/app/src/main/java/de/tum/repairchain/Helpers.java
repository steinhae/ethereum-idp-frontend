package de.tum.repairchain;

/**
 * Created by palac on 19.08.2017.
 */

public class Helpers {
    public static String getUrlFromHash(String hash){
        return "https://gateway.ipfs.io/ipfs/" + hash;
    }
}
