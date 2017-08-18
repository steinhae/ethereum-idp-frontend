package de.tum.repairchain;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import de.tum.repairchain.ipfs.AddIPFSContent;
import de.tum.repairchain.ipfs.App;
import de.tum.repairchain.ipfs.InputStreamProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

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
import okio.Okio;

import static de.tum.repairchain.Constants.*;

public class UploadImage extends AppCompatActivity {

    private IPFSDaemon ipfsDaemon = new IPFSDaemon(this);
    private boolean running = false;
    private String hashString;
    private File imageFile;

    @BindView(R.id.btn_addFile)
    Button addFile;
    @BindView(R.id.img_photo_taken)
    ImageView photoTaken;

    @OnClick ({R.id.btn_addFile})
    public void clickAddFileButton(Button btn) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, PHOTO_REQUEST);
    }

    @OnClick ({R.id.btn_done})
    public void clickDoneButton(Button btn) {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);
        ButterKnife.bind(this);

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
        File outputDir = getApplicationContext().getCacheDir();
        if (resultCode == RETURN_URL) {
            String fileUrl = data.getStringExtra(RETURN_IMAGE_URL);
            Log.i("UploadImage", "Successfully returned the image url: " + fileUrl);
            Intent returnToReport = new Intent();
            returnToReport.setAction(RETURN_HASH);
            returnToReport.putExtra(IPFS_UPLOAD_DONE, fileUrl);
            setResult(IMAGE_ADDED, returnToReport);
            new DownloadImageTask(photoTaken).execute(fileUrl);
        } else if (requestCode == PHOTO_REQUEST){
            try {
                imageFile = File.createTempFile("tempImage", "png", outputDir);
                FileOutputStream fos = new FileOutputStream(imageFile);
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                Intent addIpfsIntent = new Intent(getApplicationContext(), AddIPFSContent.class);
                addIpfsIntent.setAction(Intent.ACTION_SEND);
                addIpfsIntent.setData(android.net.Uri.parse(imageFile.toURI().toString()));
                startActivityForResult(addIpfsIntent, JUST_SOME_CODE);
//                photoTaken.setImageBitmap(bitmap);
            } catch (NullPointerException nPE) {
                Toast.makeText(this, "No photo has been taken", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                final AlertDialog errDialog = new AlertDialog.Builder(this).create();
                errDialog.setTitle("Error!");
                errDialog.setMessage("Error: \"" + e + "\" has occured!");
                errDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        errDialog.cancel();
                    }
                });
                errDialog.show();
            }
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

    private class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {
        ImageView imageView;
        ProgressDialog progressDialog;
        public DownloadImageTask(ImageView imageView){
            this.imageView = imageView;
            progressDialog = new ProgressDialog(UploadImage.this);
            progressDialog.setTitle("Downloading image");
            progressDialog.setIndeterminate(true);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Bitmap doInBackground (String... urls){
            Log.i("DownloadImageTask", "started working");
            Bitmap result = null;
            String url = urls[0];
            try {
                InputStream in = new URL(url).openStream();
                result = BitmapFactory.decodeStream(in);
                in.close();
            } catch (Exception e) {
                Log.e("UploadImage", "Failed loading IPFS image. ", e);
                e.printStackTrace();
            }
            Log.i("DownloadImageTask", "finished working");
            return result;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(100);
            progressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            progressDialog.dismiss();
            Log.i("DownloadImageTask", "postExecute");
            imageView.setImageBitmap(result);
        }
    }
}
