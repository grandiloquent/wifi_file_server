package euphoria.psycho.fileserver.handlers;

import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.File;
import java.io.FileInputStream;

import euphoria.psycho.fileserver.Database;
import euphoria.psycho.fileserver.FileServer;
import euphoria.psycho.fileserver.Nanos;
import euphoria.psycho.fileserver.Shared;
import euphoria.psycho.fileserver.Utils;

public class ExportHandler {
    public static Response handle(Database database, IHTTPSession session) {
        try {
            if (session.getMethod() == Method.POST) {
                JSONArray jsonArray = new JSONArray(
                        Shared.readAllText(new FileInputStream(
                                new File(Environment.getExternalStorageDirectory(),"psycho_public_notes.json")
                        ))
                );
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject js = jsonArray.getJSONObject(i);
                    database.insertNote(js);
                }
            } else
                //fileServer.ensureConnection();
                //String js = fileServer.executeQuery("select * from notes");
                return Nanos.crossOrigin(Response.newFixedLengthResponse(Status.OK,
                        "application/json",
                        database.queryAll()));
        } catch (Exception ignored) {
            Log.e("B5aOx2", String.format("handle, %s", ignored.getMessage()));
        }
        return Nanos.crossOrigin(Nanos.notFound());
    }
}
