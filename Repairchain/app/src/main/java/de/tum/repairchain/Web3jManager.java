package de.tum.repairchain;

import android.util.Log;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;

import java.util.concurrent.TimeUnit;

public class Web3jManager {

    private static Web3jManager instance = null;

    private Web3j web3jClient;
    private Credentials credentials;
    private boolean initialized;

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

    public void initKeystore(String walletPassword, String filepath, final OnKeystoreInitListener listener) {
        try {
            credentials = WalletUtils.loadCredentials(walletPassword, filepath);
            listener.onKeystoreInitSuccessful(credentials.getAddress());
        } catch (Exception e) {
            listener.onKeystoreInitError(e);
        }
    }
}