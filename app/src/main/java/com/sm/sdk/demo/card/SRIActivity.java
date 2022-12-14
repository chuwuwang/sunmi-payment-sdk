package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.card.wrapper.CheckCardCallbackV2Wrapper;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.regex.Pattern;

public class SRIActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_sri_4k);
        initView();
        checkCard();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_sri_card_test);
        findViewById(R.id.mb_get_uid).setOnClickListener(this);
        findViewById(R.id.mb_read_block32).setOnClickListener(this);
        findViewById(R.id.mb_write_block32).setOnClickListener(this);
        findViewById(R.id.mb_protect_block).setOnClickListener(this);
        findViewById(R.id.mb_get_block_protection).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_get_uid:
                getUid();
                break;
            case R.id.mb_read_block32:
                readBlock32();
                break;
            case R.id.mb_write_block32:
                writeBlock32();
                break;
            case R.id.mb_protect_block:
                protectBlock();
                break;
            case R.id.mb_get_block_protection:
                getBlockProtection();
                break;

        }
    }

    private void checkCard() {
        try {
            showSwingCardHintDialog();
            addStartTimeWithClear("checkCard()");
            MyApplication.app.readCardOptV2.checkCard(AidlConstantsV2.CardType.SRI.getValue(), mCheckCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {

        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(TAG, "findMagCard");
            dismissSwingCardHintDialog();
            showSpendTime();
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(TAG, "findICCard:" + atr);
            dismissSwingCardHintDialog();
            showSpendTime();
        }

        @Override
        public void findRFCard(String uuid) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(TAG, "findRFCard:" + uuid);
            dismissSwingCardHintDialog();
            showSpendTime();
        }

        @Override
        public void onError(int code, String message) throws RemoteException {
            addEndTime("checkCard()");
            showSpendTime();
        }
    };

    /** Get uid and card type */
    private void getUid() {
        try {
            Bundle bundle = new Bundle();
            addStartTimeWithClear("sriGetUid()");
            int code = MyApplication.app.readCardOptV2.sriGetUid(bundle);
            addEndTime("sriGetUid()");
            Log.e(TAG, "sriGetUid() code:" + code);
            TextView tvResult = findViewById(R.id.tv_get_uid_result);
            if (code != 0) {
                tvResult.setText(null);
                showSpendTime();
                return;
            }
            String uid = bundle.getString("uid");
            String cardType = "Unknown";
            int kindOfCard = bundle.getInt("kindOfCard");
            switch (kindOfCard) {
                case 0:
                    cardType = "CARD_SR176";
                    break;
                case 1:
                    cardType = "CARD_SRIX4K";
                    break;
                case 2:
                    cardType = "CARD_SRIX512";
                    break;
                case 3:
                    cardType = "CARD_SRI512";
                    break;
                case 4:
                    cardType = "CARD_SRI4K";
                    break;
                case 5:
                    cardType = "CARD_SRT512";
                    break;
                case 0xFF:
                    cardType = "CARD_OTHER";
                    break;
            }
            StringBuilder sb = new StringBuilder("\nuid:");
            sb.append(uid);
            sb.append("\n");
            sb.append("cardType:");
            sb.append(cardType);
            tvResult.setText(sb);
            LogUtil.e(TAG, "sriGetUid() result:" + sb);
            showSpendTime();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** Read 4 bytes data */
    private void readBlock32() {
        try {
            EditText editText = findViewById(R.id.edt_read_block32_address);
            String addressStr = editText.getText().toString();
            if (TextUtils.isEmpty(addressStr) || !checkHexValue(addressStr)) {
                showToast("address should be hex characters");
                editText.requestFocus();
                return;
            }
            int address = Integer.parseInt(addressStr, 16);
            byte[] dataOut = new byte[4];
            addStartTimeWithClear("readBlock32()");
            int code = MyApplication.app.readCardOptV2.sriReadBlock32(address, dataOut);
            addEndTime("readBlock32()");
            Log.e(TAG, "readBlock32() code:" + code);
            if (code != 0) {
                showSpendTime();
                return;
            }
            TextView tvResult = findViewById(R.id.tv_read_block32_result);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < dataOut.length; i++) {
                sb.append("\nbyte");
                sb.append(i + 1);
                sb.append(":");
                sb.append(ByteUtil.bytes2HexStr(dataOut[i]));
            }
            tvResult.setText(sb);
            LogUtil.e(TAG, "readBlock32() result:" + sb);
            showSpendTime();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** Write 4 bytes data */
    private void writeBlock32() {
        try {
            EditText editText = findViewById(R.id.edt_write_block32_address);
            String addressStr = editText.getText().toString();
            if (TextUtils.isEmpty(addressStr) || !checkHexValue(addressStr)) {
                showToast("address should be hex characters");
                editText.requestFocus();
                return;
            }
            editText = findViewById(R.id.edt_write_block32_data);
            String dataInStr = editText.getText().toString();
            if (TextUtils.isEmpty(dataInStr) || !checkHexValue(dataInStr) || dataInStr.length() != 8) {
                showToast("Data to be written should be 8 hex characters");
                editText.requestFocus();
                return;
            }
            int address = Integer.parseInt(addressStr, 16);
            byte[] dataIn = ByteUtil.hexStr2Bytes(dataInStr);
            addStartTimeWithClear("writeBlock32()");
            int code = MyApplication.app.readCardOptV2.sriWriteBlock32(address, dataIn);
            addEndTime("writeBlock32()");
            Log.e(TAG, "writeBlock32() code:" + code);
            showToast("writeBlock32 " + (code == 0 ? "success" : "failed"));
            showSpendTime();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write block's protection bits
     * <br/>对于SRI4K卡，nLockReg值含义如下:
     * <br/>bit0对应blocks7和blocks8
     * <br/>bit1到b7依次对应block9到block15
     * <br/>bit为0表示对应的block配置了写保护，不能做写操作；bit为1表示未做写保护
     */
    private void protectBlock() {
        try {
            EditText editText = findViewById(R.id.edt_protect_block_nlockReg);
            String nLockRegStr = editText.getText().toString();
            if (TextUtils.isEmpty(nLockRegStr) || !checkHexValue(nLockRegStr)) {
                showToast("nLockReg should be 2 hex characters");
                editText.requestFocus();
                return;
            }
            int nLockReg = Integer.parseInt(nLockRegStr, 16);
            addStartTimeWithClear("protectBlock()");
            int code = MyApplication.app.readCardOptV2.sriProtectBlock((byte) nLockReg);
            addEndTime("protectBlock()");
            Log.e(TAG, "protectBlock() code:" + code);
            showToast("protectBlock " + (code == 0 ? "success" : "failed"));
            showSpendTime();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get block protection bits
     * <br/>对于SRI4K卡，nLockReg值含义如下:
     * <br/>bit0对应blocks7和blocks8
     * <br/>bit1到b7依次对应block9到block15
     * <br/>bit为0表示对应的block配置了写保护，不能做写操作；bit为1表示未做写保护
     */
    private void getBlockProtection() {
        try {
            byte[] dataOut = new byte[1];
            addStartTimeWithClear("getBlockProtection()");
            int code = MyApplication.app.readCardOptV2.sriGetBlockProtection(dataOut);
            addEndTime("getBlockProtection()");
            Log.e(TAG, "getBlockProtection() code:" + code);
            TextView tvResult = findViewById(R.id.tv_block_protection_result);
            if (code != 0) {
                tvResult.setText(null);
                showSpendTime();
                return;
            }
            byte nLockReg = dataOut[0];
            StringBuilder sb = new StringBuilder("nLockReg:\n");
            sb.append(ByteUtil.bytes2HexStr(nLockReg));
            tvResult.setText(sb);
            LogUtil.e(TAG, "getBlockProtection() result:" + sb);
            showSpendTime();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** Check hex string */
    private boolean checkHexValue(String src) {
        return Pattern.matches("[0-9a-fA-F]+", src);
    }

}
