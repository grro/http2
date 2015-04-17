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
import java.util.function.BiConsumer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;



public class WebServer {

    private final String path;
    private final ServerConnector connector;
    private final Server server; 
    
    
    public static WebServerBuilder servlet(HttpServlet servlet) {
        return new WebServerBuilder(servlet, "/", 0);
    }
    
     

    public static WebServerBuilder handler(BiConsumer<HttpServletRequest, HttpServletResponse> handler) {

        HttpServlet servlet = new HttpServlet() {
            private static final long serialVersionUID = -7741340028518626628L;

            @Override
            protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                handler.accept(req, resp);
            }
        };
        
        return servlet(servlet);
    }
    
    
    public WebServer(String path, HttpServlet servlet, int port) {
        this.path = path; 
        
        QueuedThreadPool serverExecutor = new QueuedThreadPool();
        serverExecutor.setName("server");

        server = new Server(serverExecutor);
        connector = new ServerConnector(server, 1,1, new HTTP2ServerConnectionFactory(new HttpConfiguration()));
        connector.setPort(port);
        server.addConnector(connector);

        ServletContextHandler context = new ServletContextHandler(server, "/", true, false);
        context.addServlet(new ServletHolder(servlet), path);
    }
    
    void start() {
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public int getLocalport() {
        return connector.getLocalPort(); 
    }
    
    
    public String getBasepath() {
        return "http://localhost:" + getLocalport() + path;
    }
    
    

    public static class WebServerBuilder {
        private final HttpServlet servlet; 
        private final String path;
        private final int port;
        
        private WebServerBuilder(HttpServlet servlet, String path, int port) {
            this.servlet = servlet;
            this.path = path;
            this.port = port;
        }
        
        public WebServerBuilder path(String path) {
            return new WebServerBuilder(this.servlet, path, this.port);
        }

        public WebServerBuilder port(int port) {
            return new WebServerBuilder(this.servlet, this.path, port);
        }

        
        public WebServer start() {
            WebServer webServer = new WebServer(path, servlet, port);
            webServer.start();
            return webServer;
        }
    }
}
