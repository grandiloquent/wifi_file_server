package euphoria.psycho.fileserver.handlers;

import android.content.Context;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import euphoria.psycho.fileserver.Nanos;
import euphoria.psycho.fileserver.Utils;

public class NewFileHandler {
    public static Response handle(Context context, IHTTPSession session)  {
        Map<String, List<String>> parameters = session.getParameters();
        String src = Utils.getParameter(parameters, "src");
        String dst = Utils.getParameter(parameters, "dst");
        if (src == null || dst == null) {
            return Nanos.crossOrigin(Nanos.notFound());
        }
        File srcFile = new File(src);
        File dstFile = new File(srcFile, dst);
        if (srcFile.exists() && !dstFile.exists()) {
            try {
                dstFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Nanos.crossOrigin(Nanos.ok());
        }
        return Nanos.crossOrigin(Nanos.notFound());
    }
}
