package com.atakmap.android.myatakpluginproject;


// MyPluginComponent.java 파일에 이 코드를 붙여넣습니다.
//package com.mycompany.myplugin;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
// ... (ATAK SDK import는 추후 추가)

// MapComponent를 상속받아 플러그인 진입점을 만듭니다.
public class MyPluginComponent /* extends MapComponent */ { // MapComponent 상속은 SDK 추가 후

    private static final String TAG = "MyPluginTag";

    @Override
    public void onCreate(Context context, Bundle extras) {
        // super.onCreate(context, extras);
        Log.i(TAG, "MyPluginComponent: onCreate() - 플러그인 초기화 시작!");
    }

    @Override
    public void onStart(final Context context, final MapView view) {
        Log.d(TAG, "onStart");
    }

    @Override
    public void onPause(final Context context, final MapView view) {
        Log.d(TAG, "onPause");
    }

    @Override
    public void onResume(final Context context,
                         final MapView view) {
        Log.d(TAG, "onResume");
    }

    @Override
    public void onStop(final Context context,
                       final MapView view) {
        Log.d(TAG, "onStop");
    }
}