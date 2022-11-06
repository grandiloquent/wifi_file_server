package euphoria.psycho.fileserver.handlers;

import android.util.Log;

import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import euphoria.psycho.fileserver.FileServer;
import euphoria.psycho.fileserver.Nanos;
import euphoria.psycho.fileserver.Utils;

public class ListNotesHandler {
    public static Response handle(FileServer fileServer) {
        try {
            Log.e("B5aOx2", String.format("handle, %s", "ensureConnection"));
            fileServer.ensureConnection();
            Log.e("B5aOx2", String.format("handle, %s", "executeJSON"));
            String js = fileServer.executeJSON("select * from _query_notes()");
            if (js != null)
                return Nanos.crossOrigin(Response.newFixedLengthResponse(Status.OK,
                        "application/json", js));
        } catch (Exception ignored) {
            return Utils.internalError(ignored.getMessage());
        }
        return Nanos.crossOrigin(Nanos.notFound());
    }
}
