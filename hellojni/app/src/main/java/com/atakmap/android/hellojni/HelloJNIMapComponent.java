
package com.atakmap.android.hellojni;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import com.atakmap.android.hellojni.plugin.PluginNativeLoader;

import com.atakmap.android.maps.AbstractMapComponent;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.dropdown.DropDownMapComponent;

import com.atakmap.coremap.log.Log;
import com.atakmap.android.hellojni.plugin.R;

import com.atakmap.android.ipc.AtakBroadcast;
//import com.atakmap.android.ipc.DocumentedIntentFilter;
import com.atakmap.android.ipc.AtakBroadcast.DocumentedIntentFilter;

/**
 * This is an example of a MapComponent within the ATAK 
 * ecosphere.   A map component is the building block for all
 * activities within the system.   This defines a concrete 
 * thought or idea. 
 */
public class HelloJNIMapComponent extends AbstractMapComponent {

    public static final String TAG = "HelloJNIMapComponent";
    public static final String ACTION_FROM_HELLOWORLD = "com.myplugin.HELLOJNI_MESSAGE";


    private Context pluginContext;

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
        Log.d(TAG, "ksh_test onResume");
    }

    @Override
    public void onStop(final Context context,
            final MapView view) {
        Log.d(TAG, "onStop");
    }

    public void onCreate(final Context context, Intent intent,
            final MapView view) {

        // Set the theme.  Otherwise, the plugin will look vastly different
        // than the main ATAK experience.   The theme needs to be set 
        // programatically because the AndroidManifest.xml is not used.
        context.setTheme(R.style.ATAKPluginTheme);

        pluginContext = context;

        // The MapComponent serves as the primary entry point for the plugin, load
        // the JNI library here
        PluginNativeLoader.init(pluginContext);

        // load the JNI library. Note that if the library has one or more
        // dependencies, those dependencies must be explicitly loaded in
        // correct order. Android will not automatically load dependencies
        // even if they are on the system library path
        PluginNativeLoader.loadLibrary("hellojni");

        // 4. 수신할 Action을 필터에 추가하고 리시버 등록
//        IntentFilter filter = new IntentFilter(ACTION_FROM_HELLOWORLD);
////        AtakBroadcast.getInstance().registerReceiver(context, this.receiver, filter);
//        // DocumentedIntentFilter에 Action을 추가할 때는 addAction() 메서드를 사용합니다.
////        AtakBroadcast.getInstance().registerReceiver(this.receiver, filter);
//        // 🚨 [수정] getInstance()를 제거하고, 정적 메서드(Static Method) 호출 방식으로 변경합니다.
//        // 이 정적 메서드가 Context와 표준 IntentFilter를 받는 올바른 오버로드입니다.
////        com.atakmap.android.ipc.AtakBroadcast.registerReceiver(context, this.receiver, filter);
////        com.atakmap.android.ipc.AtakBroadcast.registerReceiver(this.receiver, filter);
//        // 🚨 [수정] AtakBroadcast 대신 Android Context의 registerReceiver() 메서드를 사용합니다.
//        // ATAK 5.5.0.7의 빌드 환경에서 이 방법이 API 충돌을 피하고 가장 확실하게 작동합니다.
//        // Android 5.0 (API 21)에서는 Context::registerReceiver(receiver, filter) 서명이 유효합니다.
//        context.registerReceiver(this.receiver, filter);


        // 4. 수신할 Action을 필터에 추가하고 리시버 등록
//        IntentFilter filter = new IntentFilter(ACTION_FROM_HELLOWORLD);
        DocumentedIntentFilter filter = new DocumentedIntentFilter(ACTION_FROM_HELLOWORLD);


        // 🚨 [최종 수정] DocumentedIntentFilter를 임포트할 수 없으므로,
        // AtakBroadcast의 싱글톤 메서드를 사용합니다.
        // 이 코드가 컴파일이 되어야 합니다.
        AtakBroadcast.getInstance().registerReceiver(this.receiver, filter);

//        System.out.println("HelloJNI Receiver Registered for: " + ACTION_FROM_HELLOWORLD);
        Log.d(TAG, "HelloJNI Receiver Registered for: " + ACTION_FROM_HELLOWORLD);
    }


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive");
            final String action = intent.getAction();

            if (ACTION_FROM_HELLOWORLD.equals(action)) {
                String receivedMessage = intent.getStringExtra("payload");

                if (receivedMessage != null) {
                    // 수신 확인을 위한 간단한 테스트 출력
                    Log.d(TAG, "✅ TEST SUCCESS: HelloJNI Received: " + receivedMessage);
//                    System.out.println("✅ TEST SUCCESS: HelloJNI Received: " + receivedMessage);
                }
            }
        }
    };


    @Override
    protected void onDestroyImpl(Context context, MapView view) {
        Log.d(TAG, "calling on destroy");
        // 5. 플러그인 종료 시 리시버 등록 해제
        if (this.receiver != null) {
            // 🚨 [최종 수정] Context가 아닌 AtakBroadcast를 통해 해제합니다.
            AtakBroadcast.getInstance().unregisterReceiver(this.receiver);
//            AtakBroadcast.getInstance().unregisterReceiver(this.receiver);
//            context.unregisterReceiver(this.receiver); // 🚨 Context를 사용하여 등록 해제
        }
    }
}
