package euphoria.psycho.fileserver.handlers;

import android.content.Context;
import android.util.Log;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import euphoria.psycho.fileserver.Nanos;
import euphoria.psycho.fileserver.Shared;
import fr.noop.subtitle.model.SubtitleParsingException;
import fr.noop.subtitle.srt.SrtObject;
import fr.noop.subtitle.srt.SrtParser;
import fr.noop.subtitle.vtt.VttWriter;

public class VideoHandler {
    public static Response handle(Context context, IHTTPSession session) {
        try {
            String uri = session.getUri();
            Response response = responsiveVideoPage(context, uri);
            if (response != null) return response;
            response = responsiveSubtitleFile(context, session);
            if (response != null) return response;
            return null;
        } catch (Exception error) {
            return Nanos.internalError(error);
        }
    }

    public static Response responsiveSubtitleFile(Context context, IHTTPSession session) {
        String path = Nanos.stringParam(session, "path");
        if (path == null || !path.endsWith("vtt")) return null;
        SrtParser parser = new SrtParser("utf-8");
        VttWriter writer = new VttWriter("utf-8");
        try {
            SrtObject subtitle = parser.parse(new FileInputStream(
                    Shared.substringBeforeLast(path, ".") + ".srt"
            ));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            writer.write(subtitle, bos);
            return Response.newFixedLengthResponse(Status.OK,
                    "text/vtt", bos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SubtitleParsingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Response responsiveVideoPage(Context context, String uri) {
        if (!uri.equals("/video")) {
            return null;
        }
        try {
            return Response.newChunkedResponse(Status.OK,
                    "text/html", context.getAssets().open("video.html"));
        } catch (IOException e) {
            return Nanos.internalError(e);
        }
    }
}
