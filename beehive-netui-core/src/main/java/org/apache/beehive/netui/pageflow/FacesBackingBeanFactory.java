/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Header:$
 */
package org.apache.beehive.netui.pageflow;

import org.apache.beehive.netui.core.factory.Factory;
import org.apache.beehive.netui.core.factory.FactoryUtils;
import org.apache.beehive.netui.pageflow.internal.InternalConstants;
import org.apache.beehive.netui.pageflow.internal.InternalUtils;
import org.apache.beehive.netui.pageflow.internal.AnnotationReader;
import org.apache.beehive.netui.pageflow.handler.Handlers;
import org.apache.beehive.netui.pageflow.handler.ReloadableClassHandler;
import org.apache.beehive.netui.util.internal.FileUtils;
import org.apache.beehive.netui.util.logging.Logger;
import org.apache.beehive.netui.util.config.ConfigUtil;
import org.apache.beehive.netui.util.config.bean.PageFlowFactoriesConfig;
import org.apache.beehive.netui.util.config.bean.PageFlowFactoryConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;


/**
 * Factory for creating "backing beans" for JavaServer Faces pages.
 */ 
public class FacesBackingBeanFactory
        extends Factory
        implements InternalConstants
{
    private static final Logger _log = Logger.getInstance( FacesBackingBeanFactory.class );
    
    private static final String CONTEXT_ATTR = InternalConstants.ATTR_PREFIX + "jsfBackingFactory";
    
    private ReloadableClassHandler _rch;
    
    protected void onCreate()
    {
    }
    
    protected FacesBackingBeanFactory()
    {
    }
    
    /**
     * Initialize an instance of this class in the ServletContext.  This is a framework-invoked method and should
     * normally be called directly.
     */
    public static void init( ServletContext servletContext )
    {
        PageFlowFactoriesConfig factoriesBean = ConfigUtil.getConfig().getPageFlowFactories();
        FacesBackingBeanFactory factory = null;
        
        if ( factoriesBean != null )
        {
            PageFlowFactoryConfig fcFactoryBean = factoriesBean.getFacesBackingBeanFactory();
            factory = ( FacesBackingBeanFactory ) FactoryUtils.getFactory( servletContext, fcFactoryBean, FacesBackingBeanFactory.class );
        }
        
        if ( factory == null ) factory = new FacesBackingBeanFactory();
        factory.reinit( servletContext );
        
        servletContext.setAttribute( CONTEXT_ATTR, factory );
    }
    
    /**
     * Called to reinitialize this instance, most importantly after it has been serialized/deserialized.
     * 
     * @param servletContext the current ServletContext.
     */ 
    protected void reinit( ServletContext servletContext )
    {
        super.reinit( servletContext );
        if ( _rch == null )  _rch = Handlers.get( servletContext ).getReloadableClassHandler();
    }
    
    /**
     * Get a FacesBackingBeanFactory.
     * 
     * @param servletContext the current ServletContext.
     * @return a FacesBackingBeanFactory for the given ServletContext.  It may or may not be a cached instance.
     */ 
    public static FacesBackingBeanFactory get( ServletContext servletContext )
    {
        FacesBackingBeanFactory factory = ( FacesBackingBeanFactory ) servletContext.getAttribute( CONTEXT_ATTR );
        assert factory != null
                : FacesBackingBeanFactory.class.getName() + " was not found in ServletContext attribute " + CONTEXT_ATTR;
        factory.reinit( servletContext );
        return factory;
    }
        
    /**
     * Get the "backing bean" associated with the JavaServer Faces page for a request.
     * 
     * @param requestContext a {@link RequestContext} object which contains the current request and response.
     */ 
    public FacesBackingBean getFacesBackingBeanForRequest( RequestContext requestContext )
    {
        HttpServletRequest request = requestContext.getHttpRequest();
        String uri = InternalUtils.getDecodedServletPath( request );
        assert uri.charAt( 0 ) == '/' : uri;
        String backingClassName = FileUtils.stripFileExtension( uri.substring( 1 ).replace( '/', '.' ) );
        FacesBackingBean currentBean = InternalUtils.getFacesBackingBean( request, getServletContext() );
        
        //
        // If there is no current backing bean, or if the current one doesn't match the desired classname, create one.
        //
        if ( currentBean == null || ! currentBean.getClass().getName().equals( backingClassName ) )
        {
            FacesBackingBean bean = null;
            
            if ( FileUtils.uriEndsWith( uri, FACES_EXTENSION ) || FileUtils.uriEndsWith( uri, JSF_EXTENSION ) )
            {
                bean = loadFacesBackingBean( requestContext, backingClassName );
                
                //
                // If we didn't create (or failed to create) a backing bean, and if this is a JSF request, then create
                // a default one.  This ensures that there will be a place for things like page inputs, that get stored
                // in the backing bean across postbacks to the same JSF.
                //
                if ( bean == null ) bean = new DefaultFacesBackingBean();
                
                //
                // If we created a backing bean, invoke its create callback, and tell it to store itself in the session.
                //
                if ( bean != null )
                {
                    HttpServletResponse response = requestContext.getHttpResponse();
                    
                    try
                    {
                        bean.create( request, response, getServletContext() );
                    }
                    catch ( Exception e )
                    {
                        _log.error( "Error while creating backing bean instance of " + backingClassName, e );
                    }
                    
                    bean.persistInSession( request, response );
                    return bean;
                }
            }
            
            //
            // We didn't create a backing bean.  If there's one in the session (an inappropriate one), remove it.
            //
            InternalUtils.removeCurrentFacesBackingBean( request, getServletContext() );
        }
        else if ( currentBean != null )
        {
            if ( _log.isDebugEnabled() )
            {
                _log.debug( "Using existing backing bean instance " + currentBean + " for request " +
                            request.getRequestURI() );
            }
            
            currentBean.reinitialize( request, requestContext.getHttpResponse(), getServletContext() );
        }
        
        return currentBean;
    }
    
    /**
     * Load a "backing bean" associated with the JavaServer Faces page for a request.
     * @param requestContext a {@link RequestContext} object which contains the current request and response.
     * @param backingClassName the name of the backing bean class.
     * @return an initialized FacesBackingBean, or <code>null</code> if an error occurred.
     */ 
    protected FacesBackingBean loadFacesBackingBean( RequestContext requestContext, String backingClassName )
    {
        try
        {
            Class backingClass = null;
            
            try
            {
                backingClass = getFacesBackingBeanClass( backingClassName );
            }
            catch ( ClassNotFoundException e )
            {
                // ignore -- we deal with this and log this immediately below.  getFacesBackingBeanClass() by default
                // does not throw this exception, but a derived version might.
            }
                
            if ( backingClass == null )
            {
                if ( _log.isTraceEnabled() )
                {
                    _log.trace( "No backing bean class " + backingClassName + " found for request "
                                + requestContext.getHttpRequest().getRequestURI() );
                }
            }
            else
            {
                AnnotationReader annReader = AnnotationReader.getAnnotationReader( backingClass, getServletContext() );
                    
                if ( annReader.getJpfAnnotation( backingClass, "FacesBacking" ) != null )
                {
                    if ( _log.isDebugEnabled() )
                    {
                        _log.debug( "Found backing class " + backingClassName + " for request "
                                    + requestContext.getHttpRequest().getRequestURI() + "; creating a new instance." );
                    }
                        
                    return getFacesBackingBeanInstance( backingClass );
                }
                else
                {
                    if ( _log.isDebugEnabled() )
                    {
                        _log.debug( "Found matching backing class " + backingClassName + " for request " 
                                    + requestContext.getHttpRequest().getRequestURI() + ", but it does not have the "
                                    + ANNOTATION_QUALIFIER + "FacesBacking" + " annotation." );
                    }
                }
            }
        }
        catch ( InstantiationException e )
        {
            _log.error( "Could not create backing bean instance of " + backingClassName, e );
        }
        catch ( IllegalAccessException e )
        {
            _log.error( "Could not create backing bean instance of " + backingClassName, e );
        }
        
        return null;
    }
    
    private static class DefaultFacesBackingBean
        extends FacesBackingBean
    {
    }
    
    /**
     * Get a FacesBackingBean class.  By default, this loads the class using the thread context class loader.
     * 
     * @param className the name of the {@link FacesBackingBean} class to load.
     * @return the loaded {@link FacesBackingBean} class.
     * @throws ClassNotFoundException if the requested class could not be found.
     */ 
    public Class getFacesBackingBeanClass( String className )
        throws ClassNotFoundException
    {
        return _rch.loadCachedClass( className );
    }
    
    /**
     * Get a FacesBackingBean instance, given a FacesBackingBean class.
     * 
     * @param beanClass the Class, which must be assignable to {@link FacesBackingBean}.
     * @return a new FacesBackingBean instance.
     */ 
    public FacesBackingBean getFacesBackingBeanInstance( Class beanClass )
        throws InstantiationException, IllegalAccessException
    {
        assert FacesBackingBean.class.isAssignableFrom( beanClass )
                : "Class " + beanClass.getName() + " does not extend " + FacesBackingBean.class.getName();
        return ( FacesBackingBean ) beanClass.newInstance();
    }
}
