package com.alvexcore.repo.tools;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.*;

class ClasspathURLConnection extends URLConnection {
    public ClasspathURLConnection(URL url) {
        super(url);
    }

    @Override
    public void connect() throws IOException {
        //
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return getClass().getResourceAsStream(url.getFile());
    }
}

class ClasspathURLStreamHandler extends URLStreamHandler {
    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return new ClasspathURLConnection(url);
    }
}

public class ClasspathURLStreamHandlerFactory implements URLStreamHandlerFactory {

    public static final String PROTOCOL = "classpath";
    private final URLStreamHandlerFactory factory;

    private ClasspathURLStreamHandler handler = new ClasspathURLStreamHandler();

    public ClasspathURLStreamHandlerFactory(URLStreamHandlerFactory factory)
    {
        this.factory = factory;
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String s) {
        if (!PROTOCOL.equals(s))
            return factory == null ? null : factory.createURLStreamHandler(s);

        return handler;
    }

    public static void inject() throws NoSuchFieldException, IllegalAccessException {
        URL url = null;
        try {
            url = new URL("file:///42");
        } catch (MalformedURLException e) {
            //
        }

        Field field = URL.class.getDeclaredField("factory");
        boolean isAccessible = field.isAccessible();
        field.setAccessible(true);

        field.set(url, new ClasspathURLStreamHandlerFactory((URLStreamHandlerFactory) field.get(url)));

        field.setAccessible(isAccessible);
    }
}
