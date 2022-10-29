package euphoria.psycho.fileserver;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.IStatus;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Utils {

    public static Uri buildDocumentUri(String treeUri, String path) {
        return Uri.parse(treeUri + "/document/primary%3AAndroid%2Fdata" + Uri.encode(
                Shared.substringAfterLast(path, "/Android/data")
        ));
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

    public static String getParameter(Map<String, List<String>> parameters, String key) {
        String src = null;
        if (parameters.size() > 0) {
            List<String> obj = parameters.get(key);
            if (obj != null)
                src = obj.get(0);
        }
        return src;
    }

    public static String getRealPathFromURI(Context context, Uri contentURI, String type) {
        String result = null;
        try {
            Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
            if (cursor == null) { // Source is Dropbox or other similar local file path
                result = contentURI.getPath();
                Log.d("TAG", "result******************" + result);
            } else {
                cursor.moveToFirst();
                int idx = 0;
                if (type.equalsIgnoreCase("IMAGE")) {
                    idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                } else if (type.equalsIgnoreCase("VIDEO")) {
                    idx = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA);
                } else if (type.equalsIgnoreCase("AUDIO")) {
                    idx = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);
                }
                result = cursor.getString(idx);
                Log.d("TAG", "result*************else*****" + result);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("TAG", "Exception ", e);
        }
        return result;
    }

    public static Response internalError(Exception e) {
        return Response.newFixedLengthResponse(Status.INTERNAL_ERROR,
                "text/plain", e.getMessage());
    }

    public static Response internalError(String message) {
        Response response = Response.newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", message);
        response.addHeader("Access-Control-Allow-Origin", "*");
        return response;
    }

    public static Response newFixedLengthResponse(IStatus status, String mimeType, String message) {
        Response response = Response.newFixedLengthResponse(status, mimeType, message);
        response.addHeader("Accept-Ranges", "bytes");
        return response;
    }

    public static Response notFound() {
        return Response.newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "Not Found");
    }

    public static Response ok() {
        return Response.newFixedLengthResponse(Status.OK,
                "text/plain", "Ok");
    }

    public static String processPath(String storagePath, String directory, String path) {
        if (path.startsWith(storagePath)) {
            return path;
        }
        return directory + path;
    }

    public static String readString(IHTTPSession session) throws IOException {
        int contentLength = Integer.parseInt(Objects.requireNonNull(session.getHeaders().get("content-length")));
        byte[] buffer = new byte[contentLength];
        session.getInputStream().read(buffer, 0, contentLength);
        return new String(buffer);
    }

    public static Response serveFile(Map<String, String> header, File file, String mime) {
        Response res;
        try {
            // Calculate etag
            String etag = Integer.toHexString((file.getAbsolutePath() + file.lastModified() + "" + file.length()).hashCode());
            // Support (simple) skipping:
            long startFrom = 0;
            long endAt = -1;
            String range = header.get("range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    try {
                        if (minus > 0) {
                            startFrom = Long.parseLong(range.substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            // get if-range header. If present, it must match etag or else we
            // should ignore the range request
            String ifRange = header.get("if-range");
            boolean headerIfRangeMissingOrMatching = (ifRange == null || etag.equals(ifRange));
            String ifNoneMatch = header.get("if-none-match");
            boolean headerIfNoneMatchPresentAndMatching = ifNoneMatch != null && ("*".equals(ifNoneMatch) || ifNoneMatch.equals(etag));
            // Change return code and add Content-Range header when skipping is
            // requested
            long fileLen = file.length();
            if (headerIfRangeMissingOrMatching && range != null && startFrom >= 0 && startFrom < fileLen) {
                // range request that matches current etag
                // and the startFrom of the range is satisfiable
                if (headerIfNoneMatchPresentAndMatching) {
                    // range request that matches current etag
                    // and the startFrom of the range is satisfiable
                    // would return range from file
                    // respond with not-modified
                    res = newFixedLengthResponse(Status.NOT_MODIFIED, mime, "");
                    res.addHeader("ETag", etag);
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1;
                    }
                    long newLen = endAt - startFrom + 1;
                    if (newLen < 0) {
                        newLen = 0;
                    }
                    FileInputStream fis = new FileInputStream(file);
                    fis.skip(startFrom);
                    res = Response.newFixedLengthResponse(Status.PARTIAL_CONTENT, mime, fis, newLen);
                    res.addHeader("Accept-Ranges", "bytes");
                    res.addHeader("Content-Length", "" + newLen);
                    res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
                    res.addHeader("ETag", etag);
                }
            } else {
                if (headerIfRangeMissingOrMatching && range != null && startFrom >= fileLen) {
                    // return the size of the file
                    // 4xx responses are not trumped by if-none-match
                    res = newFixedLengthResponse(Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "");
                    res.addHeader("Content-Range", "bytes */" + fileLen);
                    res.addHeader("ETag", etag);
                } else if (range == null && headerIfNoneMatchPresentAndMatching) {
                    // full-file-fetch request
                    // would return entire file
                    // respond with not-modified
                    res = newFixedLengthResponse(Status.NOT_MODIFIED, mime, "");
                    res.addHeader("ETag", etag);
                } else if (!headerIfRangeMissingOrMatching && headerIfNoneMatchPresentAndMatching) {
                    // range request that doesn't match current etag
                    // would return entire (different) file
                    // respond with not-modified
                    res = newFixedLengthResponse(Status.NOT_MODIFIED, mime, "");
                    res.addHeader("ETag", etag);
                } else {
                    // supply the file
                    res = newFixedFileResponse(file, mime);
                    res.addHeader("Content-Length", "" + fileLen);
                    res.addHeader("ETag", etag);
                }
            }
        } catch (IOException ioe) {
            res = getForbiddenResponse("Reading file failed.");
        }
        return res;
    }

    protected static Response getForbiddenResponse(String s) {
        return Response.newFixedLengthResponse(Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: " + s);
    }

    private static Response newFixedFileResponse(File file, String mime) throws FileNotFoundException {
        Response res;
        res = Response.newFixedLengthResponse(Status.OK, mime, new FileInputStream(file), (int) file.length());
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

}