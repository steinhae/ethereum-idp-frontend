package de.tum.repairchain;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.ethereum.geth.Geth;
import org.ethereum.geth.Node;
import org.ethereum.geth.NodeConfig;

/**
 * Created by zeddsdead on 15.06.17.
 */

public class BlockchainService extends IntentService {

    private Node node;

    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public BlockchainService() {
        super("BlockchainService");
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
        initTestnetNode();
    }

    private boolean initTestnetNode() {
        NodeConfig nc = new NodeConfig();
        nc.setEthereumNetworkID(3);
        nc.setWhisperEnabled(true);
        nc.setEthereumEnabled(true);
        String genesis = Geth.testnetGenesis();
        nc.setEthereumGenesis(genesis);
        try {
            node = Geth.newNode(getFilesDir() + "/.ethereum", nc);
            node.start();
            return true;
        } catch (Exception e) {
            Log.e("InitTestNet","Init of Testnet node failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        // Normally we would do some work here, like download a file.
        // For our sample, we just sleep for 5 seconds.
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
    }

    //returns the instance of the service
    public class LocalBinder extends Binder {
        public BlockchainService getServiceInstance(){
            return BlockchainService.this;
        }
    }
}