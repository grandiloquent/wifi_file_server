package euphoria.psycho.fileserver.handlers;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import euphoria.psycho.fileserver.Douyin;
import euphoria.psycho.fileserver.Nanos;
import euphoria.psycho.fileserver.Shared;
import euphoria.psycho.fileserver.TikTok;
import euphoria.psycho.fileserver.Twitter;
import euphoria.psycho.fileserver.VideoDatabase;
import euphoria.psycho.fileserver.VideoDatabase.Video;
import euphoria.psycho.fileserver.XVideos;

public class VideosHandler {
    public static Response handle(VideoDatabase videoDatabase, IHTTPSession session, String dir) {
        Map<String, List<String>> parameters = session.getParameters();
        if (session.getUri().equals("/v")) {
            return exploreVideo(videoDatabase, session);
        } else if (session.getUri().equals("/v/all")) {
            try {
                return Response.newFixedLengthResponse(Status.OK, "application/json",
                        videoDatabase.queryAll(Nanos.intParamOr(session, "t", 0)));
            } catch (JSONException e) {
                return Nanos.crossOrigin(Nanos.notFound());
            }
        } else if (session.getUri().equals("/v/remove")) {
            return deleteVideo(videoDatabase, session);
        } else if (session.getUri().equals("/v/import")) {
            try {
                final HashMap<String, String> map = new HashMap<String, String>();
                session.parseBody(map, dir);
                final String json = map.get("postData");
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Video video = new Video();
                    if (jsonObject.has("_id")) video.Id = jsonObject.getInt("_id");
                    if (jsonObject.has("title")) video.Title = jsonObject.getString("title");
                    if (jsonObject.has("url")) video.Url = jsonObject.getString("url");
                    if (jsonObject.has("play")) video.Play = jsonObject.getString("play");
                    if (jsonObject.has("music_play"))
                        video.MusicPlay = jsonObject.getString("music_play");
                    if (jsonObject.has("music_title"))
                        video.MusicTitle = jsonObject.getString("music_title");
                    if (jsonObject.has("music_author"))
                        video.MusicAuthor = jsonObject.getString("music_author");
                    if (jsonObject.has("cover")) video.Cover = jsonObject.getString("cover");
                    if (jsonObject.has("video_type"))
                        video.VideoType = jsonObject.getInt("video_type");
                    if (jsonObject.has("create_at"))
                        video.CreateAt = jsonObject.getLong("create_at");
                    if (jsonObject.has("update_at"))
                        video.UpdateAt = jsonObject.getLong("update_at");
                    videoDatabase.insertVideo(video);
                }
            } catch (Exception e) {
            }
        }
        return Nanos.crossOrigin(Nanos.notFound());
    }

    private static Response exploreVideo(VideoDatabase videoDatabase, IHTTPSession session) {
        String q = Nanos.stringParam(session, "q");
        if (q == null) {
            return Nanos.notFound();
        }
        try {
            Video video = null;
            if (q.startsWith("https://www.tiktok.com/")) {
                video = TikTok.fetch(q);
            } else if (q.startsWith("https://www.xvideos.com/")) {
                video = XVideos.fetch(q);
            } else if (q.startsWith("https://twitter.com/i/status/")) {
                video = Twitter.fetch(q);
            }else if (q.contains("https://v.douyin.com/")) {
                video = Douyin.fetch(q);
            }
            if (video == null) {
                return Nanos.notFound();
            }
            videoDatabase.insertVideo(video);
            return Nanos.ok();
        } catch (Exception e) {
            return Nanos.internalError(e);
        }
    }

    private static Response deleteVideo(VideoDatabase videoDatabase, IHTTPSession session) {
        int id = Nanos.intParamOr(session, "q", 0);
        if (id == 0) {
            return Nanos.notFound();
        }
        Video video = new Video();
        video.Id = id;
        video.VideoType = Nanos.intParamOr(session, "t", 0);
        videoDatabase.updateVideo(video);
        return Nanos.ok();
    }
}
