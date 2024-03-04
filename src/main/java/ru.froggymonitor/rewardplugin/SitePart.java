package ru.froggymonitor.rewardplugin;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SitePart extends FormDataHandler {
    public HttpServer server;

    public SitePart(String host, int port, int backlog) {
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

        System.out.println(response+" "+status_code+" "+path+" "+method);

        if (method.equals("GET")) {
            if (path.equals(Main.me.vote_page)) {
                if (params.containsKey("nickname") &&
                        params.containsKey("timestamp") &&
                        params.containsKey("secret")) {
                    String nickname = (String) params.get("nickname");
                    String timestamp = (String) params.get("timestamp");
                    String secret = (String) params.get("secret");

                    if (sha256(nickname + timestamp + Main.me.secret_token).equals(secret)) {
                        Main.me.vote_reward.execute(nickname);
                        response = "ok";
                        status_code = 200;
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

                    if (sha256(username + nickname + timestamp + Main.me.secret_token).equals(secret)) {
                        if (type.equals("insert")) {
                            Main.me.add_comment_reward.execute(nickname);

                            response = "ok";
                            status_code = 200;
                        } else if (type.equals("delete")) {
                            Main.me.del_comment_reward.execute(nickname);

                            response = "ok";
                            status_code = 200;
                        }
                    }
                }
            }
        }

        try {
            e.sendResponseHeaders(status_code, response.length());

            OutputStream os = e.getResponseBody();
            os.write(response.getBytes());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
