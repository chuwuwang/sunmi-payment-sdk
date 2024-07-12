package com.sm.sdk.demo.basic;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.DeviceUtil;
import com.sm.sdk.demo.utils.SystemPropertiesUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;

import java.util.ArrayList;
import java.util.List;

public class GetSysParamActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_get_sys_param);
        initToolbarBringBack(R.string.basic_get_sys_param);
        initView();
    }

    private void initView() {
        TextView tvInfo = findViewById(R.id.tv_info);
        List<String> keys = new ArrayList<>();
        keys.add(AidlConstantsV2.SysParam.BASE_VERSION);
        keys.add(AidlConstantsV2.SysParam.MSR2_FW_VER);
        keys.add(AidlConstantsV2.SysParam.TERM_STATUS);
        keys.add(AidlConstantsV2.SysParam.DEBUG_MODE);
        keys.add(AidlConstantsV2.SysParam.HARDWARE_VERSION);
        keys.add(AidlConstantsV2.SysParam.FIRMWARE_VERSION);
        keys.add(AidlConstantsV2.SysParam.SM_VERSION);
        keys.add(AidlConstantsV2.SysParam.SUPPORT_ETC);
        keys.add(AidlConstantsV2.SysParam.ETC_FIRM_VERSION);
        keys.add(AidlConstantsV2.SysParam.BootVersion);
        keys.add(AidlConstantsV2.SysParam.CFG_FILE_VERSION);
        keys.add(AidlConstantsV2.SysParam.FW_VERSION);
        keys.add(AidlConstantsV2.SysParam.SN);
        keys.add(AidlConstantsV2.SysParam.PN);
        keys.add(AidlConstantsV2.SysParam.TUSN);
        keys.add(AidlConstantsV2.SysParam.DEVICE_CODE);
        keys.add(AidlConstantsV2.SysParam.DEVICE_MODEL);
        keys.add(AidlConstantsV2.SysParam.RESERVED);
        keys.add(AidlConstantsV2.SysParam.PCD_PARAM_A);
        keys.add(AidlConstantsV2.SysParam.PCD_PARAM_B);
        keys.add(AidlConstantsV2.SysParam.PCD_PARAM_C);
        keys.add(AidlConstantsV2.SysParam.TUSN_KEY_KCV);
        keys.add(AidlConstantsV2.SysParam.PCD_IFM_VERSION);
        keys.add(AidlConstantsV2.SysParam.SAM_COUNT);
        keys.add(AidlConstantsV2.SysParam.SM_TYPE);
        keys.add(AidlConstantsV2.SysParam.PUSH_CFG_FILE);
        keys.add(AidlConstantsV2.SysParam.EMV_VERSION);
        keys.add(AidlConstantsV2.SysParam.PAYPASS_VERSION);
        keys.add(AidlConstantsV2.SysParam.PAYWAVE_VERSION);
        keys.add(AidlConstantsV2.SysParam.QPBOC_VERSION);
        keys.add(AidlConstantsV2.SysParam.ENTRY_VERSION);
        keys.add(AidlConstantsV2.SysParam.MIR_VERSION);
        keys.add(AidlConstantsV2.SysParam.JCB_VERSION);
        keys.add(AidlConstantsV2.SysParam.PAGO_VERSION);
        keys.add(AidlConstantsV2.SysParam.PURE_VERSION);
        keys.add(AidlConstantsV2.SysParam.AE_VERSION);
        keys.add(AidlConstantsV2.SysParam.FLASH_VERSION);
        keys.add(AidlConstantsV2.SysParam.DPAS_VERSION);
        keys.add(AidlConstantsV2.SysParam.APEMV_VERSION);
        keys.add(AidlConstantsV2.SysParam.EFTPOS_VERSION);
        keys.add(AidlConstantsV2.SysParam.RUPAY_VERSION);
        keys.add(AidlConstantsV2.SysParam.EMV_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.PAYPASS_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.PAYWAVE_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.QPBOC_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.ENTRY_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.MIR_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.JCB_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.PAGO_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.PURE_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.AE_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.FLASH_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.DPAS_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.EFTPOS_RELEASE_DATE);
        keys.add(AidlConstantsV2.SysParam.RUPAY_RELEASE_DATE);
        StringBuilder sb = new StringBuilder();
        appendSecStatus(sb);//获取安装状态
        addStartTime("getSysParam() total");
        for (String key : keys) {
            if (!key.contains("ReleaseDate")) {
                sb.append(getLiteralKey(key));
                sb.append(":");
            }
            String value = null;
            try {
                addStartTime("getSysParam() key=" + key);
                value = MyApplication.app.basicOptV2.getSysParam(key);
                addEndTime("getSysParam() key=" + key);
            } catch (Exception e) {
                e.printStackTrace();
            }
            sb.append(getLiteralValue(key, value));
            sb.append("\n");
        }
        addEndTime("getSysParam() total");
        tvInfo.setText(sb);
        showSpendTime();
    }

    /** 获取触发状态 */
    private void appendSecStatus(StringBuilder sb) {
        sb.append("SecStatus:");
        try {
            String model = getModel();
            if (model.matches("p.+") || DeviceUtil.isP2SmartPad()
                    || DeviceUtil.isFT2() || DeviceUtil.isFT2Mini()
                    || DeviceUtil.isV2SE()) {//金融设备存在触发上报机制
                addStartTime("getSecStatus()");
                int status = MyApplication.app.securityOptV2.getSecStatus();
                addEndTime("getSecStatus()");
                sb.append(Utility.formatStr("%08X", status));
            } else {
                sb.append("null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        sb.append("\n");
    }

    /** 获取显示的key */
    private String getLiteralKey(String key) {
        if (AidlConstants.SysParam.SAM_COUNT.equals(key)) {
            return "SAM count";
        }
        return key;
    }

    /** 获取显示的value */
    private String getLiteralValue(String key, String value) {
        return Utility.null2String(value);
    }

    /** 获取model */
    private String getModel() {
        String model = SystemPropertiesUtil.get("ro.sunmi.hardware");
        if (TextUtils.isEmpty(model)) {
            model = Build.MODEL;
        }
        if (TextUtils.isEmpty(model)) {
            model = Build.UNKNOWN;
        }
        return model.toLowerCase();
    }


}
