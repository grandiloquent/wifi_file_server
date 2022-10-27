package euphoria.psycho.fileserver.handlers;

import android.content.Context;
import android.util.Log;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;

import java.io.File;

import euphoria.psycho.fileserver.Utils;

public class RenameHandler {
    public static Response handle(Context context, IHTTPSession session) {
        String src = session.getParameters().get("src").get(0);
        String dst = session.getParameters().get("dst").get(0);
        File srcFile = new File(src);
        File dstFile = new File(srcFile.getParentFile(), dst);
        if (srcFile.exists() && !dstFile.exists()) {
            srcFile.renameTo(dstFile);
            return Utils.crossOrigin(Utils.ok());
        }
        return Utils.crossOrigin(Utils.notFound());
    }
}
