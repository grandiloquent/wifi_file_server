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
import euphoria.psycho.fileserver.XVideos;

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
                    videoDatabase.insertVideo(TikTok.fetch(q));
                    return Utils.crossOrigin(Utils.ok());
                } catch (Exception e) {
                    return Utils.crossOrigin(Utils.notFound());
                }
            }
            if (q.startsWith("https://www.xvideos.com/")) {
                try {
                    videoDatabase.insertVideo(XVideos.fetch(q));
                    return Utils.crossOrigin(Utils.ok());
                } catch (Exception e) {
                    return Utils.crossOrigin(Utils.notFound());
                }
            }

            if (q.startsWith("https://twitter.com/i/status/")) {
                try {
                    videoDatabase.insertVideo(XVideos.fetch(q));
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
