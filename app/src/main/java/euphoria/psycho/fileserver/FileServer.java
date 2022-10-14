package euphoria.psycho.fileserver;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.DocumentsContract.Document;
import android.util.Log;
import android.util.Pair;
import android.webkit.MimeTypeMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import euphoria.psycho.fileserver.Shared.FileInfo;

import static euphoria.psycho.fileserver.MainActivity.TREE_URI;

public class FileServer extends NanoHTTPD {
    private Context mContext;
    private AssetManager mAssetManager;
    private HashMap<String, String> mHashMap = new HashMap<>();
    private final String mTreeUri;

    public FileServer(Context context) {
        super(Shared.getDeviceIP(context), 8089);
        // 192.168.8.55:8089
        mContext = context;
        mAssetManager = mContext.getAssets();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mTreeUri = sharedPreferences.getString(TREE_URI, null);
    }

    private Response readAsset(String filename, String uri, String mimeType) {
        try {
            InputStream in = mAssetManager.open(filename);
            String contents = Shared.readAllText(in);
            mHashMap.put(uri, contents);
            in.close();
            return Response.newFixedLengthResponse(Status.OK, mimeType, contents);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected Response serve(IHTTPSession session) {
        String uri = session.getUri();
        if (uri.equals("/favicon.ico")) {
            return Response.newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "Not Found");
        } else if (uri.equals("/")) {
            String filename = "index.html";
            if (uri.equals("/string")) {
                filename = "string.html";
            }
            if (mHashMap.containsKey(uri)) {
                return Response.newFixedLengthResponse(Status.OK, "text/html", mHashMap.get(uri));
            }
            Response response = readAsset(filename, uri, "text/html");
            if (response != null)
                return response;
        } else if (uri.endsWith(".js") || uri.endsWith(".css")
                || uri.endsWith(".svg")) {
            String mimeType = "application/javascript";
            if (uri.endsWith(".svg")) {
                mimeType = "image/svg+xml";
            }
            if (mHashMap.containsKey(uri)) {
                return Response.newFixedLengthResponse(Status.OK, mimeType, mHashMap.get(uri));
            }
            Response response = readAsset(uri.substring(1), uri, mimeType);
            if (response != null)
                return response;
        } else if (uri.equals("/api/files")) {
            String[] parameters = getParameters(session);
            if (parameters[2].equals("delete")) {
                try {
                    DocumentsContract.deleteDocument(mContext.getContentResolver(),
                            Uri.parse(mTreeUri + "/document/primary%3AAndroid%2Fdata" + Uri.encode(parameters[0])));
                    // ,
                    //                            Uri.parse(mTreeUri + "/document/primary%3AAndroid%2Fdata" + Uri.encode(Shared.substringBeforeLast(parameters[0], "/")))

                } catch (Exception e) {
                    Log.e("B5aOx2", String.format("servexxxxxxxxxxxxxxxxx, %s", e.getMessage()));
                    return Response.newFixedLengthResponse(Status.INTERNAL_ERROR,
                            "text/plain", e.getMessage());
                }
            } else if (parameters[1].equals("1"))
                return listAndroidData(mContext, mTreeUri, parameters[0]);
            else {
                return serveFile(mContext, mTreeUri, parameters[0]);
            }
        }
        return Response.newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "Not Found");
    }

    private static Response listAndroidData(Context context, String treeUri, String uri) {
        List<FileInfo> files = Shared.listAndroidData(context, treeUri, Uri.encode(uri));
        JSONArray jsonArray = new JSONArray();
        for (FileInfo f : files) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", f.Name);
                jsonObject.put("isDir", f.IsDir);
                jsonObject.put("lastModified", f.LastModified);
                jsonArray.put(jsonObject);
            } catch (Exception ignored) {
            }
        }
        Response response = Response.newFixedLengthResponse(Status.OK, "application/json", jsonArray.toString());
        response.addHeader("Access-Control-Allow-Origin", "*");
        return response;
    }

    private static String[] getParameters(IHTTPSession session) {
        Map<String, List<String>> parameters = session.getParameters();
        String path = "";
        String isDir = "1";
        String action = "";
        if (parameters.containsKey("path")) {
            if (parameters.get("path").size() > 0)
                path = parameters.get("path").get(0);
        }
        if (parameters.containsKey("isDir")) {
            if (parameters.get("isDir").size() > 0)
                isDir = parameters.get("isDir").get(0);
        }
        if (parameters.containsKey("action")) {
            if (parameters.get("action").size() > 0)
                action = parameters.get("action").get(0);
        }
        return new String[]{
                path, isDir, action
        };
    }

    private static Response serveFile(Context context, String treeUri, String path) {
        try {
            InputStream stream = context.getContentResolver()
                    .openInputStream(Uri.parse(
                            //"content://content%3A%2F%2Fcom.android.externalstorage.documents%2Ftree%2Fprimary%253AAndroid%252Fdata/" +
                            treeUri + "/document/primary%3AAndroid%2Fdata" + Uri.encode(path)
                    ));
            // https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types
            // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Disposition
            Response response = Response.newChunkedResponse(Status.OK, "application/octet-stream", stream);
            response.addHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", Shared.substringAfterLast(path, "/")));
            return response;
        } catch (Exception e) {
            return Response.newFixedLengthResponse(Status.INTERNAL_ERROR,
                    "text/plain", e.getMessage());
        }
    }
}