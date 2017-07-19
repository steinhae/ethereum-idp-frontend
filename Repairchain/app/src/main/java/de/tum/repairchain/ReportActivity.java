package de.tum.repairchain;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReportActivity extends AppCompatActivity {

    @OnClick({ R.id.btn_upload })
    public void clickUploadButton(Button btn) {
        Intent intent = new Intent(getApplicationContext(), UploadImage.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        ButterKnife.bind(this);
    }
}
