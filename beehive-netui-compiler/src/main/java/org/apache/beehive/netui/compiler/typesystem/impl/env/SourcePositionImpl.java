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

import org.apache.beehive.netui.compiler.typesystem.util.SourcePosition;
import org.apache.beehive.netui.compiler.typesystem.impl.DelegatingImpl;

import java.io.File;

public class SourcePositionImpl
        extends DelegatingImpl
        implements SourcePosition
{
    protected SourcePositionImpl( com.sun.mirror.util.SourcePosition delegate )
    {
        super( delegate );
    }
    
    public static SourcePosition get( com.sun.mirror.util.SourcePosition delegate )
    {
        return delegate != null ? new SourcePositionImpl( delegate ) : null;
    }

    public File file()
    {
        return getDelegate().file();
    }

    public int line()
    {
        return getDelegate().line();
    }

    public int column()
    {
        return getDelegate().column();
    }
    
    protected com.sun.mirror.util.SourcePosition getDelegate()
    {
        return ( com.sun.mirror.util.SourcePosition ) super.getDelegate();
    }
}
