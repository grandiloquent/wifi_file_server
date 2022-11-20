package euphoria.psycho.fileserver.handlers;

import android.util.Log;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import euphoria.psycho.fileserver.Database;
import euphoria.psycho.fileserver.FileServer;
import euphoria.psycho.fileserver.Nanos;
import euphoria.psycho.fileserver.Utils;

public class BookPageHandler {
    public static Response handle(IHTTPSession session) {
        String path = Nanos.stringParam(session, "path");
        if (!path.endsWith(".html")) {
            return null;
        }
        return null;
    }
}
