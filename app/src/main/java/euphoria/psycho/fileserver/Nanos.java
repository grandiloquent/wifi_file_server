package euphoria.psycho.fileserver;


import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.util.ArrayList;
import java.util.List;

public class Nanos {
    public static Response crossOrigin(Response response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        return response;
    }

    public static int intParam(IHTTPSession session, String name) {
        List<String> values = session.getParameters().get(name);
        if (values == null || values.size() != 1) {
            throw new IllegalArgumentException(String.format("Wrong value provided for '%s' parameter: %s", name, values));
        }
        return Integer.parseInt(values.get(0));
    }

    public static int intParamOr(IHTTPSession session, String name, int defaultValue) {
        List<String> values = session.getParameters().get(name);
        if (values == null || values.size() != 1) {
            return defaultValue;
        }
        return Integer.parseInt(values.get(0));
    }

    public static long longParam(IHTTPSession session, String name) {
        List<String> values = session.getParameters().get(name);
        if (values == null || values.size() != 1) {
            throw new IllegalArgumentException(String.format("Wrong value provided for '%s' parameter: %s", name, values));
        }
        return Long.parseLong(values.get(0));
    }

    public static List<Long> longParams(IHTTPSession session, String name) {
        List<String> values = session.getParameters().get(name);
        List<Long> longs = new ArrayList<>();
        if (values != null) {
            for (String value : values) {
                String[] parts = value.split(",");
                for (String part : parts) {
                    longs.add(Long.valueOf(part));
                }
            }
        }
        return longs;
    }

    public static Response notFound() {
        Response response = Response.newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "Not Found");
        response.addHeader("Access-Control-Allow-Origin", "*");
        return response;
    }

    public static Response ok() {
        Response response = Response.newFixedLengthResponse(Status.OK, "text/plain", "Ok");
        response.addHeader("Access-Control-Allow-Origin", "*");
        return response;
    }

    public static String stringParam(IHTTPSession session, String name) {
        List<String> stringParams = session.getParameters().get(name);
        return stringParams != null && stringParams.size() > 0 ? stringParams.get(0) : null;
    }

    public static Response internalError(Exception e) {
        Response response = Response.newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", e.getMessage());
        response.addHeader("Access-Control-Allow-Origin", "*");
        return response;
    }
}
