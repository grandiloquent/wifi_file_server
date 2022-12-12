package euphoria.psycho.fileserver.handlers;

import android.util.Log;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;

import euphoria.psycho.fileserver.Nanos;
import euphoria.psycho.fileserver.Utils;

public class VideoHandler {
    public static Response handle(IHTTPSession session) {
        try {
            return Nanos.notFound();
        } catch (Exception error) {
            return Nanos.internalError(error);
        }
    }
}
