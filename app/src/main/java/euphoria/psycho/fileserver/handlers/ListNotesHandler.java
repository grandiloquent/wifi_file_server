package euphoria.psycho.fileserver.handlers;

import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import euphoria.psycho.fileserver.FileServer;
import euphoria.psycho.fileserver.Utils;

public class ListNotesHandler {
    public static Response handle(FileServer fileServer) {
        try {
            fileServer.ensureConnection();
            String js = fileServer.executeJSON("select * from _query_notes()");
            if (js != null)
                return Utils.crossOrigin(Response.newFixedLengthResponse(Status.OK,
                        "application/json", js));
        } catch (Exception ignored) {
        }
        return Utils.crossOrigin(Utils.notFound());
    }
}
