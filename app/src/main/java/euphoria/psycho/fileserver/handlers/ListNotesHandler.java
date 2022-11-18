package euphoria.psycho.fileserver.handlers;

import android.util.Log;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import euphoria.psycho.fileserver.Database;
import euphoria.psycho.fileserver.FileServer;
import euphoria.psycho.fileserver.Nanos;
import euphoria.psycho.fileserver.Utils;

public class ListNotesHandler {
    public static Response handle(Database database, IHTTPSession session) {
        try {
//            fileServer.ensureConnection();
//            String js = fileServer.executeJSON("select * from _query_notes()");
//            if (js != null)
            String tag = Nanos.stringParam(session, "tag");
            if (tag.equals("tag")) {
                return Nanos.crossOrigin(Response.newFixedLengthResponse(Status.OK,
                        "application/json", database.queryTags()));
            }
            return Nanos.crossOrigin(Response.newFixedLengthResponse(Status.OK,
                    "application/json", database.queryNotes(tag)));
        } catch (Exception ignored) {
            return Utils.internalError(ignored.getMessage());
        }
    }
}
