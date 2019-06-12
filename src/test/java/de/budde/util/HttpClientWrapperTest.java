package de.budde.util;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;

import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.HttpParams;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HttpClientWrapperTest {
    @Mock
    HttpGet get;
    @Mock
    HttpParams params;

    @Test
    public void testMessage() {
        StatusLine sl200 = new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK");
        StatusLine sl500 = new BasicStatusLine(HttpVersion.HTTP_1_1, 500, "Server error");

        Assert.assertEquals("no response from server", HttpClientWrapper.mkMessage(null));
        Assert.assertEquals(null, HttpClientWrapper.mkMessage(sl200));
        Assert.assertEquals("status code from server: 500", HttpClientWrapper.mkMessage(sl500));
    }

    @Test
    public void testHttpEntity() throws Exception {
        String entityString = "123qay(,=üäö";
        StringEntity se = new StringEntity(entityString, Charset.forName("UTF-8"));
        Assert.assertEquals(entityString, HttpClientWrapper.httpEntityToString(se));
    }

    /**
     * <b>example of a problematic test</b>. Test the method addAuthHeaderOpt: if credentials are presented, we want to make sure, that a matching header is
     * added to the
     * request. This requirement makes sense.<br>
     * <b>But note:</b> addAuthHeaderOpt uses<br>
     * - new BasicScheme().authenticate(creds, req, null), which gets the request,<br>
     * - calls req.getParams(), which has to be mocked (b.t.w. the method is deprecated, but used in BasicScheme),<br>
     * - the mocked object calls params.getParameter(...), which has to be mocked (deprecated, too; here it is safe, to return null)<br>
     * <i>This is problematic: you need a lot of knowledge about implementation details of a framework we depend on to setup the test. Upgrading the framework
     * may break the test</i>
     *
     * @throws Exception
     */
    @Test
    public void testAuth() throws Exception {
        // setup (including mocked objects)
        when(this.get.getParams()).thenReturn(this.params);
        when(this.params.getParameter(Matchers.anyString())).thenReturn(null);
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials("pid", "minscha");
        // test
        HttpClientWrapper.addAuthHeaderOpt(creds, this.get);
        // check: success if addHeader is called at least once, otherwise fail
        verify(this.get, times(1)).addHeader(Matchers.any());
    }
}
