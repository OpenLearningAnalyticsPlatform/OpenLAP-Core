package org.rwthaachen.olap.analyticsmethods.service;

import core.AnalyticsMethod;
import org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodUploadValidationException;
import org.xeustechnologies.jcl.JarClassLoader;
import org.xeustechnologies.jcl.JclObjectFactory;
import org.xeustechnologies.jcl.exception.JclException;
import org.xeustechnologies.jcl.proxy.CglibProxyProvider;
import org.xeustechnologies.jcl.proxy.ProxyProviderFactory;

/**
 * Created by lechip on 15/11/15.
 */
public class AnalyticsMethodsClassPathLoader {

    private JarClassLoader jcl;
    private JclObjectFactory factory;

    public AnalyticsMethodsClassPathLoader(String analyticsMethodsJarsFolder) {

        //JCL object for loading jars
        jcl = new JarClassLoader();
        //Loading classes from different sources
        jcl.add(analyticsMethodsJarsFolder);

        // Set default to cglib (from version 2.2.1)
        ProxyProviderFactory.setDefaultProxyProvider( new CglibProxyProvider() );
        factory = JclObjectFactory.getInstance(true);
    }

    public AnalyticsMethod loadClass(String implementingClass) throws AnalyticsMethodUploadValidationException {
        //Create object of loaded class
        AnalyticsMethod abstractMethod;
        try{
            abstractMethod = (AnalyticsMethod) factory.create(jcl, implementingClass);
            return abstractMethod;
        }
        catch (JclException e)
        {
            e.printStackTrace();
            throw new AnalyticsMethodUploadValidationException("The class " + implementingClass +
                    " was not found or does not implement the framework.");
        }
        catch (java.lang.NoSuchMethodError error)
        {
            error.printStackTrace();
            throw new AnalyticsMethodUploadValidationException("The class " + implementingClass +
                    " does not have an empty constructor.");
        }
    }
}
