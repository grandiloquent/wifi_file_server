package euphoria.psycho.fileserver.handlers;

import android.content.Context;
import android.provider.DocumentsContract;

import org.json.JSONArray;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;

import java.io.File;
import java.util.Objects;

import euphoria.psycho.fileserver.Utils;

public class DeleteHandler {
    private static boolean deleteDocument(Context context, String treeUri, String path) {
        if (path.startsWith("/Android/data")) {
            try {
                // TODO a very dangerous operation, if the wrong path is passed will completely wipe the whole folder
                DocumentsContract.deleteDocument(context.getContentResolver(), Utils.buildDocumentUri(treeUri,
                        path));
            } catch (Exception ignored) {
            }
            return true;
        }
        return false;
    }

    public static Response handle(Context context, String treeUri, IHTTPSession session) {
        try {
            JSONArray files = new JSONArray(Utils.readString(session));
            for (int i = 0; i < files.length(); i++) {
                String path = files.getString(i);
                if (deleteDocument(context, treeUri, path)) {
                    continue;
                }
                Utils.deleteFileSystem(new File(path));
            }
        } catch (Exception ignored) {
        }
        return Utils.notFound();
    }
}
