package com.andrenacci.spotapp;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Map;
import fi.iki.elonen.NanoHTTPD;

public class WebServer extends NanoHTTPD {
    //private static  String s = __getLocalIpAddress();
    public WebServer(String adr) throws Exception {
        super(8080);
        //super(adr, 8080);
        System.out.println("starting webserver "+adr+":8080");
        start();

        System.out.println("webserver started");
    }


    @Override
    public Response serve(IHTTPSession session) {
        System.out.println(">> webserver receiving");
        String j;
        JSONObject obj = new JSONObject();
        if (session.getUri().indexOf("getlog")>=0) {
            String reply = LogContent.get();
            NanoHTTPD.Response resp =  new NanoHTTPD.Response(Response.Status.OK, "text/plain", reply);
            resp.addHeader("Access-Control-Allow-Origin", "*");
            resp.addHeader("Access-Control-Allow-Headers", "Cache-Control, Pragma, Origin, Authorization, Content-Type, X-Requested-With");
            resp.addHeader("Access-Control-Allow-Method", "GET, PUT, POST, OPTIONS, X-XSRF-TOKEN");
            resp.addHeader("Access-Control-Allow-Credentials", "true");

            return resp;
        }
        try {
            try {
                JSONObject input = new JSONObject();
                obj.put("input", input);

                Map<String, String> parms = session.getParms();
                for (String k : parms.keySet())
                    input.put(k, parms.get(k));

                boolean b = (new WebCommander()).exec(obj);
                obj.put("ok", b);
            } catch (Exception e) {
                obj.put("ok", false);
                obj.put("error", e.getMessage());
            } ;
            j = obj.toString(4);
        }
        catch(JSONException e) {
            j = "\"error\"";
        }

        NanoHTTPD.Response resp =  new NanoHTTPD.Response(Response.Status.OK, "application/json", j);
        resp.addHeader("Access-Control-Allow-Origin", "*");
        //resp.addHeader("Access-Control-Allow-Origin", "localhost:9000");
        //resp.addHeader("Access-Control-Allow-Origin", "file");
        resp.addHeader("Access-Control-Allow-Headers", "Cache-Control, Pragma, Origin, Authorization, Content-Type, X-Requested-With");
        resp.addHeader("Access-Control-Allow-Method", "GET, PUT, POST, OPTIONS, X-XSRF-TOKEN");
        resp.addHeader("Access-Control-Allow-Credentials", "true");

        return resp;
    }


    public static String __getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (! inetAddress.isLoopbackAddress()) {
                        if (inetAddress instanceof Inet4Address) {
                            String ip = inetAddress.getHostAddress();
                            Log.w("WEBSERV", "local IP: "+ ip);
                            return ip;
                        }
                    }
                }
            }
        }
        catch (Exception ex) {
            Log.e("WEBSERV", ex.toString());
        }
        return "127.0.0.1";
    }

}