package euphoria.psycho.fileserver;

import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.File;

public class Utils {

    public static String processPath(String storagePath, String directory, String path) {
        if (path.startsWith(storagePath)) {
            return path;
        }
        return directory + path;
    }

    public static Response crossOrigin(Response response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        return response;
    }

    public static Response deleteFileSystem(File path) {
        Response response;
        try {
            Shared.recursiveDelete(path);
            response = ok();
        } catch (Exception e) {
            response = internalError(e);

        }
        return crossOrigin(response);
    }

    public static Response internalError(Exception e) {
        return Response.newFixedLengthResponse(Status.INTERNAL_ERROR,
                "text/plain", e.getMessage());
    }

    public static Response notFound() {
        return Response.newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "Not Found");
    }

    public static Response ok() {
        return Response.newFixedLengthResponse(Status.OK,
                "text/plain", "Ok");
    }
}