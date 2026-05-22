package com.kotikvorkotik;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public final class MCServerAPI extends JavaPlugin {

    private WebSocketServer webSocketServer;
    private Set<WebSocket> connectedClients = new HashSet<>();
    private Logger pluginLogger;

    @Override
    public void onEnable() {
        pluginLogger = getLogger();
        
        // Save default config if not exists
        saveDefaultConfig();
        
        // Get config values
        int port = getConfig().getInt("websocket.port", 8080);
        boolean localhostOnly = getConfig().getBoolean("websocket.localhost-only", true);
        
        // Start WebSocket server
        startWebSocketServer(port, localhostOnly);
        
        // Register command
        getCommand("test").setExecutor(new TestCommandExecutor());
        
        pluginLogger.info("Плагин запущен");
        pluginLogger.info("Автор KoteVorkote");
    }

    @Override
    public void onDisable() {
        // Stop WebSocket server and notify all clients
        if (webSocketServer != null && webSocketServer.isRunning()) {
            // Send shutdown message to all connected clients
            String shutdownMessage = "{\"type\": \"server_stopped\", \"message\": \"Сервер остановлен\"}";
            for (WebSocket client : connectedClients) {
                if (client.isOpen()) {
                    client.send(shutdownMessage);
                }
            }
            
            // Stop the server
            try {
                webSocketServer.stop();
            } catch (InterruptedException e) {
                pluginLogger.severe("Error stopping WebSocket server: " + e.getMessage());
            }
        }
        
        pluginLogger.info("Плагин деактивирован");
    }

    private void startWebSocketServer(int port, boolean localhostOnly) {
        InetSocketAddress address = localhostOnly 
            ? new InetSocketAddress("127.0.0.1", port) 
            : new InetSocketAddress("0.0.0.0", port);
        
        webSocketServer = new WebSocketServer(address) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                connectedClients.add(conn);
                pluginLogger.info("New WebSocket client connected: " + conn.getRemoteSocketAddress());
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                connectedClients.remove(conn);
                pluginLogger.info("WebSocket client disconnected: " + conn.getRemoteSocketAddress());
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                pluginLogger.info("Received message from " + conn.getRemoteSocketAddress() + ": " + message);
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                pluginLogger.severe("WebSocket error: " + ex.getMessage());
            }

            @Override
            public void onStart() {
                String visibility = localhostOnly ? "localhost" : "all interfaces";
                pluginLogger.info("WebSocket server started on port " + port + " with " + visibility + " visibility");
            }
        };
        
        try {
            webSocketServer.start();
        } catch (InterruptedException e) {
            pluginLogger.severe("Failed to start WebSocket server: " + e.getMessage());
        }
    }

    public class TestCommandExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                sender.sendMessage("Success");
            } else {
                sender.sendMessage("Success");
            }
            return true;
        }
    }
}
