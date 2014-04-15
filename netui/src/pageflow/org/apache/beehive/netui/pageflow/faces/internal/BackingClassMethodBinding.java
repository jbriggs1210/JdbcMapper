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
package org.apache.beehive.netui.pageflow.faces.internal;

import org.apache.beehive.netui.pageflow.internal.InternalUtils;
import org.apache.beehive.netui.pageflow.internal.AnnotationReader;
import org.apache.beehive.netui.pageflow.internal.annotationreader.ProcessedAnnotation;
import org.apache.beehive.netui.util.logging.Logger;
import org.apache.beehive.netui.util.internal.cache.FieldCache;
import org.apache.beehive.netui.util.internal.cache.MethodCache;

import javax.faces.el.MethodBinding;
import javax.faces.el.MethodNotFoundException;
import javax.faces.el.EvaluationException;
import javax.faces.context.FacesContext;
import javax.faces.context.ExternalContext;
import javax.faces.component.StateHolder;
import javax.faces.component.UIComponentBase;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

/**
 * Internal class used in JSF/Page Flow integration.  This exists to cause form beans to be submitted to Page Flow
 * actions raised from JSF command event handlers.
 * @see org.apache.beehive.netui.pageflow.faces.PageFlowApplicationFactory
 */ 
public class BackingClassMethodBinding
        extends MethodBinding
        implements StateHolder
{
    private static final Logger _log = Logger.getInstance( BackingClassMethodBinding.class );
    private static final FieldCache _fieldCache = new FieldCache();
    private static final MethodCache _methodCache = new MethodCache();
    
    private String _methodName;
    private Class[] _params;
    private MethodBinding _delegate;
    private boolean _transient = false;
    
    public BackingClassMethodBinding()
    {
    }
    
    public BackingClassMethodBinding( String methodName, Class[] params, MethodBinding delegate )
    {
        _methodName = methodName;
        _params = params;
        _delegate = delegate;
    }

    public Class getType( FacesContext context )
        throws MethodNotFoundException
    {
        return _delegate.getType( context );
    }

    public String getExpressionString() {
        return _delegate.getExpressionString();
    }

    /**
     * Before returning the result from the base MethodBinding, see if the bound method is annotated with
     * Jpf.CommandHandler.  If it is, look through the "raiseActions" annotation array for a form bean member variable
     * associated with the action being raised.  If one is found, set it in the request so it gets passed to the action.
     */ 
    public Object invoke( FacesContext context, Object params[] )
        throws EvaluationException, MethodNotFoundException
    {
        Object result = _delegate.invoke( context, params );
        
        if ( result instanceof String )
        {
            String action = ( String ) result;
            ExternalContext externalContext = context.getExternalContext();
            Object request = externalContext.getRequest();
            Object servletContextObject = externalContext.getContext();
            assert request != null;

            if (request instanceof HttpServletRequest && servletContextObject instanceof ServletContext) {
                ServletContext servletContext = (ServletContext) servletContextObject;
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                Object backingBean = InternalUtils.getFacesBackingBean(httpRequest, servletContext);

                if (backingBean != null) {
                    Class backingClass = backingBean.getClass();
                    Method method = _methodCache.getMethod(backingClass, _methodName, _params);

                    if (method == null) {
                        throw new MethodNotFoundException(_methodName);
                    }
                    AnnotationReader annReader = AnnotationReader.getAnnotationReader(backingClass, servletContext);
                    ProcessedAnnotation ann = annReader.getJpfAnnotation(method, "CommandHandler");

                    if (ann != null) {
                        ProcessedAnnotation[] raiseActions =
                                AnnotationReader.getAnnotationArrayAttribute(ann, "raiseActions");

                        if (raiseActions != null) {
                            setOutputFormBeans(raiseActions, backingClass, backingBean, action, httpRequest);
                        }
                    }
                }
            }
        }
    
        return result;
    }
    
    private static void setOutputFormBeans( ProcessedAnnotation[] raiseActions, Class backingClass, Object backingBean,
                                            String action, HttpServletRequest httpRequest )
    {
        for ( int i = 0; i < raiseActions.length; i++ )
        {
            ProcessedAnnotation raiseAction = raiseActions[i];
            String actionAttr = AnnotationReader.getStringAttribute( raiseAction, "action" );
                                
            if ( actionAttr.equals( action ) )
            {
                String formBeanMember =
                        AnnotationReader.getStringAttribute( raiseAction, "outputFormBean" );
                                    
                if ( formBeanMember != null && formBeanMember.length() > 0 )
                {
                    try
                    {
                        Field field = _fieldCache.getDeclaredField( backingClass, formBeanMember );
                        if ( field == null )
                        {
                            _log.error( "Could not find field " + formBeanMember + " specified as the outputFormBean "
                                        + "for action " + action + " raised by " + backingClass.getName() );
                            return;
                        }
                        Object value = field.get( backingBean );
                        InternalUtils.setForwardedFormBean( httpRequest, InternalUtils.wrapFormBean( value ) );
                    }
                    catch ( IllegalAccessException e )
                    {
                        _log.error( "Could not access field " + formBeanMember + " specified as the outputFormBean "
                                    + "for action " + action + " raised by " + backingClass.getName(), e );
                    }
                }
            }
        }
    }
    
    public Object saveState( FacesContext context )
    {
        return new Object[]{ _methodName, _params, UIComponentBase.saveAttachedState( context, _delegate ) };
    }
    
    public void restoreState( FacesContext context, Object state )
    {
        Object[] values = ( Object[] ) state;
        _methodName = ( String ) values[0];
        _params = ( Class[] ) values[1];
        _delegate = ( MethodBinding ) UIComponentBase.restoreAttachedState( context, values[2] );
    }
    
    public boolean isTransient()
    {
        return _transient;
    }
    
    public void setTransient( boolean newTransientValue )
    {
        _transient = newTransientValue;
    }
}
