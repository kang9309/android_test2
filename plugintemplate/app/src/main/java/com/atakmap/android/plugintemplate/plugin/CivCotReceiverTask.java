package com.atakmap.android.plugintemplate.plugin;



import android.util.Log;
import android.os.Bundle;
import android.widget.Toast;
import android.content.Context;

import com.atakmap.android.maps.MapView;

import android.os.Handler;
import android.os.Looper;

//import com.atakmap.android.cot.CotEvent;
import com.atakmap.coremap.cot.event.CotEvent;
//import com.atakmap.android.cot.detail.CotDetail;
import com.atakmap.coremap.cot.event.CotDetail;

import com.atakmap.android.maps.MapView;
import com.atakmap.android.cot.CotMapComponent;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.atakmap.android.cot.CotMapComponent;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.cot.event.CotEvent;
import com.atakmap.coremap.cot.event.CotDetail;
import com.atakmap.android.cot.CotEventDispatcher;
import com.atakmap.android.cot.CotEventListener;

public class CivCotReceiverTask {

    private static final String TAG = "CivCotReceiverTask";
    private final Context context;

    public CivCotReceiverTask(Context context) {
        this.context = context;
        initializeCotReceiver();
    }

    private void initializeCotReceiver() {
        try {
            // CotEventDispatcher 통해 리스너 등록
            CotEventDispatcher dispatcher = CotMapComponent.getInstance().getCotEventDispatcher();
            dispatcher.addListener(new CotEventListener() {
                @Override
                public void onCotEvent(CotEvent cotEvent) {
                    handleCotEvent(cotEvent);
                }
            });

            Log.i(TAG, "✅ CoT Receiver initialized successfully.");
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to initialize CoT Receiver", e);
        }
    }

    private void handleCotEvent(CotEvent cotEvent) {
        if (cotEvent == null) return;

        try {
            String uid = cotEvent.getUID();
            String type = cotEvent.getType();
            CotDetail detail = cotEvent.getDetail();
            String remarks = "";

            if (detail != null) {
                CotDetail remarksDetail = detail.getFirstChildByName(0, "remarks");
                if (remarksDetail != null && remarksDetail.getInnerText() != null) {
                    remarks = remarksDetail.getInnerText();
                }
            }

            String msg = "📡 Received CoT: UID=" + uid + ", Type=" + type + ", Remarks=" + remarks;
            Log.i(TAG, msg);

            // UI thread에서 Toast 출력
            new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            );

        } catch (Exception e) {
            Log.e(TAG, "⚠️ Error while handling CoT event", e);
        }
    }
}
