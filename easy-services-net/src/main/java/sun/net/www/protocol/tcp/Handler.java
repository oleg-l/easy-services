package sun.net.www.protocol.tcp;

import java.io.IOException;
import java.net.URL;

import org.zenframework.easyservices.net.TcpURLConnection;

public class Handler extends java.net.URLStreamHandler {

    @Override
    protected java.net.URLConnection openConnection(URL u) throws IOException {
        return new TcpURLConnection(u);
    }

}
