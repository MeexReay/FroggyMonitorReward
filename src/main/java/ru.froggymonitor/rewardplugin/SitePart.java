package ru.froggymonitor.rewardplugin;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class SitePart extends FormDataHandler {
    public HttpServer server;

    public String host;
    public int port;

    public SitePart(String host, int port, int backlog) {
        this.host = host;
        this.port = port;

        try {
            server = HttpServer.create(new InetSocketAddress(host,port),backlog);
            server.createContext("/",this);
            server.setExecutor(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        server.start();

        if (Main.me.enable_logs) Main.me.getLogger().info("Site started at "+host+":"+port);
    }

    public void stop() {
        server.stop(1);
    }

    public String sha256(String s) {
        return Hashing.sha256().hashString(s, Charsets.UTF_8).toString();
    }

    @Override
    public void handle(HttpExchange e, Map<String,Object> params, String data) {
        String response = "error";
        int status_code = 500;

        String method = e.getRequestMethod();
        String path = e.getRequestURI().getPath();

        try {
            if (method.equals("GET")) {
                if (path.equals(Main.me.vote_page)) {
                    if (params.containsKey("nickname") &&
                            params.containsKey("timestamp") &&
                            params.containsKey("secret")) {
                        String nickname = (String) params.get("nickname");
                        String timestamp = (String) params.get("timestamp");
                        String secret = (String) params.get("secret");

                        String secret_gen = sha256(nickname + timestamp + Main.me.secret_token);

                        if (secret_gen.equals(secret)) {
                            Main.me.vote_reward.execute(nickname);
                            response = "ok";
                            status_code = 200;

                            if (Main.me.enable_logs) Main.me.getLogger().info("Reward \"vote\" gave to player "+nickname);
                        }
                    }
                } else if (path.equals(Main.me.comment_page)) {
                    if (params.containsKey("nickname") &&
                            params.containsKey("type") &&
                            params.containsKey("username") &&
                            params.containsKey("timestamp") &&
                            params.containsKey("secret")) {
                        String type = (String) params.get("type");
                        String username = (String) params.get("username");
                        String nickname = (String) params.get("nickname");
                        String timestamp = (String) params.get("timestamp");
                        String secret = (String) params.get("secret");

                        String secret_gen = sha256(username + nickname + timestamp + Main.me.secret_token);

                        if (secret_gen.equals(secret)) {
                            if (type.equals("insert")) {
                                Main.me.add_comment_reward.execute(nickname);

                                response = "ok";
                                status_code = 200;

                                if (Main.me.enable_logs) Main.me.getLogger().info("Reward \"add_comment\" gave to player "+nickname);
                            } else if (type.equals("delete")) {
                                Main.me.del_comment_reward.execute(nickname);

                                response = "ok";
                                status_code = 200;

                                if (Main.me.enable_logs) Main.me.getLogger().info("Reward \"del_comment\" gave to player "+nickname);
                            }
                        }
                    }
                }
            }

            e.sendResponseHeaders(status_code, response.length());

            OutputStream os = e.getResponseBody();
            os.write(response.getBytes());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
