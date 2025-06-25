package gr.uniwa.labcontrolclient;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Map;

public class NetworkUtils {

    // Magic packet για WOL
    public static void sendWOL(String macStr, String broadcastIP) throws Exception {
        byte[] macBytes = getMacBytes(macStr);
        byte[] packet = new byte[6 + 16 * macBytes.length];
        // 6 bytes FF
        for (int i = 0; i < 6; i++) packet[i] = (byte) 0xFF;
        // 16 επαναλήψεις MAC
        for (int i = 6; i < packet.length; i += macBytes.length)
            System.arraycopy(macBytes, 0, packet, i, macBytes.length);

        DatagramPacket dp = new DatagramPacket(packet, packet.length,
                InetAddress.getByName(broadcastIP), 9);
        DatagramSocket ds = new DatagramSocket();
        ds.setBroadcast(true);
        ds.send(dp);
        ds.close();
    }

    private static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        String[] hex = macStr.split("[:\\-]");
        if (hex.length != 6) throw new IllegalArgumentException("Invalid MAC address.");
        byte[] bytes = new byte[6];
        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) Integer.parseInt(hex[i], 16);
        }
        return bytes;
    }

    // Εκτέλεση TCP εντολής και ανάγνωση απαντήσεων
    public static void sendCommand(String host, int port, String cmd, ResultCallback cb) {
        new Thread(() -> {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("192.168.1.68", port), 3000);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                out.println(cmd);
                String line;
                while ((line = in.readLine()) != null) {
                    cb.onLine(host + ": " + line);
                }
            } catch (Exception e) {
                cb.onLine(host + ": ERROR → " + e.getMessage());
            }
        }).start();
    }

    public interface ResultCallback {
        void onLine(String line);
    }
}
