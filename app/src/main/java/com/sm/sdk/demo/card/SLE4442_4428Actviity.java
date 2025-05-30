package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sm.sdk.demo.wrapper.CheckCardCallbackV2Wrapper;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.Arrays;

public class SLE4442_4428Actviity extends BaseAppCompatActivity {
    private EditText edtAuthKey;
    private EditText edtChgOldKey;
    private EditText edtChgNewKey;
    private EditText edtReadStartAddr;
    private EditText edtReadLen;
    private EditText edtReadResult;
    private EditText edtWriteKey;
    private EditText edtWriteStartAddr;
    private EditText edtWriteData;
    private TextView txtRemainAuthCount;
    private EditText edtWriteProtectKey;
    private EditText edtWriteProtectStartAddr;
    private EditText edtWriteProtectLen;
    private EditText edtReadProtectStartAddr;
    private EditText edtReadProtectLen;
    private TextView txtReadProtectResult;

    private int cardType = AidlConstantsV2.CardType.SLE4442.getValue();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_sle4442_4428);
        initView();
        checkCard();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_sle4442_4428);
        edtAuthKey = findViewById(R.id.edt_auth_key);
        edtChgOldKey = findViewById(R.id.edt_chg_key_old_key);
        edtChgNewKey = findViewById(R.id.edt_chg_key_new_key);
        edtReadStartAddr = findViewById(R.id.edt_read_data_start_address);
        edtReadLen = findViewById(R.id.edt_read_data_len);
        edtReadResult = findViewById(R.id.edt_read_data_result);
        edtWriteKey = findViewById(R.id.edt_write_key);
        edtWriteStartAddr = findViewById(R.id.edt_write_start_address);
        edtWriteData = findViewById(R.id.edt_write_data);
        txtRemainAuthCount = findViewById(R.id.txt_remain_count);
        edtWriteProtectKey = findViewById(R.id.edt_write_protect_key);
        edtWriteProtectStartAddr = findViewById(R.id.edt_write_protect_start_address);
        edtWriteProtectLen = findViewById(R.id.edt_write_protect_len);
        edtReadProtectStartAddr = findViewById(R.id.edt_read_protect_start_address);
        edtReadProtectLen = findViewById(R.id.edt_read_protect_len);
        txtReadProtectResult = findViewById(R.id.txt_read_protect_result);

        findViewById(R.id.mb_auth).setOnClickListener(this);
        findViewById(R.id.mb_chg_key).setOnClickListener(this);
        findViewById(R.id.mb_read_data).setOnClickListener(this);
        findViewById(R.id.mb_write_data).setOnClickListener(this);
        findViewById(R.id.mb_read_remain_count).setOnClickListener(this);
        findViewById(R.id.mb_write_protect).setOnClickListener(this);
        findViewById(R.id.mb_read_protect).setOnClickListener(this);
        RadioGroup group = findViewById(R.id.rdo_group_card_type);
        group.setOnCheckedChangeListener((group1, checkedId) -> {
            if (checkedId == R.id.rdo_sle4442) {
                cardType = AidlConstantsV2.CardType.SLE4442.getValue();
                checkCard();
            } else if (checkedId == R.id.rdo_sle4428) {
                cardType = AidlConstantsV2.CardType.SLE4428.getValue();
                checkCard();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_auth:
                sleAuthKey(edtAuthKey);
                break;
            case R.id.mb_chg_key:
                sleChangeKey();
                break;
            case R.id.mb_read_data:
                sleReadData();
                break;
            case R.id.mb_write_data:
                sleWriteData();
                break;
            case R.id.mb_read_remain_count:
                sleGetRemainAuthCount();
                break;
            case R.id.mb_write_protect:
                sleWriteProtectionMemory();
                break;
            case R.id.mb_read_protect:
                sleReadProtectMemory();
                break;
        }
    }

    private void checkCard() {
        try {
            showSwingCardHintDialog(1);
            // card type can be SLE4442/SLE428
            addStartTimeWithClear("checkCard()");
            MyApplication.app.readCardOptV2.checkCard(cardType, mCheckCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {

        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findMagCard");
            dismissSwingCardHintDialog();
            showSpendTime();
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findICCard:" + atr);
            dismissSwingCardHintDialog();
            showSpendTime();
        }

        @Override
        public void findRFCard(String uuid) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findRFCard:" + uuid);
            dismissSwingCardHintDialog();
            showSpendTime();
        }

        @Override
        public void onError(int code, String message) throws RemoteException {
            addEndTime("checkCard()");
            showSpendTime();
            dismissSwingCardHintDialog();
            String tip = "check card failed, code:" + code + ",msg:" + message;
            LogUtil.e(TAG, tip);
            showToast(tip);
        }
    };

    /** SLE4442/4428 verify password */
    private boolean sleAuthKey(EditText edtKey) {
        try {
            if (!checkInputKey(edtKey)) {
                return false;
            }
            String key = edtKey.getText().toString();
            byte[] keyBytes = ByteUtil.hexStr2Bytes(key);
            addStartTimeWithClear("sleAuthKey()");
            int code = MyApplication.app.readCardOptV2.sleAuthKey(keyBytes);
            addEndTime("sleAuthKey()");
            if (code != 0) {
                showToast(" sleAuthKey failed");
                LogUtil.e(Constant.TAG, "sleAuthKey failed,code:" + code);
                showSpendTime();
                return false;
            }
            showToast(" sleAuthKey success");
            LogUtil.e(Constant.TAG, "sleAuthKey success");
            showSpendTime();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** SLE4442/4428 read data */
    private boolean sleChangeKey() {
        try {
            if (!checkInputKey(edtChgOldKey) || !checkInputKey(edtChgNewKey)) {
                return false;
            }
            if (!sleAuthKey(edtChgOldKey)) {
                LogUtil.e(Constant.TAG, "sleChangeKey failed");
                showToast("sleChangeKey failed");
                return false;
            }
            String newKey = edtChgNewKey.getText().toString();
            byte[] newKeyBytes = ByteUtil.hexStr2Bytes(newKey);
            addStartTimeWithClear("sleChangeKey()");
            int code = MyApplication.app.readCardOptV2.sleChangeKey(newKeyBytes);
            addEndTime("sleChangeKey()");
            if (code != 0) {
                showToast(" sleChangeKey failed");
                LogUtil.e(Constant.TAG, "sleChangeKey failed,code:" + code);
                showSpendTime();
                return false;
            }
            showToast(" sleChangeKey success");
            LogUtil.e(Constant.TAG, "sleChangeKey success");
            showSpendTime();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** SLE4442/4428 change password */
    private boolean sleReadData() {
        try {
            if (!checkInputStartAddress(edtReadStartAddr) || !checkInputLength(edtReadLen)) {
                return false;
            }
            String startAddress = edtReadStartAddr.getText().toString();
            String length = edtReadLen.getText().toString();
            int startAddr = Integer.parseInt(startAddress, 16);
            int len = Integer.parseInt(length);
            byte[] out = new byte[2048];
            addStartTimeWithClear("sleReadData()");
            int retLen = MyApplication.app.readCardOptV2.sleReadData(startAddr, len, out);
            addEndTime("sleReadData()");
            if (retLen < 0) {
                showToast(" sleReadData failed");
                LogUtil.e(Constant.TAG, "sleReadData failed,code:" + retLen);
                showSpendTime();
                return false;
            }
            String data = ByteUtil.bytes2HexStr(Arrays.copyOf(out, retLen));
            edtReadResult.setText(data);
            LogUtil.e(Constant.TAG, "sleReadData success, data:" + data);
            showSpendTime();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** SLE4442/4428 write data */
    private boolean sleWriteData() {
        try {
            if (!checkInputKey(edtWriteKey) || !checkInputStartAddress(edtWriteStartAddr)
                    || !checkInputData(edtWriteData)) {
                return false;
            }
            if (!sleAuthKey(edtWriteKey)) {
                LogUtil.e(Constant.TAG, "sleWriteData failed");
                showToast("sleWriteData error,auth key failed.");
                return false;
            }
            String startAddress = edtWriteStartAddr.getText().toString();
            String writeData = edtWriteData.getText().toString();
            int startAddr = Integer.parseInt(startAddress, 16);
            byte[] data = ByteUtil.hexStr2Bytes(writeData);
            addStartTimeWithClear("sleWriteData()");
            int code = MyApplication.app.readCardOptV2.sleWriteData(startAddr, data);
            addEndTime("sleWriteData()");
            if (code != 0) {
                showToast(" sleWriteData failed");
                LogUtil.e(Constant.TAG, "sleWriteData error,code:" + code);
                showSpendTime();
                return false;
            }
            showToast(" sleWriteData success");
            LogUtil.e(Constant.TAG, "sleWriteData success");
            showSpendTime();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** SLE4442/4428 get remain times of verify password */
    private boolean sleGetRemainAuthCount() {
        try {
            addStartTimeWithClear("sleGetRemainAuthCount()");
            int count = MyApplication.app.readCardOptV2.sleGetRemainAuthCount();
            addEndTime("sleGetRemainAuthCount()");
            if (count < 0) {
                showToast(" sleGetRemainAuthCount failed");
                LogUtil.e(Constant.TAG, "sleGetRemainAuthCount error,code:" + count);
                showSpendTime();
                return false;
            }
            String strRemain = getString(R.string.card_remain_count) + count;
            txtRemainAuthCount.setText(strRemain);
            LogUtil.e(Constant.TAG, "sleGetRemainAuthCount success:" + strRemain);
            showSpendTime();
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** SLE4442/4428 write memory protection bits */
    private boolean sleWriteProtectionMemory() {
        try {
            if (!checkInputKey(edtWriteProtectKey) || !checkInputStartAddress(edtWriteProtectStartAddr)
                    || !checkInputLength(edtWriteProtectLen)) {
                return false;
            }
            if (!sleAuthKey(edtWriteProtectKey)) {
                LogUtil.e(Constant.TAG, "sleWriteProtectionMemory failed");
                showToast("sleWriteProtectionMemory error,auth key failed.");
                return false;
            }
            String startAddress = edtWriteProtectStartAddr.getText().toString();
            String length = edtWriteProtectLen.getText().toString();
            int startAddr = Integer.parseInt(startAddress, 16);
            int len = Integer.parseInt(length);
            addStartTimeWithClear("sleWriteProtectionMemory()");
            int code = MyApplication.app.readCardOptV2.sleWriteProtectionMemory(startAddr, len);
            addEndTime("sleWriteProtectionMemory()");
            if (code != 0) {
                showToast("sleWriteProtectionMemory failed");
                LogUtil.e(Constant.TAG, "sleWriteProtectionMemory error,code:" + code);
                showSpendTime();
                return false;
            }
            showToast("sleWriteProtectionMemory success");
            LogUtil.e(Constant.TAG, "sleWriteProtectionMemory success");
            showSpendTime();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** SLE4442/4428 read memory protection status */
    private boolean sleReadProtectMemory() {
        try {
            if (!checkInputStartAddress(edtReadProtectStartAddr)
                    || !checkInputLength(edtReadProtectLen)) {
                return false;
            }
            String startAddress = edtReadProtectStartAddr.getText().toString();
            String length = edtReadProtectLen.getText().toString();
            int startAddr = Integer.parseInt(startAddress, 16);
            int len = Integer.parseInt(length);
            byte[] out = new byte[len];
            addStartTimeWithClear("sleReadMemoryProtectionStatus()");
            int retLen = MyApplication.app.readCardOptV2.sleReadMemoryProtectionStatus(startAddr, len, out);
            addEndTime("sleReadMemoryProtectionStatus()");
            if (retLen < 0) {
                showToast("sleReadProtectMemory failed");
                LogUtil.e(Constant.TAG, "sleReadProtectMemory error,code:" + retLen);
                showSpendTime();
                return false;
            }
            byte[] valid = Arrays.copyOf(out, retLen);
            StringBuilder strStatus = new StringBuilder("Protection status:");
            for (byte b : valid) {
                strStatus.append(b & 0xff);
            }
            txtReadProtectResult.setText(strStatus);
            showSpendTime();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Check input password */
    private boolean checkInputKey(EditText etdKey) {
        String key = etdKey.getText().toString();
        if (TextUtils.isEmpty(key) || !Utility.checkHexValue(key) || key.length() < 4 || key.length() > 6) {
            showToast("key should be 4~6 hex characters");
            etdKey.requestFocus();
            return false;
        }
        return true;
    }

    /** Check input start address */
    private boolean checkInputStartAddress(EditText edtStartAddress) {
        String startAddress = edtStartAddress.getText().toString();
        if (TextUtils.isEmpty(startAddress) || !Utility.checkHexValue(startAddress)) {
            showToast("startAddress should be 1~3 hex characters");
            edtStartAddress.requestFocus();
            return false;
        }
        int address = Integer.parseInt(startAddress, 16);
        if (address < 0 || address > 0x3FF) {
            showToast("startAddress should be in [000~3FF]");
            edtStartAddress.requestFocus();
            return false;
        }
        return true;
    }

    /** Check input length */
    private boolean checkInputLength(EditText edtLength) {
        String length = edtLength.getText().toString();
        if (TextUtils.isEmpty(length)) {
            showToast("startAddress should in [0~1024]");
            edtLength.requestFocus();
            return false;
        }
        int len = Integer.parseInt(length);
        if (len < 0 || len > 1024) {
            showToast("startAddress should  in [0~1024]");
            edtLength.requestFocus();
            return false;
        }
        return true;
    }

    /** Check input data */
    private boolean checkInputData(EditText edtData) {
        String data = edtData.getText().toString();
        if (TextUtils.isEmpty(data) || !Utility.checkHexValue(data)) {
            showToast("input data should be hex characters");
            edtData.requestFocus();
            return false;
        }
        if (data.length() % 2 != 0) {
            showToast("illegal input data length");
            edtData.requestFocus();
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCheckCard();
    }

    private void cancelCheckCard() {
        try {
            MyApplication.app.readCardOptV2.cancelCheckCard();
            MyApplication.app.readCardOptV2.cardOff(cardType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
