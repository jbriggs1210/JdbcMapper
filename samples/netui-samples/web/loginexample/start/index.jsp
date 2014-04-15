<%--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at
   
       http://www.apache.org/licenses/LICENSE-2.0
   
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
  
   $Header:$
--%>
<%@ page language="java" contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="netui" uri="http://beehive.apache.org/netui/tags-html-1.0"%>
<%@ taglib prefix="netui-data" uri="http://beehive.apache.org/netui/tags-databinding-1.0"%>
<%@ taglib prefix="netui-template" uri="http://beehive.apache.org/netui/tags-template-1.0"%>


<netui-template:template templatePage="/resources/template/template.jsp">

    <netui-template:setAttribute name="sampleTitle" value="Login Example - Start"/>

    <netui-template:section name="main">
        This example demonstrates the following features:
        <ul>
            <li>Plugging in your own LoginHandler</li>
            <li>Using Page Flow inheritance for common actions and exception handlers</li>
            <li>Using nested page flows</li>
        </ul>

        Some notes:
        <ul>
            <li>
                All login-related behavior (current user, login, logout, etc.) is defined by
                <code>org.apache.beehive.samples.netui.loginexample.ExampleLoginHandler</code>, which
                is registered as the login handler in WEB-INF/beehive-netui-config.xml.  This overrides the
                default behavior, which is to use the current Servlet container's login mechanism.
            </li>
            <li>
                When you click 'go to a protected flow', a <code>NotLoggedInException</code> will be
                thrown, because the 'goProtectedFlow' action is marked with <code>loginRequired=true</code>,
                and you are not currently logged in.
            </li>
            <li>
                The current page flow does not handle <code>NotLoggedInException</code>, but its base class
                (<code>loginexample.BaseFlow</code>).  The base class
                forwards to the Login nested page flow when this exception occurs.
            </li>
            <li>
                There are two possible <strong>return actions</strong> from the Login nested page flow:
                'loginSuccess' or 'loginCancel'.  Take a look at each action, defined in
                <code>loginexample.BaseFlow</code>.  The 'loginSuccess'
                action <strong>re-runs the original target action</strong>, which was 'goProtectedFlow'.  Now
                that you're logged in, the action will succeed.  The 'loginCancel' action just takes you back
                to the original page you were on.
            </li>
            <li>
                If you've logged in successfully, you can hit 'go to a protected flow' again -- this time it
                will succeed.
            </li>
            <li>
                Note that the current page flow (or any other page flow) knows <i>nothing</i> about the Login
                page flow.  It only has to mark its protected actions with <code>loginRequired=true</code>.
                The base class page flow and the Login flow take care of the rest.
            </li>
        </ul>

        <br/>
        <netui:anchor action="goProtectedFlow">go to a protected page flow</netui:anchor>
        <br/>
        <netui:anchor action="logout">log out</netui:anchor>
    </netui-template:section>

</netui-template:template>
