package euphoria.psycho.fileserver;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
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
        Log.e("B5aOx2", String.format("serve, %s", uri));
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
            Log.e("B5aOx2", String.format("serve, %s", MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension("svg")));
            List<FileInfo> files = Shared.listAndroidData(mContext, mTreeUri);
            JSONArray jsonArray = new JSONArray();
            for (FileInfo f : files) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", f.Name);
                    jsonObject.put("isDir", f.IsDir);
                    jsonObject.put("lastModified", f.LastModified);
                    jsonArray.put(jsonObject);
                } catch (Exception e) {
                }
            }
            Response response = Response.newFixedLengthResponse(Status.NOT_FOUND, "application/json", jsonArray.toString());
            response.addHeader("Access-Control-Allow-Origin", "*");
            return response;
        }
        return Response.newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "Not Found");
    }
}