
package com.atakmap.android.plugintemplate.plugin;

import android.content.Context;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;

import com.atak.plugins.impl.PluginContextProvider;
import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoPoint;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

import gov.tak.api.plugin.IPlugin;
import gov.tak.api.plugin.IServiceController;
import gov.tak.api.ui.IHostUIService;
import gov.tak.api.ui.Pane;
import gov.tak.api.ui.PaneBuilder;
import gov.tak.api.ui.ToolbarItem;
import gov.tak.api.ui.ToolbarItemAdapter;
import gov.tak.platform.marshal.MarshalManager;


public class PluginTemplate implements IPlugin {
    public static final String CLASS_TAG = "PluginTemplate";
    IServiceController serviceController;
    Context pluginContext;
    IHostUIService uiService;
    ToolbarItem toolbarItem;
    Pane templatePane;

    public PluginTemplate(IServiceController serviceController) {
        this.serviceController = serviceController;
        final PluginContextProvider ctxProvider = serviceController
                .getService(PluginContextProvider.class);
        if (ctxProvider != null) {
            pluginContext = ctxProvider.getPluginContext();
            pluginContext.setTheme(R.style.ATAKPluginTheme);
        }

        // obtain the UI service
        uiService = serviceController.getService(IHostUIService.class);

        // initialize the toolbar button for the plugin

        // create the button
        toolbarItem = new ToolbarItem.Builder(
                pluginContext.getString(R.string.app_name),
                MarshalManager.marshal(
                        pluginContext.getResources().getDrawable(R.drawable.ic_launcher),
                        android.graphics.drawable.Drawable.class,
                        gov.tak.api.commons.graphics.Bitmap.class))
                .setListener(new ToolbarItemAdapter() {
                    @Override
                    public void onClick(ToolbarItem item) {
                        showPane();
                    }
                })
                .build();
    }

    private void toast(String str) {
        MapView mapView = MapView.getMapView();
        Toast.makeText(mapView.getContext(), str,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart() {
        // the plugin is starting, add the button to the toolbar
        if (uiService == null)
            return;

        uiService.addToolbarItem(toolbarItem);
    }

    @Override
    public void onStop() {
        // the plugin is stopping, remove the button from the toolbar
        if (uiService == null)
            return;

        uiService.removeToolbarItem(toolbarItem);
    }

    private void showPane() {
        // instantiate the plugin view if necessary
        if (templatePane == null) {
            // 1. 레이아웃을 View 객체로 인플레이트합니다. 이 View가 Pane의 '내용물'이 됩니다.
            View pluginView = PluginLayoutInflater.inflate(pluginContext,
                    R.layout.main_layout, null);
            // 2. 인플레이트된 View 내부의 버튼 클릭 리스너를 설정합니다. (View 상호작용 준비)
            setupViewInteractions(pluginView);

            // Remember to use the PluginLayoutInflator if you are actually inflating a custom view
            // In this case, using it is not necessary - but I am putting it here to remind
            // developers to look at this Inflator

            templatePane = new PaneBuilder(pluginView)
                    // relative location is set to default; pane will switch location dependent on
                    // current orientation of device screen
                    .setMetaValue(Pane.RELATIVE_LOCATION, Pane.Location.Default)
                    // pane will take up 50% of screen width in landscape mode
                    .setMetaValue(Pane.PREFERRED_WIDTH_RATIO, 0.5D)
                    // pane will take up 50% of screen height in portrait mode
                    .setMetaValue(Pane.PREFERRED_HEIGHT_RATIO, 0.5D)
                    .build();
        }

        // if the plugin pane is not visible, show it!
        if (!uiService.isPaneVisible(templatePane)) {
            uiService.showPane(templatePane, null);
        }


    }

    /**
     * 인플레이트된 View 내부의 위젯들에 대한 상호작용(리스너)을 설정합니다.
     * 이 코드는 View가 Pane에 포함되기 직전에 실행됩니다.
     */
    private void setupViewInteractions(View pluginView) {
        try {
            Button send_multicastBTN = pluginView.findViewById(R.id.send_multicast);
            if (send_multicastBTN != null) {
                // 버튼 클릭 이벤트를 onPluginButtonClicked 메서드에 연결합니다.
                send_multicastBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(pluginContext, "send_multicast", Toast.LENGTH_SHORT).show();
                        sendCoTMulticast();
                    }
                });
            }
        } catch (Exception e) {
            // 해당 ID의 버튼이 없는 경우의 오류 처리
            Log.e(CLASS_TAG, LogUtils.getLogPosition() + e.getMessage());
//            System.err.println("Error setting button click listener: " + e.getMessage());
        }
    }

    public void sendCoTMulticast() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String MULTICAST_ADDRESS = "224.0.0.1";
                final int MULTICAST_PORT = 6969; // ATAK에서 자주 사용하는 포트
                Log.e(CLASS_TAG, LogUtils.getLogPosition() + "sendCoTMulticast.");

                try {
                    // 현재 지도 중심 좌표를 얻습니다.
                    MapView mapView = MapView.getMapView();
                    if (mapView == null) {
                        Log.e(CLASS_TAG, LogUtils.getLogPosition() + "MapView is null, cannot send CoT event.");
                        return;
                    }

                    // Chat CoT는 위치 정보가 중요하지 않지만, 기본 마커 정보는 필요합니다.
                    GeoPoint center = mapView.getSelfMarker().getPoint();
                    // 테스트를 위한 CoT XML 메시지 생성
                    String cotXml = String.format(
                            "<event version='2.0' type='a-h-G-U-T' uid='ATAK_Test_Multicast_%d' time='%s' start='%s' stale='%s' how='h-g'>" +
                                    "<point lat='%.6f' lon='%.6f' hae='0.0' ce='9999999.0' le='9999999.0'/>" +
                                    "</event>",
                            System.currentTimeMillis(),
                            "2025-01-01T00:00:00.000Z", // 실제 UTC 시간으로 포맷해야 합니다.
                            "2025-01-01T00:00:00.000Z",
                            "2025-01-01T00:00:10.000Z",
                            center.getLatitude(),
                            center.getLongitude()
                    );
                    MulticastSocket socket = new MulticastSocket();
                    InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                    byte[] buffer = cotXml.getBytes(StandardCharsets.UTF_8);

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, MULTICAST_PORT);
                    socket.send(packet);
                    socket.close();

                    Log.d(CLASS_TAG, LogUtils.getLogPosition() + "CoT 멀티캐스트 전송 성공: " + center.toString());

                } catch (IOException e) {
                    Log.e(CLASS_TAG, LogUtils.getLogPosition() + "CoT 멀티캐스트 전송 오류", e);
                }
            }
        }).start();
    }
}