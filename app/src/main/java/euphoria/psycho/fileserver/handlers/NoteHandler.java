package euphoria.psycho.fileserver.handlers;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.IOException;

import euphoria.psycho.fileserver.Database;
import euphoria.psycho.fileserver.Utils;

public class NoteHandler {
    public static Response handle(Database database, IHTTPSession session) {
        if (session.getMethod() == Method.POST) {
            try {
                JSONObject obj = new JSONObject(Utils.readString(session));
                long r = 0;
                if (obj.has("id")) {
                    r = database.updateNote(
                            obj.getInt("id"),
                            obj.getString("title"),
                            obj.getString("content")
                    );
                } else {
                    r = database.insertNote(
                            obj.getString("title"),
                            obj.getString("content")
                    );
                }
                return Utils.crossOrigin(Response.newFixedLengthResponse(Status.OK,
                        "text/plain", Long.toString(r)));
            } catch (Exception ignored) {
            }
        } else {
            int id = Integer.parseInt(session.getParameters().get("id").get(0));
            try {
                String js = database.queryNote(id);
                return Utils.crossOrigin(Response.newFixedLengthResponse(Status.OK,
                        "application/json",
                        js));
            } catch (Exception ignored) {
                Log.e("B5aOx2", String.format("handle, %s", ignored.getMessage()));
            }
        }
        return Utils.crossOrigin(Utils.notFound());
    }
}
