package euphoria.psycho.fileserver.handlers;

import org.json.JSONException;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import euphoria.psycho.fileserver.Database;
import euphoria.psycho.fileserver.Utils;

public class ListNotesHandler {
    public static Response handle(Database database) {
        try {
            return Response.newFixedLengthResponse(Status.OK,
                    "application/json", database.queryNotes());
        } catch (JSONException ignored) {
        }
        return Utils.crossOrigin(Utils.notFound());
    }
}
