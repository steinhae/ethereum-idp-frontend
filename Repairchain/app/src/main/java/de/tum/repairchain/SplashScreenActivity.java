package de.tum.repairchain;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashScreenActivity extends Activity {

    @BindView(R.id.msg_greeting)
    TextView txtLaunch;

    @BindView(R.id.loader)
    ProgressBar loader;

    private static final String TAG = SplashScreenActivity.class.getSimpleName();

    private final String WEB3J_CONNECTION_URL = "http://192.168.1.7:8545";
    private String walletJson = "{\"address\":\"51979cae42dcd802d2a24ac2b3b13fd957198740\",\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"2e59d155cfe99b927c5c900f2555a4f297ebc0fd189e7717671a47ed33be2dac\",\"cipherparams\":{\"iv\":\"5ac2035ad061d5b514a9a38311c3ac17\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":1024,\"p\":2048,\"r\":8,\"salt\":\"a33af1489f1d97f44a69749a38a3c3193976cbbc4b273ce3b409a7f702defd2a\"},\"mac\":\"67445bfed9a56989914766ad611c30748a06864d9382baea881dabae50212cc7\"},\"id\":\"ec15c662-9a37-46d8-a786-7a6910a9da7b\",\"version\":3}\"";
    private String walletFilename = "UTC--2017-06-16T16-55-03.383013788Z--51979cae42dcd802d2a24ac2b3b13fd957198740";
    private String walletPassword = "***REMOVED***";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        ButterKnife.bind(this);

        loader.setIndeterminate(true);
        loader.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);

        final BlockchainManager blockchainManager = BlockchainManager.getInstance();
        blockchainManager.init(this);
        blockchainManager.writeWalletFileToDisk(walletJson, walletFilename);
        blockchainManager.setConnectionMethod(BlockchainConnector.WEB3J);

        final Web3jManager web3j = Web3jManager.getInstance();
        web3j.init(WEB3J_CONNECTION_URL, new Web3jManager.OnInitListener() {
            @Override
            public void onInitSuccessful(String clientVersion) {
                web3j.initKeystore(walletPassword, blockchainManager.getKeystorePath() + walletFilename, new Web3jManager.OnKeystoreInitListener() {
                    @Override
                    public void onKeystoreInitSuccessful(String walletAddress) {
                        startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                    }

                    @Override
                    public void onKeystoreInitError(Exception e) {
                        Log.d(TAG, "OnKeyStoreInitError: " + e.getMessage());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loader.setVisibility(View.GONE);
                                txtLaunch.setText("Keystore initialization error!");
                            }
                        });
                    }
                });
            }

            @Override
            public void onInitError(Exception e) {
                Log.d(TAG, "OnInitError: " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loader.setVisibility(View.GONE);
                        txtLaunch.setText("Web3j initialization error!");

                    }
                });

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
