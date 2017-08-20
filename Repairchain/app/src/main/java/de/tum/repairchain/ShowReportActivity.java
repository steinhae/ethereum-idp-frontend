package de.tum.repairchain;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes20;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import butterknife.BindView;
import butterknife.OnClick;
import de.tum.repairchain.contracts.Report_sol_Repairchain;

import static de.tum.repairchain.Constants.*;
import static de.tum.repairchain.Helpers.*;

public class ShowReportActivity extends AppCompatActivity {

    private String reportId;
    private Report report;
    private boolean isFix;
    private String imageHash;

    @BindView(R.id.txt_report_title)
    TextView reportTitle;
    @BindView(R.id.txt_report_description)
    TextView reportDescription;
    @BindView(R.id.img_photo_taken)
    ImageView reportPhoto;
    @BindView(R.id.txt_confirmations)
    TextView reportConfirmations;
    @BindView(R.id.txt_time)
    TextView reportTime;
    @BindView(R.id.btn_confirm)
    Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_report);
        Bundle extras = getIntent().getExtras();
        reportId = extras.get(REPORT_ID).toString();
        report = new Report(CITY, reportId);
        isFix = report.getFixedFlag();
        imageHash = report.getPictureHash();

        reportDescription.setText(report.getDescription());
        if (isFix) {
            reportTitle.setText(R.string.fix);
            reportConfirmations.setText(report.getFixConfirmationCount());
            if (report.getEnoughFixConfirmations())
                confirmButton.setEnabled(false);
        } else {
            reportConfirmations.setText(report.getConfirmationCount());
            if (report.getEnouoghConfirmationsFlag())
                confirmButton.setEnabled(false);
        }
        reportTime.setText(report.getCreationDate().toString());

        new DownloadImageTask(ShowReportActivity.this, reportPhoto).execute(imageHash);
    }

    @OnClick({R.id.btn_confirm})
    public void confirmButtonClicked (Button btn) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Submitting confirmation...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Report_sol_Repairchain repairchain = Web3jManager.getInstance().getRepairchain();
                try {
                    final TransactionReceipt transactionReceipt;
                    if (isFix)
                        transactionReceipt = repairchain.addFixConfirmationToReport(
                                new Utf8String(CITY),
                                new Bytes20(reportId.getBytes())).get();
                    else
                        transactionReceipt = repairchain.addConfirmationToReport(
                                new Utf8String(CITY),
                                new Bytes20(reportId.getBytes())).get();
                    if (transactionReceipt != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Confirmation successfully submitted, txHash = " + transactionReceipt.getTransactionHash(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Error while submitting confirmation.", Toast.LENGTH_LONG).show();
                        }
                    });
                    e.printStackTrace();
                } finally {
                    progressDialog.dismiss();
                }
            }
        }).start();
        finish();
    }
}
