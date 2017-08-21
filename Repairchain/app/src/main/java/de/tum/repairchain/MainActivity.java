package de.tum.repairchain;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @OnClick({ R.id.btn_report })
    public void clickReportButton(ImageButton btn) {
        Intent intent = new Intent(getApplicationContext(), ReportActivity.class);
        startActivity(intent);
    }

    @OnClick({ R.id.btn_map_activity })
    public void clickBalanceButton(ImageButton btn) {
        Intent intent = new Intent(getApplicationContext(), ReportsMapActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }
}