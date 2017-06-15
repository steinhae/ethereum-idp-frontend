package de.tum.repairchain;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import org.ethereum.geth.*;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private long lastUpdate = 0;
    private TextView textbox;

    NewHeadHandler handler = new NewHeadHandler() {
        @Override public void onError(String error) { }
        @Override public void onNewHead(final Header header) {
            long now = System.currentTimeMillis();
            if (now - lastUpdate >= 1000) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        String txt = "#" + header.getNumber() + ": " + header.getHash().getHex().substring(0, 10) + "…\n";
                        textbox.append(txt);
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

        setTitle("Android In-Process Node");
        textbox = (TextView) findViewById(R.id.textView);
        textbox.setMovementMethod(new ScrollingMovementMethod());

        Context ctx = new Context();

        try {

            NodeConfig nc = new NodeConfig();
            nc.setEthereumNetworkID(3);
            nc.setWhisperEnabled(true);
            nc.setEthereumEnabled(true);
            String genesis = Geth.testnetGenesis();
            nc.setEthereumGenesis(genesis);
            Node node = Geth.newNode(getFilesDir() + "/.ethereum", nc);
            node.start();

            NodeInfo info = node.getNodeInfo();
            textbox.append("My name: " + info.getName() + "\n");
            textbox.append("My address: " + info.getListenerAddress() + "\n");
            textbox.append("My protocols: " + info.getProtocols() + "\n\n");

            EthereumClient ec = node.getEthereumClient();
            ec.subscribeNewHead(ctx, handler,  16);
            textbox.append("Latest block: " + ec.getBlockByNumber(ctx, -1).getNumber() + ", syncing...\n");

            lastUpdate = System.currentTimeMillis();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}