package weixinlib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weixinlib.response.AccessTokenResult;
import weixinlib.response.DownloadMediaResult;
import weixinlib.response.UploadMediaResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import com.ning.http.multipart.FilePart;

public class WeixinApi {

    private static final int INTERNAL_ERROR = -99;

    private static final Logger logger = LoggerFactory.getLogger(WeixinApi.class);

    public static AccessTokenResult getAccessToken(String appId, String appSecret) {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        try {
            Future<Response> f = asyncHttpClient.prepareGet(
                    "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
                            + URLEncoder.encode(appId, "utf-8") + "&secret=" + URLEncoder.encode(appSecret, "utf-8"))
                    .execute();
            Response r = f.get();
            String response = r.getResponseBody();
            ObjectMapper mapper = new ObjectMapper();
            AccessTokenResult result = null;
            try {
                result = mapper.readValue(response, AccessTokenResult.class);
            } catch (Exception e) {
                logger.debug(e.getMessage());
                result.setErrcode(INTERNAL_ERROR);
            }
            if (result == null) {
                result = new AccessTokenResult();
                result.setErrcode(INTERNAL_ERROR);
            }
            return result;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            asyncHttpClient.close();
        }
        return null;
    };

    public static UploadMediaResult uploadMedia(String accessToken, String type, String filePath) {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        try {
            String url = "http://file.api.weixin.qq.com/cgi-bin/media/upload?access_token="
                    + URLEncoder.encode(accessToken, "utf-8") + "&type=" + URLEncoder.encode(type, "utf-8");
            File file = new File(filePath);
            ListenableFuture<Response> f = asyncHttpClient.preparePost(url)
                    .setHeader("Content-Type", "multipart/form-data").addBodyPart(new FilePart("media", file))
                    .execute();
            Response r = f.get();
            ObjectMapper mapper = new ObjectMapper();
            UploadMediaResult result = null;
            try {
                result = mapper.readValue(r.getResponseBody(), UploadMediaResult.class);
            } catch (Exception e) {
                logger.debug(e.getMessage());
                result.setErrcode(INTERNAL_ERROR);
            }
            if (result == null) {
                result = new UploadMediaResult();
                result.setErrcode(INTERNAL_ERROR);
            }
            return result;
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (ExecutionException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } finally {
            asyncHttpClient.close();
        }
        return null;
    }

    public static DownloadMediaResult downloadMedia(String accessToken, String mediaId, String filePath) {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        try {
            String url = "http://file.api.weixin.qq.com/cgi-bin/media/get?access_token="
                    + URLEncoder.encode(accessToken, "utf-8") + "&media_id=" + URLEncoder.encode(mediaId, "utf-8");
            File file = new File(filePath);
            final ResultHolder resultHolder = new ResultHolder();
            FileOutputStream stream = new FileOutputStream(file);
            Future<Response> f = asyncHttpClient.prepareGet(url).execute(new AsyncHandler<Response>() {

                private final Response.ResponseBuilder builder = new Response.ResponseBuilder();

                public void onThrowable(Throwable t) {
                }

                public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
                    if (resultHolder.success) {
                        bodyPart.writeTo(stream);
                    } else {
                        builder.accumulate(bodyPart);
                    }
                    return STATE.CONTINUE;
                }

                @Override
                public STATE onStatusReceived(HttpResponseStatus status) throws Exception {
                    builder.accumulate(status);
                    return STATE.CONTINUE;
                }

                @Override
                public STATE onHeadersReceived(HttpResponseHeaders headers) throws Exception {
                    builder.accumulate(headers);
                    FluentCaseInsensitiveStringsMap headerMap = headers.getHeaders();
                    if (!headerMap.getFirstValue("Content-Type").equalsIgnoreCase("text/plain")
                            && headerMap.containsKey("Content-disposition")) {
                        resultHolder.success = true;
                    } else {
                        resultHolder.success = false;
                    }
                    return STATE.CONTINUE;
                }

                @Override
                public Response onCompleted() throws Exception {
                    return builder.build();
                }
            });

            Response r = f.get();
            stream.close();

            DownloadMediaResult result = new DownloadMediaResult();
            if (resultHolder.success) {
                result.setErrcode(0);
                result.setMedia_id(mediaId);
            } else {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    result = mapper.readValue(r.getResponseBody(), DownloadMediaResult.class);
                } catch (Exception e) {
                    logger.debug(e.getMessage());
                    result.setErrcode(INTERNAL_ERROR);
                }
            }
            return result;
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (ExecutionException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } finally {
            asyncHttpClient.close();
        }
        return null;
    }

}
