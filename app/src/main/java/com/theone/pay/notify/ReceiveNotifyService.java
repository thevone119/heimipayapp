package com.theone.pay.notify;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * 用于接收通知的服务
 * 通过AccessibilityService进行实现
 *
 * 测试无效
 *
 */
public class ReceiveNotifyService  extends AccessibilityService implements TextToSpeech.OnInitListener {
    private static final String TAG="ReceiveNotifyService";

    TextToSpeech speech;
    boolean mTextToSpeechInitialized = false;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i(TAG,"***** onAccessibilityEvent");
        if (event.getPackageName().equals(getPackageName())) {
            return;
        }
        if (!mTextToSpeechInitialized) {
            return;
        }
        Log.i(TAG,"pkgname " + event.getPackageName());
        Log.i(TAG,"classname " + event.getClassName());
        Log.i(TAG,"action      : " + event.getAction());
        List<CharSequence> text = event.getText();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.size(); i++) {
            builder.append(text.get(i));
            Log.i(TAG,"t " + text.get(i));
        }
        speech.speak(builder.toString(), TextToSpeech.QUEUE_FLUSH, null);

        AccessibilityNodeInfo source = event.getSource();
        if (source == null) {
            Log.i(TAG," null ");
            return;
        }
        Log.i(TAG,"count " + source.getChildCount());

        Bundle extras = source.getExtras();
        Iterator<String> iterator = extras.keySet().iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            Object obj = extras.get(next);
            Log.i(TAG," . " + next + " . " + obj);
        }
    }

    @Override
    public void onInterrupt() {
        Log.i(TAG,"-----------interrupt");
        //System.out.println("-----------interrupt");
    }

    @Override
    public void onServiceConnected() {
        Log.i(TAG,"----------connect");

        speech = new TextToSpeech(getApplicationContext(), this);
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.notificationTimeout = 100;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        setServiceInfo(info);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            speech.setLanguage(Locale.US);
            mTextToSpeechInitialized = true;
        }
    }

}
