package euphoria.psycho.fileserver.handlers;

import android.util.Log;

import org.json.JSONException;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import euphoria.psycho.fileserver.Database;
import euphoria.psycho.fileserver.Utils;

public class ListNotesHandler {
    public static Response handle(Database database) {
        try {
            String js = database.queryNotes();
            if (js != null)
                return Response.newFixedLengthResponse(Status.OK,
                        "application/json", js);
        } catch (Exception ignored) {
        }
        return Utils.crossOrigin(Utils.notFound());
    }
}
