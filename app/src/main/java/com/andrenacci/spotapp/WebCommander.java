package com.andrenacci.spotapp;

import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.PlaybackBitrate;
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.SpotifyPlayer;

import org.json.JSONException;
import org.json.JSONObject;
import com.spotify.sdk.android.player.Metadata;

public class WebCommander {
    //public static String appId;
    //public static String token;
    public static SpotifyManager manager;
    public static MainActivity activity;


    public boolean exec(JSONObject req)  {
        try {
            return _exec(req);
        } catch (Exception e) {
            e.printStackTrace();
            activity.videolog("wex="+e.getMessage());
            return false;
        }
    }

    private static SpotifyPlayer player() {
        return SpotifyManager.getPlayer();
    }

    public boolean _exec(JSONObject req) throws Exception {
        JSONObject input = req.getJSONObject("input");
        String cmd = req.getJSONObject("input").getString("cmd").toLowerCase();
        if (cmd!=null && !cmd.equals("getstatus"))
            activity.videolog("cmd="+cmd);

        System.out.println("WWW "+cmd+": "+input.toString());

        try {
            if (cmd.equals("play"))
                return execPlay(req);
//            if (cmd.equals("getauth"))
//                return execGetAuth(req);
            if (cmd.equals("getstatus"))
                return execGetStatus(req);
            if (cmd.equals("pause"))
                return execPause(req);
            if (cmd.equals("resume"))
                return execResume(req);

            if (cmd.equals("next"))
                return execSkipToNext(req);
            if (cmd.equals("previous"))
                return execSkipToPrevious(req);
            if (cmd.equals("seek"))
                return execSeekToPosition(req);
            if (cmd.equals("queue"))
                return execQueue(req);
            if (cmd.equals("shuffle"))
                return execSetShuffle(req);
            if (cmd.equals("repeat"))
                return execSetRepeat(req);
//            if (cmd.equals("settoken"))
//                return execSetToken(req);
            if (cmd.equals("ping"))
                return execPing(req);
            if (cmd.equals("getlog"))
                return execGetLog(req);

        }
        catch(Exception e) {
            req.put("error", e.getMessage());
            activity.videolog("weberr: "+e.getMessage());
            return false;
        }
        req.put("error", "command not found");
        return false;
    }

    public boolean execPlay(JSONObject req) throws Exception {
        String url = req.getJSONObject("input").getString("url");
        if (player()!=null)
            player().playUri(null, url, 0, 0);
        else
            activity.videolog("skipped (player==null)");
        return true;
    }


    public boolean execGetStatus(JSONObject req)  throws Exception  {
        if (player()==null) {
            System.out.println("getStatus: player==null");
            return false;
        }

        try {
            PlaybackState s = player().getPlaybackState();
            //req.put("endOfSongCount", manager.endOfSongCount);
            req.put("isPlaying", s.isPlaying);
            req.put("positionMs", s.positionMs);
            req.put("isActiveDevice", s.isActiveDevice);
            req.put("isShuffling", s.isShuffling);
            req.put("endOfSongCount", SpotifyManager.endOfSongCount);

            Metadata.Track t = SpotifyManager.currTrack;
            if (t!=null) {
                JSONObject track = new JSONObject();
                req.put("track", track);
                track.put("artistName", t.artistName);
                track.put("albumName", t.albumName);
                track.put("albumUri", t.albumUri);
                track.put("trackUri", t.uri);
                track.put("artistUri", t.artistUri);
                track.put("albumCoverWebUrl", t.albumCoverWebUrl);
                track.put("durationMs", t.durationMs);
                System.out.println(track.toString(2));
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean execPause(JSONObject req)  throws Exception  {
        if (player()!=null)
            player().pause(null);
        return true;
    }

    public boolean execResume(JSONObject req)  throws Exception  {
        if (player()!=null)
            player().resume(null);
        return true;
    }


    public boolean execSkipToNext(JSONObject req)  throws Exception  {
        if (player()!=null)
            player().skipToNext(null);
        return true;
    }
    public boolean execSkipToPrevious(JSONObject req)  throws Exception  {
        if (player()!=null)
            player().skipToPrevious(null);
        return true;
    }

    public boolean execQueue(JSONObject req)  throws Exception  {
        String uri = req.getJSONObject("input").getString("uri");
        if (player()!=null)
            player().queue(null, uri);
        return true;
    }

    public boolean execSetShuffle(JSONObject req)  throws Exception  {
        boolean value = req.getJSONObject("input").getBoolean("value");
        if (player()!=null)
            player().setShuffle(null, value);
        return true;
    }

    public boolean execSetRepeat(JSONObject req)  throws Exception  {
        boolean value = req.getJSONObject("input").getBoolean("value");
        if (player()!=null)
            player().setRepeat(null, value);
        return true;
    }

    public boolean execSeekToPosition(JSONObject req)  throws Exception  {
        if (req.getJSONObject("input").has("ms")) {
            int pos = req.getJSONObject("input").getInt("ms");
            if (player()!=null)
                player().seekToPosition(null, pos);
            return true;
        }
        if (req.getJSONObject("input").has("perc")) {
            double pos = req.getJSONObject("input").getDouble("perc");
            Metadata.Track t = player().getMetadata().currentTrack;
            if (t!=null) {
                double k = pos*t.durationMs/100.;
                if (player()!=null)
                    player().seekToPosition(null, (int) k);
                return true;
            }
        }
        return false;
    }

    public boolean execGetLog(JSONObject req)  throws Exception  {
        req.put("log", LogContent.get());
        return true;
    }

    public boolean execPing(JSONObject req)  throws Exception  {
        return true;
    }

//    public boolean execSetToken(JSONObject req)  throws Exception  {
////        manager.setToken(null);
////        String token = req.getJSONObject("input").getString("token");
////        manager.setToken(token);
//        return true;
//    }


}
