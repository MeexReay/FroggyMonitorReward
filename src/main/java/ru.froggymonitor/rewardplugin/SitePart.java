package themixray.monitoringreward;

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

    public SitePart(String host, int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(host,port),0);
            server.createContext("/vote",this);
            server.setExecutor(null);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String sha1(String s) {
        return Hashing.sha1().hashString(s, Charsets.UTF_8).toString();
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

        if (method.equals("POST")) {
            if (path.equals("/vote/hotmc")) {
                if (params.size() == 3) {
                    if (params.containsKey("nick") &&
                            params.containsKey("time") &&
                            params.containsKey("sign")) {
                        String nick = (String) params.get("nick");
                        String time = (String) params.get("time");
                        String sign = (String) params.get("sign");
                        String sign_gen = sha1(nick+time+Main.me.hotmc_token);

                        if (sign.equals(sign_gen)) {
                            response = "ok";
                            status_code = 200;
                            Main.me.sendVote(nick,"hotmc");
                        }
                    }
                }
            } else if (path.equals("/vote/mineserv")) {
                if (params.size() == 4) {
                    if (params.containsKey("project") &&
                            params.containsKey("username") &&
                            params.containsKey("timestamp") &&
                            params.containsKey("signature")) {
                        String project = (String) params.get("project");
                        String username = (String) params.get("username");
                        String timestamp = (String) params.get("timestamp");
                        String signature = (String) params.get("signature");
                        String sign_gen = sha256(project+"."+Main.me.mineserv_token+"."+timestamp+"."+username);

                        if (signature.equals(sign_gen)) {
                            response = "done";
                            status_code = 200;
                            Main.me.sendVote(username,"mineserv");
                        }
                    }
                }
            } else if (path.equals("/vote/minecraftrating")) {
                if (params.size() == 4) {
                    if (params.containsKey("ip") &&
                            params.containsKey("username") &&
                            params.containsKey("timestamp") &&
                            params.containsKey("signature")) {
                        String username = (String) params.get("username");
                        String timestamp = (String) params.get("timestamp");
                        String signature = (String) params.get("signature");
                        String sign_gen = sha1(username+timestamp+Main.me.minecraftrating_token);

                        if (signature.equals(sign_gen)) {
                            response = "ok";
                            status_code = 200;
                            Main.me.sendVote(username,"minecraftrating");
                        }
                    }
                }
            } else if (path.equals("/vote/misterlauncher")) {
                if (params.size() == 4) {
                    if (params.containsKey("ip") &&
                            params.containsKey("username") &&
                            params.containsKey("timestamp") &&
                            params.containsKey("signature")) {
                        String username = (String) params.get("username");
                        String timestamp = (String) params.get("timestamp");
                        String signature = (String) params.get("signature");
                        String sign_gen = sha1(username+timestamp+Main.me.misterlauncher_token);

                        if (signature.equals(sign_gen)) {
                            response = "ok";
                            status_code = 200;
                            Main.me.sendVote(username,"misterlauncher");
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
