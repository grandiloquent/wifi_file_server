package euphoria.psycho.fileserver.handlers;

import android.content.Context;
import android.provider.DocumentsContract;
import android.util.Log;

import org.json.JSONArray;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;

import java.io.File;
import java.io.IOException;
import java.util.List;

import euphoria.psycho.fileserver.Nanos;
import euphoria.psycho.fileserver.Shared;
import euphoria.psycho.fileserver.Utils;

public class MoveHandler {
    private static boolean moveDocument(Context context, String treeUri, String path, String dst) {
        if (path.startsWith("/Android/data")) {
            try {
                DocumentsContract.moveDocument(context.getContentResolver(), Utils.buildDocumentUri(treeUri,
                        path), Utils.buildDocumentUri(treeUri,
                        Shared.substringBeforeLast(path, "/")), Utils.buildDocumentUri(treeUri, dst));
            } catch (Exception ignored) {
                Log.e("B5aOx2", String.format("moveDocument, %s", ignored));
            }
            return true;
        }
        return false;
    }

    public static Response handle(Context context, String treeUri, String directory, IHTTPSession session) {
        try {
            List<String> dh = session.getParameters().get("dst");
            String dst = dh == null ? directory : dh.get(0);
            JSONArray files = new JSONArray(Utils.readString(session));
            for (int i = 0; i < files.length(); i++) {
                String path = files.getString(i);
                if (moveDocument(context, treeUri, path, dst)) {
                    continue;
                }
                File src = new File(path);
                File d = new File(dst, src.getName());
                if (src.exists() && !d.exists()) {
                    src.renameTo(d);
                }
            }
            return Nanos.ok();
        } catch (Exception ignored) {
            Log.e("B5aOx2", String.format("handle, %s", ignored.getMessage()));
        }
        return Nanos.notFound();
    }

    public static void recursiveRename(File sourceDir, File destDir) throws IOException {
        File[] childFiles = sourceDir.listFiles();
        if (childFiles == null) {
            throw new IOException(String.format(
                    "Failed to recursively copy. Could not determine contents for directory '%s'",
                    sourceDir.getAbsolutePath()));
        }
        for (File childFile : childFiles) {
            File destChild = new File(destDir, childFile.getName());
            if (childFile.isDirectory()) {
                if (!destChild.mkdir()) {
                    throw new IOException(String.format("Could not create directory %s",
                            destChild.getAbsolutePath()));
                }
                recursiveRename(childFile, destChild);
            } else if (childFile.isFile()) {
                childFile.renameTo(destChild);
            }
        }
    }
}

/*
https://gerrit.googlesource.com/plugins/delete-project/+/refs/heads/stable-2.10/src/main/java/com/googlesource/gerrit/plugins/deleteproject/fs/DeleteTrashFolders.java
https://android.googlesource.com/platform/tools/tradefederation/+/ae241fc/src/com/android/tradefed/util/StreamUtil.java
 */
