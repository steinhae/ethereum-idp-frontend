package de.tum.repairchain;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Wallet;
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

/**
 * Web3j Manager
 *
 * Web3j manager is a singleton class that handles the communication with the blockchain via web3j as well as
 * wallet/credential management. To fully initialize the class 4 steps are necessary.
 * 1. Call getInstance to create and get an instance.
 * 2. Call the init method.
 * 3. The credentials need to be initialized. That can happen via initKeystoreWallet or via initKeystoreJson.
 * 4. Finally, a call to initRepairchain is necessary.
 * At this point the getRepairchain method can be used to get a reference to the Repairchain object. The Repairchain
 * object enables communication with the smart contract/blockchain.
 *
 */
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

    /**
     * Method to initialize credentials via wallet file. The wallet file needs to be saved to disk before it can be
     * used. It can be saved before calling this function via Helpers.writeWalletFileToDisk. This method is slower
     * compared to initKeystoreJson as the wallet decryption takes place at runtime.
     *
     * @param walletPassword the password to decrypt the wallet
     * @param filepath the full file path of the wallet (on the Android device)
     */
    public void initKeystoreWallet(String walletPassword, String filepath, final OnKeystoreInitListener listener) {
        try {
            credentials = WalletUtils.loadCredentials(walletPassword, filepath);
            listener.onKeystoreInitSuccessful(credentials.getAddress());
        } catch (Exception e) {
            listener.onKeystoreInitError(e);
        }
    }

    /**
     * Method to initialize credentials via serialized credentials (decrypted wallet file). Serialization can be
     * performed with the ethereum-wallet-decryptor tool. This method is faster compared to initKeystoreWallet.
     * (https://github.com/steinhae/ethereum-wallet-decryptor/releases)
     *
     * @param credentialsJson the serialized credentials in json format
     */
    public void initKeystoreJson(String credentialsJson, final OnKeystoreInitListener listener) {
        try {
            Gson gson = new Gson();
            credentials = gson.fromJson(credentialsJson, Credentials.class);
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
