package de.tum.repairchain;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import de.tum.repairchain.contracts.Report_sol_Repairchain;
import org.ethereum.geth.*;
import android.util.Log;
import android.widget.Toast;
import java.math.BigInteger;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

public class MainActivity extends AppCompatActivity {

    private Web3j web3jClient;

    private BlockchainConnector connectionMethod;

    private EthereumClient ethereumClient;
    private Context ctx;
    private Node node;
    private KeyStore gethKeystore;
    private Credentials web3jCredentials;
    private boolean firstHeader = false;
    private Account gethAccount;

    @BindView(R.id.txt_version) TextView txtVersion;
    @BindView(R.id.txt_latestblock) TextView txtLatestBlock;
    @BindView(R.id.btn_balance) Button btnAccountBalance;
    @BindView(R.id.btn_transaction) Button btnTransaction;
    @BindView(R.id.txt_syncing) TextView txtSyncing;

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
                BigInt weiBalance = ethereumClient.getBalanceAt(ctx, gethAccount.getAddress(), -1);
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
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending transaction...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (connectionMethod == BlockchainConnector.GETH) {
                    try {
                        BigInt amount = new BigInt(1000000000000000000l);
                        BigInt gasLimit = new BigInt(200000l);
                        BigInt gasPrice = new BigInt(500000000000000000l);
                        Transaction tx = new Transaction(1337l, new Address("0xAb54bF5311dDF905CF59bA8426505b671F1d8E96"), amount, gasLimit, gasPrice, null);
                        gethKeystore.unlock(gethAccount, "penis");
                        Transaction signedTx = gethKeystore.signTx(gethAccount, tx, new BigInt(3));
                        ethereumClient.sendTransaction(ctx, signedTx);
                        Receipt txReceipt = ethereumClient.getTransactionReceipt(ctx, tx.getHash());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if (connectionMethod == BlockchainConnector.WEB3J) {
                    final TransactionReceipt transactionReceipt;
                    try {
                        EthGasPrice gasPrice = web3jClient.ethGasPrice().sendAsync().get();
                        Report_sol_Repairchain repairchain = Report_sol_Repairchain.load("0x9b68cf0752d280a9A163E3329590cEba92d2b472", web3jClient, web3jCredentials, gasPrice.getGasPrice(), new BigInteger("440000"));
                        Utf8String hash1 = repairchain.getPictureHash1(new Utf8String("minga"), new Uint256(0l)).get();
                        transactionReceipt = repairchain.addReportToCity(new Utf8String("minga"), new Utf8String("0x666")).get();
//                        transactionReceipt = Transfer.sendFundsAsync(
//                                web3jClient, web3jCredentials, "0x6f565CE03e99FdbCFe097e960961FB9C8Bed28b0", BigDecimal.valueOf(0.1), Convert.Unit.ETHER).get();
                        if (transactionReceipt != null) {
                            Log.i(connectionMethod.toString() + " transaction", "Transaction with hash " + transactionReceipt.getTransactionHash() + " successful");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Transaction successful, txHash = " + transactionReceipt.getTransactionHash(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        connectionMethod = BlockchainManager.getInstance().getConnectionMethod();
        try {
            web3jClient = Web3jManager.getInstance().getWeb3jClient();
            web3jCredentials = Web3jManager.getInstance().getCredentials();
        } catch (Exception e) {
            e.printStackTrace();
        }
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