package euphoria.psycho.fileserver;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import euphoria.psycho.fileserver.Shared.FileInfo;
import euphoria.psycho.fileserver.handlers.DeleteHandler;
import euphoria.psycho.fileserver.handlers.ExportHandler;
import euphoria.psycho.fileserver.handlers.ListNotesHandler;
import euphoria.psycho.fileserver.handlers.MoveHandler;
import euphoria.psycho.fileserver.handlers.NoteHandler;

import static euphoria.psycho.fileserver.MainActivity.TREE_URI;

public class FileServer extends NanoHTTPD {
    private final String mDirectory;
    private final String mStoragePath;
    private final String mTreeUri;
    private Context mContext;
    private AssetManager mAssetManager;
    private HashMap<String, String> mHashMap = new HashMap<>();
    private Database mDatabase;

    public FileServer(Context context) {
        super(Shared.getDeviceIP(context), 8089);
        // 192.168.8.55:8089
        mContext = context;
        mAssetManager = mContext.getAssets();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mTreeUri = sharedPreferences.getString(TREE_URI, null);
        mStoragePath = Shared.getExternalStoragePath(mContext);
        mDirectory = Shared.substringBeforeLast(mContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath(), "/Android/data");
        mDatabase = new Database(mContext, new File(
                mContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "notes.db"
        ).getAbsolutePath());
    }

    private static String getMimeType(String path) {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                Shared.substringAfterLast(path, "."));
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

    private static Response getThumbnail(String path) {
        try {
            File videoFile = new File(path);
            File parent = new File(videoFile.getParentFile(), ".images");
            if (!parent.isDirectory()) {
                parent.mkdir();
            }
            File thumbnailFile = new File(parent, Shared.md5(videoFile.getAbsolutePath()));
            if (thumbnailFile.exists()) {
                return Response.newChunkedResponse(Status.OK,
                        "image/jpeg", new FileInputStream(thumbnailFile));
            }
            Bitmap thumbnail = Shared.createVideoThumbnail(videoFile.getAbsolutePath());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            thumbnail.compress(CompressFormat.JPEG, 85, stream);
            byte[] bytes = stream.toByteArray();
            FileOutputStream outputStream = new FileOutputStream(thumbnailFile);
            outputStream.write(bytes);
            outputStream.close();
            Response res = Response.newFixedLengthResponse(Status.OK,
                    "image/jpeg", bytes);
            return res;
        } catch (Exception e) {
            return Response.newFixedLengthResponse(Status.INTERNAL_ERROR,
                    "text/plain", e.getMessage());
        }
    }

    private static Response listAndroidData(Context context, String treeUri, String uri) {
        List<FileInfo> files = Shared.listAndroidData(context, treeUri, Uri.encode(uri));
        return serveFiles(files);
    }

    public static Response serveFile(Context context, String treeUri, String path) {
        try {
            InputStream stream = context.getContentResolver()
                    .openInputStream(Utils.buildDocumentUri(treeUri, path));
            return serverFile(stream, path);
        } catch (Exception e) {
            Log.e("B5aOx2", String.format("serveFile, %s", e.getMessage()));
            return Response.newFixedLengthResponse(Status.INTERNAL_ERROR,
                    "text/plain", e.getMessage());
        }
    }

    private static Response serveFiles(List<FileInfo> files) {
        JSONArray jsonArray = new JSONArray();
        for (FileInfo f : files) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", f.Name);
                jsonObject.put("parent", f.Parent);
                jsonObject.put("isDir", f.IsDir);
                jsonObject.put("lastModified", f.LastModified);
                jsonObject.put("length", f.Length);
                jsonArray.put(jsonObject);
            } catch (Exception ignored) {
            }
        }
        Response response = Response.newFixedLengthResponse(Status.OK, "application/json", jsonArray.toString());
        response.addHeader("Access-Control-Allow-Origin", "*");
        return response;
    }

    private static Response serveNormalFile(IHTTPSession session, String path) {
        try {
            Response response =
                    Utils.serveFile(session.getHeaders(), new File(path), getMimeType(path));
            response.addHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", Uri.encode(Shared.substringAfterLast(path, "/"))));
            return response;
            //serverFile(stream, parameters[0]);
        } catch (Exception e) {
            return Utils.internalError(e);
        }
    }

    private static Response serveNormalFiles(String path) {
        File dir = new File(path);
        File[] fileArray = dir.listFiles();
        List<FileInfo> files = new ArrayList<>();
        if (fileArray != null) {
            for (File file : fileArray) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.Parent = path;
                fileInfo.Name = file.getName();
                fileInfo.LastModified = file.lastModified();
                fileInfo.IsDir = file.isDirectory();
                if (!fileInfo.IsDir) {
                    fileInfo.Length = file.length();
                }
                files.add(fileInfo);
            }
        }
        return serveFiles(files);
    }

    private static Response serverFile(InputStream stream, String path) {
        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types
        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Disposition
        String mimeType = "application/octet-stream";
        if (path.endsWith(".mp4")) {
            mimeType = "video/mp4";
        }
        Response response = Response.newChunkedResponse(Status.OK, mimeType, stream);
        response.addHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", Uri.encode(Shared.substringAfterLast(path, "/"))));
        return response;

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

    private static Response handleNotFound(String uri) {
        if (uri.equals("/favicon.ico"))
            return Utils.notFound();
        return null;
    }

    private Response handleStaticFiles(String uri) {
        if (uri.equals("/") || uri.endsWith(".js") || uri.endsWith(".css")
                || uri.endsWith(".svg") || uri.endsWith(".html")) {
            String mimeType = "application/javascript";
            if (uri.endsWith(".svg")) {
                mimeType = "image/svg+xml";
            } else if (uri.equals("/") || uri.endsWith(".html")) {
                mimeType = "text/html";
            } else if (uri.endsWith(".css")) {
                mimeType = "text/css";
            }
            if (mHashMap.containsKey(uri)) {
                return Response.newFixedLengthResponse(Status.OK, mimeType, mHashMap.get(uri));
            }
            String fileName = uri.substring(1);
            if (uri.equals("/")) {
                fileName = "index.html";
            }
            return readAsset(fileName, uri, mimeType);
        }
        return null;
    }

    @Override
    protected Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Response res = handleNotFound(uri);
        if (res != null) {
            return res;
        }
        res = handleStaticFiles(uri);
        if (res != null) {
            return res;
        }
        if (uri.equals("/api/files")) {
            String[] parameters = getParameters(session);
            if (parameters[2].equals("delete") && parameters[0].startsWith("/")) {
                if (parameters[0].startsWith("/Android/data")) {
                    try {
                        // TODO a very dangerous operation, if the wrong path is passed will completely wipe the whole folder
                        DocumentsContract.deleteDocument(mContext.getContentResolver(), Utils.buildDocumentUri(mTreeUri, parameters[0]));
                    } catch (Exception e) {
                        return Response.newFixedLengthResponse(Status.INTERNAL_ERROR,
                                "text/plain", e.getMessage());
                    }
                } else {
                    return Utils.deleteFileSystem(new File(parameters[0]
                    ));
                }
            } else if (parameters[2].equals("move") && parameters[0].startsWith("/")) {
                if (parameters[0].contains("/Android/data")) {
                } else {
                    String path = parameters[0];
                    if (path.startsWith(mStoragePath)) {
                    } else {
                        File dir = new File(mDirectory, ".Recycle");
                        if (!dir.exists()) {
                            dir.mkdir();
                        }
                        File source = new File(path);
                        source.renameTo(new File(dir, source.getName()));
                        return Utils.crossOrigin(Utils.ok());
                    }
                    return Utils.notFound();
                }
            } else if (parameters[2].equals("preview") && parameters[0].startsWith("/")) {
                if (parameters[0].contains("/Android/data")) {
                    // getDocumentThumbnail site:googlesource.com
                    try {
                        String name = Shared.substringBeforeLast(Shared.substringAfterLast(parameters[0], "Android/data"),
                                "/files")
                                + "/files/Documents/"
                                + Shared.md5(mDirectory + parameters[0]);
                        return Response.newChunkedResponse(Status.OK,
                                "image/jpeg", mContext.getContentResolver().openInputStream(
                                        Utils.buildDocumentUri(mTreeUri, name)
                                ));

                    } catch (Exception e) {
                        Log.e("B5aOx2", String.format("serve, %s", e.getMessage()));
                        return Utils.internalError(e);
                    }
                } else
                    return getThumbnail(parameters[0]);
            } else if (parameters[1].equals("1"))
                if (parameters[0].contains("/Android/data"))
                    return listAndroidData(mContext, mTreeUri, Shared.substringAfterLast(parameters[0], "/Android/data"));
                else {
                    String p = parameters[0];
                    if (p.length() == 0) {
                        p = mDirectory;
                    }
                    return serveNormalFiles(p);
                }
            else {
                if (parameters[0].contains("/Android/data"))
                    return serveFile(mContext, mTreeUri, parameters[0]);
                else {
                    return serveNormalFile(session, parameters[0]);
                }
            }
        }
        if (uri.equals("/api/delete")) {
            return DeleteHandler.handle(mContext, mTreeUri, session);
        }
        if (uri.equals("/api/move")) {
            return MoveHandler.handle(mContext, mTreeUri, mDirectory, session);
        }
        if (uri.equals("/post")) {
            Map<String, String> files = new HashMap<String, String>();
            try {
                session.parseBody(files, mDirectory);
            } catch (Exception e) {
                Log.e("B5aOx2", String.format("serve, %s", e.getMessage()));
            }
            return Utils.ok();
        }
        if (uri.equals("/api/notes")) {
            return ListNotesHandler.handle(mDatabase);
        }
        if (uri.equals("/api/note")) {
            return NoteHandler.handle(mDatabase,session);
        }
        if (uri.equals("/api/export")) {
            return ExportHandler.handle(mDatabase);
        }
        return Utils.notFound();
    }

    private String toString(Map<String, ? extends Object> map) {
        if (map.size() == 0) {
            return "";
        }
        return unsortedList(map);
    }

    private String unsortedList(Map<String, ? extends Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ul>");
        for (Map.Entry<String, ? extends Object> entry : map.entrySet()) {
            Log.e("B5aOx2", String.format("unsortedList, %s = %s", entry.getKey(), entry.getValue()));
        }
        sb.append("</ul>");
        return sb.toString();
    }


}