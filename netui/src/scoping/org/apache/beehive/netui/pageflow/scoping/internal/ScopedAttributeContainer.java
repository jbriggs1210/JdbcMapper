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
package org.apache.beehive.netui.pageflow.scoping.internal;

import org.apache.beehive.netui.pageflow.scoping.ScopedServletUtils;


/**
 * Base class for wrapper objects that keep their own scoped attributes, ignoring the attributes
 * of the wrapped objects.
 */
public class ScopedAttributeContainer extends AttributeContainer
{
    private Object _scopeKey;

    public ScopedAttributeContainer( Object scopeKey )
    {
        _scopeKey = scopeKey;
    }

    public final String getScopedName( String baseName )
    {
        return ScopedServletUtils.getScopedName( baseName, _scopeKey );
    }

    public boolean isInScope( String keyName )
    {
        return isInScope( keyName, _scopeKey );
    }

    public static boolean isInScope( String keyName, Object scopeKey )
    {
        return keyName.startsWith( scopeKey.toString() );
    }

    public String removeScope( String keyName )
    {
        return removeScope( keyName, _scopeKey );
    }
    
    public static String removeScope( String keyName, Object scopeKey )
    {
        assert keyName.startsWith( scopeKey.toString() ) : keyName;
        return keyName.substring( scopeKey.toString().length() );
    }
    
    public final Object getScopeKey()
    {
        return _scopeKey;
    }

    public void renameScope( Object newScopeKey )
    {
        _scopeKey = newScopeKey;
    }
}
