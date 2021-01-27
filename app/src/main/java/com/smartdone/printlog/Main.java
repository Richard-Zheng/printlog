package com.smartdone.printlog;

import android.app.Application;
import android.content.Context;
import android.util.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by smartdone on 2017/4/5.
 */

public class Main implements IXposedHookLoadPackage {
    public static final String MYIPAD_PACKAGE_NAME = "com.ne" + "tspace" + ".my" + "ipad";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!loadPackageParam.packageName.contains(MYIPAD_PACKAGE_NAME)) {
            return;
        }
        XposedHelpers.findAndHookMethod("android.app.Instrumentation", loadPackageParam.classLoader, "newApplication", ClassLoader.class, String.class, Context.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Context context = (Context) param.args[2];
                Context plicontext = context.createPackageContext(MYIPAD_PACKAGE_NAME, Context.CONTEXT_IGNORE_SECURITY);
                InputStream in = plicontext.getAssets().open("libslog.so");
                File so = new File(context.getFilesDir(), "libslog.so");
                if (!so.getParentFile().exists()) {
                    so.getParentFile().mkdirs();
                }
                FileOutputStream fout = new FileOutputStream(so);
                byte[] buffer = new byte[1024];
                int len = in.read(buffer);
                while (len > 0) {
                    fout.write(buffer);
                    len = in.read(buffer);
                }
                fout.flush();
                fout.close();
                in.close();
                android.util.Log.e("LOGTEST", "write so to /data/data/... success");
                System.load(so.getAbsolutePath());
                ClassLoader classLoader = (ClassLoader) param.args[0];
                Class<?> log = XposedHelpers.findClass("android.util.Log", classLoader);
                MethodReplaceImpl methodReplace = new MethodReplaceImpl();
                XposedHelpers.findAndHookMethod(log, "i", String.class, String.class, methodReplace);
                XposedHelpers.findAndHookMethod(log, "v", String.class, String.class, methodReplace);
                XposedHelpers.findAndHookMethod(log, "e", String.class, String.class, methodReplace);
                XposedHelpers.findAndHookMethod(log, "w", String.class, String.class, methodReplace);
                XposedHelpers.findAndHookMethod(log, "d", String.class, String.class, methodReplace);
            }
        });
    }
}
