package de.tum.repairchain;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import org.ethereum.geth.*;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.security.Key;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.txt_version) TextView txtVersion;
    @BindView(R.id.txt_latestblock) TextView txtLatestBlock;
    @BindView(R.id.btn_balance) Button btnAccountBalance;
    @BindView(R.id.btn_transaction) Button btnTransaction;
    @BindView(R.id.txt_syncing) TextView txtSyncing;

    public enum BlockchainConnector {
        GETH,
        WEB3J
    };

    @OnClick({ R.id.btn_report })
    public void clickReportButton(Button btn) {
        Intent intent = new Intent(getApplicationContext(), ReportActivity.class);
        startActivity(intent);
    }

    @OnClick({ R.id.btn_balance })
    public void clickBalanceButton(Button btn) {
        double etherBalance = 0;
        if (connectionMethod == BlockchainConnector.GETH) {
            try {
                BigInt weiBalance = ethereumClient.getBalanceAt(ctx, account.getAddress(), -1);
                etherBalance =  Double.valueOf(weiBalance.toString()) / Math.pow(10, 18);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (connectionMethod == BlockchainConnector.WEB3J) {
            try {
                EthGetBalance balance = web3jClient.ethGetBalance("0x96C9c314acfFab773bC95838d7487518D88D032d", DefaultBlockParameterName.LATEST).sendAsync().get();
                BigInteger weiBalance = balance.getBalance();
                etherBalance = weiBalance.doubleValue() / Math.pow(10, 18);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (etherBalance != 0) {
            Toast.makeText(getApplicationContext(), "Balance: " + String.valueOf(etherBalance), Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(getApplicationContext(), "Balance: " + String.valueOf(etherBalance), Toast.LENGTH_SHORT).show();

    }

    @OnClick( { R.id.btn_transaction })
    public void clickTransactionButton(Button btn) {
        try {
            BigInt amount = new BigInt(1000000000000000000l);
            BigInt gasLimit = new BigInt(200000l);
            BigInt gasPrice = new BigInt(500000000000000000l);
            Transaction tx = new Transaction(1337l, new Address("0xAb54bF5311dDF905CF59bA8426505b671F1d8E96"), amount, gasLimit, gasPrice, null);
            keyStore.unlock(account, "***REMOVED***");
            Transaction signedTx = keyStore.signTx(account, tx, new BigInt(3));
            ethereumClient.sendTransaction(ctx, signedTx);
            Receipt txReceipt = ethereumClient.getTransactionReceipt(ctx, tx.getHash());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long lastUpdate = 0;
    private EthereumClient ethereumClient;
    private Web3j web3jClient;
    private Context ctx;
    private Node node;
    private KeyStore keyStore;
    private boolean firstHeader = false;
    private Account account;
    private BlockchainConnector connectionMethod;

    NewHeadHandler handler = new NewHeadHandler() {
        @Override public void onError(String error) { }
        @Override public void onNewHead(final Header header) {
            long now = System.currentTimeMillis();
            if (now - lastUpdate >= 1000) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        if (!firstHeader) {
                            enableButtons();
                            firstHeader = true;
                        }
                        String txt = "#" + header.getNumber() + ": " + header.getHash().getHex().substring(0, 10) + "…\n";
                        txtLatestBlock.setText(txt);
                        Log.v("NewBlock", txt);
                    }
                });
                lastUpdate = now;
            }
        }
    };

    private void enableButtons() {
        btnAccountBalance.setEnabled(true);
        btnTransaction.setEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        connectionMethod = BlockchainConnector.WEB3J;

        if (savedInstanceState == null) {
            if (connectionMethod == BlockchainConnector.WEB3J) {
                initWeb3jConnection();
                enableButtons();
            } else if (connectionMethod == BlockchainConnector.GETH) {
                initGethConnection(savedInstanceState);
            }
        } else {
            Log.d("OnCreate", "Node already initialized.");
        }
    }

    private void initWeb3jConnection() {
        web3jClient = Web3jFactory.build(new HttpService("http://192.168.1.7:8545"));
        Web3ClientVersion web3ClientVersion = null;

        try {
            web3ClientVersion = web3jClient.web3ClientVersion().sendAsync().get();
            String clientVersion = web3ClientVersion.getWeb3ClientVersion();
            Log.i("Web3J connection", clientVersion.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initGethConnection(Bundle savedInstanceState) {
        ctx = new Context();
        if (initTestnetNode()) {
            initKeyStore();
            NodeInfo info = node.getNodeInfo();
            txtVersion.setText("My name: " + info.getName() + "\n");
            //            textbox.append("My address: " + info.getListenerAddress() + "\n");
            //            textbox.append("My protocols: " + info.getProtocols() + "\n\n");

            if (initEthereumNode()) {
                subscribeToNewHead();
            } else {
                finish();
            }
        }
    }

    private void initKeyStore() {
        String walletJson = "{\"address\":\"51979cae42dcd802d2a24ac2b3b13fd957198740\",\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"2e59d155cfe99b927c5c900f2555a4f297ebc0fd189e7717671a47ed33be2dac\",\"cipherparams\":{\"iv\":\"5ac2035ad061d5b514a9a38311c3ac17\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":1024,\"p\":2048,\"r\":8,\"salt\":\"a33af1489f1d97f44a69749a38a3c3193976cbbc4b273ce3b409a7f702defd2a\"},\"mac\":\"67445bfed9a56989914766ad611c30748a06864d9382baea881dabae50212cc7\"},\"id\":\"ec15c662-9a37-46d8-a786-7a6910a9da7b\",\"version\":3}\"";
        String walletFilename = "UTC--2017-06-16T16-55-03.383013788Z--51979cae42dcd802d2a24ac2b3b13fd957198740";
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(getFilesDir() + "/.ethereum/keystore/" + walletFilename), "utf-8"))) {
            writer.write(walletJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
        keyStore = Geth.newKeyStore(getFilesDir() + "/.ethereum/keystore", 8, 16);
        try {
            account = keyStore.getAccounts().get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            updateSyncing();
            return true;
        } catch (Exception e) {
            Log.e("InitTestNet","Init of Testnet node failed: " + e.getMessage());
            return false;
        }
    }

    private boolean initEthereumNode() {
        try {
            ethereumClient = node.getEthereumClient();
        } catch (Exception e) {
            Log.e("GetEthereumClient", "Failed to get the Ethereum client: " + e.getMessage());
            return false;
        }
        updateSyncing();
        return true;
    }

    private boolean subscribeToNewHead() {
        try {
            ethereumClient.subscribeNewHead(ctx, handler, 16);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void updateSyncing(){
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    SyncProgress progress = ethereumClient.syncProgress(ctx);
                                    if (progress != null){
                                        if (progress.getCurrentBlock() >= progress.getHighestBlock()) {
                                            txtSyncing.setText(" up-to-date");
                                            txtLatestBlock.setText("");
                                        } else {
                                            txtSyncing.setText(" syncing");
                                            txtLatestBlock.setText("Latest block: " + progress.getCurrentBlock() + "\n");
                                        }
                                    } else {
                                        txtSyncing.setText(" not syncing");
                                        txtLatestBlock.setText("");
                                    }
                                } catch (Exception e) {
                                    Log.e("Getting sync progress", "Could not get syncing progress. ", e);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e("Update Syncing", "Could not update Syncing. ", e);
                }
            }
        };

        thread.start();
    }
}