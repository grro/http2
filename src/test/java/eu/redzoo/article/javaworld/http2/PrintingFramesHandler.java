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

import java.util.concurrent.CompletableFuture;

import org.eclipse.jetty.http2.api.Stream;
import org.eclipse.jetty.http2.api.Stream.Listener;
import org.eclipse.jetty.http2.frames.DataFrame;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.jetty.http2.frames.PushPromiseFrame;
import org.eclipse.jetty.util.Callback;




public class PrintingFramesHandler extends Stream.Listener.Adapter {

    private final CompletableFuture<Void> completedFuture = new CompletableFuture<>();
    
    public CompletableFuture<Void> getCompletedFuture() {
        return completedFuture;
    }
    
    
    @Override
    public Listener onPush(Stream stream, PushPromiseFrame frame) {
        System.out.println("[" + stream.getId() + "] PUSH_PROMISE " + frame.getMetaData().toString());
        return new PrintingFramesHandler(); 
    }
    
    
    @Override
    public void onHeaders(Stream stream, HeadersFrame frame) {
        System.out.println("[" + stream.getId() + "] HEADERS " + frame.getMetaData().toString());
        frame.getMetaData().getFields().forEach(field -> System.out.println("[" + stream.getId() + "]     " + field.getName() + ": " + field.getValue()));
    }
    
    @Override
    public void onData(Stream stream, DataFrame frame, Callback callback) {
        byte[] bytes = new byte[frame.getData().remaining()];
        frame.getData().get(bytes);
        System.out.println("[" + stream.getId() + "] DATA " + new String(bytes));
        callback.succeeded();
        
        if (frame.isEndStream()) {
            completedFuture.complete(null);
        }
    }
}
