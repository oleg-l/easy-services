package org.zenframework.easyservices.jndi.impl;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

public class InitialContextFactoryImpl implements InitialContextFactory {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
        Context context = new TreeContext();
        context.getEnvironment().putAll((Hashtable) environment);
        return context;
    }

}
