package euphoria.psycho.fileserver.handlers;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import euphoria.psycho.fileserver.Utils;
import euphoria.psycho.fileserver.VideoDatabase;

public class VideosHandler {
    public static Response handle(VideoDatabase videoDatabase, IHTTPSession session) {
        if (session.getUri().equals("/x")) {
        }
        return Utils.crossOrigin(Utils.notFound());
    }
}
