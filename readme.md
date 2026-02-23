# 💬 Chat P2P Descentralizado com Spring Boot

Este projeto é uma implementação de um sistema de chat Peer-to-Peer (P2P) descentralizado, desenvolvido como requisito avaliativo para a disciplina de **Sistemas Distribuídos / Redes**.

O sistema permite que múltiplos usuários se comuniquem diretamente via rede local sem a necessidade de um servidor central de mensagens, utilizando uma arquitetura híbrida de **WebSockets** (para a interface) e **Sockets TCP/UDP** (para a comunicação entre peers).

---

## 🚀 Funcionalidades

* **Arquitetura Descentralizada:** Cada peer atua simultaneamente como cliente e servidor.
* **Múltiplas Conexões:** Suporte a conversas com vários peers ao mesmo tempo (Multithreading).
* **Descoberta Automática (Peer Discovery):** Utiliza **UDP Broadcast** para encontrar outros usuários na rede local automaticamente.
* **Interface Web Responsiva:** Frontend moderno construído com HTML5, CSS3 e JavaScript.
* **Comunicação em Tempo Real:** Uso de WebSockets para atualizar a tela instantaneamente.
* **Conexão Manual:** Fallback para conectar via IP e Porta caso o UDP seja bloqueado por firewalls.
* **Histórico de Mensagens:** Persistência em memória das mensagens trocadas durante a sessão.

---

## 🛠️ Tecnologias Utilizadas

* **Java 17+**
* **Spring Boot 3.x** (Web, WebSocket, Thymeleaf)
* **Java Sockets (java.net)**
    * `ServerSocket` / `Socket` (TCP)
    * `DatagramSocket` (UDP Broadcast)
* **Maven** (Gerenciamento de dependências)
* **HTML/JS/CSS** (Frontend)

---

## 📦 Como Rodar o Projeto

Para testar o chat P2P em uma única máquina, precisamos simular dois peers rodando em portas diferentes.

### Pré-requisitos
* JDK 17 ou superior instalado.
* Maven instalado (ou usar o wrapper `mvnw` incluso no projeto).

### Passo 1: Iniciar o Primeiro Peer (Alice)
Você pode rodar este peer diretamente pela sua IDE (IntelliJ/Eclipse) ou pelo terminal:

```bash
# Porta Web: 8080 | Porta P2P: 9000
./mvnw spring-boot:run