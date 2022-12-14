package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidlv2.security.SecurityOptV2;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class RsaRecoverActivity extends BaseAppCompatActivity {

    public static final int TYPE_SHA1 = 0;
    public static final int TYPE_SHA224 = 1;
    public static final int TYPE_SHA256 = 2;
    public static final int TYPE_SHA384 = 3;
    public static final int TYPE_SHA512 = 4;

    private EditText mEditData;
    private EditText mEditKeyIndex;
    private TextView mTvInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rsa_recover);
        initToolbarBringBack(R.string.hsm_rsa_recover);
        initView();
    }

    private int mHashType = TYPE_SHA256;

    private void initView() {
        RadioGroup hashTypeRadioGroup = findViewById(R.id.hash_type_group);
        hashTypeRadioGroup.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    switch (checkedId) {
                        case R.id.rb_hash_type1:
                            mHashType = TYPE_SHA1;
                            break;
                        case R.id.rb_hash_type2:
                            mHashType = TYPE_SHA224;
                            break;
                        case R.id.rb_hash_type3:
                            mHashType = TYPE_SHA256;
                            break;
                        case R.id.rb_hash_type4:
                            mHashType = TYPE_SHA384;
                            break;
                        case R.id.rb_hash_type5:
                            mHashType = TYPE_SHA512;
                            break;
                    }
                }
        );

        mEditData = findViewById(R.id.source_data);
        mEditKeyIndex = findViewById(R.id.key_index);

        mTvInfo = findViewById(R.id.tv_info);

        findViewById(R.id.mb_ok).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.mb_ok:
                rsaRecover();
                break;
        }
    }

    private void rsaRecover() {
        String dataStr = mEditData.getText().toString();
        String keyIndexStr = mEditKeyIndex.getText().toString();

        int keyIndex;
        try {
            keyIndex = Integer.parseInt(keyIndexStr);
            if (keyIndex < 0 || keyIndex > 19) {
                showToast(R.string.security_rsa_key_hint);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast(R.string.security_mksk_key_index_hint);
            return;
        }
        if (dataStr.trim().length() == 0) {
            showToast(R.string.security_source_data_hint);
            return;
        }
        if (dataStr.length() % 2 != 0) {
            showToast(R.string.security_source_data_hint);
            return;
        }
        byte[] dataIn = ByteUtil.hexStr2Bytes(dataStr);
        byte[] dataOut = signBySP(keyIndex, dataIn, mHashType);
        String hexStr = ByteUtil.bytes2HexStr(dataOut);
        LogUtil.e(TAG, "dataDecrypt output:" + hexStr);
        mTvInfo.setText(hexStr);
    }

    public byte[] signBySP(int keyIndex, byte[] dataIn, int type) {
        if (dataIn == null || dataIn.length == 0) {
            return null;
        }
        try {
            byte[] hash = new byte[128];
            int ret = algASN1Sha(type, dataIn, dataIn.length, hash); //hash运算
            if (ret < 0) { // ret ：长度（包括asn1格式数据）
                return null;
            }
            byte[] dataOut = new byte[2048];
            byte[] hashIn = Arrays.copyOf(hash, ret);
            SecurityOptV2 securityOptV2 = MyApplication.app.securityOptV2;
            if (securityOptV2 != null) {
                addStartTimeWithClear("rsaEncryptOrDecryptData()");
                int len = securityOptV2.rsaEncryptOrDecryptData(keyIndex, 1, hashIn, dataOut); //sp签名
                addEndTime("rsaEncryptOrDecryptData()");
                showSpendTime();
                if (len < 0) {
                    toastHint(len);
                    return null;
                }
                return Arrays.copyOf(dataOut, len);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 哈希运算
     *
     * @param type      哈希算法类型 AlgorithmicType
     * @param dataBytes 待运算数据缓冲区
     * @param dataLen   待运算数据缓冲区长度
     * @param hash      运算结果数据缓冲区
     * @return
     */
    private int algASN1Sha(int type, byte[] dataBytes, int dataLen, byte[] hash) {
        if (dataBytes == null || dataLen == 0 || dataLen != dataBytes.length || hash == null) {
            return -1;
        }
        int hashLen;
        byte[] asn1Byte;
        DevicePvkRecoverActivity.SHAEncryptType shaType;
        if (type == TYPE_SHA1) {
            asn1Byte = ByteUtil.hexStr2Bytes("3021300906052B0E03021A05000414"); // ASN1: 1.3.14.3.2.26 sha1 (OIW)
            shaType = DevicePvkRecoverActivity.SHAEncryptType.SHA1;
            hashLen = 20;
        } else if (type == TYPE_SHA224) {
            asn1Byte = ByteUtil.hexStr2Bytes("3031300D06096086480165030402040500041C"); // ASN1: 1.3.14.3.2.26 sha1 (OIW)
            shaType = DevicePvkRecoverActivity.SHAEncryptType.SHA224;
            hashLen = 28;
        } else if (type == TYPE_SHA256) {
            asn1Byte = ByteUtil.hexStr2Bytes("3031300D060960864801650304020105000420"); // ASN1: 2.16.840.1.101.3.4.2.1 sha-256 (NIST Algorithm)
            shaType = DevicePvkRecoverActivity.SHAEncryptType.SHA256;
            hashLen = 32;
        } else if (type == TYPE_SHA384) {
            asn1Byte = ByteUtil.hexStr2Bytes("3031300D060960864801650304020205000430"); // ASN1: 2.16.840.1.101.3.4.2.2 sha-384 (NIST Algorithm)
            shaType = DevicePvkRecoverActivity.SHAEncryptType.SHA384;
            hashLen = 48;
        } else if (type == TYPE_SHA512) {
            asn1Byte = ByteUtil.hexStr2Bytes("3031300D060960864801650304020305000440"); // ASN1: 2.16.840.1.101.3.4.2.3 sha-512 (NIST Algorithm)
            shaType = DevicePvkRecoverActivity.SHAEncryptType.SHA512;
            hashLen = 64;
        } else {
            asn1Byte = ByteUtil.hexStr2Bytes("3031300D060960864801650304020105000420"); // ASN1: 2.16.840.1.101.3.4.2.1 sha-256 (NIST Algorithm)
            shaType = DevicePvkRecoverActivity.SHAEncryptType.SHA256;
            hashLen = 32;
        }
        byte[] hashOut = ByteUtil.hexStr2Bytes(encrypt(ByteUtil.bytes2HexStr(dataBytes), shaType));
        System.arraycopy(asn1Byte, 0, hash, 0, asn1Byte.length);
        System.arraycopy(hashOut, 0, hash, asn1Byte.length, hashLen);
        return asn1Byte.length + hashLen;
    }

    enum SHAEncryptType {
        SHA1("sha-1"),
        SHA224("sha-224"),
        SHA256("sha-256"),
        SHA384("sha-384"),
        SHA512("sha-512"),
        ;

        private String value;

        SHAEncryptType(String str) {
            value = str;
        }

        public String getValue() {
            return value;
        }
    }

    public String encrypt(String data, DevicePvkRecoverActivity.SHAEncryptType type) {
        if (TextUtils.isEmpty(data)) {
            return "";
        }
        if (type == null) {
            type = DevicePvkRecoverActivity.SHAEncryptType.SHA256;
        }

        try {
            MessageDigest md5 = MessageDigest.getInstance(type.getValue());
            byte[] bytes = md5.digest(ByteUtil.hexStr2Bytes(data));
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result.append(temp);
            }
            return result.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "";
    }

}