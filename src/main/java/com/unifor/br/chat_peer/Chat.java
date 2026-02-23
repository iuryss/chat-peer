package com.unifor.br.chat_peer;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Chat {

    private String userName;

    private ServerSocket serverSocket;

    private List<Socket> connections = new ArrayList<>();

    public Chat(String userName, int port) {
        this.userName = userName;

        try {
            this.serverSocket = new ServerSocket(port);
            System.out.println("O peer "+ userName + " está ouvindo na porta: " + port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void start(){
        new Thread(this::listenForConnections).start();
        new Thread(this::listenForUserInput).start();
    }

    private void listenForUserInput() {
        try {
            BufferedReader userInput = new BufferedReader(
                    new InputStreamReader(System.in));
            while (true){
                String message = userInput.readLine();
                broadcastMessage(message);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void broadcastMessage(String message) {
        for (Socket socket: connections){
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(this.userName + ": "+ message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private void listenForConnections() {
        while(true){
            try {
                Socket socket = this.serverSocket.accept();
                this.connections.add(socket);
                new Thread(()-> handleConnections(socket)).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private void handleConnections(Socket socket) {

        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()
                    ));
            String message;
            while ((message = in.readLine()) != null){
                System.out.println("message: "+message);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void connectionToPeer(String host, int port){

        try {
            Socket socket = new Socket(host, port);
            this.connections.add(socket);
            new Thread(() -> handleConnections(socket)).start();
            System.out.println("Conectado a um peer em: " + host + ":" + port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.println("Digite o nome do Usuario: ");
        String userName = sc.nextLine();

        System.out.println("Digite a porta para escutar: ");
        int port = sc.nextInt();
        System.out.println(port);
        sc.nextLine();

        Chat peer = new Chat(userName, port);
        peer.start();

        System.out.println("Deseja conectar a outro peer? (s/n):");
        String anwser = sc.nextLine();

        if(anwser.equalsIgnoreCase("s")){
            System.out.println("Digite endereço do outro host: ");
            String peerHost = sc.nextLine();

            System.out.println("Digite a porta de  outro peer: ");
            int peerPort = sc.nextInt();
            System.out.println(peerPort);
            //sc.nextLine();

            peer.connectionToPeer(peerHost, peerPort);

        }

        sc.close();

    }
}
