package com.unifor.br.chat_peer.service;

import com.unifor.br.chat_peer.model.ChatMessage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class P2PService {


    @Value("${p2p.username:PeerDefault}")
    private String userName;

    @Value("${p2p.port:9000}")
    private int p2pPort;

    private ServerSocket serverSocket;

    private final Map<Socket, ObjectOutputStream> connections = new ConcurrentHashMap<>();;

    private final List<ChatMessage> messageHistory = new CopyOnWriteArrayList<>();

    private final SimpMessagingTemplate messagingTemplate;

    public P2PService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @PostConstruct
    public void startServer() {
        new Thread(() -> {
            try {
                this.serverSocket = new ServerSocket(p2pPort);
                System.out.println("O peer " + userName + " está ouvindo conexões P2P na porta: " + p2pPort);
                listenForConnections();
            } catch (IOException e) {
                System.err.println("Erro ao iniciar o servidor P2P: " + e.getMessage());
            }
        }).start();
    }

    private void listenForConnections() {
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                connections.put(socket, new ObjectOutputStream(socket.getOutputStream()));
                System.out.println("Novo peer conectado: " + socket.getInetAddress());

                new Thread(() -> handleIncomingMessages(socket)).start();
            } catch (IOException e) {
                if (!serverSocket.isClosed()) {
                    System.err.println("Erro ao aceitar conexão: " + e.getMessage());
                }
            }
        }
    }


    private void handleIncomingMessages(Socket socket) {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            while (!socket.isClosed()) {
                ChatMessage message = (ChatMessage) in.readObject();
                System.out.println("Mensagem recebida: " + message);

                messageHistory.add(message);

                messagingTemplate.convertAndSend("/topic/public", message);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Conexão com peer encerrada.");
            connections.remove(socket);
        }
    }

    public void broadcastMessage(String content) {
        ChatMessage message = new ChatMessage(this.userName, content);
        messageHistory.add(message);
        messagingTemplate.convertAndSend("/topic/public", message);

        for (Map.Entry<Socket, ObjectOutputStream> entry : connections.entrySet()) {
            Socket socket = entry.getKey();
            ObjectOutputStream out = entry.getValue();

            try {
                out.writeObject(message);
                out.flush();
            } catch (IOException e) {
                System.err.println("Erro ao enviar mensagem para o peer " + socket.getInetAddress() + ". Removendo.");
                connections.remove(socket);
            }
        }
    }

    public void connectToPeer(String host, int port) {
        try {
            Socket socket = new Socket(host, port);
            connections.put(socket, new ObjectOutputStream(socket.getOutputStream()));

            new Thread(() -> handleIncomingMessages(socket)).start();
            System.out.println("Conectado com sucesso ao peer em: " + host + ":" + port);
        } catch (IOException e) {
            System.err.println("Não foi possível conectar ao peer " + host + ":" + port);
        }
    }

    @PreDestroy
    public void shutdown() {
        System.out.println("Encerrando conexões");

        for (Socket socket : connections.keySet()) {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                // Ignorar
            }
        }
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<ChatMessage> getMessageHistory() {
        return messageHistory;
    }
}