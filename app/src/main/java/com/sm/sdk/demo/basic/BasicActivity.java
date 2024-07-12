package com.sm.sdk.demo.basic;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.R;

public class BasicActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic);
        initToolbarBringBack(R.string.basic);
        initView();
    }

    private void initView() {
        View item = findViewById(R.id.get_sys_param);
        TextView leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_get_sys_param);
        item.setOnClickListener(this);

        item = findViewById(R.id.set_sys_param);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_set_sys_param);
        item.setOnClickListener(this);

        item = findViewById(R.id.buzzer);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_buzzer);
        item.setOnClickListener(this);

        item = findViewById(R.id.led);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_led);
        item.setOnClickListener(this);

        item = findViewById(R.id.screen_mode);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_screen_mode);
        item.setOnClickListener(this);

        item = findViewById(R.id.emv_callback_time);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_emv_callback_timeout_time);
        item.setOnClickListener(this);

        item = findViewById(R.id.pin_anti_exhaustive_mode);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.pin_anti_exhaustive_mode);
        item.setOnClickListener(this);

        item = findViewById(R.id.sys_set_wakeup);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_sys_set_wakeup);
        item.setOnClickListener(this);

        item = findViewById(R.id.kb_beep_mode);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_kb_beep_mode);
        item.setOnClickListener(this);

        item = findViewById(R.id.shard_lib);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.basic_shared_lib_test);
        item.setOnClickListener(this);

        item = findViewById(R.id.data_transmission);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.test_transmission_stress_test);
        item.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.get_sys_param:
                openActivity(GetSysParamActivity.class);
                break;
            case R.id.set_sys_param:
                openActivity(SetSysParamActivity.class);
                break;
            case R.id.buzzer:
                openActivity(BuzzerActivity.class);
                break;
            case R.id.led:
                openActivity(LedActivity.class);
                break;
            case R.id.screen_mode:
                openActivity(ScreenModelActivity.class);
                break;
            case R.id.emv_callback_time:
                openActivity(EMVCallbackTimeActivity.class);
                break;
            case R.id.pin_anti_exhaustive_mode:
                openActivity(PinAntiExhaustiveMode.class);
                break;
            case R.id.sys_set_wakeup:
                openActivity(SysSetWakeupActivity.class);
                break;
            case R.id.kb_beep_mode:
                openActivity(KBBeepModeActivity.class);
                break;
            case R.id.shard_lib:
                openActivity(SharedLibActivity.class);
                break;
            case R.id.data_transmission:
                openActivity(TransmissionTestActivity.class);
                break;
        }
    }


}
