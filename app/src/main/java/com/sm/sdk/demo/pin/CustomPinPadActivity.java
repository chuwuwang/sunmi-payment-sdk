package com.sm.sdk.demo.pin;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.view.FixPasswordKeyboard;
import com.sm.sdk.demo.view.PasswordEditText;
import com.sm.sdk.demo.view.TitleView;
import com.sunmi.pay.hardware.aidlv2.bean.PinPadConfigV2;
import com.sunmi.pay.hardware.aidlv2.bean.PinPadDataV2;
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadListenerV2;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;

public class CustomPinPadActivity extends BaseAppCompatActivity {
    private final int[] mKeyboardCoordinate = {0, 0};  // 密码键盘第一个button左顶点位置（绝对位置）
    private final int[] mCancelCoordinate = {0, 0};    // 取消键左顶点位置（绝对位置）

    private ImageView mBackView;
    private PasswordEditText mPasswordEditText;
    private FixPasswordKeyboard mFixPasswordKeyboard;

    public String cardNo = "";
    public PinPadConfigV2 customPinPadConfigV2;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_pad_custom);
        initView();
        getKeyboardCoordinate();
    }

    private void initView() {
        Intent mIntent = getIntent();
        customPinPadConfigV2 = (PinPadConfigV2) mIntent.getSerializableExtra("PinPadConfigV2");
        cardNo = mIntent.getStringExtra("cardNo");
        TitleView titleView = findViewById(R.id.title_view);
        TextView mTvTitle = titleView.getCenterTextView();
        mTvTitle.setText(getString(R.string.pin_pad_custom_keyboard));
        mBackView = titleView.getLeftImageView();
        mBackView.setOnClickListener(v -> onBackPressed());
        TextView tvMoney = findViewById(R.id.tv_money);
        tvMoney.setText(longCent2DoubleMoneyStr(1));
        TextView tvCardNum = findViewById(R.id.tv_card_num);
        tvCardNum.setText(cardNo);
        mPasswordEditText = findViewById(R.id.passwordEditText);
        mFixPasswordKeyboard = findViewById(R.id.fixPasswordKeyboard);
    }

    @Override
    public void initToolbarBringBack(String title) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        toolbar.setNavigationIcon(R.drawable.ic_back_white);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(
                v -> onBackPressed()
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume()...");
        screenMonopoly(getApplicationInfo().uid);
    }

    @Override
    protected void onDestroy() {
        screenMonopoly(-1);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void getKeyboardCoordinate() {
        Log.e(TAG, "getKeyboardCoordinate()...");
        mFixPasswordKeyboard.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Log.e(TAG, "onGlobalLayout()...");
                        mFixPasswordKeyboard.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        String keyNumber = initPinPad();
                        if (!TextUtils.isEmpty(keyNumber)) {
                            importPinPadData(keyNumber);
                        } else {
                            showToast("init PinPad failed..");
                        }
                    }
                }
        );
    }

    /** init PinPad */
    private String initPinPad() {
        String keyNumber = null;
        try {
            PinPadConfigV2 config = new PinPadConfigV2();
            config.setMaxInput(12);
            config.setMinInput(4);
            config.setPinPadType(1);
            config.setAlgorithmType(customPinPadConfigV2.getAlgorithmType());
            config.setPinType(customPinPadConfigV2.getPinType());
            config.setTimeout(customPinPadConfigV2.getTimeout());
            config.setOrderNumKey(customPinPadConfigV2.isOrderNumKey());
            config.setPinblockFormat(customPinPadConfigV2.getPinblockFormat());
            config.setKeySystem(customPinPadConfigV2.getKeySystem());
            config.setPinKeyIndex(customPinPadConfigV2.getPinKeyIndex());
            int length = cardNo.length();
            byte[] panBlock = cardNo.substring(length - 13, length - 1).getBytes("US-ASCII");
            config.setPan(panBlock);

            addStartTimeWithClear("initPinPad()");
            keyNumber = MyApplication.app.pinPadOptV2.initPinPad(config, mPinPadListener);
            if (TextUtils.isEmpty(keyNumber)) {
                String msg = "initPinPad failed";
                LogUtil.e(TAG, msg);
                showToast(msg);
            } else {
                mPasswordEditText.clearText();
                mFixPasswordKeyboard.setKeepScreenOn(true);
                mFixPasswordKeyboard.setKeyBoard(keyNumber);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyNumber;
    }

    /** Import PinPad data to sdk */
    private void importPinPadData(String keyNumber) {
        //1.get key view location
        TextView key_0 = mFixPasswordKeyboard.getKey_0();
        if (isRTL()) {
            key_0 = mFixPasswordKeyboard.getKey_2();
        }
        key_0.getLocationOnScreen(mKeyboardCoordinate);
        // key view item width
        int keyWidth = key_0.getWidth();
        // key view item height
        int keyHeight = key_0.getHeight();
        // width of divider line
        int mInterval = 1;
        mBackView.getLocationOnScreen(mCancelCoordinate);
        // cancel key width
        int cancelKeyWidth = mBackView.getWidth();
        // cancel key height
        int cancelKeyHeight = mBackView.getHeight();

        //2.import key view data to sdk
        PinPadDataV2 pinPadData = new PinPadDataV2();
        pinPadData.numX = mKeyboardCoordinate[0];
        pinPadData.numY = mKeyboardCoordinate[1];
        pinPadData.numW = keyWidth;
        pinPadData.numH = keyHeight;
        pinPadData.lineW = mInterval;
        pinPadData.cancelX = mCancelCoordinate[0];
        pinPadData.cancelY = mCancelCoordinate[1];
        pinPadData.cancelW = cancelKeyWidth;
        pinPadData.cancelH = cancelKeyHeight;
        pinPadData.lineW = 0;
        pinPadData.rows = 5;
        pinPadData.clos = 3;
        if (isRTL()) {
            keyMapRTL(keyNumber, pinPadData);
        } else {
            keyMapLTR(keyNumber, pinPadData);
        }
        try {
            addStartTimeWithClear("importPinPadData()");
            MyApplication.app.pinPadOptV2.importPinPadData(pinPadData);
            addEndTime("importPinPadData()");
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final PinPadListenerV2 mPinPadListener = new PinPadListenerV2.Stub() {

        @Override
        public void onPinLength(int len) throws RemoteException {
            LogUtil.e(Constant.TAG, "onPinLength len:" + len);
            updatePasswordView(len);
        }

        @Override
        public void onConfirm(int type, byte[] pinBlock) throws RemoteException {
            addEndTime("initPinPad()");
            LogUtil.e(Constant.TAG, "onConfirm pinType:" + type);
            String pinBlockStr = ByteUtil.bytes2HexStr(pinBlock);
            LogUtil.e(Constant.TAG, "pinBlock:" + pinBlockStr);
            if (TextUtils.equals("00", pinBlockStr)) {
                handleOnConfirm("");
            } else {
                handleOnConfirm(pinBlockStr);
            }
            showSpendTime();
        }

        @Override
        public void onCancel() throws RemoteException {
            addEndTime("initPinPad()");
            LogUtil.e(Constant.TAG, "onCancel");
            handleOnCancel();
            showSpendTime();
        }

        @Override
        public void onError(int code) throws RemoteException {
            addEndTime("initPinPad()");
            LogUtil.e(Constant.TAG, "onError code:" + code);
            handleOnError();
            showSpendTime();
        }
    };

    private void updatePasswordView(int len) {
        runOnUiThread(() -> {
            char[] stars = new char[len];
            Arrays.fill(stars, '*');
            mPasswordEditText.setText(new String(stars));
        });
    }

    private void handleOnConfirm(String pinBlock) {
        showToast("CONFIRM");
        Intent intent = getIntent();
        intent.putExtra("pinCipher", pinBlock);
        setResult(0, intent);
        finish();
    }

    private void handleOnCancel() {
        showToast("CANCEL");
        finish();
    }

    private void handleOnError() {
        showToast("ERROR");
        finish();
    }

    /** LTR（Left-to-right）layout direction */
    private void keyMapLTR(String keyNumber, PinPadDataV2 data) {
        data.keyMap = new byte[64];
        for (int i = 0, j = 0; i < 15; i++, j++) {
            if (i == 9 || i == 12) {
                data.keyMap[i] = 0x1B;//cancel
                j--;
            } else if (i == 13) {
                data.keyMap[i] = 0x0C;//clear
                j--;
            } else if (i == 11 || i == 14) {
                data.keyMap[i] = 0x0D;//confirm
                j--;
            } else {
                data.keyMap[i] = (byte) keyNumber.charAt(j);
            }
        }
    }

    /** RTL（Right-to-left）layout direction */
    private void keyMapRTL(String keyNumber, PinPadDataV2 data) {
        data.keyMap = new byte[64];
        for (int i = 0; i < 9; i += 3) {
            for (int j = 0; j < 3; j++) {
                data.keyMap[i + j] = (byte) keyNumber.charAt(i + 2 - j);
            }
        }
        data.keyMap[9] = 0x0D;//confirm
        data.keyMap[10] = (byte) keyNumber.charAt(9);
        data.keyMap[11] = 0x1B;//cancel
        data.keyMap[12] = 0x0D;//confirm
        data.keyMap[13] = 0x0C;//clear
        data.keyMap[14] = 0x1B;//cancel
    }

    /** 将Long类型的钱（单位：分）转化成String类型的钱（单位：元） */
    public static String longCent2DoubleMoneyStr(long amount) {
        BigDecimal bd = new BigDecimal(amount);
        double doubleValue = bd.divide(new BigDecimal("100")).doubleValue();
        DecimalFormat df = new DecimalFormat("#0.00");
        return df.format(doubleValue);
    }

    /** 屏幕独占 */
    private void screenMonopoly(int mode) {
        try {
            addStartTimeWithClear("setScreenMode()");
            MyApplication.app.basicOptV2.setScreenMode(mode);
            addEndTime("setScreenMode()");
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 是否是RTL（Right-to-left）语系 */
    private boolean isRTL() {
        return mFixPasswordKeyboard.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }
}
