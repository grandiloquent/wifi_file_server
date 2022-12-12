package euphoria.psycho.fileserver.handlers;

import android.content.Context;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.IOException;

import euphoria.psycho.fileserver.Nanos;

public class VideoHandler {
    public static Response handle(Context context, IHTTPSession session) {
        try {
            String uri = session.getUri();
            Response response = responsiveVideoPage(context, uri);
            if (response != null) return response;
            return null;
        } catch (Exception error) {
            return Nanos.internalError(error);
        }
    }

    public static Response responsiveVideoPage(Context context, String uri) {
        if (!uri.equals("/video")) {
            return null;
        }
        try {
            return Response.newChunkedResponse(Status.OK,
                    "text/html", context.getAssets().open("video.html"));
        } catch (IOException e) {
            return Nanos.internalError(e);
        }
    }
}
