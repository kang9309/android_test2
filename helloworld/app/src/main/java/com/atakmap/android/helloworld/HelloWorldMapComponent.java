
package com.atakmap.android.helloworld;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.atak.plugins.impl.AtakPluginRegistry;
import com.atakmap.android.contact.ContactLocationView;
import com.atakmap.android.cot.CotMapComponent;
import com.atakmap.android.cot.UIDHandler;
import com.atakmap.android.cot.detail.CotDetailHandler;
import com.atakmap.android.cot.detail.CotDetailManager;
import com.atakmap.android.cotdetails.ExtendedInfoView;
import com.atakmap.android.cotdetails.extras.ExtraDetailsManager;
import com.atakmap.android.cotdetails.extras.ExtraDetailsProvider;
import com.atakmap.android.data.URIContentManager;
import com.atakmap.android.dropdown.DropDownMapComponent;
import com.atakmap.android.helloworld.aidl.ILogger;
import com.atakmap.android.helloworld.aidl.SimpleService;
import com.atakmap.android.helloworld.importer.HelloImportResolver;
import com.atakmap.android.helloworld.plugin.R;
import com.atakmap.android.helloworld.routes.RouteExportMarshal;
import com.atakmap.android.helloworld.sender.HelloWorldContactSender;
import com.atakmap.android.helloworld.service.ExampleAidlService;
import com.atakmap.android.helloworld.view.ViewOverlayExample;
import com.atakmap.android.importexport.CotEventFactory;
import com.atakmap.android.importexport.ExporterManager;
import com.atakmap.android.importexport.ImportExportMapComponent;
import com.atakmap.android.importexport.ImportReceiver;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.ipc.AtakBroadcast.DocumentedIntentFilter;
import com.atakmap.android.ipc.DocumentedExtra;
import com.atakmap.android.layers.LayersMapComponent;
import com.atakmap.android.maps.MapEvent;
import com.atakmap.android.maps.MapEventDispatcher;
import com.atakmap.android.maps.MapEventDispatcher.MapEventDispatchListener;
import com.atakmap.android.maps.MapGroup;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Marker;
import com.atakmap.android.maps.PointMapItem;
import com.atakmap.android.maps.graphics.GLMapItemFactory;
import com.atakmap.android.munitions.DangerCloseReceiver;
import com.atakmap.android.preference.AtakPreferences;
import com.atakmap.android.radiolibrary.RadioMapComponent;
import com.atakmap.android.statesaver.StateSaverPublisher;
import com.atakmap.android.user.FilterMapOverlay;
import com.atakmap.android.user.geocode.GeocodeManager;
//import com.atakmap.android.plugin.PluginManager;
//import com.atakmap.android.plugin.PluginMessage;
import com.atakmap.app.preferences.ToolsPreferenceFragment;
import com.atakmap.comms.CommsMapComponent;
import com.atakmap.coremap.concurrent.NamedThreadFactory;
import com.atakmap.coremap.cot.event.CotDetail;
import com.atakmap.coremap.cot.event.CotEvent;
import com.atakmap.coremap.cot.event.CotPoint;


import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoBounds;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.maps.time.CoordinatedTime;
//import com.atakmap.coremap.cot.geo.CotPoint;

import com.atakmap.net.AtakAuthenticationCredentials;
import com.atakmap.net.AtakAuthenticationDatabase;
import com.atakmap.net.DeviceProfileClient;



import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;



/**
 * This is an example of a MapComponent within the ATAK 
 * ecosphere.   A map component is the building block for all
 * activities within the system.   This defines a concrete 
 * thought or idea. 
 */
public class HelloWorldMapComponent extends DropDownMapComponent implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = "HelloWorldMapComponent";

    private Context pluginContext;
    // 1. Context를 저장할 private 멤버 변수 선언
//    private Context context;
    // 2. 생성자에서 Context를 전달받아 저장합니다.
    // 플러그인 매니저가 이 클래스를 인스턴스화할 때 Context를 넘겨줍니다.
//    public HelloWorldMapComponent(final Context context) {
//        super(); // 부모 클래스의 기본 생성자 호출
//    }

    public static final String ACTION_TO_HELLOJNI = "com.myplugin.HELLOJNI_MESSAGE";

    private HelloWorldDropDownReceiver dropDown;
    private WebViewDropDownReceiver wvdropDown;
    private HelloWorldMapOverlay mapOverlay;
    private View genericRadio;
    private SpecialDetailHandler sdh;
    private CotDetailHandler aaaDetailHandler;
    private ContactLocationView.ExtendedSelfInfoFactory extendedselfinfo;
    private HelloWorldContactSender contactSender;
    private HelloWorldWidget helloWorldWidget;
    private ViewOverlayExample viewOverlayExample;
    private ExtraDetailsProvider edp;
    private HelloImportResolver helloImporter;

    private AtakPreferences prefs;
    private AtakAuthenticationCredentials authenticationCredentials;

    @Override
    public void onStart(final Context context, final MapView view) {
        Log.d(TAG, "onStart");
    }

    @Override
    public void onPause(final Context context, final MapView view) {
        Log.d(TAG, "onPause");
        sendCustomDataCot("test", "test2");
    }

    // 이 함수를 플러그인의 적절한 클래스 (예: 메인 액티비티, 서비스) 내부에 정의하세요.
    public void sendCustomDataCot(String text1, String text2) {
        // CoT 이벤트의 고유 식별자를 위한 타입 정의. 수신 측과 동일해야 합니다.
        final String CUSTOM_COT_TYPE = "a-f-G-E-V-customdata";
        final String TAG = "CustomCotSender"; // 로그를 위한 태그
        if (text1 == null || text2 == null) {
            Log.w(TAG, "text1 또는 text2가 null입니다. 데이터를 보내지 않습니다.");
            return;
        }

        try {
            String eventUid = "custom-data-" + UUID.randomUUID().toString();

            MapView mapView = MapView.getMapView();
            if (mapView == null) {
                Log.e(TAG, "MapView is null, cannot send CoT event.");
                return;
            }

            GeoPoint geoPoint = new GeoPoint(0.0, 0.0, 0.0, GeoPoint.UNKNOWN, GeoPoint.UNKNOWN);

            Marker tempMarker = new Marker(geoPoint, eventUid);
            tempMarker.setType(CUSTOM_COT_TYPE);
            tempMarker.setMetaString("how", "m-g");
            tempMarker.setMetaString("callsign", "CustomDataSender");
            tempMarker.setVisible(false);

            CotEvent cotEvent = CotEventFactory.createCotEvent(tempMarker);

            cotEvent.setType(CUSTOM_COT_TYPE);
            cotEvent.setHow("m-g");

            // *******************************************************************
            // ** 6. 최종 수정: 수동으로 CotDetail을 구성하여 Element 내용을 복사 **
            // *******************************************************************

            // text1 Element를 위한 CotDetail 생성
            CotDetail text1Detail = new CotDetail("text1");
            text1Detail.setAttribute("value", text1);

            // text2 Element를 위한 CotDetail 생성
            CotDetail text2Detail = new CotDetail("text2");
            text2Detail.setAttribute("value", text2);

            // 커스텀 데이터의 루트 Detail 생성. 이름은 '__custom_data'여야 합니다.
            CotDetail customDataContainer = new CotDetail("__custom_data");

            // 2. 자식 Detail 추가 (text1, text2)
            customDataContainer.addChild(text1Detail);
            customDataContainer.addChild(text2Detail);

            // 3. *진짜 루트 Detail*을 생성합니다. 이름은 'detail'이어야 합니다.
            // 이 코드가 CotEvent.setDetail()에 전달됩니다.
            CotDetail cotDetailRoot = new CotDetail("detail");
            // 4. 진짜 루트 Detail에 커스텀 데이터 컨테이너를 자식으로 추가합니다.
            cotDetailRoot.addChild(customDataContainer);

            // ********************************************************
            // ** 7. CotEvent에 최종 Detail 객체 설정 **
            // ********************************************************
//            cotEvent.setDetail(rootDetail); // 수동으로 구성된 Detail 설정
            cotEvent.setDetail(cotDetailRoot); // **cotDetailRoot**를 사용해야 함

            // ************************************************
            // ** 8. CoT 전송: sendCotMessage 사용 (이전 오류 해결된 메서드) **
            // ************************************************
            String cotXml = cotEvent.toString();

            if (cotXml != null) {
                Intent cotIntent = new Intent("com.atakmap.android.maps.SEND_COT");
                cotIntent.putExtra("data", cotXml);
                // CotMapComponent 내에서 context를 사용할 수 있는 경우 context 필요
                // AtakBroadcast.getInstance().sendBroadcast(context, cotIntent);
                AtakBroadcast.getInstance().sendBroadcast(cotIntent); // MapComponent에서 Context 없이 전송
            }


            Log.d(TAG, "Custom CoT event sent: Type=" + CUSTOM_COT_TYPE + ", Text1='" + text1 + "', Text2='" + text2 + "'");

        } catch (Exception e) { // 기타 일반적인 예외 처리
            Log.e(TAG, "Failed to send custom CoT message", e);
        }
    }
    /**
     * org.w3c.dom.Element 객체를 XML 문자열로 변환합니다.
     * @param element 변환할 Element 객체
     * @return 변환된 XML 문자열
     * @throws Exception 변환 중 오류가 발생할 경우 (TransformerException 등)
     */
    private String toXmlString(Element element) throws Exception {
        // 1. TransformerFactory 인스턴스 생성
        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        // 2. Transformer 인스턴스 생성
        Transformer transformer = transformerFactory.newTransformer();

        // 3. XML 출력 시 선언(<?xml ...>)을 생략하도록 설정
        // ATAK CoT의 Detail 부분은 보통 XML 선언이 필요 없습니다.
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        // 4. 출력을 위한 StringWriter 객체 생성
        StringWriter writer = new StringWriter();

        // 5. Element를 Source로, StringWriter를 Result로 설정하여 변환 실행
        transformer.transform(new DOMSource(element), new StreamResult(writer));

        // 6. 변환된 문자열 반환
        return writer.getBuffer().toString();
    }
    private String escapeXml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    @Override
    public void onResume(final Context context,
            final MapView view) {
        Log.d(TAG, "onResume");
//        Log.d(TAG, "ksh_test onResume");
//        sendTestMessageToHelloJNI("test");
//        Log.d(TAG, "onResume end");
    }

    @Override
    public void onStop(final Context context,
            final MapView view) {
        Log.d(TAG, "onStop");
    }

    public void sendTestMessageToHelloJNI(String message) {
        Log.d(TAG, "sendTestMessageToHelloJNI");
        // 1. Intent 객체 생성 및 고유 Action 설정
        Intent intent = new Intent(ACTION_TO_HELLOJNI);

        // 2. 전송할 데이터를 Intent에 추가 (예: "payload" 키 사용)
        intent.putExtra("payload", message);

        // 3. AtakBroadcast를 사용하여 Intent 전송
        // 'context'는 MapComponent가 초기화될 때 전달받은 유효한 Context여야 합니다.
//        AtakBroadcast.getInstance().sendBroadcast(context, intent);
        AtakBroadcast.getInstance().sendBroadcast(intent);

        Log.d(TAG, "Broadcast Sent to hellojni: " + message);
//        System.out.println("Broadcast Sent to hellojni: " + message);
    }

//    public void sendTestMessageToHelloJNI(String message) {
//        // 1. 대상 플러그인의 UID 정의
//        // 'hellojni' 플러그인이 메시지 리스너를 등록할 때 사용한 고유 ID를 사용해야 합니다.
//        // 일반적으로 플러그인 이름(예: "HelloWorldPlugin", "hellojni")을 UID로 사용합니다.
//        final String TARGET_PLUGIN_UID = "hellojni";
//
//        // 2. PluginMessage 객체 생성
//        // 대상 UID와 전송할 String 데이터를 페이로드로 지정합니다.
//        PluginMessage pluginMessage = new PluginMessage(TARGET_PLUGIN_UID, message);
//
//        // 3. PluginManager를 통해 메시지 송신
//        // 이 메시지를 수신하려면 'hellojni' 플러그인이 TARGET_PLUGIN_UID로 리스너를 등록해야 합니다.
//        PluginManager.getInstance().sendMessage(pluginMessage);
//
//        System.out.println("Message Sent to [" + TARGET_PLUGIN_UID + "]: " + message);
//    }

//    public void sendCotMessage(double lat, double lon) {
//        try {
//            // ✅ 고유 식별자 (UID)
//            String uid = "plugin-helloworld-" + java.util.UUID.randomUUID();
//
//            // ✅ 시간 정보 설정
//            com.atakmap.coremap.maps.time.CoordinatedTime now =
//                    new com.atakmap.coremap.maps.time.CoordinatedTime();
//            com.atakmap.coremap.maps.time.CoordinatedTime stale =
//                    new com.atakmap.coremap.maps.time.CoordinatedTime(now.getMilliseconds() + 60_000);
//
//            // ✅ 지도 중심 좌표 가져오기 (또는 직접 lat/lon 사용)
//            com.atakmap.android.maps.MapView mapView = com.atakmap.android.maps.MapView.getMapView();
////            com.atakmap.coremap.maps.coords.GeoPoint gp = mapView.getCenterPoint().get();
////
////            // ※ 중심 좌표 대신 인자로 받은 lat/lon을 사용할 수도 있음
////            com.atakmap.coremap.maps.coords.GeoPoint point =
////                    new com.atakmap.coremap.maps.coords.GeoPoint(lat, lon, 0.0);
//
//            // 지도 중심 좌표 가져오기
//            GeoPoint gp = mapView.getCenterPoint().get();
//
//            // CotPoint 객체 생성 (CotEvent 생성자용)
//            CotPoint point = new CotPoint(gp);
//
//            // ✅ CoT 이벤트 생성
//            // 기존 (에러 발생)
////            com.atakmap.coremap.maps.CotEvent event = ...
//
//            // 변경
//            com.atakmap.coremap.cot.event.CotEvent event =
//                    new com.atakmap.coremap.cot.event.CotEvent(
//                            uid,               // UID
//                            "b-t-f",           // Type (Blue Force Friendly)
//                            "m-g",             // How (machine-generated)
//                            point,             // GeoPoint
//                            now,               // start
//                            stale,             // stale
//                            now,               // how time
//                            "HelloWorld",      // detail name
//                            null,              // detail (null 가능)
//                            "Hello World!",    // remarks
//                            "plugin",          // creator group
//                            "helloworld"       // creator name
//                    );
//
//            // ✅ CoT 송신
////            com.atakmap.android.cot.CotMapComponent.getInstance().sendCot(event);
//            // sendCotMessage 사용
////            com.atakmap.android.cot.CotMapComponent.getInstance().sendCotMessage(event);
//            CotEventBus.getInstance(mapView).post(event);
//
//            android.util.Log.i("ATAK_HelloWorld", "📤 Sent CoT message: " + uid
//                    + " (" + lat + ", " + lon + ")");
//        } catch (Exception e) {
//            android.util.Log.e("ATAK_HelloWorld", "sendCotMessage failed", e);
//        }
//    }


    /**
     * Simple uncalled example for how to import a file.
     */
    private void importFileExample(final File file) {
        /**
         * Case 1 where the file type is known and in this example, the file is a map type.
         */
        Log.d(TAG, "testImport: " + file.toString());
        Intent intent = new Intent(
                ImportExportMapComponent.ACTION_IMPORT_DATA);
        intent.putExtra(ImportReceiver.EXTRA_URI,
                file.getAbsolutePath());
        intent.putExtra(ImportReceiver.EXTRA_CONTENT,
                LayersMapComponent.IMPORTER_CONTENT_TYPE);
        intent.putExtra(ImportReceiver.EXTRA_MIME_TYPE,
                LayersMapComponent.IMPORTER_DEFAULT_MIME_TYPE);

        AtakBroadcast.getInstance().sendBroadcast(intent);
        Log.d(TAG, "testImportDone: " + file);

        /**
         * Case 2 where the file type is unknown and the file is just imported.
         */
        Log.d(TAG, "testImport: " + file);
        intent = new Intent(
                ImportExportMapComponent.USER_HANDLE_IMPORT_FILE_ACTION);
        intent.putExtra("filepath", file.toString());
        intent.putExtra("importInPlace", false); // copies it over to the general location if true
        intent.putExtra("promptOnMultipleMatch", true); //prompts the users if this could be multiple things
        intent.putExtra("zoomToFile", false); // zoom to the outer extents of the file.
        AtakBroadcast.getInstance().sendBroadcast(intent);
        Log.d(TAG, "testImportDone: " + file);

    }

    @Override
    public void onCreate(final Context context, Intent intent,
            final MapView view) {

        // Set the theme.  Otherwise, the plugin will look vastly different
        // than the main ATAK experience.   The theme needs to be set 
        // programatically because the AndroidManifest.xml is not used.
        context.setTheme(R.style.ATAKPluginTheme);

        super.onCreate(context, intent, view);
        // ✅ [수정] onCreate()에서 Context를 전달받아 저장합니다.
//        this.context = context;

        pluginContext = context;

        GLMapItemFactory.registerSpi(GLSpecialMarker.SPI);

        // Register capability to handle detail tags that TAK does not 
        // normally process.
        CotDetailManager.getInstance().registerHandler(
                "__special",
                sdh = new SpecialDetailHandler());

        CotDetailManager.getInstance().registerHandler(
                aaaDetailHandler = new CotDetailHandler("__aaa") {
                    private final String TAG = "AAACotDetailHandler";

                    @Override
                    public CommsMapComponent.ImportResult toItemMetadata(
                            MapItem item, CotEvent event, CotDetail detail) {
                        Log.d(TAG, "detail received: " + detail + " in:  "
                                + event);
                        return CommsMapComponent.ImportResult.SUCCESS;
                    }

                    @Override
                    public boolean toCotDetail(MapItem item, CotEvent event,
                            CotDetail root) {
                        Log.d(TAG, "converting to cot detail from: "
                                + item.getUID());
                        return true;
                    }
                });

        // __custom_data 태그를 처리하는 핸들러 등록
// **수정: registerHandler(Handler) 오버로드를 사용하도록 변경**
        CotDetailManager.getInstance().registerHandler(
                new CotDetailHandler("__custom_data") {
                    private final String TAG = "CustomDataHandler";

                    @Override
                    public CommsMapComponent.ImportResult toItemMetadata(
                            MapItem item, CotEvent event, CotDetail detail) {

                        // 1. 이벤트의 타입이 우리가 보낸 타입인지 확인 (선택 사항이지만 안전함)
                        if (!event.getType().equals("a-f-G-E-V-customdata")) {
                            return CommsMapComponent.ImportResult.SUCCESS;
                        }

                        // 2. CotDetail에서 자식 Detail을 찾습니다. (이전 수정된 0 인자 유지)
                        CotDetail text1Detail = detail.getFirstChildByName(0, "text1");
                        CotDetail text2Detail = detail.getFirstChildByName(0, "text2");

                        // 3. 값 추출.
                        String text1 = text1Detail != null ? text1Detail.getAttribute("value") : "N/A";
                        String text2 = text2Detail != null ? text2Detail.getAttribute("value") : "N/A";

                        // 4. 로컬에서 원하는 동작 수행 (로그 출력)
                        Log.d(TAG, "Custom Data Received LOCALLY: Text1=" + text1 + ", Text2=" + text2);

                        // 5. 이 데이터를 MapItem의 메타데이터로 저장
                        item.setMetaString("custom_text1", text1);
                        item.setMetaString("custom_text2", text2);

                        return CommsMapComponent.ImportResult.SUCCESS;
                    }

                    @Override
                    public boolean toCotDetail(MapItem item, CotEvent event, CotDetail root) {
                        return true;
                    }
                }); // **인자 하나만 전달**

        //HelloWorld MapOverlay added to Overlay Manager.
        this.mapOverlay = new HelloWorldMapOverlay(view, pluginContext);
        view.getMapOverlayManager().addOverlay(this.mapOverlay);

        //MapView.getMapView().getRootGroup().getChildGroupById(id).setVisible(true);

        /*Intent new_cot_intent = new Intent();
        new_cot_intent.setAction("com.atakmap.android.maps.COT_PLACED");
        new_cot_intent.putExtra("uid", point.getUID());
        AtakBroadcast.getInstance().sendBroadcast(
                new_cot_intent);*/

        // End of Overlay Menu Test ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        // In this example, a drop down receiver is the 
        // visual component within the ATAK system.  The 
        // trigger for this visual component is an intent.   
        // see the plugin.HelloWorldTool where that intent
        // is triggered.
        this.dropDown = new HelloWorldDropDownReceiver(view, context,
                this.mapOverlay);

        // We use documented intent filters within the system
        // in order to automatically document all of the 
        // intents and their associated purposes.

        Log.d(TAG, "registering the show hello world filter");
        DocumentedIntentFilter ddFilter = new DocumentedIntentFilter();
        ddFilter.addAction(HelloWorldDropDownReceiver.SHOW_HELLO_WORLD,
                "Show the Hello World drop-down");
        ddFilter.addAction(HelloWorldDropDownReceiver.CHAT_HELLO_WORLD,
                "Chat message sent to the Hello World contact");
        ddFilter.addAction(HelloWorldDropDownReceiver.SEND_HELLO_WORLD,
                "Sending CoT to the Hello World contact");
        ddFilter.addAction(HelloWorldDropDownReceiver.LAYER_DELETE,
                "Delete example layer");
        ddFilter.addAction(HelloWorldDropDownReceiver.LAYER_VISIBILITY,
                "Toggle visibility of example layer");
        this.registerDropDownReceiver(this.dropDown, ddFilter);
        Log.d(TAG, "registered the show hello world filter");

        this.wvdropDown = new WebViewDropDownReceiver(view, context);
        Log.d(TAG, "registering the webview filter");
        DocumentedIntentFilter wvFilter = new DocumentedIntentFilter();
        wvFilter.addAction(WebViewDropDownReceiver.SHOW_WEBVIEW,
                "web view");
        this.registerDropDownReceiver(this.wvdropDown, wvFilter);

        // in this case we also show how one can register
        // additional information to the uid detail handle when 
        // generating cursor on target.   Specifically the 
        // NETT-T service specification indicates the the 
        // details->uid should be filled in with an appropriate
        // attribute.   

        // add in the nett-t required uid entry.
        UIDHandler.getInstance().addAttributeInjector(
                new UIDHandler.AttributeInjector() {
                    public void injectIntoDetail(Marker marker,
                            CotDetail detail) {
                        if (marker.getType().startsWith("a-f"))
                            return;
                        detail.setAttribute("nett", "XX");
                    }

                    public void injectIntoMarker(CotDetail detail,
                            Marker marker) {
                        if (marker.getType().startsWith("a-f"))
                            return;
                        String callsign = detail.getAttribute("nett");
                        if (callsign != null)
                            marker.setMetaString("nett", callsign);
                    }

                });

        // In order to use shared preferences with a plugin you will need
        // to use the context from ATAK since it has the permission to read
        // and write preferences.
        // Additionally - in the XML file you cannot use PreferenceCategory
        // to enclose your Preferences - otherwise the preference will not
        // be persisted.   You can fake a PreferenceCategory by adding an
        // empty preference category at the top of each group of preferences.
        // See how this is done in the current example.

        DangerCloseReceiver.ExternalMunitionQuery emq = new DangerCloseReceiver.ExternalMunitionQuery() {
            @Override
            public String queryMunitions() {
                return BuildExternalMunitionsQuery();
            }
        };

        DangerCloseReceiver.getInstance().setExternalMunitionQuery(emq);

        // for custom preferences
        ToolsPreferenceFragment
                .register(
                        new ToolsPreferenceFragment.ToolPreference(
                                "Hello World Preferences",
                                "This is the sample preference for Hello World",
                                "helloWorldPreference",
                                context.getResources().getDrawable(
                                        R.drawable.ic_launcher, null),
                                new HelloWorldPreferenceFragment(context)));

        // example for how to register a radio with the radio map control.

        LayoutInflater inflater = LayoutInflater.from(pluginContext);
        genericRadio = inflater.inflate(R.layout.radio_item_generic, null);

        RadioMapComponent.getInstance().registerControl("generic-radio-uid", genericRadio);

        // demonstrate how to customize the view for ATAK contacts.   In this case
        // it will show a customized line of test when pulling up the contact 
        // detail view.
        ContactLocationView.register(
                extendedselfinfo = new ContactLocationView.ExtendedSelfInfoFactory() {
                    @Override
                    public ExtendedInfoView createView() {
                        return new ExtendedInfoView(view.getContext()) {
                            @Override
                            public void setMarker(PointMapItem m) {
                                Log.d(TAG, "setting the marker: "
                                        + m.getMetaString("callsign", ""));
                                TextView tv = new TextView(view.getContext());
                                tv.setLayoutParams(new LayoutParams(
                                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                                        LayoutParams.WRAP_CONTENT));
                                this.addView(tv);
                                tv.setText("Example: " + m
                                        .getMetaString("callsign", "unknown"));

                            }
                        };
                    }
                });

        // send out some customized information as part of the SA or PPLI message.
        CotDetail cd = new CotDetail("temp");
        cd.setAttribute("temp", Integer.toString(76));
        CotMapComponent.getInstance().addAdditionalDetail(cd.getElementName(),
                cd);

        // register a listener for when a the radial menu asks for a special 
        // drop down.  SpecialDetail is really a skeleton of a class that 
        // shows a very basic drop down.
        DocumentedIntentFilter filter = new DocumentedIntentFilter();
        filter.addAction("com.atakmap.android.helloworld.myspecialdetail",
                "this intent launches the special drop down",
                new DocumentedExtra[] {
                        new DocumentedExtra("targetUID",
                                "the map item identifier used to populate the drop down")
                });
        registerDropDownReceiver(new SpecialDetail(view, pluginContext),
                filter);

        //see if any hello profiles/data are available on the TAK Server. Requires the server to be
        //properly configured, and "Apply TAK Server profile updates" setting enabled in ATAK prefs
        Log.d(TAG, "Checking for Hello profile on TAK Server");
        DeviceProfileClient.getInstance().getProfile(view.getContext(),
                "hello");

        //register profile request to run upon connection to TAK Server, in case we're not yet
        //connected, or the the request above fails
        CotMapComponent.getInstance().addToolProfileRequest("hello");

        registerSpisVisibilityListener(view);

        view.addOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent event) {
                Log.d(TAG, "dispatchKeyEvent: " + event.toString());
                return false;
            }
        });

        GeocodeManager.getInstance(context).registerGeocoder(fakeGeoCoder);

        TextView tv = new TextView(context);
        LayoutParams lp_tv = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        lp_tv.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        tv.setText("Test Center Layout");
        tv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "Test Test Test");
            }
        });
        com.atakmap.android.video.VideoDropDownReceiver.registerVideoViewLayer(
                new com.atakmap.android.video.VideoViewLayer("test-layer", tv,
                        lp_tv));

        ExporterManager.registerExporter(
                context.getString(R.string.route_exporter_name),
                context.getDrawable(R.drawable.ic_route),
                RouteExportMarshal.class);

        // Code to listen for when a state saver is completely loaded or wait to perform some action
        // after all of the markers are completely loaded.

        final BroadcastReceiver ssLoadedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                reprocessMapItems();
            }
        };

        AtakBroadcast.getInstance().registerReceiver(ssLoadedReceiver,
                new DocumentedIntentFilter(
                        StateSaverPublisher.STATESAVER_COMPLETE_LOAD));
        // because the plugin can be loaded after the above intent has been fired, there is a method
        // to check to see if a load has already occured.

        if (StateSaverPublisher.isFinished()) {
            // no need to listen for the intent
            AtakBroadcast.getInstance().unregisterReceiver(ssLoadedReceiver);
            reprocessMapItems();
        }

        // example of how to save and retrieve credentials using the credential management system
        // within core ATAK
        saveAndRetrieveCredentials();

        // Content sender example
        URIContentManager.getInstance().registerSender(
                contactSender = new HelloWorldContactSender(view,
                        pluginContext));

        helloWorldWidget = new HelloWorldWidget();
        helloWorldWidget.onCreate(context, intent, view);

        viewOverlayExample = new ViewOverlayExample();
        viewOverlayExample.onCreate(context, intent, view);

        Log.d(TAG, "binding to the simple aidl service");
        final Intent serviceIntent = new Intent(pluginContext,
                ExampleAidlService.class);
        view.getContext().bindService(serviceIntent, connection,
                Context.BIND_AUTO_CREATE);
        Log.d(TAG, "finished calling bindService to the simple aidl service");


        // In this example we only need to request the permission if the OS is 13 or higher - but
        // one can adapt this example for any number of versions.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            final Intent permissionActivity = new Intent(context, PluginPermissionActivity.class);
            permissionActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(permissionActivity);

            DocumentedIntentFilter dif = new DocumentedIntentFilter(PluginPermissionActivity.PLUGIN_PERMISSION_REQUEST_ERROR);
            AtakBroadcast.getInstance().registerSystemReceiver(br, dif);
        }

        ExtraDetailsManager.getInstance().addProvider(edp = new ExtraDetailsProvider() {
            @Override
            public View getExtraView(MapItem mapItem, View existing) {
                if (existing == null) {

                    TextView tv = new TextView(view.getContext());
                    tv.setBackgroundColor(Color.RED);
                    tv.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    existing = tv;
                }
                if (mapItem != null)
                    ((TextView)existing).setText("Extra User Experience Provided by HelloWorld: " + mapItem.getTitle());

                return existing;
            }
        });

        ImportExportMapComponent.getInstance().addImporterClass(
                this.helloImporter = new HelloImportResolver(view));



        // handling a preference  SharedPreferences saved preferences

        prefs = AtakPreferences.getInstance(context);
        prefs.registerListener(this);

        // handling encrypted username and password from a preference
        authenticationCredentials =
                AtakAuthenticationDatabase.getCredentials(HelloWorldPreferenceFragment.CREDENTIALS_DB_KEY);
        if (authenticationCredentials != null) {
            Log.d(TAG, "saved username and password: " +
                    authenticationCredentials.username + " " + authenticationCredentials.password + " " + authenticationCredentials.site + " " + authenticationCredentials.type);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key == null)
            return;

        if (key.equals(HelloWorldPreferenceFragment.CREDENTIALS_PREF_KEY)) {
            authenticationCredentials = AtakAuthenticationDatabase.getCredentials(HelloWorldPreferenceFragment.CREDENTIALS_DB_KEY);
            if (authenticationCredentials != null) {
                Log.d(TAG, "saved username and password: " +
                        authenticationCredentials.username + " " + authenticationCredentials.password + " " + authenticationCredentials.site + " " + authenticationCredentials.type);
            }
        } else if (key.equals("key_for_helloworld")) {
            Log.d(TAG, "edit text preference (\"key_for_helloworld\"): " + sharedPreferences.getString(key, ""));
        }
    }

    final BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            if (action == null)
                return;

            if (action.equals(PluginPermissionActivity.PLUGIN_PERMISSION_REQUEST_ERROR)) {
                Log.d(TAG, "user has decided not to provide the appropriate permissions");
                AtakPluginRegistry.get().unloadPlugin("com.atakmap.android.helloworld.plugin");
            }
        }
    };


    private final GeocodeManager.Geocoder fakeGeoCoder = new GeocodeManager.Geocoder() {
        @Override
        public String getUniqueIdentifier() {
            return "fake-geocoder";
        }

        @Override
        public String getTitle() {
            return "Gonna get you Lost";
        }

        @Override
        public String getDescription() {
            return "Sample Geocoder implementation registered with TAK";
        }

        @Override
        public boolean testServiceAvailable() {
            return true;
        }

        @Override
        public List<Address> getLocation(GeoPoint geoPoint) {
            Address a = new Address(Locale.getDefault());
            a.setAddressLine(0, "100 WrongWay Street");
            a.setAddressLine(1, "Boondocks, Nowhere");
            a.setCountryCode("UNK");
            a.setPostalCode("999999");
            a.setLatitude(geoPoint.getLatitude());
            a.setLongitude(geoPoint.getLongitude());
            return new ArrayList<>(Collections.singleton(a));
        }

        @Override
        public List<Address> getLocation(String s, GeoBounds geoBounds) {
            Address a = new Address(Locale.getDefault());
            a.setAddressLine(0, "100 WrongWay Street");
            a.setAddressLine(1, "Boondocks, Nowhere");
            a.setCountryCode("UNK");
            a.setPostalCode("999999");
            a.setLatitude(0);
            a.setLongitude(0);
            return new ArrayList<>(Collections.singleton(a));
        }
    };

    private void registerSpisVisibilityListener(MapView view) {
        spiListener = new SpiListener(view);
        for (int i = 0; i < 4; ++i) {
            MapItem mi = view
                    .getMapItem(view.getSelfMarker().getUID() + ".SPI" + i);
            if (mi != null) {
                mi.addOnVisibleChangedListener(spiListener);
            }
        }

        final MapEventDispatcher dispatcher = view.getMapEventDispatcher();
        dispatcher.addMapEventListener(MapEvent.ITEM_REMOVED, spiListener);
        dispatcher.addMapEventListener(MapEvent.ITEM_ADDED, spiListener);

    }

    private SpiListener spiListener;

    private static class SpiListener implements MapEventDispatchListener,
            MapItem.OnVisibleChangedListener {
        private final MapView view;

        SpiListener(MapView view) {
            this.view = view;
        }

        @Override
        public void onMapEvent(MapEvent event) {
            MapItem item = event.getItem();
            if (item == null)
                return;
            if (event.getType().equals(MapEvent.ITEM_ADDED)) {
                if (item.getUID()
                        .startsWith(view.getSelfMarker().getUID() + ".SPI")) {
                    item.addOnVisibleChangedListener(this);
                    Log.d(TAG, "visibility changed for: " + item.getUID() + " "
                            + item.getVisible());
                }
            } else if (event.getType().equals(MapEvent.ITEM_REMOVED)) {
                if (item.getUID()
                        .startsWith(view.getSelfMarker().getUID() + ".SPI"))
                    item.removeOnVisibleChangedListener(this);
            }
        }

        @Override
        public void onVisibleChanged(MapItem item) {
            Log.d(TAG, "visibility changed for: " + item.getUID() + " "
                    + item.getVisible());
        }
    }

    @Override
    protected void onDestroyImpl(Context context, MapView view) {
        prefs.unregisterListener(this);
        ExtraDetailsManager.getInstance().removeProvider(edp);
        helloWorldWidget.onDestroyWidgets(context, view);
        viewOverlayExample.onDestroy(context, view);
        Log.d(TAG, "calling on destroy");
        ContactLocationView.unregister(extendedselfinfo);
        GLMapItemFactory.unregisterSpi(GLSpecialMarker.SPI);
        this.dropDown.dispose();
        ToolsPreferenceFragment.unregister("helloWorldPreference");
        RadioMapComponent.getInstance().unregisterControl("generic-radio-uid");
        view.getMapOverlayManager().removeOverlay(mapOverlay);
        CotDetailManager.getInstance().unregisterHandler(
                sdh);
        CotDetailManager.getInstance().unregisterHandler(aaaDetailHandler);
        ExporterManager.unregisterExporter(
                context.getString(R.string.route_exporter_name));
        URIContentManager.getInstance().unregisterSender(contactSender);
        if (helloImporter != null) {
            ImportExportMapComponent.getInstance().removeImporterClass(this.helloImporter);
        }
        super.onDestroyImpl(context, view);

        // Example call on how to end ATAK if the plugin is unloaded.
        // It would be important to possibly show the user a dialog etc.

        //Intent intent = new Intent("com.atakmap.app.QUITAPP");
        //intent.putExtra("FORCE_QUIT", true);
        //AtakBroadcast.getInstance().sendBroadcast(intent);

    }

    private String BuildExternalMunitionsQuery() {
        String xmlString = "";
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory
                    .newDocumentBuilder();
            Document doc = documentBuilder.newDocument();

            Element rootEl = doc.createElement("Current_Flights");
            Element catEl = doc.createElement("category");
            catEl.setAttribute("name", "lead");
            Element weaponEl = doc.createElement("weapon");
            weaponEl.setAttribute("name", "GBU-12");
            weaponEl.setAttribute("proneprotected", "130");
            weaponEl.setAttribute("standing", "175");
            weaponEl.setAttribute("prone", "200");
            weaponEl.setAttribute("description", "(500-lb LGB)");
            weaponEl.setAttribute("active", "false");
            weaponEl.setAttribute("id", "1");
            catEl.appendChild(weaponEl);
            rootEl.appendChild(catEl);
            doc.appendChild(rootEl);

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();

            DOMSource domSource = new DOMSource(doc.getDocumentElement());
            OutputStream output = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(output);

            transformer.transform(domSource, result);
            xmlString = output.toString();
        } catch (Exception ex) {
            Log.d(TAG, "Exception in BuildExternalMunitionsQuery: "
                    + ex.getMessage());
        }
        return xmlString;
    }

    /**
     * This is a simple example on how to save, retrieve and delete credentials in ATAK using the
     * credential management system.
     */
    private void saveAndRetrieveCredentials() {
        AtakAuthenticationDatabase.saveCredentials("helloworld.plugin", "",
                "username", "password", false);
        // can also specify a host if needed
        AtakAuthenticationCredentials aac = AtakAuthenticationDatabase
                .getCredentials("helloworld.plugin", "");
        if (aac != null) {
            Log.d(TAG, "credentials: " + aac.username + " " + aac.password);
        }
        AtakAuthenticationDatabase.delete("helloworld.plugin", "");

        aac = AtakAuthenticationDatabase.getCredentials("helloworld.plugin",
                "");
        if (aac == null)
            Log.d(TAG, "deleted credentials");
        else
            Log.d(TAG, "credentials: " + aac.username + " " + aac.password);

    }

    private final ServiceConnection connection = new ServiceConnection() {

        SimpleService service;

        // Allow for the print out to use the atak logging mechanism that is unavaible from
        // the service.
        final ILogger logger = new ILogger.Stub() {
            @Override
            public void e(String tag, String msg, String exception)
                    throws RemoteException {
                Log.e(tag, "SERVICE: " + msg + "" + exception);
            }

            @Override
            public void d(String tag, String msg, String exception)
                    throws RemoteException {
                Log.d(tag, "SERVICE: " + msg + "" + exception);
            }
        };

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder iBinder) {

            service = SimpleService.Stub.asInterface(iBinder);
            Log.d(TAG, "connected to the simple service");
            try {
                service.registerLogger(logger);
            } catch (RemoteException ignored) {
            }

            // this could be anywhere in your plugin code.
            try {
                Log.d(TAG, "result from the service: " + service.add(2, 2));
            } catch (RemoteException ignored) {
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "disconnected from the simple service");
        }
    };



    private void reprocessMapItems() {
        final MapGroup rootGroup = MapView.getMapView().getRootGroup();
        // look for all things that start with any of the items
        reprocessMapItemsService.submit(new Runnable() {
            @Override
            public void run() {
                rootGroup.deepForEachItem(new FilterMapOverlay.TypeFilter(
                        Set.of(new String[] {"a-f", "a-n", "a-h" })) {

                    @Override
                    public boolean onItemFunction(final MapItem item) {
                        if (!super.onItemFunction(item))
                            return false;

                        //reload the marker reprocessing the detail
                        CotEvent ce = CotEventFactory.createCotEvent(item);
                        CotDetail cd = ce.getDetail().getChild("__aaa");
                        if (cd != null)
                            aaaDetailHandler.toItemMetadata(item, ce, cd);

                        // return false to continue the hunt for more, if you return true
                        // now it will stop deep searching
                        return false;
                    }
                });
            }
        });
    }

    private final ExecutorService reprocessMapItemsService =
            Executors.newSingleThreadExecutor(new NamedThreadFactory("fixup-markers"));
}
