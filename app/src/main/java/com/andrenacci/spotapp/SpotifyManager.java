package com.andrenacci.spotapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import com.spotify.sdk.android.player.ConnectionStateCallback;

import java.util.Date;

public class SpotifyManager {
    public static int endOfSongCount = 0;
    public static Metadata.Track currTrack;
    private static class Costanti {
        private final static String SCOPES[] = new String[]{
                "playlist-read-private",
                "playlist-read-collaborative",
                "playlist-modify-public",
                "playlist-modify-private",
                "streaming",
                "user-follow-modify",
                "user-follow-read",
                "user-library-read",
                "user-library-modify",
                "user-read-private",
                "user-read-birthdate",
                "user-read-email",
                "user-top-read"
        };

        private static final String CLIENT_ID = "bde94bcf881f43e085f7dd7aea0d922e";
        private static final String REDIRECT_URI = "pspotapp://callback";
        private static final int REQUEST_CODE = 1337;    // Can be any integer
        private static Object playerReference = new Object();
    };

    private static MainActivity activity;

    private static SpotifyManager singleton = new SpotifyManager();
    private SpotifyPlayer player;
    //public SpotifyManager(MainActivity activity) {
    //    this.activity = activity;
    //}

    private SpotifyManager() {  }

    public static void setActivity(MainActivity act) {
        activity = act;
    }

//    public static void doAuthentication() {
//        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(
//                CLIENT_ID,
//                AuthenticationResponse.Type.TOKEN,
//                REDIRECT_URI);
//        builder.setScopes(SpotifyManager.SCOPES);
//        AuthenticationRequest req = builder.build();
//        AuthenticationClient.openLoginActivity(activity, REQUEST_CODE, req);
//    }

    private SpotifyPlayer.NotificationCallback notification = new Player.NotificationCallback() {
        @Override // from SpotifyPlayer.NotificationCallback
        public void onPlaybackEvent(PlayerEvent playerEvent) {
            Log.d("MainActivity", "Playback event received: " + playerEvent.name());
            activity.videolog("play evt: " + playerEvent.name());
            switch (playerEvent) {
                case kSpPlaybackNotifyAudioDeliveryDone:
                    endOfSongCount++;
                    break;
                case kSpPlaybackNotifyTrackChanged:
                    currTrack = SpotifyManager.this.player.getMetadata().currentTrack;
                    break;
                default:
                    break;
            }
        };

        @Override // from SpotifyPlayer.NotificationCallback
        public void onPlaybackError(Error error) {
            Log.d("MainActivity", "Playback error received: " + error.name());
            activity.videolog("play err: " + error.name());
        }
    };

    private SpotifyPlayer.InitializationObserver initialization = new SpotifyPlayer.InitializationObserver(){
        @Override // from SpotifyPlayer.InitializationObserver
        public void onInitialized(SpotifyPlayer spotifyPlayer) {
            activity.videolog("obtained new player");
            player = spotifyPlayer;
            player.addConnectionStateCallback(connectionState);
            player.addNotificationCallback(notification);
            synchronized(semaforo) {
                semaforo.notify();
            }
        }

        @Override // from SpotifyPlayer.InitializationObserver
        public void onError(Throwable throwable) {
            activity.videolog("error: " + throwable.getMessage());
            Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
        }
    };

    private ConnectionStateCallback connectionState = new ConnectionStateCallback() {
        @Override // from ConnectionStateCallback;
        public void onLoggedIn() {
            Log.d("MainActivity", "User logged in");
        }

        @Override // from ConnectionStateCallback
        public void onLoginFailed(Error var1) {
            Log.e("MainActivity", "onLoginFailed " + var1.toString());
            activity.videolog("login failed");
        }

        @Override // from ConnectionStateCallback
        public void onTemporaryError() {
            Log.d("MainActivity", "Temporary error occurred");
        }

        @Override // from ConnectionStateCallback
        public void onConnectionMessage(String message) {
            Log.d("MainActivity", "Received connection message: " + message);
        }

        @Override // from ConnectionStateCallback
        public void onLoggedOut() {
            activity.videolog("logged out");
            Log.d("MainActivity", "User logged out");
        }
    };



    public static void destroyPlayer() {
        if (singleton.player!=null) {
            Spotify.destroyPlayer(Costanti.playerReference);
            singleton.player = null;
        }
    }

    private static Object lock = new Object();

    public static SpotifyPlayer getPlayer() {
        //activity.videolog("getPLayer");
        return singleton._getPlayer();
    }

    public synchronized SpotifyPlayer _getPlayer() {
        //activity.videolog("_getPlayer");

        try {
            if (singleton.player==null) {
                activity.videolog("start task");
                AuthenticationBackgroundTask task = new AuthenticationBackgroundTask();
                task.execute();
                singleton.player = task.get();
                activity.videolog("------------------ "+(singleton.player!=null));
            }
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        //activity.videolog("exit _getPlayer");

        return singleton.player;
    };

    private static String semaforo = "";
    private static class AuthenticationBackgroundTask extends AsyncTask<Void, Integer, SpotifyPlayer> {

        public AuthenticationBackgroundTask() {}

        @Override
        protected void onPreExecute() {
            activity.videolog("pre EXECUTE");
            super.onPreExecute();
        }

        @Override
        protected SpotifyPlayer doInBackground(Void... arg0) {
            try  {
                activity.videolog("doInBackground");
                AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(
                        Costanti.CLIENT_ID,
                        AuthenticationResponse.Type.TOKEN,
                        Costanti.REDIRECT_URI);
                builder.setScopes(Costanti.SCOPES);
                activity.videolog("doInBackground pre build");
                AuthenticationRequest req = builder.build();
                activity.videolog("doInBackground post build");
                AuthenticationClient.openLoginActivity(activity, Costanti.REQUEST_CODE, req);
                activity.videolog("doInBackground post login activity");

                activity.videolog("doInBackground pre wait");
                synchronized(semaforo) {
                    semaforo.wait();
                }
                activity.videolog("doInBackground post wait");
            }
            catch (Exception e){
                e.printStackTrace();
                activity.videolog(e.getMessage());
            }

            return singleton.player;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(SpotifyPlayer result) {
            super.onPostExecute(result);
            singleton.player = result;
            Toast.makeText(activity, "autenticazione ok",   Toast.LENGTH_SHORT).show();
        }

    }

    public static boolean manageActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode != Costanti.REQUEST_CODE)
            return false;
        AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

        if (response.getType() == AuthenticationResponse.Type.ERROR) {
            activity.videolog("response.getError()=" + response.getError());
            System.out.println("response.getError()=" + response.getError());
        }

        if (response.getType() == AuthenticationResponse.Type.TOKEN) {
            //System.out.println("TOK=" + response.getAccessToken().substring(0, 8) + "...");
            activity.videolog("TOK=" + response.getAccessToken().substring(0, 8) + "...");
            activity.videolog("expires=" + response.getExpiresIn());

//            WebCommander.appId = Costanti.CLIENT_ID;
//            WebCommander.token = token;

            Config playerConfig = new Config(activity, response.getAccessToken(), Costanti.CLIENT_ID);
            playerConfig.useCache(false);
            activity.videolog("get new player");
            Spotify.getPlayer( playerConfig, Costanti.playerReference, singleton.initialization);
        }
        return true;
    }

//    public void setToken(String tk) {
//        if (tk==null) {
//            if (player != null) {
//                activity.videolog("destroy player ");
//                Spotify.destroyPlayer(singleton);
//                player = null;
//            }
//            return;
//        }
//        if (tk.equals(token))
//            return;
//        token = tk;
//        activity.videolog("real set token "+token.substring(0,6)+"...");
//        if (player!=null) {
//            activity.videolog("destroy player");
//            Spotify.destroyPlayer(this);
//            player = null;
//        }
//
//        WebCommander.appId = Costanti.CLIENT_ID;
//        WebCommander.token = token;
//
//        Config playerConfig = new Config(activity, token, Costanti.CLIENT_ID);
//        playerConfig.useCache(false);
//        activity.videolog("get new player");
//        Spotify.getPlayer( playerConfig, this, this);
//    }


}