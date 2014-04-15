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

import org.apache.beehive.netui.util.Bundle;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * Exception that is thrown when <code>rolesAllowed</code> is set on the current action's annotation
 * ({@link org.apache.beehive.netui.pageflow.annotations.Jpf.Action &#64;Jpf.Action} or
 * {@link org.apache.beehive.netui.pageflow.annotations.Jpf.SimpleAction &#64;Jpf.SimpleAction}), and there is a logged-in
 * user who does not fulfil any of the given roles.
 */ 
public class UnfulfilledRolesException
        extends PageFlowException
        implements ResponseErrorCodeSender
{
    private String[] _roleNames;
    private String _rolesList;
    

    /**
     * Construct on the list of roles that were allowed access to the action.
     * 
     * @param roleNames an array of String role names.
     */ 
    public UnfulfilledRolesException( String[] roleNames, String rolesList, String actionName, FlowController fc )
     {
        super( actionName, fc );
        _roleNames = roleNames;
        _rolesList = rolesList;
    }

    /**
     * Get the names of the roles that were allowed access to the action.
     * 
     * @return an array of String role names.
     */ 
    public String[] getRoleNames()
    {
        return _roleNames;
    }
    

    protected Object[] getMessageArgs()
    {
        return new Object[]{ getActionName(), getFlowControllerURI(), _rolesList };
    }

    public String[] getMessageParts()
    {
        return new String[]
        {
            "Action ", " on Page Flow ", " requires the user to be in one of the following roles: ", "."
        };
    }

    public void sendResponseErrorCode( HttpServletResponse response ) throws IOException
    {
        String msg = Bundle.getString( "PageFlow_UnfulfilledRolesException_ResponseMessage", getActionName() );
        response.sendError( HttpServletResponse.SC_BAD_REQUEST, msg );
    }

    /**
     * Tell whether the root cause may be session expiration in cases where the requested session ID is different than
     * the actual session ID.  In this case, the answer is <code>true</code>.
     */ 
    public boolean causeMayBeSessionExpiration()
    {
        return true;
    }
}
