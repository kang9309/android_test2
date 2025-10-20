package com.atakmap.android.plugintemplate.plugin;

import com.atakmap.comms.CotDispatcher;
import com.atakmap.android.cot.CotMapComponent;
import com.atakmap.coremap.cot.event.CotEvent;
import com.atakmap.coremap.log.Log;

public class CoTSender {
    public static final String CLASS_TAG = "CoTSender";
    public static void sendInternalCoT() {
        try {
            String cotXml = "<event version='2.0' type='b-t-f' uid='ANDROID-LOCAL' " +
                    "time='2025-10-20T06:00:00Z' start='2025-10-20T06:00:00Z' " +
                    "stale='2025-10-20T07:00:00Z' how='m-g'>" +
                    "<point lat='37.5651' lon='126.98955' hae='0' ce='9999999' le='9999999'/>" +
                    "</event>";

            // 내부 Dispatcher 가져오기
            CotDispatcher internal = CotMapComponent.getInternalDispatcher();

            // 이벤트 파싱
            CotEvent event = CotEvent.parse(cotXml);

            // 내부 버스로 전송 → ATAK에서 수신한 것처럼 처리됨
            internal.dispatch(event);

//            System.out.println("Internal CoT message dispatched!");
            Log.e(CLASS_TAG, LogUtils.getLogPosition());
        } catch (Exception e) {
//            e.printStackTrace();
            Log.e(CLASS_TAG, LogUtils.getLogPosition() + e.getMessage());
        }
    }
}
