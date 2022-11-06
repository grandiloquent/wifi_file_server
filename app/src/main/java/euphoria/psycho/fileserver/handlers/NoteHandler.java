package euphoria.psycho.fileserver.handlers;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import euphoria.psycho.fileserver.FileServer;
import euphoria.psycho.fileserver.Nanos;
import euphoria.psycho.fileserver.Utils;

public class NoteHandler {
    public static Response handle(FileServer fileServer, IHTTPSession session) {
        if (session.getMethod() == Method.POST) {
            try {
                fileServer.ensureConnection();
                String r = fileServer.executeQuery(String.format("select * from _insert_notes('%s')", escapeMetaCharacters(Utils.readString(session))));
                return Nanos.crossOrigin(Response.newFixedLengthResponse(Status.OK,
                        "text/plain", r));
            } catch (Exception ignored) {
            }

        } else {
            int id = Integer.parseInt(session.getParameters().get("id").get(0));
            try {
                fileServer.ensureConnection();
                String js = fileServer.executeJSON(String.format("select * from _query_note(%d)", id));
                return Nanos.crossOrigin(Response.newFixedLengthResponse(Status.OK,
                        "application/json",
                        js));
            } catch (Exception ignored) {
                return Nanos.crossOrigin(Response.newFixedLengthResponse(Status.NOT_FOUND, "text/plain", ignored.getMessage()));
            }

        }
        return Nanos.crossOrigin(Nanos.notFound());
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
