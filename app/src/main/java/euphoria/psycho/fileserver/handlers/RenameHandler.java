package euphoria.psycho.fileserver.handlers;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;

import euphoria.psycho.fileserver.Utils;

public class RenameHandler {
    public static Response handle(IHTTPSession session) {
        return Utils.crossOrigin(Utils.notFound());
    }
}
