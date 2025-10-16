package com.atakmap.android.plugintemplate.plugin;

import android.util.Log;

import java.net.InetAddress;
import java.net.MulticastSocket;

// 멀티캐스트 소켓을 위한 Java 네트워크 라이브러리 추가
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

public class MulticastReceiver implements Runnable {
    private final String CLASS_TAG = "MulticastReceiver";
    private final String MULTICAST_ADDRESS;
    private final int MULTICAST_PORT;

    private volatile boolean running = true;
    private MulticastSocket socket = null;
    private InetAddress group = null;

    /**
     * Constructor for MulticastReceiver.
     * @param address The multicast address (e.g., "239.2.1.1")
     * @param port The port (e.g., 4242)
     */
    public MulticastReceiver(String address, int port) {
        this.MULTICAST_ADDRESS = address;
        this.MULTICAST_PORT = port;
    }
    /**
     * Terminates the receiver loop and closes the socket to unblock the receive call.
     */
    public void terminate() {
        Log.d(CLASS_TAG, "Termination requested. Setting flag to false and closing socket.");
        running = false;
        if (socket != null && !socket.isClosed()) {
            // Closing the socket will interrupt the blocking socket.receive() call.
            socket.close();
        }
    }

    @Override
    public void run() {
        try {
            // MulticastSocket 생성 및 그룹 참여
            group = InetAddress.getByName(MULTICAST_ADDRESS);

            // MulticastSocket 생성. ATAK 표준 포트 사용.
            socket = new MulticastSocket(MULTICAST_PORT);
            socket.joinGroup(group);

            // 소켓 타임아웃을 설정하여 주기적으로 running 플래그를 확인합니다.
            socket.setSoTimeout(5000);

            Log.d(CLASS_TAG, LogUtils.getLogPosition() + "Multicast Receiver 시작: " + MULTICAST_ADDRESS + ":" + MULTICAST_PORT);

            byte[] buf = new byte[4096];
            while (running) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);

                try {
                    // 패킷 수신 대기 (블로킹)
                    socket.receive(packet);
                } catch (java.net.SocketTimeoutException ste) {
                    // 타임아웃 발생 시 무시하고 running 플래그를 다시 확인합니다.
                    continue;
                }

                if (!running) break;

                // 수신된 데이터 디코딩
                String received = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);

                // 수신된 CoT XML 데이터를 로그로 출력
                Log.d(CLASS_TAG, LogUtils.getLogPosition() + "CoT Multicast Received: " + received.substring(0, Math.min(received.length(), 100)) + "...");
            }
        } catch (IOException e) {
            if (running) {
                Log.e(CLASS_TAG, LogUtils.getLogPosition() + "Multicast Receiver 오류 발생", e);
            } else {
                // running이 false일 때 발생하는 IOException은 socket.close()로 인한 정상 종료입니다.
                Log.d(CLASS_TAG, LogUtils.getLogPosition() + "Multicast Receiver successfully terminated by socket close.");
            }
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            Log.d(CLASS_TAG, LogUtils.getLogPosition() + "Multicast Receiver 종료됨");
        }
    }
}
