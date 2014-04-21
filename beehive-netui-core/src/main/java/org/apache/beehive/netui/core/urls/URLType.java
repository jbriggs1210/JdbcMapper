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
package org.apache.beehive.netui.core.urls;

/**
 * Passed to {@link URLRewriter#rewriteURL} for normal (non-resource) and resource, non-secure and
 * secure URLs.
 */
public class URLType
{
    protected static final int INT_ACTION = 0;
    protected static final int INT_RESOURCE = 1;
    
    public static final URLType ACTION = new URLType( INT_ACTION );
    public static final URLType RESOURCE = new URLType( INT_RESOURCE );
    
    private int _val;
    
    private URLType( int val )
    {
        _val = val;
    }
    
    public String toString()
    {
        switch ( _val )
        {
            case INT_ACTION: return "action";
            case INT_RESOURCE: return "resource";
        }
        
        assert false : _val;
        return "<unknown URLType>";
    }
    
    public boolean equals( Object o )
    {
        if ( o == null ) return false;
        if ( o == this ) return true;
        if ( ! ( o instanceof URLType ) ) return false;
        return ( ( URLType ) o )._val == _val;
    }
    
    public int hashCode()
    {
        return _val;
    }
}