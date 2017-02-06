package org.zenframework.easyservices.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.commons.debug.TimeChecker;
import org.zenframework.easyservices.ClientException;
import org.zenframework.easyservices.RequestMapper;
import org.zenframework.easyservices.ServiceLocator;
import org.zenframework.easyservices.descriptor.ClassDescriptorFactory;
import org.zenframework.easyservices.descriptor.MethodDescriptor;
import org.zenframework.easyservices.descriptor.ClassDescriptor;
import org.zenframework.easyservices.descriptor.ValueDescriptor;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.SerializerFactory;

public class ServiceInvocationHandler implements InvocationHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceInvocationHandler.class);

    private final ServiceLocator serviceLocator;
    private final Class<?> serviceClass;
    private final ClassDescriptorFactory serviceDescriptorFactory;
    private final SerializerFactory serializerFactory;
    private final RequestMapper requestMapper;

    public ServiceInvocationHandler(ServiceLocator serviceLocator, Class<?> serviceClass, ClassDescriptorFactory serviceDescriptorFactory,
            SerializerFactory serializerFactory, RequestMapper requestMapper) {
        this.serviceLocator = serviceLocator;
        this.serviceClass = serviceClass;
        this.serviceDescriptorFactory = serviceDescriptorFactory;
        this.serializerFactory = serializerFactory;
        this.requestMapper = requestMapper;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // Check service locator is absolute
        if (serviceLocator.isRelative())
            throw new ClientException("Service locator '" + serviceLocator + "' must be absolute");

        Serializer serializer = serializerFactory.getSerializer();
        ClassDescriptor classDescriptor = serviceDescriptorFactory.getClassDescriptor(serviceClass);
        MethodDescriptor methodDescriptor = classDescriptor != null ? classDescriptor.getMethodDescriptor(method)
                : new MethodDescriptor(args.length);
        ValueDescriptor[] argDescriptors = methodDescriptor.getParameterDescriptors();

        // Find and replace proxy objects with references
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                ValueDescriptor argDescriptor = argDescriptors[i];
                if (argDescriptor != null && argDescriptor.isReference()) {
                    ServiceInvocationHandler handler = (ServiceInvocationHandler) Proxy.getInvocationHandler(args[i]);
                    args[i] = handler.getServiceLocator();
                }
            }
        }

        // Serialize arguments
        String serializedArgs = serializer.serialize(args);

        TimeChecker time = null;
        if (LOG.isDebugEnabled())
            time = new TimeChecker("CALL " + serviceLocator.getServiceUrl() + ' ' + method.getName() + (args != null ? serializedArgs : "[]"), LOG);

        try {

            // Call service
            URL url = requestMapper.getRequestURI(serviceLocator.getServiceUrl(), method.getName(), serializedArgs).toURL();
            String data = readData(url.openStream());
            if (time != null)
                time.printDifference(data);

            ValueDescriptor returnDescriptor = methodDescriptor.getReturnDescriptor();
            // If result is reference, replace with proxy object
            if (returnDescriptor != null && returnDescriptor.isReference()) {
                ServiceLocator locator = (ServiceLocator) serializer.deserialize(data, ServiceLocator.class, returnDescriptor);
                if (locator.isRelative())
                    locator = ServiceLocator.qualified(serviceLocator.getBaseUrl(), locator.getServiceName());
                return getProxy(locator, method.getReturnType());
            }
            // Else return deserialized result
            return serializer.deserialize(data, method.getReturnType(), returnDescriptor);

        } catch (Throwable e) {
            if (time != null)
                time.printDifference(e);
            throw e;
        }

    }

    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    private static String readData(InputStream in) throws IOException {
        StringBuilder str = new StringBuilder(8192);
        char buf[] = new char[8192];
        InputStreamReader reader = new InputStreamReader(in);
        try {
            for (int n = reader.read(buf); n >= 0; n = reader.read(buf))
                str.append(new String(buf, 0, n));
            return str.toString();
        } finally {
            reader.close();
        }
    }

    private Object getProxy(ServiceLocator serviceLocator, Class<?> serviceClass) throws IllegalArgumentException, ClientException {
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { serviceClass },
                new ServiceInvocationHandler(serviceLocator, serviceClass, serviceDescriptorFactory, serializerFactory, requestMapper));
    }

}
