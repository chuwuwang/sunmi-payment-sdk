package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidlv2.security.SecurityOptV2;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;

public class HsmRsaTestActivity extends BaseAppCompatActivity {
    private final SecurityOptV2 securityOptV2 = MyApplication.app.securityOptV2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hsm_rsa_test);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.hsm_rsa_keypair_test);
        findViewById(R.id.btn_hsm_gen_key_pair).setOnClickListener(this);
        findViewById(R.id.btn_hsm_inject_key_pair).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_hsm_gen_key_pair:
                generateRsaKeyPair();
                break;
            case R.id.btn_hsm_inject_key_pair:
                injectRsaKey();
                break;
        }
    }

    /**
     * Generate RSA keypair
     */
    private void generateRsaKeyPair() {
        try {
            String pvtKeyIndexStr = this.<EditText>findViewById(R.id.edt_gen_pvt_key_index).getText().toString();
            if (TextUtils.isEmpty(pvtKeyIndexStr)) {
                showToast("private key index should not be empty");
                return;
            }
            String pvtKeySizeStr = this.<EditText>findViewById(R.id.edt_gen_pvt_key_size).getText().toString();
            if (TextUtils.isEmpty(pvtKeySizeStr)) {
                showToast("private key size should not be empty");
                return;
            }
            String rsaKeyExpStr = this.<EditText>findViewById(R.id.edt_gen_rsa_key_exp).getText().toString();
            if (TextUtils.isEmpty(rsaKeyExpStr)) {
                showToast("rsa Key Exponent should not be empty");
                return;
            }
            int pvtKeyIndex = Integer.parseInt(pvtKeyIndexStr);
            int pvtKeySize = Integer.parseInt(pvtKeySizeStr);

            byte[] dataOut = new byte[512];
            addStartTimeWithClear("hsmGenerateRSAKeypair()");
            int len = securityOptV2.hsmGenerateRSAKeypair(pvtKeyIndex, pvtKeySize, rsaKeyExpStr, dataOut);
            addEndTime("hsmGenerateRSAKeypair()");
            LogUtil.e(TAG, "hsmGenerateRSAKeypair len:" + len);
            showSpendTime();
            if (len >= 0) {
                String dataOutStr = ByteUtil.bytes2HexStr(dataOut);
                LogUtil.e(TAG, "dataOutStr = " + dataOutStr);
                PublicKey rsa = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger(1, dataOut), new BigInteger(rsaKeyExpStr)));
                String publicKeyStr = ByteUtil.bytes2HexStr(rsa.getEncoded());
                LogUtil.e(TAG, "publicKeyStr = " + publicKeyStr);
                if (publicKeyStr.contains(dataOutStr)) {
                    showToast("hsm generate RSA keypair success");
                } else {
                    showToast("hsm generate RSA keypair failed");
                }
            } else {
                showToast("hsm generate RSA keypair failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Inject RSA keypair
     */
    private void injectRsaKey() {
        try {
            String pvtKeyIndexStr = this.<EditText>findViewById(R.id.edt_inject_pvt_key_index).getText().toString();
            if (TextUtils.isEmpty(pvtKeyIndexStr)) {
                showToast("private key index should not be empty");
                return;
            }
            String pvtKeySizeStr = this.<EditText>findViewById(R.id.edt_inject_pvt_key_size).getText().toString();
            if (TextUtils.isEmpty(pvtKeySizeStr)) {
                showToast("private key size should not be empty");
                return;
            }
            String rsaKeyModuleStr = this.<EditText>findViewById(R.id.edt_inject_rsa_key_module).getText().toString();
            if (TextUtils.isEmpty(rsaKeyModuleStr)) {
                showToast("rsa key module should not be empty");
                return;
            }
            String rsaKeyPubExpStr = this.<EditText>findViewById(R.id.edt_inject_rsa_pub_key_exp).getText().toString();
            String rsaKeyPvtExpStr = this.<EditText>findViewById(R.id.edt_inject_rsa_pvt_key_exp).getText().toString();
            if (TextUtils.isEmpty(rsaKeyPubExpStr) && TextUtils.isEmpty(rsaKeyPvtExpStr)) {
                showToast("rsa public private key exponent should not all be empty");
                return;
            }
            int pvtKeyIndex = Integer.parseInt(pvtKeyIndexStr);
            int pvtKeySize = Integer.parseInt(pvtKeySizeStr);

            String rsaKeyExpStr = TextUtils.isEmpty(rsaKeyPubExpStr) ? rsaKeyPvtExpStr : rsaKeyPubExpStr;
            addStartTimeWithClear("hsmInjectRSAKey()");
            int result = securityOptV2.hsmInjectRSAKey(pvtKeyIndex, pvtKeySize, rsaKeyModuleStr, rsaKeyExpStr);
            addEndTime("hsmInjectRSAKey()");
            LogUtil.e(TAG, "result = " + result);
            if (result == 0) {
                PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger(1, ByteUtil.hexStr2Bytes(rsaKeyModuleStr)), new BigInteger(ByteUtil.hexStr2Bytes(rsaKeyPubExpStr))));
                PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new RSAPrivateKeySpec(new BigInteger(1, ByteUtil.hexStr2Bytes(rsaKeyModuleStr)), new BigInteger(ByteUtil.hexStr2Bytes(rsaKeyPvtExpStr))));
                byte[] kekValue = ByteUtil.hexStr2Bytes("44444444444444444444444444444444");
                byte[] encrypt = encrypt(kekValue, publicKey);
                LogUtil.e(TAG, "encrypt = " + ByteUtil.bytes2HexStr(encrypt));
                byte[] decrypt = decrypt(encrypt, privateKey);
                LogUtil.e(TAG, "decrypt = " + ByteUtil.bytes2HexStr(decrypt));
                showToast("hsm generate RSA keypair success");
            } else {
                showToast("hsm generate RSA keypair failed");
            }
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final int ENCRYPT_BLOCK_SIZE = 245;//加密时分组长度,245字节
    private static final int DECRYPT_BLOCK_SIZE = 256;//解密时分组长度,256字节

    /**
     * 用RSA算法加密数据，分组长度固定为245字节
     *
     * @param dataIn 待加密的数据
     * @param rsaKey 加密密钥
     * @return 加密后的数据
     */
    public static byte[] encrypt(byte[] dataIn, Key rsaKey) {
        if (dataIn == null || dataIn.length == 0) {
            return null;
        }
        int inputLen = dataIn.length;
        int blockSize = ENCRYPT_BLOCK_SIZE;
        int offset = 0;
        List<byte[]> list = new ArrayList<>();
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, rsaKey);
            while (offset < inputLen) { //对数据分段加密/解密
                if (offset + blockSize <= inputLen) {
                    list.add(cipher.doFinal(dataIn, offset, blockSize));
                } else {
                    list.add(cipher.doFinal(dataIn, offset, inputLen - offset));
                }
                offset += blockSize;
            }
            byte[] cipherData = ByteUtil.concatByteArrays(list);
            return cipherData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;//加密失败
    }

    /**
     * 用RSA算法解密数据,分组长度为256字节
     *
     * @param dataIn 待解密的数据
     * @param rsaKey 解密密钥
     * @return 解密后的数据
     */
    public static byte[] decrypt(byte[] dataIn, Key rsaKey) {
        if (dataIn == null || dataIn.length == 0 || dataIn.length % DECRYPT_BLOCK_SIZE != 0) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, rsaKey);
            int count = dataIn.length / DECRYPT_BLOCK_SIZE;
            for (int i = 0; i < count; i++) {
                byte[] block = Arrays.copyOfRange(dataIn, i * DECRYPT_BLOCK_SIZE, (i + 1) * DECRYPT_BLOCK_SIZE);
                cipher.update(block);
            }
            return cipher.doFinal();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;//解密失败
    }

}