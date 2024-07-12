package com.sm.sdk.demo.utils;

import android.os.Build;
import android.text.TextUtils;

public final class DeviceUtil {
    private DeviceUtil() {
        throw new AssertionError("create instance of DeviceUtil is prohibited");
    }

    /** 是否是P1N */
    public static boolean isP1N() {
        String model = getModel();
        return model.matches("p1n(-.+)?");
    }

    /** 是否是P1_4G */
    public static boolean isP14G() {
        String model = getModel();
        return model.matches("(p1\\(4g\\)|p1_4g)(-.+)?");
    }

    /** 是否是P2lite */
    public static boolean isP2Lite() {
        String model = getModel();
        return model.matches("p2lite(-.+)?");
    }

    /** 是否是P2_PRO */
    public static boolean isP2Pro() {
        String model = getModel();
        return model.matches("p2_pro(-.+)?");
    }

    /** 是否是P2 */
    public static boolean isP2() {
        String model = getModel();
        return model.matches("p2(-.+)?");
    }

    /** 是否是P2mini */
    public static boolean isP2Mini() {
        String model = getModel();
        return model.matches("p2mini(-.+)?");
    }

    /** 是否是p2_smartpad(Banjul改名为p2_retail，再改名为p2_smartpad) */
    public static boolean isP2SmartPad() {
        String model = getModel();
        return model.matches("(p2_smartpad|p2_retail|pinpad|qcm2150|t6a10)(-.+)?");
    }

    /** 是否是P2_Xpro(P2H改名为P2_Xpro) */
    public static boolean isP2XPro() {
        String model = getModel();
        return model.matches("(p2_xpro|p2h|uis8581e5h10_natv)(-.+)?");
    }

    /** 是否是FT2 */
    public static boolean isFT2() {
        String model = getModel();
        return model.matches("ft2(-.+)?");
    }

    /** 是否是FT2Mini */
    public static boolean isFT2Mini() {
        String model = getModel();
        return model.matches("ft2mini(-.+)?");
    }

    /** 是否是V2_SE */
    public static boolean isV2SE() {
        String model = getModel();
        return model.matches("(v2_se|xqt530)(-.+)?");
    }

    /** 获取model */
    private static String getModel() {
        String model = SystemPropertiesUtil.get("ro.sunmi.hardware");
        if (TextUtils.isEmpty(model)) {
            model = Build.MODEL;
        }
        if (TextUtils.isEmpty(model)) {
            model = Build.UNKNOWN;
        }
        return model == null ? "" : model.toLowerCase();
    }
}
