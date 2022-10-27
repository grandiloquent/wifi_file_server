package euphoria.psycho.fileserver.handlers;

import android.content.Context;
import android.util.Log;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;

import java.io.File;
import java.util.List;
import java.util.Map;

import euphoria.psycho.fileserver.Utils;

public class RenameHandler {


    public static Response handle(Context context, IHTTPSession session) {
        Map<String, List<String>> parameters = session.getParameters();
        String src = Utils.getParameter(parameters, "src");
        String dst = Utils.getParameter(parameters, "dst");
        if (src == null || dst == null) {
            return Utils.crossOrigin(Utils.notFound());
        }
        File srcFile = new File(src);
        File dstFile = new File(srcFile.getParentFile(), dst);
        if (srcFile.exists() && !dstFile.exists()) {
            srcFile.renameTo(dstFile);
            return Utils.crossOrigin(Utils.ok());
        }
        return Utils.crossOrigin(Utils.notFound());
    }
}