package eu.vcmi.vcmi.util;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * @author F
 */
public abstract class AsyncRequest<T> extends AsyncTask<String, Void, ServerResponse<T>>
{
    protected ServerResponse<T> sendRequest(final String url)
    {
        try
        {
            final HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            final int responseCode = conn.getResponseCode();
            if (!ServerResponse.isResponseCodeValid(responseCode))
            {
                return new ServerResponse<>(responseCode, null);
            }
            final Scanner s = new java.util.Scanner(conn.getInputStream()).useDelimiter("\\A");
            final String response = s.hasNext() ? s.next() : "";
            return new ServerResponse<>(responseCode, response);
        }
        catch (Exception e)
        {
            Log.e(this, "Request failed: ", e);
        }
        return new ServerResponse<>(ServerResponse.LOCAL_ERROR_IO, null);
    }

}
