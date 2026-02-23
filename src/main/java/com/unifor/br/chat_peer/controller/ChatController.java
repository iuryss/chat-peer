package com.unifor.br.chat_peer.controller;

import com.unifor.br.chat_peer.model.ChatMessage;
import com.unifor.br.chat_peer.model.PeerAddress;
import com.unifor.br.chat_peer.service.P2PService;
import com.unifor.br.chat_peer.service.PeerDiscoveryService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class ChatController {

    private final P2PService p2pService;

    private final PeerDiscoveryService discoveryService;

    public ChatController(P2PService p2pService, PeerDiscoveryService discoveryService) {
        this.p2pService = p2pService;
        this.discoveryService = discoveryService;
    }

    @GetMapping("/api/peers")
    @ResponseBody
    public Map<String, String> getDiscoveredPeers() {
        return discoveryService.getDiscoveredPeers();
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("history", p2pService.getMessageHistory());
        return "chat";
    }


    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        p2pService.broadcastMessage(chatMessage.getContent());
    }

    @PostMapping("/api/connect")
    @ResponseBody
    public String connectToPeer(@RequestBody PeerAddress peerAddress) {
        p2pService.connectToPeer(peerAddress.getHost(), peerAddress.getPort());
        return "Tentando conectar a " + peerAddress.getHost() + ":" + peerAddress.getPort();
    }

    @GetMapping("/api/history")
    @ResponseBody
    public List<ChatMessage> getHistory() {
        return p2pService.getMessageHistory();
    }
}