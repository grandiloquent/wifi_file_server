package euphoria.psycho.fileserver.handlers;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

import euphoria.psycho.fileserver.Nanos;
import euphoria.psycho.fileserver.Shared;
import euphoria.psycho.fileserver.Utils;

public class HtmlHandler {
    public static Response handle(Context context, IHTTPSession session) {
        Pattern pattern = Pattern.compile("\\.(?:jpg|svg|png|jpeg)");
        String path = Nanos.stringParam(session, "path");
        if (path == null && (!session.getUri().contains("/api") || !pattern.matcher(session.getUri()).find())) {
            return null;
        } else if (path != null && (!path.endsWith(".html") && !path.endsWith(".xhtml"))) {
            return null;
        }
        File source = null;
        if (path != null) {
            source = new File(path);
        }
        if (source == null || !source.exists()) {
            String referer = session.getHeaders().get("referer");
            if (referer != null) {
                String dir = Shared.substringBeforeLast(Uri.parse(referer).getQueryParameter("path"), "/");
                source = new File(dir, Shared.substringAfterLast(session.getUri(), "/api"));
            }
        }
        if (source == null || !source.exists()) {
            return Nanos.notFound();
        }
        if (path != null && (path.endsWith(".html") || path.endsWith(".xhtml") || path.endsWith(".htm"))) {
            try {
                InputStream isa = context.getAssets().open("html.html");
                String template = Shared.readAllText(isa);
                isa.close();
                FileInputStream is = new FileInputStream(source);
                String body = Shared.readAllText(is);
                body = Shared.substringAfter(Shared.substring(body, "<body", "</body>"), ">");
                is.close();
                body = template.replace("{{body}}", body);
                return Response.newFixedLengthResponse(Status.OK, "text/html; charset=utf-8", body.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                return Nanos.internalError(e);
            }
        } else {
            try {
                byte[] bytes = Files.readAllBytes(source.toPath());
                return Response.newFixedLengthResponse(Status.OK, MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        Shared.substringAfterLast(session.getUri(), ".")
                ), bytes);
            } catch (IOException e) {
                return Nanos.internalError(e);
            }
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

