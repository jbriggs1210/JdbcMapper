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

/**
 * Exception that occurs when the user tries to execute an action that does not exist on the page flow.
 */ 
public class ActionNotFoundException extends PageFlowException
{
    private Object _form;
    
    public ActionNotFoundException( String actionName, FlowController fc, Object form )
    {
        super( actionName, fc );
        _form = form;
    }

    protected Object[] getMessageArgs()
    {
        return new Object[]{ getActionName(), getFlowControllerURI(), _form != null ? _form.getClass().getName() : null };
    }

    protected String[] getMessageParts()
    {
        return new String[]{ "Unable to find action ", " (form=",  ") in Page Flow ", "." };
    }
    
    protected Object getForm()
    {
        return _form;
    }

    /**
     * Tell whether the root cause may be session expiration in cases where the requested session ID is different than
     * the actual session ID.  In this case, the answer is <code>false</code>.
     */ 
    public boolean causeMayBeSessionExpiration()
    {
        return false;
    }
}
