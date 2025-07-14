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

import java.net.InetSocketAddress;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HostPortHttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.http2.api.Session;
import org.eclipse.jetty.http2.api.Stream;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.jetty.util.FuturePromise;
import org.eclipse.jetty.util.Promise;
import org.junit.jupiter.api.Test;


@SuppressWarnings("serial")
public class LowLevelHttp2ClientTest {


    
    @Test
    public void lowLevelApiTest() throws Exception {
        
        // start the test server
        class MyServlet extends HttpServlet { 
            
            @Override
            protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                resp.getWriter().write("<html> <header> ...");
            }
        };
        
        WebServer server = WebServer.servlet(new MyServlet())
                                    .start();

        

        
        // create a low-level jetty HTTP/2 client
        HTTP2Client lowLevelClient = new HTTP2Client();
        lowLevelClient.start();
        


        
        // create a new session which will open a (multiplexed) connection to the server  
        FuturePromise<Session> sessionFuture = new FuturePromise<>();
        lowLevelClient.connect(new InetSocketAddress("localhost", server.getLocalport()), new Session.Listener.Adapter(), sessionFuture);
        Session session = sessionFuture.get();
        
        // create the header frame 
        MetaData.Request metaData = new MetaData.Request("GET", HttpURI.from("http://localhost:" + server.getLocalport() + "/"), HttpVersion.HTTP_2, HttpFields.build());
        HeadersFrame frame = new HeadersFrame(1, metaData, null, true);

        // ... and perform the http transaction
        PrintingFramesHandler framesHandler = new PrintingFramesHandler();
        session.newStream(frame, new Promise.Adapter<Stream>(), framesHandler);

        
        
        
        // wait until response is received (PrintingFramesHandler will write the response frame to console) 
        framesHandler.getCompletedFuture().get();
        
        
        // shut down the client and server
        lowLevelClient.stop();
        server.stop();
    }
}
