package de.tum.repairchain;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import de.tum.repairchain.ipfs.AddIPFSContent;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.tum.repairchain.ipfs.IPFSDaemon;
import de.tum.repairchain.ipfs.IPFSDaemonService;
import de.tum.repairchain.ipfs.State;
import io.ipfs.kotlin.IPFS;
import io.ipfs.kotlin.model.BandWidthInfo;
import io.ipfs.kotlin.model.VersionInfo;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

import static de.tum.repairchain.Constants.*;
import static de.tum.repairchain.Helpers.*;

/**
 * Upload Image
 *
 *
 *
 */
public class UploadImage extends AppCompatActivity {

    private IPFSDaemon ipfsDaemon = new IPFSDaemon(this);
    private boolean running = false;
    private String hashString;
    // Contains the actual image file
    private File imageFile;
    private Uri photoURI;
    // Contains the directory of the file. Don't know why it is a File type
    private File outputDir;

    @BindView(R.id.btn_addFile)
    Button addFile;
    @BindView(R.id.img_photo_taken)
    ImageView photoTaken;

    @OnClick ({R.id.btn_addFile})
    public void clickAddFileButton(Button btn) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        startActivityForResult(cameraIntent, PHOTO_REQUEST);
    }

    @OnClick ({R.id.btn_done})
    public void clickDoneButton(Button btn) {
        imageFile.delete();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);
        ButterKnife.bind(this);

        outputDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        Log.i("UploadImage", "activity has been called");

        if (!ipfsDaemon.isReady()) {
            ipfsDaemon.download(this, new Function0<Unit>() {
                @Override
                public Unit invoke() {
                    Log.d("Done", "Done");
                    return null;
                }
            });
        } else {
            if (!State.isDaemonRunning) {
                startService();
            }
        }

        try {
            imageFile = File.createTempFile("tempImage", ".png", outputDir);
            photoURI = Uri.fromFile(imageFile);
        } catch (Exception e) {
            Log.e("UploadImage", "file creation failed", e);
        }
    }

    private void startService() {
        startService(new Intent(this, IPFSDaemonService.class));
        State.isDaemonRunning = true;

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("starting daemon");
        progressDialog.show();


        new Thread(new Runnable() {
            @Override
            public void run() {
                VersionInfo version = null;
                while (version == null) {
                    try {
                        Thread.sleep(1000);
                        version = new IPFS().getInfo().version();
                    } catch (Exception e) {
                        Log.d("Exception IPFS", "IPFS daemon is not running.");
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        startInfoRefresh();
                    }
                });
            }
        }).start();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("UploadImage", "onactivityresult called");
        if (resultCode == RETURN_IMAGE_HASH) {
            String fileHash = data.getStringExtra(RETURN_IMAGE_URL);
            Log.i("UploadImage", "Successfully returned the image url: " + fileHash);
            Intent returnToReport = new Intent();
            returnToReport.setAction(RETURN_HASH);
            returnToReport.putExtra(IPFS_UPLOAD_DONE, fileHash);
            setResult(IMAGE_ADDED, returnToReport);
            new DownloadImageTask(UploadImage.this, photoTaken).execute(fileHash);
        } else if (requestCode == PHOTO_REQUEST){
                Intent addIpfsIntent = new Intent(getApplicationContext(), AddIPFSContent.class);
                addIpfsIntent.setAction(Intent.ACTION_SEND);
                addIpfsIntent.setData(photoURI);
                startActivityForResult(addIpfsIntent, JUST_SOME_CODE);
        }
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
