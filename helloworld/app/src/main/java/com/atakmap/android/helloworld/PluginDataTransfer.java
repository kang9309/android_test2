//package com.example.plugin;
//
//import com.atakmap.android.contact.Contact;
//import com.atakmap.android.contact.ContactManager;
//import com.atakmap.android.contact.ContactUtil;
//import com.atakmap.coremap.log.Log;
//import com.atakmap.coremap.maps.coords.GeoPoint;
////import com.atakmap.map.hittest.MapItem;
//import com.atakmap.android.maps.MapItem;
//import com.atakmap.android.maps.Marker;
//import com.atakmap.android.maps.MapView;
//import com.atakmap.android.ipc.AtakBroadcast.DocumentedIntentFilter;
//import com.atakmap.android.ipc.AtakBroadcast.DocumentedBroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.util.Pair;
//import java.util.HashMap;
//import java.util.Map;
//import org.json.JSONObject;
//
///**
// * ATAK 플러그인 간 데이터 송수신을 위한 핵심 클래스입니다.
// * 연락처 기반 메시징 시스템을 활용하여 데이터를 전송하고 수신합니다.
// */
//public class PluginDataTransfer {
//
//    private static final String TAG = "PluginDataTransfer";
//
//    // 플러그인 고유의 메시지 액션 (수신자가 필터링할 키)
//    private static final String CUSTOM_DATA_ACTION = "com.example.plugin.CUSTOM_DATA_EXCHANGE";
//
//    // JSON 키 정의
//    private static final String KEY_ACTION = "action";
//    private static final String KEY_PAYLOAD = "payload";
//    private static final String ACTION_SEND_LOCATION = "SEND_LOCATION";
//
//    // 현재 ATAK 맵 뷰 컨텍스트
//    private final Context pluginContext;
//    private final ContactManager contactManager;
//
//    public PluginDataTransfer(Context context) {
//        this.pluginContext = context;
//        this.contactManager = ContactManager.getInstance();
//    }
//
//    // =========================================================================================
//    // 1. 데이터 송신 (Sender Logic)
//    // =========================================================================================
//
//    /**
//     * 특정 Contact에게 구조화된 데이터를 전송합니다.
//     * 데이터는 JSON 문자열로 캡슐화되어 표준 ATAK 메시징 채널을 통해 전달됩니다.
//     *
//     * @param targetUid 수신 Contact의 UID (ATAK에서 연락처를 식별하는 고유 ID)
//     * @param point 전송할 지도상의 지점 데이터
//     */
//    public void sendDataToContact(String targetUid, GeoPoint point) {
//        try {
//            // 1. 전송할 데이터 페이로드를 생성합니다 (JSON 사용)
//            JSONObject payload = new JSONObject();
//            payload.put("latitude", point.getLatitude());
//            payload.put("longitude", point.getLongitude());
//            payload.put("time", System.currentTimeMillis());
//
//            // 2. 전체 메시지 구조를 정의합니다 (플러그인 액션 포함)
//            JSONObject message = new JSONObject();
//            message.put(KEY_ACTION, CUSTOM_DATA_ACTION);
//            message.put(ACTION_SEND_LOCATION, payload);
//
//            String jsonMessage = message.toString();
//
//            // 3. ContactManager를 사용하여 메시지를 전송합니다.
//            // ATAK은 이 메시지를 적절한 네트워크 프로토콜(TP)을 통해 대상에게 라우팅합니다.
//            Contact contact = contactManager.getContactByUid(targetUid);
//
//            if (contact != null) {
//                // ContactUtil.sendMessage는 Chat/Message 기능을 사용하여 메시지를 전송합니다.
//                ContactUtil.sendMessage(contact, jsonMessage);
//                Log.d(TAG, "데이터 전송 성공: UID=" + targetUid + ", 메시지: " + jsonMessage);
//            } else {
//                Log.e(TAG, "대상 Contact를 찾을 수 없습니다: UID=" + targetUid);
//            }
//
//        } catch (Exception e) {
//            Log.e(TAG, "데이터 전송 중 오류 발생", e);
//        }
//    }
//
//
//    // =========================================================================================
//    // 2. 데이터 수신 (Receiver Logic)
//    // =========================================================================================
//
//    // 수신한 데이터를 처리할 콜백 인터페이스
//    public interface DataHandler {
//        void onDataReceived(String senderUid, GeoPoint location);
//    }
//
//    private class CustomMessageReceiver extends DocumentedBroadcastReceiver {
//
//        private final DataHandler handler;
//
//        // CustomMessageReceiver는 ContactManager의 메시지 이벤트를 수신합니다.
//        public CustomMessageReceiver(DataHandler handler) {
//            this.handler = handler;
//        }
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//
//            // ContactManager가 발송하는 기본 메시지 수신 액션을 확인합니다.
//            if (ContactManager.MESSAGE_EVENT.equals(action)) {
//
//                // 메시지 관련 정보를 Intent에서 추출합니다.
//                String senderUid = intent.getStringExtra("uid");
//                String messageText = intent.getStringExtra("message");
//
//                if (messageText == null || senderUid == null) {
//                    return;
//                }
//
//                try {
//                    // 수신된 텍스트가 플러그인의 커스텀 JSON 형식인지 확인합니다.
//                    JSONObject json = new JSONObject(messageText);
//
//                    // 1. 플러그인 고유 액션이 포함되어 있는지 확인하여 메시지를 식별합니다.
//                    if (CUSTOM_DATA_ACTION.equals(json.optString(KEY_ACTION))) {
//
//                        // 2. 메시지 유형(예: 위치 전송)을 파악합니다.
//                        if (json.has(ACTION_SEND_LOCATION)) {
//                            JSONObject payload = json.getJSONObject(ACTION_SEND_LOCATION);
//
//                            double lat = payload.getDouble("latitude");
//                            double lon = payload.getDouble("longitude");
//
//                            GeoPoint receivedPoint = new GeoPoint(lat, lon);
//
//                            Log.d(TAG, "커스텀 데이터 수신 성공: " + senderUid +
//                                    ", 위치: " + receivedPoint.toString());
//
//                            // 3. 콜백 인터페이스를 통해 데이터를 플러그인 내부 로직으로 전달합니다.
//                            handler.onDataReceived(senderUid, receivedPoint);
//                        }
//                    }
//
//                } catch (Exception e) {
//                    // 일반적인 채팅 메시지는 여기서 JSON 파싱 오류가 발생하지만 무시합니다.
//                    // 해당 메시지는 플러그인 커스텀 데이터가 아닌 일반 채팅 메시지일 가능성이 높습니다.
//                    // Log.d(TAG, "일반 메시지 수신 또는 파싱 오류");
//                }
//            }
//        }
//    }
//
//    private CustomMessageReceiver receiver;
//
//    /**
//     * 커스텀 메시지 수신을 위한 브로드캐스트 리시버를 등록합니다.
//     * @param handler 수신된 데이터를 처리할 핸들러
//     */
//    public void registerReceiver(DataHandler handler) {
//        if (receiver == null) {
//            receiver = new CustomMessageReceiver(handler);
//        }
//
//        DocumentedIntentFilter filter = new DocumentedIntentFilter(
//                ContactManager.MESSAGE_EVENT,
//                "ATAK 연락처 메시지 이벤트를 수신하여 커스텀 데이터 처리를 합니다."
//        );
//
//        // ATAK의 브로드캐스트 시스템에 리시버를 등록합니다.
//        MapView.get=MapView.getMapView().getContext();
//        pluginContext.registerReceiver(receiver, filter);
//        Log.d(TAG, "커스텀 메시지 리시버 등록 완료");
//    }
//
//    /**
//     * 리시버 등록을 해제합니다. (플러그인 종료 시 필수)
//     */
//    public void unregisterReceiver() {
//        if (receiver != null) {
//            pluginContext.unregisterReceiver(receiver);
//            receiver = null;
//            Log.d(TAG, "커스텀 메시지 리시버 해제 완료");
//        }
//    }
//}
