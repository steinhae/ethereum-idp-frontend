package de.tum.repairchain;

import android.app.Activity;
import android.content.Context;
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

/**
 * Splash Screen Activity
 *
 * This activity is the first activity that the user sees after starting the app. It shows a splash screen with
 * a progress bar. In the background the connection to the geth instance via web3j is established and the wallet gets
 * initialized.
 *
 * There are two ways to initialize a wallet. They are documented in the Web3jManager class.
 *
 */
public class SplashScreenActivity extends Activity {

    @BindView(R.id.msg_greeting)
    TextView txtLaunch;

    @BindView(R.id.loader)
    ProgressBar loader;

    private static final String TAG = SplashScreenActivity.class.getSimpleName();

    private String web3jConnectionUrl;
    private String walletFilename = "NOT NEEDED IF CREDENTIALS ARE SET";
    private String walletPassword = "NOT NEEDED IF CREDENTIALS ARE SET";
    private String credentialsJson;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        ButterKnife.bind(this);

        credentialsJson = Helpers.getConfigValue(this, "credentials_json");
        web3jConnectionUrl = Helpers.getConfigValue(this, "web3j_connection_url");

        loader.setIndeterminate(true);
        loader.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);

        final Context ctx = this;

        final BlockchainManager blockchainManager = BlockchainManager.getInstance();
        blockchainManager.init(ctx);
//        blockchainManager.writeWalletFileToDisk(walletJson, walletFilename);
        blockchainManager.setConnectionMethod(BlockchainConnector.WEB3J);

        final Web3jManager web3j = Web3jManager.getInstance();
        web3j.init(web3jConnectionUrl, new Web3jManager.OnInitListener() {
            @Override
            public void onInitSuccessful(String clientVersion) {
                web3j.saveCredentials(credentialsJson, ctx);
                web3j.initKeystore(walletPassword, blockchainManager.getKeystorePath() + walletFilename, ctx, new Web3jManager.OnKeystoreInitListener() {
                    @Override
                    public void onKeystoreInitSuccessful(String walletAddress) {
                        try {
                            web3j.initRepairchain();
                            startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
