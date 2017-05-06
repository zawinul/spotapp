package com.andrenacci.spotapp;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity  extends Activity {

    public static String CSPOT_VERSION = "1.0.3";


    private GoogleApiClient client;
    //private SpotifyManager spotifyManager = new SpotifyManager(this);

    public WebServer webServer = null;
    public void videolog(String m) {
        final String log = LogContent.add(m);
        final MainActivity act = this;
        if (created) {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        TextView t = (TextView) act.findViewById(R.id.intestazione);
                        t.setText(log);
                    } catch (Exception e) {  }
                }
            });
        }
    }
    boolean created = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        videolog("------------------ on Create");

        super.onCreate(savedInstanceState);
        SpotifyManager.setActivity(this);
        String s = WebServer.__getLocalIpAddress();
        videolog("[IP] = "  +s);
        videolog("version "+CSPOT_VERSION);
        try {
            webServer = new WebServer(s);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        WebCommander.activity = this;
        //WebCommander.manager = spotifyManager;
        Log.i("MainActivity", "onCreate");
        setContentView(R.layout.activity_main);

        final Button button = (Button) findViewById(R.id.clearButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                videolog("|clear|");
            }
        });

        final Button button2 = (Button) findViewById(R.id.exitButton);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    SpotifyManager.destroyPlayer();
                }catch(Exception e) {}
                try {
                    webServer.closeAllConnections();
                    webServer.stop();
                }catch(Exception e) {}
                System.exit(0);
            }
        });

        final Button button3 = (Button) findViewById(R.id.getPlayerButton);
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    (new Thread() {
                        @Override
                        public void run() {
                            SpotifyManager.getPlayer();
                        }
                    }).start();
                    videolog("getPlayer DONE");
                }catch(Exception e) {}
            }
        });

        //SpotifyManager.doAuthentication();
        created = true;
        videolog("created");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        videolog("onActivityResult req="+requestCode+" res="+resultCode);
        if (SpotifyManager.manageActivityResult(requestCode, resultCode, intent))
            return;
    }

    private void saveToken(final String token) {
//        Thread t = new Thread(){
//            @Override
//            public void run() {
//            try {
//                URL url = new URL("http://www.andrenacci.com/getandset/set.php?name=spotify-token&data=" + URLEncoder.encode(token, "utf-8"));
//                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//                int responseCode = urlConnection.getResponseCode();
//                urlConnection.getInputStream().close();
//            }
//            catch(Exception e) {
//                e.printStackTrace();
//            }
//            }
//        };
//        t.start();
    }


    private void saveIp(String ip) {
//        final String eip = ip;
//        Thread t = new Thread() {
//            @Override
//            public void run() {
//                try {
//                    URL url = new URL("http://www.andrenacci.com/getandset/set.php?name=spotifyip.txt&data=" + URLEncoder.encode(eip, "utf-8"));
//                    Log.d("MainActivity", "url="+url.toString());
//                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//                    int responseCode = urlConnection.getResponseCode();
//
//                    urlConnection.getInputStream().close();
//                }
//                catch(Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//        t.start();
    }

    @Override
    protected void onDestroy() {
        created = false;
        super.onDestroy();
        videolog("on destroy");
        //c SpotifyManager.destroyPlayer();
        videolog("after spotify destroy");
        webServer.closeAllConnections();
        webServer.stop();
        videolog("after ws close");
    }

    @Override
    public void onStart() {
        super.onStart();
        videolog("on start");
    }

    @Override
    public void onPause() {
        super.onPause();
        videolog("on pause");
    }

    @Override
    public void onResume() {
        super.onResume();
        videolog("on resume");
    }


    @Override
    public void onStop() {
        super.onStop();
        videolog("on stop");
    }
}