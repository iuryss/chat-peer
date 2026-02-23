package com.unifor.br.chat_peer.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PeerDiscoveryService {

    @Value("${p2p.username:PeerDefault}")
    private String userName;

    @Value("${p2p.port:9000}")
    private int p2pPort;


    private static final int DISCOVERY_PORT = 9090;
    private DatagramSocket udpSocket;
    private boolean running = true;

    private final Map<String, String> discoveredPeers = new ConcurrentHashMap<>();

    @PostConstruct
    public void startDiscovery() {
        try {
            udpSocket = new DatagramSocket(null);
            udpSocket.setReuseAddress(true);
            udpSocket.bind(new InetSocketAddress(DISCOVERY_PORT));
            udpSocket.setBroadcast(true);

            new Thread(this::listenForBroadcasts).start();
            new Thread(this::broadcastPresence).start();

            System.out.println("Serviço de descoberta P2P iniciado na porta UDP " + DISCOVERY_PORT);
        } catch (Exception e) {
            System.err.println("Erro ao iniciar descoberta UDP: " + e.getMessage());
        }
    }

    private void broadcastPresence() {
        while (running) {
            try {
                String message = "PEER_HERE:" + userName + ":" + p2pPort;
                byte[] buffer = message.getBytes();

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT);
                udpSocket.send(packet);

                Thread.sleep(5000);
            } catch (Exception e) {
                // Ignora
            }
        }
    }

    private void listenForBroadcasts() {
        byte[] buffer = new byte[256];
        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);
                String data = new String(packet.getData(), 0, packet.getLength());

                if (data.startsWith("PEER_HERE:")) {
                    String[] parts = data.split(":");
                    if (parts.length == 3) {
                        String peerName = parts[1];
                        int peerPort = Integer.parseInt(parts[2].trim());
                        String peerIp = packet.getAddress().getHostAddress();

                        if (peerPort != this.p2pPort) {
                            String addressKey = peerIp + ":" + peerPort;
                            discoveredPeers.put(addressKey, peerName);
                        }
                    }
                }
            } catch (Exception e) {
                // ignora
            }
        }
    }

    public Map<String, String> getDiscoveredPeers() {
        return discoveredPeers;
    }

    @PreDestroy
    public void stopDiscovery() {
        running = false;
        if (udpSocket != null && !udpSocket.isClosed()) {
            udpSocket.close();
        }
    }
}