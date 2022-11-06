package euphoria.psycho.fileserver.handlers;

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
        if (parameters.containsKey("q")) {
            List<String> qs = parameters.get("q");
            if (qs == null || qs.size() == 0) {
                return Utils.crossOrigin(Utils.notFound());
            }
            String q = qs.get(0);
            if (q.startsWith("https://www.tiktok.com/")) {
                try {
                    String contents = TikTok.fetchJson(q);
                    return Response.newFixedLengthResponse(Status.OK,
                            "application/json", contents);
                } catch (Exception e) {
                    return Utils.crossOrigin(Utils.notFound());
                }
            }
        }
        return Utils.crossOrigin(Utils.notFound());
    }
}
