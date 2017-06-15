package de.tum.repairchain;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import org.ethereum.geth.*;
import android.util.Log;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.txt_version) TextView txtVersion;
    @BindView(R.id.txt_latestblock) TextView txtLatestBlock;

    private long lastUpdate = 0;
    private EthereumClient ethereumClient;
    private Context ctx;
    private Node node;

    NewHeadHandler handler = new NewHeadHandler() {
        @Override public void onError(String error) { }
        @Override public void onNewHead(final Header header) {
            long now = System.currentTimeMillis();
            if (now - lastUpdate >= 1000) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        String txt = "#" + header.getNumber() + ": " + header.getHash().getHex().substring(0, 10) + "…\n";
                        txtLatestBlock.setText(txt);
                        Log.v("NewBlock", txt);
                    }
                });
                lastUpdate = now;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setTitle("Android In-Process Node");

        if (savedInstanceState == null) {
            ctx = new Context();
            if (initTestnetNode()) {
                NodeInfo info = node.getNodeInfo();
                txtVersion.setText("My name: " + info.getName() + "\n");
                //            textbox.append("My address: " + info.getListenerAddress() + "\n");
                //            textbox.append("My protocols: " + info.getProtocols() + "\n\n");

                if (initEthereumNode()) {
                    subscribeToNewHead();
                    displayLatestBlock();
                } else {
                    finish();
                }
            }
        } else {
            Log.d("OnCreate","Node already initialized.");
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

    private void displayLatestBlock() {
        try {
            txtLatestBlock.setText("Latest block: " + ethereumClient.getBlockByNumber(ctx, -1).getNumber() + ", syncing...\n");
        } catch (Exception e) {
            Log.d("DisplayLatestBlock", "Could not display latest block: " + e.getMessage());
        }
    }
}