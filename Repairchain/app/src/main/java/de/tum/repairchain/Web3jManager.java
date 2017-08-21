package de.tum.repairchain;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import de.tum.repairchain.contracts.Report_sol_Repairchain;

public class Web3jManager {

    private static final String TAG = Web3jManager.class.getSimpleName();

    private static Web3jManager instance = null;

    private Web3j web3jClient;
    private Credentials credentials;
    private boolean initialized;

    private Report_sol_Repairchain repairchain;

    public interface OnInitListener {
        void onInitSuccessful(String clientVersion);
        void onInitError(Exception e);
    }

    public interface OnKeystoreInitListener {
        void onKeystoreInitSuccessful(String walletAddress);
        void onKeystoreInitError(Exception e);
    }

    public static Web3jManager getInstance() {
        if (instance == null) {
            instance = new Web3jManager();
        }
        return instance;
    }

    private Web3jManager() {
    }

    public void init(String connectionUrl, OnInitListener listener) {
        initialized = false;
        web3jClient = null;
        initWeb3jConnection(connectionUrl, listener);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public Web3j getWeb3jClient() throws Exception  {
        if (isInitialized()) {
            return this.web3jClient;
        } else {
            throw new Exception("Web3j client not initialized.");
        }
    }

    public Credentials getCredentials() {
        return this.credentials;
    }

    public Report_sol_Repairchain getRepairchain() {
        return repairchain;
    }

    private void initWeb3jConnection(final String url, final OnInitListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                web3jClient = Web3jFactory.build(new HttpService(url));
                Web3ClientVersion web3ClientVersion = null;
                try {
                    web3ClientVersion = web3jClient.web3ClientVersion().sendAsync().get(5, TimeUnit.SECONDS);
                    String clientVersion = web3ClientVersion.getWeb3ClientVersion();
                    initialized = true;
                    listener.onInitSuccessful(clientVersion);
                } catch (Exception e) {
                    initialized = false;
                    listener.onInitError(e);
                }
            }
        }).start();
    }

    public void saveCredentials(Credentials cred, Context ctx) {
        Gson gson = new Gson();
        String credentialsSerialized = gson.toJson(cred);
        saveCredentials(credentialsSerialized, ctx);
    }

    public void saveCredentials(String credentialsSerialized, Context ctx) {
        FileOutputStream fos = null;
        try {
            fos = ctx.openFileOutput("credentials", Context.MODE_PRIVATE);
            fos.write(credentialsSerialized.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Credentials loadCredentials(Context ctx) {
        FileInputStream fis = null;
        StringBuilder cred = new StringBuilder();
        try {
            fis = ctx.openFileInput("credentials");
            int c;
            while ((c = fis.read()) != -1) {
                cred.append(Character.toString((char)c));

            }
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Gson gson = new Gson();
        return  gson.fromJson(cred.toString(), Credentials.class);
    }

    public void initKeystore(String walletPassword, String filepath, Context ctx, final OnKeystoreInitListener listener) {
        try {
            credentials = loadCredentials(ctx);
            if (credentials == null) {
                credentials = WalletUtils.loadCredentials(walletPassword, filepath);
                saveCredentials(credentials, ctx);
            }
            listener.onKeystoreInitSuccessful(credentials.getAddress());
        } catch (Exception e) {
            listener.onKeystoreInitError(e);
        }
    }

    public void initRepairchain() throws Exception {
        EthGasPrice gasPrice = null;
        if (isInitialized() && credentials != null) {
            try {
                gasPrice = web3jClient.ethGasPrice().sendAsync().get();
                repairchain = Report_sol_Repairchain.load(Constants.REPAIRCHAIN_ADDRESS, web3jClient, credentials, gasPrice.getGasPrice(), new BigInteger(Constants.REPAIRCHAIN_GAS_LIMIT));
            } catch (Exception e) {
                Log.d(TAG, "Error while getting ETH gas price.");
                e.printStackTrace();
            }
        } else {
            throw new Exception("Web3j client or keystore not initialized.");
        }
    }
}