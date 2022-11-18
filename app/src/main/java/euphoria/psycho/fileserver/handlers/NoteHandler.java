package euphoria.psycho.fileserver.handlers;

import android.util.Log;

import org.json.JSONObject;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import euphoria.psycho.fileserver.Database;
import euphoria.psycho.fileserver.FileServer;
import euphoria.psycho.fileserver.Nanos;
import euphoria.psycho.fileserver.Utils;

public class NoteHandler {
    public static Response handle(Database database, IHTTPSession session) {
        if (session.getMethod() == Method.POST) {
            try {
                //   fileServer.ensureConnection();
                // String r = fileServer.executeQuery(String.format("select * from _insert_notes('%s')", escapeMetaCharacters(Utils.readString(session))));
                JSONObject js = new JSONObject(Utils.readString(session));
                String r = "";
                if (js.has("_id"))
                    r = Long.toString(database.updateNote(js.getInt("_id"), js.getString("title"), js.getString("content"),js.getString("tag")));
                else
                    r = Long.toString(database.insertNote(js.getString("title"), js.getString("content")));
                return Nanos.crossOrigin(Response.newFixedLengthResponse(Status.OK,
                        "text/plain", r));
            } catch (Exception ignored) {
                return Nanos.internalError(ignored);
            }

        } else {
            int id = Integer.parseInt(session.getParameters().get("id").get(0));
            try {
                //fileServer.ensureConnection();
                //String js = fileServer.executeJSON(String.format("select * from _query_note(%d)", id));
                return Nanos.crossOrigin(Response.newFixedLengthResponse(Status.OK,
                        "application/json",
                        database.queryNote(id)));
            } catch (Exception ignored) {
                return Nanos.crossOrigin(Response.newFixedLengthResponse(Status.NOT_FOUND, "text/plain", ignored.getMessage()));
            }

        }
    }

    public static String escapeMetaCharacters(String inputString) {
        final String[] metaCharacters = {"'",};// "\""
        for (int i = 0; i < metaCharacters.length; i++) {
            if (inputString.contains(metaCharacters[i])) {
                inputString = inputString.replace(metaCharacters[i], metaCharacters[i] + metaCharacters[i]);
            }
        }
        return inputString;
    }
}
