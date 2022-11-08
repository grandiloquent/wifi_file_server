package euphoria.psycho.fileserver.handlers;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import euphoria.psycho.fileserver.Key;
import euphoria.psycho.fileserver.Nanos;

public class TtsHandler {
    public static Response handle(IHTTPSession session) {
        String q = Nanos.stringParam(session, "q");
        String sessionId = Nanos.stringParam(session, "sessionId");
        String modelType = Nanos.stringParam(session, "modelType");
        String videoType = Nanos.stringParam(session, "videoType");
        String volume = Nanos.stringParam(session, "volume");
        try {
            byte[] bytes = tts(q, sessionId, modelType, videoType, volume,
                    Key.SECRET_ID, Key.SECRET_KEY);
            Response response = Response.newFixedLengthResponse(
                    Status.OK,
                    "audio/wav",
                    bytes
            );
            response.addHeader("Content-Disposition", "attachment; filename=\"audio.wav\"");
            return response;
        } catch (Exception e) {
            return Nanos.internalError(e);
        }
    }

    static byte[] tts(String text, String sessionId, String modelType, String videoType, String volume, String secretId, String secretKey) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("POSTtts.tencentcloudapi.com/?Action")
                .append("=").append("TextToVoice")
                .append("&ModelType")
                .append("=").append(modelType)
                .append("&Nonce")
                .append("=").append(String.valueOf(Math.abs(new SecureRandom().nextInt())))
                .append("&Region")
                .append("=").append("ap-beijing")
                .append("&RequestClient")
                .append("=").append("SDK_NET_3.0.635")
                .append("&SecretId")
                .append("=").append(secretId)
                .append("&SessionId")
                .append("=").append(sessionId)
                .append("&SignatureMethod")
                .append("=").append("HmacSHA256")
                .append("&Text")
                .append("=").append(text.replaceAll("\r", ""))
                .append("&Timestamp")
                .append("=").append(System.currentTimeMillis() / 1000)
                .append("&Version")
                .append("=").append("2019-08-23");
        if (videoType != null) {
            sb.append("&VoiceType=").append(videoType);
        }
        if (volume != null) {
            sb.append("&Volume")
                    .append("=").append(volume);
        }
        Mac mac = Mac.getInstance("HmacSHA256");
        byte[] hash;
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), mac.getAlgorithm());
        mac.init(secretKeySpec);
        hash = mac.doFinal(sb.toString().getBytes(StandardCharsets.UTF_8));
        sb.append("&Signature").append("=").append(
                URLEncoder.encode(Base64.getEncoder().encodeToString(hash), "UTF-8").replaceAll("\n", "%0A")
        );
        HttpURLConnection c = (HttpURLConnection) new URL("https://tts.tencentcloudapi.com").openConnection();
        c.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        c.setDoOutput(true);
        c.setRequestMethod("POST");
        OutputStream outputStream = c.getOutputStream();
        outputStream.write(sb.toString().split("\\?", 2)[1].getBytes(StandardCharsets.UTF_8));
        outputStream.close();
        String result = new BufferedReader(new InputStreamReader(c.getInputStream()))
                .lines().parallel().collect(Collectors.joining("\n"));
        int start = result.indexOf("\"Audio\":\"") + 9;
        int end = result.indexOf("\"", start);
        result = result.substring(start, end);
        return Base64.getDecoder().decode(result);
    }

}
