package com.sm.sdk.demo.scan;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.R;

import java.util.ArrayList;
import java.util.HashMap;

public class ScanActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        initToolbarBringBack(R.string.scan);
        initView();
    }

    private void initView() {

        View item = findViewById(R.id.barcode_scanner_1d);
        TextView leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.scan_barcode_scanner_1d);
        item.setOnClickListener(this);

        item = findViewById(R.id.camera_scanner);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.scan_camera_scanner);
        item.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.barcode_scanner_1d:
                openActivity(BarcodeActivity.class);

                break;
            case R.id.camera_scanner:
                startScan();

                break;
        }
    }

    private void startScan() {
        Intent intent = new Intent();
        if (hasScanner()) {
            intent.setAction("com.sunmi.scanner.qrscanner");
            intent.setPackage("com.sunmi.scanner");
        } else {
            intent.setAction("com.summi.scan");
            intent.setPackage("com.sunmi.sunmiqrcodescanner");
        }
        intent.putExtra("IS_SHOW_SETTING", false);      // whether to display the setting button, default true
        intent.putExtra("IDENTIFY_MORE_CODE", true);    // identify multiple qr code in the screen
        intent.putExtra("IS_AZTEC_ENABLE", true);       // allow read of AZTEC code
        intent.putExtra("IS_PDF417_ENABLE", true);      // allow read of PDF417 code
        intent.putExtra("IS_DATA_MATRIX_ENABLE", true); // allow read of DataMatrix code
        PackageManager packageManager = getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, 100);
        } else {
            showToast(R.string.error_scan);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && data != null) {
            Bundle bundle = data.getExtras();
            @SuppressWarnings("unchecked")
            ArrayList<HashMap<String, String>> result = (ArrayList<HashMap<String, String>>) bundle.getSerializable("data");
            if (result != null && result.size() > 0) {
                String type = result.get(0).get("TYPE");
                String value = result.get(0).get("VALUE");
                Intent intent = new Intent(this, ScanResultActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("value", value);
                startActivity(intent);
            } else {
                showToast("Scan Failed");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private boolean hasScanner() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.sunmi.scanner", 0);
            return info != null && compareVer(info.versionName, "4.4.4", true, 3);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean compareVer(String nVer, String oVer, boolean isEq, int bit) {
        if (nVer.isEmpty() || oVer.isEmpty()) return false;
        String[] nArr = nVer.split("[.]");
        String[] oArr = oVer.split("[.]");
        if (nArr.length < bit || oArr.length < bit) return false;
        boolean vup = false;
        for (int i = 0; i < bit; i++) {
            int n = Integer.parseInt(nArr[i]);
            int o = Integer.parseInt(oArr[i]);
            if (n >= o) {
                if (n > o) {
                    vup = true;
                    break;
                } else if (isEq && i == (bit - 1)) {
                    vup = true;
                    break;
                }
            } else {
                break;
            }
        }
        return vup;
    }
}