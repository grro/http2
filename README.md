
#Examples of **What HTTP/2 means to Java developers**
   * [Low-level HTTP/2 client example](src/test/java/eu/redzoo/article/javaworld/http2/LowLevelHttp2ClientTest.java)
   * [High-level HTTP/2 client example](src/test/java/eu/redzoo/article/javaworld/http2/HighLevelHttp2ClientTest.java)
   * [HTTP/2 push example](src/test/java/eu/redzoo/article/javaworld/http2/Http2PushTest.java)
   
Please consider that the examples use direct negotiation as well as an embedded jetty server. By modifying the examples to call http2-supporting pages you have to configure SSL. Please refer https://github.com/http2/http2-spec/wiki/Implementations 	   