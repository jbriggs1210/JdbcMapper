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
 * Exception that occurs when an action output has been added to a {@link Forward} that resolves to a
 * {@link org.apache.beehive.netui.pageflow.annotations.Jpf.Forward &#64;Jpf.Forward} annotation marked with
 * <code>{@link org.apache.beehive.netui.pageflow.annotations.Jpf.Forward#redirect redirect}=true</code>.
 * Action outputs may not be used on redirect forwards.
 */ 
public class IllegalActionOutputException extends PageFlowException
{
    private String _forwardName;
    private String _actionOutputName;

    
    /**
     * Constructor.
     * 
     * @param forwardName the name of the relevant {@link Forward}.
     * @param actionName the name of the current action being run.
     * @param flowController the current {@link FlowController} instance.
     * @param actionOutputName the name of the relevant action output.
     */ 
    public IllegalActionOutputException( String forwardName, String actionName, FlowController flowController,
                                         String actionOutputName )
    {
        super( actionName, flowController );
        _forwardName = forwardName;
        _actionOutputName = actionOutputName;
    }

    /**
     * Get the name of the relevant {@link Forward}.
     * 
     * @return a String that is the name of the relevant {@link Forward}.
     */ 
    public String getForwardName()
    {
        return _forwardName;
    }

    /**
     * Set the name of the relevant {@link Forward}.
     * 
     * @param forwardName a String that is the name of the relevant {@link Forward}.
     */ 
    public void setForwardName( String forwardName )
    {
        _forwardName = forwardName;
    }

    /**
     * Get the name of the relevant action output.
     * 
     * @return a String that is the name of the relevant action output.
     */ 
    public String getActionOutputName()
    {
        return _actionOutputName;
    }

    /**
     * Set the name of the relevant action output.
     * 
     * @param actionOutputName a String that is the name of the relevant action output.
     */ 
    public void setActionOutputName( String actionOutputName )
    {
        _actionOutputName = actionOutputName;
    }

    protected Object[] getMessageArgs()
    {
        return new Object[]{ _forwardName, getActionName(), getFlowControllerURI(), _actionOutputName };
    }

    public String[] getMessageParts()
    {
        return new String[]
        {
            "The forward \"", "\" on action ", " in page flow ", " has at least one action output (\"",
            "\"), but is set to redirect=\"true\".  Action outputs may not be used on redirect forwards."
        };
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
