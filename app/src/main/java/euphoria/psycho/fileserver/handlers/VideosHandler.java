package euphoria.psycho.fileserver.handlers;

import org.json.JSONException;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import euphoria.psycho.fileserver.Nanos;
import euphoria.psycho.fileserver.TikTok;
import euphoria.psycho.fileserver.Twitter;
import euphoria.psycho.fileserver.VideoDatabase;
import euphoria.psycho.fileserver.VideoDatabase.Video;
import euphoria.psycho.fileserver.XVideos;

public class VideosHandler {
    public static Response handle(VideoDatabase videoDatabase, IHTTPSession session) {
        Map<String, List<String>> parameters = session.getParameters();
        if (session.getUri().equals("/v")) {
            return exploreVideo(videoDatabase, session);
        } else if (session.getUri().equals("/v/all")) {
            try {
                return Response.newFixedLengthResponse(Status.OK, "application/json", videoDatabase.queryAll());
            } catch (JSONException e) {
                return Nanos.crossOrigin(Nanos.notFound());
            }
        } else if (session.getUri().equals("/v/remove")) {
            return deleteVideo(videoDatabase, session);
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
        videoDatabase.deleteVideo(id);
        return Nanos.ok();
    }
}
