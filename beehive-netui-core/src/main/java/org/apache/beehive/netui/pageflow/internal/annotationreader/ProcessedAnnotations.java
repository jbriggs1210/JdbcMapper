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
package org.apache.beehive.netui.pageflow.internal.annotationreader;

import java.util.Map;
import java.util.Collections;
import java.io.Serializable;

/**
 *
 */
public class ProcessedAnnotations
        implements Serializable
{

    private String _typeName;
    private Map _annotatedElements;

    public ProcessedAnnotations()
    {
    }

    public ProcessedAnnotations( String typeName, Map annotatedElements )
    {
        _typeName = typeName;
        _annotatedElements = annotatedElements;
    }

    public String getTypeName()
    {
        return _typeName;
    }

    public void setTypeName( String typeName )
    {
        _typeName = typeName;
    }

    public Map getAnnotatedElements()
    {
        return _annotatedElements != null ? _annotatedElements : Collections.EMPTY_MAP;
    }

    public void setAnnotatedElements( Map annotatedElements )
    {
        _annotatedElements = annotatedElements;
    }
}
