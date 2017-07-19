package de.tum.repairchain;

import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import java.net.ConnectException;

import butterknife.ButterKnife;
import butterknife.OnClick;
import de.tum.repairchain.ipfs.IPFSDaemon;
import de.tum.repairchain.ipfs.IPFSDaemonService;
import de.tum.repairchain.ipfs.State;
import io.ipfs.kotlin.model.BandWidthInfo;
import io.ipfs.kotlin.model.VersionInfo;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import io.ipfs.kotlin.IPFS;

public class UploadImage extends AppCompatActivity {

    private IPFSDaemon ipfsDaemon = new IPFSDaemon(this);
    private boolean running = false;

    @OnClick ({R.id.btn_addFile})
    public void clickAddFileButton(Button btn) {
        startInfoRefresh();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);
        ButterKnife.bind(this);

        if (!ipfsDaemon.isReady()) {
            ipfsDaemon.download(this, new Function0<Unit>() {
                @Override
                public Unit invoke() {
                    Log.d("Done", "Done");
                    return null;
                }
            });
        }
        startService(new Intent(this, IPFSDaemonService.class));
        State.isDaemonRunning = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        startInfoRefresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        running = false;
    }

    private void startInfoRefresh() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                running = true;
                while (running) {
                    try {
                        Thread.sleep(2000);
                        //VersionInfo version = ipfs.getInfo().version();
                        BandWidthInfo bandWidth = new IPFS().getStats().bandWidth();
                        String bandWidthText;
                        if (bandWidth != null) {
                            bandWidthText = "TotlalIn:" + bandWidth.getTotalIn() + "\n" +
                                    "TotalOut:" + bandWidth.getTotalOut() + "\n" +
                                    "RateIn:" + bandWidth.getRateIn() + "\n" +
                                    "RateOut:" + bandWidth.getRateOut();
                        } else {
                            bandWidthText = " could not get information";
                        }
                        Log.d("BandWidth", bandWidthText);
                    } catch (Exception e){
                        Log.d("IPFS service error", "IPFS service is not started.");
                    }
                }
            }
        }).start();
    }
}
