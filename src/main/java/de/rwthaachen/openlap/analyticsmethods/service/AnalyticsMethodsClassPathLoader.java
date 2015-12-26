package de.rwthaachen.openlap.analyticsmethods.service;

import core.AnalyticsMethod;
import de.rwthaachen.openlap.analyticsmethods.exceptions.AnalyticsMethodLoaderException;
import org.xeustechnologies.jcl.JarClassLoader;
import org.xeustechnologies.jcl.JclObjectFactory;
import org.xeustechnologies.jcl.exception.JclException;
import org.xeustechnologies.jcl.proxy.CglibProxyProvider;
import org.xeustechnologies.jcl.proxy.ProxyProviderFactory;

/**
 * A custom Java Class Path Loader that is responsible of loading JARs dynamically to the active ClassPath.
 * It uses the JCL developed by Kamran Zafar(http://kamranzafar.org/)
 * and available at https://github.com/kamranzafar/JCL/
 */
public class AnalyticsMethodsClassPathLoader {

    private JarClassLoader jcl;
    private JclObjectFactory factory;

    /**
     * Standard constructor that prepares this classpath loader to use the given JAR location to load classes from.
     * @param analyticsMethodsJarsFolder JAR File where the desired loading classes are located.
     */
    public AnalyticsMethodsClassPathLoader(String analyticsMethodsJarsFolder) {

        //JCL object for loading jars
        jcl = new JarClassLoader();
        //Loading classes from different sources
        jcl.add(analyticsMethodsJarsFolder);

        // Set ClassPathLoader priorities to prevent collisions when loading
        jcl.getParentLoader().setOrder(1);
        jcl.getLocalLoader().setOrder(2);
        jcl.getSystemLoader().setOrder(3);
        jcl.getThreadLoader().setOrder(4);
        jcl.getCurrentLoader().setOrder(5);

        // Set default to cglib (from version 2.2.1)
        ProxyProviderFactory.setDefaultProxyProvider( new CglibProxyProvider() );
        factory = JclObjectFactory.getInstance(true);
    }

    /**
     * Loads a class from a JAR of the folder specified in the constructor to the current ClassPath.
     * @param implementingClass The name of class to be loaded to the current ClassPath
     * @return An AnalyticsMethod that is loaded from the JAR.
     * @throws AnalyticsMethodLoaderException If the specified JAR file does not contain the requested class.
     */
    public AnalyticsMethod loadClass(String implementingClass) throws AnalyticsMethodLoaderException {
        //Create object of loaded class
        AnalyticsMethod abstractMethod;
        try{
            abstractMethod = (AnalyticsMethod) factory.create(jcl, implementingClass);
            return abstractMethod;
        }
        catch (JclException e)
        {
            e.printStackTrace();
            throw new AnalyticsMethodLoaderException("The class " + implementingClass +
                    " was not found or does not implement the framework.");
        }
        catch (java.lang.NoSuchMethodError error)
        {
            error.printStackTrace();
            throw new AnalyticsMethodLoaderException("The class " + implementingClass +
                    " does not have an empty constructor.");
        }
    }
}
