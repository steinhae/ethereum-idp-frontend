package de.tum.repairchain;

import android.util.Log;

import org.web3j.abi.datatypes.StaticArray;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes20;
import org.web3j.abi.datatypes.generated.Uint256;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.tum.repairchain.contracts.Report_sol_Repairchain;

/**
 * Created by palac on 19.08.2017.
 */

public class Helpers {

    private static final String TAG = Helpers.class.getSimpleName();

    public static String getUrlFromHash(String hash){
        return "https://gateway.ipfs.io/ipfs/" + hash;
    }

    public static List<String> getAllReportIdsFromCity(String city){
        Report_sol_Repairchain repairchain = Web3jManager.getInstance().getRepairchain();
        int reportsLength = 0;
        try {
            reportsLength = Integer.parseInt((repairchain.getReportsLengthOfCity(new Utf8String(city)).get()).toString());
        } catch (Exception e) {
            Log.d(TAG, "Could not get Number of Reports from Blockchain");
            e.printStackTrace();
        }

        int loops = reportsLength/100;
        List<String> resultList = new ArrayList<>();
        for(long i = 0; i <= loops; i++){
            try {
                StaticArray<Bytes20> reportIds = repairchain.getReportIdsFromCity(new Utf8String(city), new Uint256(i)).get();
                for(Bytes20 item : reportIds.getValue()){
                    resultList.add(item.toString());
                }
            } catch (Exception e) {
                Log.d(TAG, "Could not get Report Ids from Blockchain");
                e.printStackTrace();
            }
        }
        return resultList;
    }
}
