package euphoria.psycho.fileserver.handlers;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

import euphoria.psycho.fileserver.Database;
import euphoria.psycho.fileserver.Nanos;
import euphoria.psycho.fileserver.Shared;

public class GitHubHandler {
    public static Response handle() {
        try {
            List<String> htmlUrls = fetchHtmlUrls().get();
            // .limit(10)
            String contents = htmlUrls.stream().parallel().map(x -> {
                try {
                    HttpsURLConnection c = (HttpsURLConnection) new URL(
                            "https://raw.githubusercontent.com/"
                                    + Shared.substringAfter(x, "com/").replace("/blob/","/")
                    ).openConnection();
                    return readString(c);
                } catch (IOException e) {
                    return "";
                }
            }).collect(Collectors.joining("\n"));
            Response response = Response.newFixedLengthResponse(Status.OK,
                    "text/plain", contents);
            return Nanos.crossOrigin(response);
        } catch (Exception e) {
            return Nanos.internalError(e);
        }
    }

    static CompletableFuture<List<String>> fetchHtmlUrls() {
        CompletableFuture<List<String>> task = CompletableFuture.supplyAsync(() -> {
            try {
                HttpsURLConnection c = (HttpsURLConnection) new URL("https://api.github.com/search/code?q=vmess+extension%3Atxt&sort=indexed&order=desc").openConnection();
                c.addRequestProperty("Accept", "application/vnd.github+json");
                c.addRequestProperty("X-GitHub-Api-Version", "2022-11-28");
                // https://github.com/settings/personal-access-tokens
                c.addRequestProperty("Authorization", "Bearer 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");
                String contents = readString(c);
                JSONObject object = new JSONObject(contents);
                JSONArray items = object.getJSONArray("items");
                List<String> array = new ArrayList<>();
                for (int i = 0; i < items.length(); i++) {
                    array.add(items.getJSONObject(i).getString("html_url"));
                }
                return array;
            } catch (Exception ignored) {
            }
            return null;
        });
        return task;
    }

    public static String readString(HttpURLConnection connection) {
        InputStream in;
        BufferedReader reader = null;
        try {
            String contentEncoding = connection.getHeaderField("Content-Encoding");
            if (contentEncoding != null && contentEncoding.equals("gzip")) {
                in = new GZIPInputStream(connection.getInputStream());
            } else {
                in = connection.getInputStream();
            }
            /*
            "implementation group": "org.brotli', name: 'dec', version: '0.1.1",
            else if (contentEncoding != null && contentEncoding.equals("br")) {
                in = new BrotliInputStream(connection.getInputStream());
            } */
            //  if (contentEncoding != null && contentEncoding.equals("br")) {
            //in = new BrotliInputStream(connection.getInputStream());
            //  }
            reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\r\n");
            }
            return sb.toString();
        } catch (Exception ignored) {
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}