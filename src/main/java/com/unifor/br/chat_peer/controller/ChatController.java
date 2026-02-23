package com.unifor.br.chat_peer.controller;

import com.unifor.br.chat_peer.model.ChatMessage;
import com.unifor.br.chat_peer.model.PeerAddress;
import com.unifor.br.chat_peer.service.P2PService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ChatController {

    private final P2PService p2pService;

    public ChatController(P2PService p2pService) {
        this.p2pService = p2pService;
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