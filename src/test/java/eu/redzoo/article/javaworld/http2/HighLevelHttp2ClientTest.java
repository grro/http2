/*
 * Copyright (c) 2015 Gregor Roth
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.redzoo.article.javaworld.http2;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


@SuppressWarnings("serial")
public class HighLevelHttp2ClientTest {

    private WebServer server;
    
    
    @Before
    public void before() {

        class MyServlet extends HttpServlet { 
            
            @Override
            protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                resp.getOutputStream().write("...my body data...".getBytes());
            }
        };
        
        server = WebServer.servlet(new MyServlet())
                          .start();
System.out.println(server.getBasepath());        
    }

    
    @After
    public void after() {
        server.stop();
    }


    
    
    @Test
    public void lowLevelApiTest() throws Exception {
        
        HTTP2Client lowLevelClient = new HTTP2Client();
        lowLevelClient.start();
        
        HttpClient client = new HttpClient(new HttpClientTransportOverHTTP2(lowLevelClient), null);
        client.start();

        ContentResponse response = client.GET("http://localhost:" + server.getLocalport());
        
        System.out.println(response.getVersion() + " " + response.getStatus() + " ");
        Assert.assertEquals("...my body data...", new String(response.getContent()));


        client.stop();
        server.stop();
    }
}
