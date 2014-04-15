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
package org.apache.beehive.netui.compiler.typesystem.impl.env;

import org.apache.beehive.netui.compiler.typesystem.declaration.AnnotationTypeDeclaration;
import org.apache.beehive.netui.compiler.typesystem.declaration.Declaration;
import org.apache.beehive.netui.compiler.typesystem.declaration.TypeDeclaration;
import org.apache.beehive.netui.compiler.typesystem.env.CoreAnnotationProcessorEnv;
import org.apache.beehive.netui.compiler.typesystem.env.Filer;
import org.apache.beehive.netui.compiler.typesystem.env.Messager;
import org.apache.beehive.netui.compiler.typesystem.impl.DelegatingImpl;
import org.apache.beehive.netui.compiler.typesystem.impl.WrapperFactory;
import org.apache.beehive.netui.compiler.typesystem.impl.declaration.AnnotationTypeDeclarationImpl;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

public class CoreAnnotationProcessorEnvImpl
        extends DelegatingImpl
        implements CoreAnnotationProcessorEnv
{
    private TypeDeclaration[] _specifiedTypeDeclarations;
    private Map _attributes;
    
    protected CoreAnnotationProcessorEnvImpl( com.sun.mirror.apt.AnnotationProcessorEnvironment delegate )
    {
        super( delegate );
    }
    
    public static CoreAnnotationProcessorEnv get( com.sun.mirror.apt.AnnotationProcessorEnvironment delegate )
    {
        return delegate != null ? new CoreAnnotationProcessorEnvImpl( delegate ) : null;
    }

    public Map getOptions()
    {
        return getDelegate().getOptions();
    }

    public Messager getMessager()
    {
        return MessagerImpl.get( getDelegate().getMessager() );
    }

    public Filer getFiler()
    {
        return FilerImpl.get( getDelegate().getFiler() );
    }

    public TypeDeclaration[] getSpecifiedTypeDeclarations()
    {
        if ( _specifiedTypeDeclarations == null )
        {
            Collection<com.sun.mirror.declaration.TypeDeclaration> delegateCollection = getDelegate().getSpecifiedTypeDeclarations();
            TypeDeclaration[] array = new TypeDeclaration[delegateCollection.size()];
            int j = 0;
            for ( com.sun.mirror.declaration.TypeDeclaration i : delegateCollection )
            {
                array[j++] = WrapperFactory.get().getTypeDeclaration( i );
            }
            _specifiedTypeDeclarations = array;
        }

        return _specifiedTypeDeclarations;
    }

    public TypeDeclaration getTypeDeclaration( String s )
    {
        return WrapperFactory.get().getTypeDeclaration( getDelegate().getTypeDeclaration( s ) );
    }

    public Declaration[] getDeclarationsAnnotatedWith( AnnotationTypeDeclaration decl )
    {
        assert decl instanceof AnnotationTypeDeclarationImpl : decl.getClass().getName();
        Collection< com.sun.mirror.declaration.Declaration > delegateCollection =
                getDelegate().getDeclarationsAnnotatedWith( ( ( AnnotationTypeDeclarationImpl ) decl ).getDelegate() );
        Declaration[] array = new Declaration[ delegateCollection.size() ];
        int j = 0;
        for ( com.sun.mirror.declaration.Declaration i : delegateCollection )
        {
            array[ j++ ] = WrapperFactory.get().getDeclaration( i );
        }
        return array;
    }

    protected com.sun.mirror.apt.AnnotationProcessorEnvironment getDelegate()
    {
        return ( com.sun.mirror.apt.AnnotationProcessorEnvironment ) super.getDelegate();
    }

    public void setAttribute( String propertyName, Object value )
    {
        if ( _attributes == null ) _attributes = new HashMap();
        _attributes.put( propertyName, value );
    }

    public Object getAttribute( String propertyName )
    {
        return _attributes != null ? _attributes.get( propertyName ) : null;
    }
}
