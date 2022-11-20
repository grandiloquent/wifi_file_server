package euphoria.psycho.fileserver.handlers;

import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import euphoria.psycho.fileserver.Nanos;
import euphoria.psycho.fileserver.Shared;
import euphoria.psycho.fileserver.Utils;

public class HtmlHandler {
    public static Response handle(IHTTPSession session) {
        String path = Nanos.stringParam(session, "path");
        if (path == null || !path.endsWith(".html")) {
            return null;
        }
        File source = new File(path);
        if (!source.exists()) {
        }
        if (path.endsWith(".html")) {
            try {
                FileInputStream is = new FileInputStream(source);
                String body = Shared.readAllText(is);
                body = Shared.substringAfter(Shared.substring(body, "<body", "</body>"), ">");
                is.close();
                return Response.newFixedLengthResponse(Status.OK, "text/html; charset=utf-8", body.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                return Nanos.internalError(e);
            }
        }
//        List<String> paths = session.getParameters().get("path");
//        if (paths == null) {
//            if (!session.getUri().contains("/api/")||!session.getUri().contains(".")) return null;
//            String referer = session.getHeaders().get("referer");
//            if (referer != null) {
//                String dir = Shared.substringBeforeLast(Uri.parse(referer).getQueryParameter("path"), "/");
//                String filename = Shared.substringAfterLast(session.getUri(), "/api");
//                try {
//                    byte[] bytes = Files.readAllBytes(Paths.get(dir + filename));
//                    return Response.newFixedLengthResponse(Status.OK, MimeTypeMap.getSingleton().getMimeTypeFromExtension(
//                            Shared.substringAfterLast(session.getUri(), ".")
//                    ), bytes);
//                } catch (IOException e) {
//                    return Nanos.notFound();
//                }
//            }
//            return null;
//        }
//        String p = paths.get(0);
//        if (p.endsWith(".html")) {
//            try {
//                byte[] bytes = Files.readAllBytes(Paths.get(p));
//                return Response.newFixedLengthResponse(Status.OK, "text/html; charset=utf-8", bytes);
//            } catch (IOException e) {
//                return Nanos.notFound();
//            }
//
//        }
    }
}
