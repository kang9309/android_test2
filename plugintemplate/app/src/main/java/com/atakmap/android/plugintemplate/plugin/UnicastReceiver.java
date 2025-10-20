package com.atakmap.android.plugintemplate.plugin;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.net.InetSocketAddress; // 새로 추가된 import

import android.util.Log;

/**
 * UnicastReceiver: A dedicated Runnable class for receiving standard UDP/Unicast packets
 * on a specified local port. This uses a standard DatagramSocket.
 */
class UnicastReceiver implements Runnable {

    private final String TAG = "CustomUnicastReceiver";

    private final int PORT;

    private volatile boolean running = true;
    private DatagramSocket socket = null;

    /**
     * Constructor for UnicastReceiver.
     * @param port The port (e.g., 4243)
     */
    public UnicastReceiver(int port) {
        this.PORT = port;
    }

    /**
     * Terminates the receiver loop and closes the socket to unblock the receive call.
     */
    public void terminate() {
        Log.d(TAG, "Termination requested. Setting flag to false and closing socket.");
        running = false;
        if (socket != null && !socket.isClosed()) {
            // Closing the socket will interrupt the blocking socket.receive() call.
            socket.close();
        }
    }

    @Override
    public void run() {
        try {
            // DatagramSocket 생성 및 로컬 포트에 바인딩
            // 1. 포트 없이 DatagramSocket을 생성하고
            socket = new DatagramSocket(null);

            // 2. SO_REUSEADDR 옵션을 설정하여 포트 재사용을 허용합니다. (BindException 방지)
            socket.setReuseAddress(true);

            // 3. 로컬 주소와 포트에 바인딩합니다.
            socket.bind(new InetSocketAddress(PORT));

            // 소켓 타임아웃을 설정하여 주기적으로 running 플래그를 확인합니다.
            socket.setSoTimeout(5000);

            Log.d(TAG, "Unicast Receiver 시작: Port " + PORT);

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

                // 수신된 데이터를 로그로 출력
                Log.d(TAG, "Unicast Data Received from " + packet.getAddress().getHostAddress() + ": " + received.substring(0, Math.min(received.length(), 100)) + "...");

                LogUtils.logReceivedData(received);
//                received
            }
        } catch (java.net.SocketException e) {
            // BindException은 SocketException의 자식 클래스이므로 여기서 처리됩니다.
            if (running) {
                // EADDRINUSE 오류 발생 시 포트를 확인하라는 오류 메시지 출력
                Log.e(TAG, "Unicast Socket 오류 발생 (Bind or other issue). 포트 [" + PORT + "]가 이미 사용 중인지 확인하세요.", e);
            } else {
                // running이 false일 때 발생하는 SocketException은 socket.close()로 인한 정상 종료입니다.
                Log.d(TAG, "Unicast Receiver successfully terminated by socket close.");
            }
        } catch (IOException e) {
            Log.e(TAG, "Unicast I/O 오류 발생", e);
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            Log.d(TAG, "Unicast Receiver 종료됨");
        }
    }
}