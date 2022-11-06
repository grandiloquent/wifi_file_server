package euphoria.psycho.fileserver.handlers;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.util.List;
import java.util.Map;

import euphoria.psycho.fileserver.TikTok;
import euphoria.psycho.fileserver.Utils;
import euphoria.psycho.fileserver.VideoDatabase;

public class VideosHandler {
    public static Response handle(VideoDatabase videoDatabase, IHTTPSession session) {
        Map<String, List<String>> parameters = session.getParameters();
        if (session.getUri().equals("/v") && parameters.containsKey("q")) {
            List<String> qs = parameters.get("q");
            if (qs == null || qs.size() == 0) {
                return Utils.crossOrigin(Utils.notFound());
            }
            String q = qs.get(0);
            if (q.startsWith("https://www.tiktok.com/")) {
                try {
                    String contents = TikTok.fetchJson(q);
                    JSONObject jsonObject = new JSONObject(contents);
                    videoDatabase.insertVideo(
                            jsonObject.getJSONObject("data").getString("title"),
                            q,
                            "https://www.tikwm.com/" + jsonObject.getJSONObject("data").getString("hdplay"),
                            jsonObject.getJSONObject("data").getJSONObject("music_info").getString("play"),
                            jsonObject.getJSONObject("data").getJSONObject("music_info").getString("title"),
                            jsonObject.getJSONObject("data").getJSONObject("music_info").getString("author"),
                            "https://www.tikwm.com/" + jsonObject.getJSONObject("data").getString("cover"),
                            System.currentTimeMillis(),
                            System.currentTimeMillis()
                    );
                    return Utils.crossOrigin(Utils.ok());
                } catch (Exception e) {
                    return Utils.crossOrigin(Utils.notFound());
                }
            }
        } else if (session.getUri().equals("/v/all")) {
            try {
                return Response.newFixedLengthResponse(Status.OK,
                        "application/json", videoDatabase.queryAll());
            } catch (JSONException e) {
                return Utils.crossOrigin(Utils.notFound());
            }
        } else if (session.getUri().equals("/v/remove")) {
            if (parameters.containsKey("q")) {
                List<String> qs = parameters.get("q");
                if (qs == null || qs.size() == 0) {
                    return Utils.crossOrigin(Utils.notFound());
                }
                String q = qs.get(0);
                videoDatabase.deleteVideo(Integer.parseInt(q));
            }
        }
        return Utils.crossOrigin(Utils.notFound());
    }
}
