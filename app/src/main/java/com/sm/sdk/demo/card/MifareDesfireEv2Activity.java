package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.view.View;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.card.wrapper.CheckCardCallbackV2Wrapper;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MifareDesfireEv2Activity extends BaseAppCompatActivity {
    private static final String TAG = Constant.TAG;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_mifare_desfire_ev2);
        initView();
        checkCard();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_mifare_desfire_ev2);
        findViewById(R.id.mb_get_app_id).setOnClickListener(this);
        findViewById(R.id.mb_select_app).setOnClickListener(this);
        findViewById(R.id.mb_read_data_file).setOnClickListener(this);
        findViewById(R.id.mb_write_data_file).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_get_app_id:
                getApplicationIds();
                break;
            case R.id.mb_select_app:
                getCardId();
                break;
            case R.id.mb_read_data_file:
                break;
            case R.id.mb_write_data_file:

        }
    }

    private void getApplicationIds() {
        List<byte[]> list = new ArrayList<>();
        byte[] cmd = {(byte) 0x90, 0x60, 0x00, 0x00, 0x00};
        byte[] recv = transmitApdu(cmd);
        if (recv.length >= 2) {
            list.add(Arrays.copyOf(recv, recv.length - 2));
        }
        while (true) {
            recv = transmitApdu(new byte[]{(byte) 0x90, (byte) 0xaf, 0x00, 0x00, 0x00});
            if (recv.length >= 2) {
                list.add(Arrays.copyOf(recv, recv.length - 2));
            }
            if (recv.length == 0 || (recv[recv.length - 1] & 0xff) != 0xaf) {
                break;
            }
        }
        for (byte[] version : list) {
            LogUtil.e(TAG, "version:" + ByteUtil.byte2PrintHex(version, 0, version.length));
        }
    }

    private byte[] transmitApdu(byte[] send) {
        byte[] result = new byte[0];
        try {
            byte[] recv = new byte[260];
            addStartTimeWithClear("transmitApdu()");
            int len = MyApplication.app.readCardOptV2.transmitApdu(CardType.MIFARE_DESFIRE.getValue(), send, recv);
            addEndTime("transmitApdu()");
            if (len < 0) {
                LogUtil.e(TAG, "transmitApdu failed,code:" + len);
            } else {
                LogUtil.e(TAG, "transmitApdu success,recv:" + ByteUtil.bytes2HexStr(recv));
                result = Arrays.copyOf(recv, len);
            }
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    private void getCardId() {
        byte[] cmd = {(byte) 0x90, 0x51, 0x00, 0x00, 0x00};
        byte[] recv = transmitApdu(cmd);
        LogUtil.e(TAG, "cardId:" + ByteUtil.bytes2HexStr(recv));
    }

    private void checkCard() {
        try {
            showSwingCardHintDialog(0);
            addStartTimeWithClear("checkCard()");
            MyApplication.app.readCardOptV2.checkCard(CardType.MIFARE_DESFIRE.getValue(), mCheckCardCallback, 60);
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

    @Override
    protected void onDestroy() {
        cancelCheckCard();
        super.onDestroy();
    }

    private void cancelCheckCard() {
        try {
            MyApplication.app.readCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
