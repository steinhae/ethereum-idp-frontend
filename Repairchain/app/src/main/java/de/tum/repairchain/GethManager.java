package de.tum.repairchain;

import android.os.Bundle;
import android.util.Log;
import org.ethereum.geth.*;

/**
 * Created by Hannes on 17.08.2017.
 */
public class GethManager {

//    private boolean initTestnetNode() {
//        NodeConfig nc = new NodeConfig();
//        nc.setEthereumNetworkID(3);
//        nc.setWhisperEnabled(true);
//        nc.setEthereumEnabled(true);
//        String genesis = Geth.testnetGenesis();
//        nc.setEthereumGenesis(genesis);
//        try {
//            node = Geth.newNode(ethereumFolderPath, nc);
//            node.start();
//            updateSyncing();
//            return true;
//        } catch (Exception e) {
//            Log.e("InitTestNet","Init of Testnet node failed: " + e.getMessage());
//            return false;
//        }
//    }
//
//    private boolean initEthereumNode() {
//        try {
//            ethereumClient = node.getEthereumClient();
//        } catch (Exception e) {
//            Log.e("GetEthereumClient", "Failed to get the Ethereum client: " + e.getMessage());
//            return false;
//        }
//        updateSyncing();
//        return true;
//    }
//
//    private boolean subscribeToNewHead() {
//        try {
//            ethereumClient.subscribeNewHead(ctx, handler, 16);
//        } catch (Exception e) {
//            return false;
//        }
//        return true;
//    }
//
//    private void initKeystore() {
//        String accountAddress = "";
//        if (connectionMethod == MainActivity.BlockchainConnector.GETH) {
//            gethKeystore = Geth.newKeyStore(keystorePath, 8, 16);
//            try {
//                gethAccount = gethKeystore.getAccounts().get(0);
//                accountAddress = gethKeystore.getAccounts().toString();
//            } catch (Exception e) {
//                initKeystoreError(e, "");
//                return;
//            }
//        }
//    }

//    private void initGethConnection(Bundle savedInstanceState) {
//        ctx = new Context();
//        if (initTestnetNode()) {
//            NodeInfo info = node.getNodeInfo();
//            txtVersion.setText("My name: " + info.getName() + "\n");
//            //            textbox.append("My address: " + info.getListenerAddress() + "\n");
//            //            textbox.append("My protocols: " + info.getProtocols() + "\n\n");
//
//            if (initEthereumNode()) {
//                subscribeToNewHead();
//            } else {
//                finish();
//            }
//        }
//    }

//    NewHeadHandler handler = new NewHeadHandler() {
//        @Override public void onError(String error) { }
//        @Override public void onNewHead(final Header header) {
//            long now = System.currentTimeMillis();
//            if (now - lastUpdate >= 1000) {
//                MainActivity.this.runOnUiThread(new Runnable() {
//                    public void run() {
//                        if (!firstHeader) {
//                            firstHeader = true;
//                        }
//                        String txt = "#" + header.getNumber() + ": " + header.getHash().getHex().substring(0, 10) + "…\n";
//                        txtLatestBlock.setText(txt);
//                        Log.v("NewBlock", txt);
//                    }
//                });
//                lastUpdate = now;
//            }
//        }
//    };
}
