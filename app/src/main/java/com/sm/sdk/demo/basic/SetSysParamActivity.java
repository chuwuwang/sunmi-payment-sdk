package com.sm.sdk.demo.basic;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;

public class SetSysParamActivity extends BaseAppCompatActivity {
    private EditText mEditKey;
    private EditText mEditValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_set_sys_param);
        initToolbarBringBack(R.string.basic_set_sys_param);
        initView();
    }

    private void initView() {
        mEditKey = findViewById(R.id.edit_key);
        mEditValue = findViewById(R.id.edit_value);
        findViewById(R.id.mb_ok).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.mb_ok:
                setSysParam();
                break;
        }
    }

    private void setSysParam() {
        try {
            String name = mEditKey.getText().toString();
            String value = mEditValue.getText().toString();
            if (name.trim().isEmpty()) {
                showToast(R.string.basic_sys_key_hint);
                return;
            }
            addStartTimeWithClear("setSysParam()");
            int result = MyApplication.app.basicOptV2.setSysParam(name, value);
            addEndTime("setSysParam()");
            toastHint(result);
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
