package weixinlib;

import java.io.FileInputStream;
import java.util.Properties;

import junit.framework.TestCase;
import weixinlib.response.AccessTokenResult;
import weixinlib.response.DownloadMediaResult;
import weixinlib.response.UploadMediaResult;

public class WeixinApiTest extends TestCase {
    private String appId;
    private String appSecret;

    @Override
    protected void setUp() throws Exception {
        // TODO Auto-generated method stub
        super.setUp();

        Properties props = new Properties();
        props.load(new FileInputStream("config.properties"));
        appId = props.getProperty("appId");
        appSecret = props.getProperty("appSecret");
    }

    public void testGetAccessToken() {
        AccessTokenResult accessToken = WeixinApi.getAccessToken(appId, appSecret);

        assertNotNull(accessToken);

        if (accessToken.getErrcode() == 0) {
            System.out.println("AccessToken" + accessToken.getAccess_token());
        } else {
            System.out.println("Error:" + accessToken.getErrcode() + " " + accessToken.getErrmsg());
        }
    }

    public void testUploadMedia() {
        AccessTokenResult accessToken = WeixinApi.getAccessToken(appId, appSecret);

        assertNotNull(accessToken);

        UploadMediaResult result = WeixinApi.uploadMedia(accessToken.getAccess_token(), "image", "image.jpg");

        if (result.getErrcode() == 0) {
            System.out.println(result.getMedia_id());
        } else {
            System.out.println("Error:" + result.getErrcode() + " " + result.getErrmsg());
        }
    }

    public void testDownloadMedia() {
        AccessTokenResult accessToken = WeixinApi.getAccessToken(appId, appSecret);

        assertNotNull(accessToken);

        DownloadMediaResult result = WeixinApi.downloadMedia(accessToken.getAccess_token(),
                "jGeQ9ps4r1Vj8qduCmJuzRgOzzowe__D9yz-miTFfqVrnKx7p1NbunrR3vJeoiHt", "tmp.jpg");

        if (result.getErrcode() == 0) {
            System.out.println(result.getMedia_id());
        } else {
            System.out.println("Error:" + result.getErrcode() + " " + result.getErrmsg());
        }
    }

}
