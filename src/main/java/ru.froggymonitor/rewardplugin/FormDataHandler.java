package ru.froggymonitor.rewardplugin;

import com.google.common.base.Charsets;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;

public abstract class FormDataHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Headers headers = httpExchange.getRequestHeaders();

        if (headers.containsKey("Content-type")) {
            Map<String, Object> params = new HashMap<>();

            String contentType = headers.getFirst("Content-Type");
            String data = new String(httpExchange.getRequestBody().readAllBytes());

            if (contentType.startsWith("multipart/form-data")) {
                String boundary = "--" + contentType.substring(contentType.indexOf("boundary=") + 9) + "\r\n";
                for (String part : data.split(boundary)) {
                    String[] lines = part.split("\r\n");
                    if (lines.length >= 3) {
                        if (lines[0].startsWith("Content-Disposition: form-data; name=\"")) {
                            String name = lines[0].substring("Content-Disposition: form-data; name=\"".length());
                            name = name.substring(0, name.length() - 1);
                            String value = lines[2];
                            params.put(name, value);
                        }
                    }
                }
            } else if (contentType.startsWith("application/json")) {
                try {
                    Object o = new JSONParser().parse(data);
                    if (o instanceof Map) {
                        for (Map.Entry<Object, Object> e : ((Map<Object, Object>) o).entrySet())
                            params.put(e.getKey().toString(), e.getValue());
                    } else {
                        params.put("data", o);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else if (contentType.startsWith("application/x-www-form-urlencoded")) {
                if (data.startsWith("?")) data = data.substring(1);
                for (String s : data.split("&")) {
                    String[] ss = s.split("=");
                    String k = URLDecoder.decode(ss[0], Charsets.UTF_8);
                    String v = URLDecoder.decode(ss[1], Charsets.UTF_8);
                    params.put(k, v);
                }
            }

            try {
                handle(httpExchange, params, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                handle(httpExchange, new HashMap<>(), "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public abstract void handle(HttpExchange httpExchange,Map<String,Object> parts,String data) throws IOException;
}