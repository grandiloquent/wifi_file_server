package euphoria.psycho.fileserver.handlers;

import android.net.Uri;
import android.webkit.MimeTypeMap;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import euphoria.psycho.fileserver.Shared;
import euphoria.psycho.fileserver.Utils;

public class HtmlHandler {
    public static Response handle(IHTTPSession session) {
        List<String> paths = session.getParameters().get("path");
        if (paths == null) {
            if (!session.getUri().contains("/api/")||!session.getUri().contains(".")) return null;
            String referer = session.getHeaders().get("referer");
            if (referer != null) {
                String dir = Shared.substringBeforeLast(Uri.parse(referer).getQueryParameter("path"), "/");
                String filename = Shared.substringAfterLast(session.getUri(), "/api");
                try {
                    byte[] bytes = Files.readAllBytes(Paths.get(dir + filename));
                    return Response.newFixedLengthResponse(Status.OK, MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                            Shared.substringAfterLast(session.getUri(), ".")
                    ), bytes);
                } catch (IOException e) {
                    return Utils.notFound();
                }
            }
            return null;
        }
        String p = paths.get(0);
        if (p.endsWith(".html")) {
            try {
                byte[] bytes = Files.readAllBytes(Paths.get(p));
                return Response.newFixedLengthResponse(Status.OK, "text/html", bytes);
            } catch (IOException e) {
                return Utils.notFound();
            }

        }
        return null;
    }
}
