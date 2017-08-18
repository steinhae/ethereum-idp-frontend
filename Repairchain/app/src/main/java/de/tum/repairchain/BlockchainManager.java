package de.tum.repairchain;

import android.content.Context;
import android.util.Log;

import java.io.*;

public class BlockchainManager {

    private static final String TAG = BlockchainManager.class.getSimpleName();

    private static BlockchainManager instance = null;

    private String ethereumFolderPath;
    private String keystorePath;
    private BlockchainConnector connectionMethod;

    private BlockchainManager() {

    }

    public static BlockchainManager getInstance() {
        if (instance == null) {
            instance = new BlockchainManager();
        }
        return instance;
    }

    public void init(Context ctx) {
        ethereumFolderPath = ctx.getFilesDir() + "/.ethereum/";
        keystorePath =  ethereumFolderPath + "keystore/";
    }

    public void setConnectionMethod(BlockchainConnector method) {
        connectionMethod = method;
    }

    public BlockchainConnector getConnectionMethod() {
        return connectionMethod;
    }

    public String getEthereumFolderPath() {
        return ethereumFolderPath;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public void writeWalletFileToDisk(String walletJson, String walletFilename) {
        File keystoreDirectory = new File(keystorePath);
        boolean directoryExists = keystoreDirectory.exists();

        if (!directoryExists) {
            directoryExists = keystoreDirectory.mkdirs();
        }

        if (directoryExists) {
            File walletFile = new File(keystorePath + walletFilename);
            if (!walletFile.exists()) {
                try {
                    if (walletFile.createNewFile()) {
                        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(keystorePath + walletFilename), "utf-8"))) {
                            writer.write(walletJson);
                            Log.d(TAG, "Wallet file got written to disk successfully.");
                        } catch (IOException e) {
                            Log.d(TAG, "Error while writing wallet file to disk.");
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    Log.d(TAG, "Error while writing wallet file to disk.");
                    e.printStackTrace();
                }
            }
        } else {
            Log.d(TAG, "Error while writing wallet file to disk.");
        }
    }
}
