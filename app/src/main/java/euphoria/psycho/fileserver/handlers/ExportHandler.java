package euphoria.psycho.fileserver.handlers;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import euphoria.psycho.fileserver.Database;
import euphoria.psycho.fileserver.FileServer;
import euphoria.psycho.fileserver.Utils;

public class ExportHandler {
    public static Response handle(FileServer fileServer, IHTTPSession session) {
        try {
            fileServer.ensureConnection();
            String js = fileServer.executeQuery("select * from notes");
            return Utils.crossOrigin(Response.newFixedLengthResponse(Status.OK,
                    "application/json",
                    js));
        } catch (Exception ignored) {
        }
        return Utils.crossOrigin(Utils.notFound());
    }
}
