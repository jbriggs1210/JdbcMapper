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
package org.apache.beehive.netui.pageflow.annotations;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.Inherited;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Wrapper interface for all Page Flow annotations.
 */
public interface Jpf
{
    /**
     * Enumeration used by {@link Controller#multipartHandler()} to determine the type of multipart handler.
     */ 
    public enum MultipartHandler
    {
        /** Indicates that multipart handling is disabled in this Controller. **/
        disabled,
        
        /** Indicates that multipart requests will be processed in-memory for this Controller. */
        memory,
        
        /** Indicates that multipart requests will be processed on disk for this Controller. */
        disk
    }
    
    /**
     * Enumeration used by {@link Forward#navigateTo()}, {@link SimpleAction#navigateTo()}, and 
     * {@link ConditionalForward#navigateTo()} to determine the next navigation point.
     */
    public enum NavigateTo
    {
        /** Indicates that the next page shown should be the most recent one shown. */
        currentPage,
        
        /** Indicates that the next page shown should be the one before the most recent one shown. */
        previousPage,
        
        /** Indicates that the previous action should be re-run. **/
        previousAction,
        
        /** Either {@link #currentPage} or {@link #previousPage} should be used instead. */
        @Deprecated
        page
    }

    /**
     * Enumeration used by {@link Controller#validatorVersion} to determine the version of Commons Validator that is
     * being used.  This affects the format of generated Commons Validator configuration files.
     */ 
    public enum ValidatorVersion
    {
        /** Indicates that Commons Validator version 1.0 is being used. */
        oneZero,
        
        /** Indicates that Commons Validator version 1.1 is being used. */
        oneOne
    }

    /**
     * Main class-level annotation required to be present on all page flow
     * ({@link org.apache.beehive.netui.pageflow.PageFlowController}-derived) and shared flow
     * ({@link org.apache.beehive.netui.pageflow.SharedFlowController}-derived) classes.
     */ 
    @Target( TYPE )
    @Retention( RUNTIME )
    public @interface Controller
    {
        //-----------------------
        // Optional attributes...
        //-----------------------

        /**
         * Array of declarative catches, which can reroute to a page or to a handler method ({@link ExceptionHandler})
         * when a particular exception is thrown.
         */
        Catch[] catches() default {};
        
        /**
         * Array of additional webapp-relative file paths to be added to the <code>pathnames</code> property of the
         * ValidatorPlugIn initialization in the generated Struts config XML for this controller.
         */ 
        String[] customValidatorConfigs() default {};

        /**
         * Array of Forwards that can be used from any action ({@link Action} or {@link SimpleAction}) or
         * exception handler ({@link ExceptionHandler}) in this controller.  An action or exception handler method
         * uses a forward by returning a {@link org.apache.beehive.netui.pageflow.Forward} object whose name matches
         * the one in the Forward annotation.  A simple action uses a forward by naming it in the
         * {@link SimpleAction#forwardRef} attribute.
         */
        Forward[] forwards() default {};

        /**
         * If set to <code>true</code>, a {@link org.apache.beehive.netui.pageflow.NotLoggedInException} will be thrown
         * for any action ({@link Action} or {@link SimpleAction}) in this controller when the current
         * {@link org.apache.beehive.netui.pageflow.handler.LoginHandler} returns <code>null</code> for
         * <code>getUserPrincipal</code>.  The default LoginHandler simply calls <code>getUserPrincipal</code> on
         * {@link javax.servlet.http.HttpServletRequest}.
         */
        boolean loginRequired() default false;

        /**
         * If set to <code>true</code>, then this page flow does not get discarded when another page flow is hit
         * (only valid for {@link org.apache.beehive.netui.pageflow.PageFlowController}s).  It ramains stored in the
         * background, and is reinstated when it is requested again.  Long-lived page flows may be deleted using
         * {@link org.apache.beehive.netui.pageflow.PageFlowUtils#removeLongLivedPageFlow}.
         */
        boolean longLived() default false;

        /**
         * Array of message bundles used by this controller.  Any message key (like those in validation messages) refers
         * to a message in one of the listed bundles.
         */
        MessageBundle[] messageBundles() default {};
        
        /**
         * A value that determines the type of multipart handling for actions ({@link Action} or {@link SimpleAction})
         * in this controller.  <i>For security, multipart handling is disabled by default.</i>.
         */
        MultipartHandler multipartHandler() default MultipartHandler.disabled;

        /**
         * If set to <code>true</code>, then this is a reusable, modular flow that can be "nested" during other flows.
         * It has entry points (actions with optional form bean arguments), and exit points ({@link Forward},
         * {@link SimpleAction}, or {@link ConditionalForward} annotations that have <code>returnAction</code>
         * attributes).
         */
        boolean nested() default false;

        /**
         * If set to <code>true</code>, then by default all actions ({@link Action} or {@link SimpleAction}) in this
         * controller have "promised" that they will not modify member data.  In containers that support clustering,
         * this allows the framework to avoid serializing the controller instance for session failover after the
         * method is run.  This is a performance optimization; it does not have an effect on the behavior of the
         * action itself.
         */
        boolean readOnly() default false;

        /**
         * Array of roles allowed to access actions in this controller.  If this array is non-empty, then two
         * exceptions may be thrown when an action ({@link Action} or {@link SimpleAction}) in this controller is
         * raised:
         *     <ul>
         *         <li>
         *             A {@link org.apache.beehive.netui.pageflow.NotLoggedInException} will be thrown when the current
         *             {@link org.apache.beehive.netui.pageflow.handler.LoginHandler} returns <code>null</code> for
         *             <code>getUserPrincipal</code>.  The default LoginHandler simply calls
         *             <code>getUserPrincipal</code> on {@link javax.servlet.http.HttpServletRequest}.
         *         </li>
         *         <li>
         *             An {@link org.apache.beehive.netui.pageflow.UnfulfilledRolesException} will be thrown when the
         *             current {@link org.apache.beehive.netui.pageflow.handler.LoginHandler} returns
         *             <code>false</code> from <code>isUserInRole</code> for each of the roles in the list.  The
         *             default LoginHandler simply calls <code>isUserInRole</code> on
         *             {@link javax.servlet.http.HttpServletRequest}.
         *         </li>
         *     </ul>
         */
        String[] rolesAllowed() default {};

        /**
         * Array of shared flow references used by a page flow.  Each one maps a local shared flow name to an actual
         * shared flow type.  Referenced shared flows add common actions (addressed in the form
         * "<i>shared-flow-name</i>.<i>shared-flow-action-name</i>") and fallback exception handlers, and can provide
         * a location for shared state.
         */ 
        SharedFlowRef[] sharedFlowRefs() default {};
        
        /**
         * Array of simple actions.
         */ 
        SimpleAction[] simpleActions() default {};

        /**
         * Location of the "Struts merge" file, whose elements/attributes override those in the Struts config XML file
         * generated from this controller.  The path is relative to this controller, or, if it starts with '/', is
         * relative to a source root.
         */
        String strutsMerge() default "";
        
        /**
         * Array of webapp-relative paths to Tiles Definitions Config XML files.  Definitions within these files may
         * be referenced in {@link Forward#tilesDefinition}, {@link SimpleAction#tilesDefinition}, or
         * {@link ConditionalForward#tilesDefinition}.
         */
        String[] tilesDefinitionsConfigs() default {};

        /**
         * Array of validation rules on a per-bean (class) basis.  An action ({@link Action} or {@link SimpleAction})
         * that accepts a form bean can trigger validation rules for any of these bean types.
         * @see ValidateRequired
         * @see ValidateMinLength
         * @see ValidateMaxLength
         * @see ValidateMask
         * @see ValidateType
         * @see ValidateDate
         * @see ValidateRange
         * @see ValidateCreditCard
         * @see ValidateEmail
         * @see ValidateValidWhen
         * @see ValidateCustomRule
         */
        ValidatableBean[] validatableBeans() default {};

        /**
         * Location of the "Validator merge" file, whose elements/attributes override those in the ValidatorPlugIn
         * config XML file generated from this controller.  The path is relative to this controller, or, if it starts
         * with '/', is relative to a source root.
         */
        String validatorMerge() default "";
        
        /**
         * The version of the commons-validator DTD to use for the ValidatorPlugIn config XML generated from this
         * controller.
         */
        ValidatorVersion validatorVersion() default ValidatorVersion.oneZero;

        /**
         * This relates to any local path (does not start with "/") inherited from a {@link SimpleAction},
         * {@link Action}, {@link Forward}, etc. in a base class.  Normally, this path would be relative to the
         * <strong>current</strong> controller.  When this value is set to <code>true</code>, then the path is
         * relative to the <strong>base class</strong> controller.
         */
        boolean inheritLocalPaths() default false;
    }
    
    /**
     * Declaration of a shared flow reference, which maps a local shared flow name to an actual shared flow type.
     * Referenced shared flows add common actions (addressed in the form
     * "<i>shared-flow-name</i>.<i>shared-flow-action-name</i>") and fallback exception handlers, and can provide
     * a location for shared state.  This annotation is used within {@link Controller}.
     */ 
    @Target( ANNOTATION_TYPE )
    @Retention( RUNTIME )
    public @interface SharedFlowRef
    {
        /**
         * The local name of the shared flow reference.
         */
        String name();

        /**
         * The actual type of the shared flow.
         */
        Class type();
    }

    /**
     * Annotation used within {@link SimpleAction} to forward conditionally, based on the evaluation of a JSP 2.0-style
     * expression.
     */
    @Target( ANNOTATION_TYPE )
    @Retention( RUNTIME )
    public @interface ConditionalForward
    {
        //-----------------------
        // Required attributes...
        //-----------------------

        /**
         * The JSP 2.0-style expression (e.g., <code>${pageFlow.myProperty}</code> that will trigger this forward.  If
         * the expression evaluates to <code>true</code>, then the forward will be used.
         */
        String condition();
        
        //-----------------------
        // Optional attributes...
        //-----------------------

        /**
         * The name of an action to forward to.  Mutually-exclusive with {@link #path}, {@link #navigateTo},
         * {@link #returnAction}, and {@link #tilesDefinition}.
         */ 
        String action() default "";

        /**
         * When set to <code>true</code>, then this Forward will redirect to a path that is external to the
         * current webapp; for example, the following path would redirect to /dir/mypage.jsp in webapp "/myapp":
         * <blockquote>
         *     <code>path="/myapp/dir/mypage.jsp", externalRedirect=true</code>
         * </blockquote>
         * With <code>externalRedirect</code> set to <code>false</code>, the path above would forward to
         * /myapp/dir/mypage.jsp under the <i>current</i> webapp.  Note that <code>externalRedirect=true</code>
         * implies that <code>redirect=true</code>.
         */
        boolean externalRedirect() default false;

        /**
         * The forward name, which is optional for ConditionalForwards.
         */ 
        String name() default "";
        
        /**
         * A symbolic name for the page/action to which to navigate.  Mutually-exclusive with {@link #path},
         * {@link #returnAction}, {@link #action}, and {@link #tilesDefinition}.
         * @see NavigateTo
         */
        NavigateTo navigateTo() default NavigateTo.currentPage;
        
        /**
         * The type of form bean that will be passed along (to a page or to another action) with this forward.  A new
         * instance of the given class will be created.
         */
        Class outputFormBeanType() default Void.class;

        /**
         * The name of a member variable whose value will be passed along (to a page or to another action) with this
         * forward.
         */
        String outputFormBean() default "";

        /**
         * The forward path.  Mutually-exclusive with {@link #navigateTo}, {@link #returnAction}, {@link #action},
         * and {@link #tilesDefinition}.
         */ 
        String path() default "";
        
        /**
         * If <code>true</code>, there will be a browser redirect (not a server forward) to the destination path.
         */
        boolean redirect() default false;

        /**
         * If <code>true</code>, the original URL query string will be restored when the previous page or action is
         * run.  Only valid when the <code>navigateTo</code> attribute is used.
         */ 
        boolean restoreQueryString() default false;
        
        /**
         * The action to be invoked on the calling page flow.  Mutually-exclusive with {@link #path},
         * {@link #navigateTo}, {@link #action}, and {@link #tilesDefinition}, and only valid in a nested page flow
         * ({@link Controller#nested} must be <code>true</code>).
         */ 
        String returnAction() default "";
        
        /**
         * A Tiles definition to forward to.  The Tiles definition is found in one of the config files specified with
         * {@link Controller#tilesDefinitionsConfigs}. Mutually-exclusive with {@link #path}, {@link #navigateTo},
         * {@link #returnAction}, and {@link #action}.
         */
        String tilesDefinition() default "";
    }
    
    /**
     * Optional class-level annotation that can store tool-specific view properties.
     */ 
    @Target( TYPE )
    @Retention( SOURCE )
    public @interface ViewProperties
    {
        String[] value() default {};
    }
    
    /**
     * <p>
     * Method-level annotation that configures an action method.  This annotation is required for a method to be
     * recognized as an action.  Nearly every action will define a list of forwards through the {@link #forwards}
     * attribute.  An example of an action method is shown below:
     * <blockquote>
     * <code>
     *    &#64;Jpf.Action(<br/>
     *        &nbsp;&nbsp;&nbsp;&nbsp;forwards={<br/>
     *            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&#64;Jpf.Forward(name="page1", page="page1.jsp"),<br/>
     *            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&#64;Jpf.Forward(name="page2", page="page2.jsp")<br/>
     *        &nbsp;&nbsp;&nbsp;&nbsp;},<br/>
     *        &nbsp;&nbsp;&nbsp;&nbsp;validationErrorForward=@Jpf.Forward(name="failure", navigateTo=Jpf.NavigateTo.currentPage)<br/>
     *    )<br/>
     *    public Forward someAction(MyFormBean bean)<br/>
     *    {<br/>
     *        &nbsp;&nbsp;&nbsp;&nbsp;if (...)<br/>
     *        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return new Forward("page1");<br/>
     *        &nbsp;&nbsp;&nbsp;&nbsp;else<br/>
     *        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return new Forward("page2");<br/>
     *     }
     * </code>
     * </blockquote>
     * </p>
     * <p>
     * For actions that do not require Java code (for example, a begin action that simply
     * forwards to a particular page), {@link SimpleAction} can be used instead.
     * </p>
     * 
     * @see SimpleAction
     */
    @Target( METHOD )
    @Retention( RUNTIME )
    public @interface Action
    {
        /**
         * Array of declarative catches, which can reroute to a page or to a handler method ({@link ExceptionHandler})
         * when a particular exception is thrown.
         */
        Catch[] catches() default {};
        
        /**
         * Enable or disable form validation for this action.  If {@link #validationErrorForward} is set while
         * <code>doValidation</code> is not, then validation is enabled automatically.
         */
        boolean doValidation() default false;

        /**
         * Array of Forwards that can be used from this action.  The method uses a forward by returning a
         * {@link org.apache.beehive.netui.pageflow.Forward} object whose name matches the one in the Forward
         * annotation.  When an action method returns <code>null</code>, no forwarding is done.
         */
        Forward[] forwards() default {};
        
        /**
         * If set to <code>true</code>, a {@link org.apache.beehive.netui.pageflow.NotLoggedInException} will be thrown
         * for this action when the current {@link org.apache.beehive.netui.pageflow.handler.LoginHandler} returns
         * <code>null</code> for <code>getUserPrincipal</code>.  The default LoginHandler simply calls
         * <code>getUserPrincipal</code> on {@link javax.servlet.http.HttpServletRequest}.
         */
        boolean loginRequired() default false;
        
        /**
         * <p>
         * Use a session-scoped token to prevent multiple submits to this action.  When the server detects a double
         * submit on the token, a {@link org.apache.beehive.netui.pageflow.DoubleSubmitException} is thrown.
         * </p>
         * <p>
         * This is a server-side solution that guards against double processing; however, it is still a good
         * idea to supplement this with a client-side solution to prevent double-submits from happening in the first
         * place (an example of this is the <code>disableSecondClick</code> attribute on the NetUI Button tag, which
         * disables the button through JavaScript as soon as it is pressed).  
         * </p>
         */ 
        boolean preventDoubleSubmit() default false;

        /**
         * If set to <code>true</code>, then by default this action has "promised" that it will not modify member data.
         * In containers that support clustering, this allows the framework to avoid serializing the controller
         * instance for session failover after the action is run.  This is a performance optimization; it does not have
         * an effect on the behavior of the action itself.
         */
        boolean readOnly() default false;
        
        /**
         * Array of roles allowed to access this action.  If this array is non-empty, then two exceptions may be thrown
         * when the action is raised:
         *     <ul>
         *         <li>
         *             A {@link org.apache.beehive.netui.pageflow.NotLoggedInException} will be thrown when the current
         *             {@link org.apache.beehive.netui.pageflow.handler.LoginHandler} returns <code>null</code> for
         *             <code>getUserPrincipal</code>.  The default LoginHandler simply calls
         *             <code>getUserPrincipal</code> on {@link javax.servlet.http.HttpServletRequest}.
         *         </li>
         *         <li>
         *             An {@link org.apache.beehive.netui.pageflow.UnfulfilledRolesException} will be thrown when the
         *             current {@link org.apache.beehive.netui.pageflow.handler.LoginHandler} returns
         *             <code>false</code> from <code>isUserInRole</code> for each of the roles in the list.  The
         *             default LoginHandler simply calls <code>isUserInRole</code> on
         *             {@link javax.servlet.http.HttpServletRequest}.
         *         </li>
         *     </ul>
         */
        String[] rolesAllowed() default {};
        
        /**
         * If set, then the form bean for this action method will be the named member variable, rather than a
         * newly-created instance.  This is referred to as a "flow-scoped form bean".  Note that if the member variable
         * is null, it will be set with a new instance of the correct type.
         */
        String useFormBean() default "";
        
        /**
         * Array of validation rules that are applied to properties on this action's form bean.
         * @see ValidateRequired
         * @see ValidateMinLength
         * @see ValidateMaxLength
         * @see ValidateMask
         * @see ValidateType
         * @see ValidateDate
         * @see ValidateRange
         * @see ValidateCreditCard
         * @see ValidateEmail
         * @see ValidateValidWhen
         * @see ValidateCustomRule
         */
        ValidatableProperty[] validatableProperties() default {};
        
        /**
         * The Forward used when form bean validation fails.  Setting this value automatically enables validation for
         * this action method, unless {@link #doValidation} is set to <code>false</code>.  Validation is always
         * disabled when this value is not set.
         */
        Forward validationErrorForward() default @Jpf.Forward( name="" );
    }
    
    /**
     * A "simple" action, which defines its behavior wholly through an annotation, rather than through a method.  This
     * is useful when an action does not require any Java code to run (for example, a begin action that simply forwards
     * to a particular page).  Actions that <i>do</i> require Java code are built using methods annotated with
     * {@link Action}.
     * 
     * @see Action
     */
    @Target( ANNOTATION_TYPE )
    @Retention( RUNTIME )
    public @interface SimpleAction
    {
        //-----------------------
        // Required attributes...
        //-----------------------
        
        /**
         * The action name.
         */ 
        String name();
        
        //---------------------------------------------
        // Optional attributes, like those on Action...
        //---------------------------------------------

        /**
         * Array of declarative catches, which can reroute to a page or to a handler method ({@link ExceptionHandler})
         * when a particular exception is thrown.
         */
        Catch[] catches() default {};
        
        /**
         * Array of conditional forwards.  Each one is triggered by a JSP 2.0-style expression evaluating to
         * <code>true</code>.  The conditions are evaluated in order, so if more than one would evaluate to
         * <code>true</code>, the first one wins.
         */
        ConditionalForward[] conditionalForwards() default {};
        
        /**
         * Enable or disable form validation for this action.  If {@link #validationErrorForward} is set while
         * <code>doValidation</code> is not, then validation is enabled automatically.
         */
        boolean doValidation() default false;

        /**
         * <p>
         * The name of a class-level forward (a {@link Forward} in {@link Controller#forwards}) which will serve as the
         * destination for this simple action.  Mutually-exclusive with with {@link #action}, {@link #path},
         * {@link #navigateTo}, {@link #returnAction}, and {@link #tilesDefinition}.
         * </p>
         * <p>
         * If this simple action handles the return from a nested page flow that was shown in a popup window, then
         * the <code>forwardRef</code> attribute can be set to the special value "_auto".  This causes the framework to
         * write out the correct javascript to close the popup window instead of forwarding to another page.
         * </p>
         */
        String forwardRef() default "";

        /**
         * If set to <code>true</code>, a {@link org.apache.beehive.netui.pageflow.NotLoggedInException} will be thrown
         * for this action when the current {@link org.apache.beehive.netui.pageflow.handler.LoginHandler} returns
         * <code>null</code> for <code>getUserPrincipal</code>.  The default LoginHandler simply calls
         * <code>getUserPrincipal</code> on {@link javax.servlet.http.HttpServletRequest}.
         */
        boolean loginRequired() default false;
        
        /**
         * <p>
         * Use a session-scoped token to prevent multiple submits to this action.  When the server detects a double
         * submit on the token, a {@link org.apache.beehive.netui.pageflow.DoubleSubmitException} is thrown.
         * </p>
         * <p>
         * This is a server-side solution that guards against double processing; however, it is still a good
         * idea to supplement this with a client-side solution to prevent double-submits from happening in the first
         * place (an example of this is the <code>disableSecondClick</code> attribute on the NetUI Button tag, which
         * disables the button through JavaScript as soon as it is pressed).  
         * </p>
         */ 
        boolean preventDoubleSubmit() default false;

        /**
         * If set to <code>true</code>, then by default this action has "promised" that it will not modify member data.
         * In containers that support clustering, this allows the framework to avoid serializing the controller
         * instance for session failover after the action is run.  This is a performance optimization; it does not have
         * an effect on the behavior of the action itself.
         */
        boolean readOnly() default false;
        
        /**
         * Array of roles allowed to access this action.  If this array is non-empty, then two exceptions may be thrown
         * when the action is raised:
         *     <ul>
         *         <li>
         *             A {@link org.apache.beehive.netui.pageflow.NotLoggedInException} will be thrown when the current
         *             {@link org.apache.beehive.netui.pageflow.handler.LoginHandler} returns <code>null</code> for
         *             <code>getUserPrincipal</code>.  The default LoginHandler simply calls
         *             <code>getUserPrincipal</code> on {@link javax.servlet.http.HttpServletRequest}.
         *         </li>
         *         <li>
         *             An {@link org.apache.beehive.netui.pageflow.UnfulfilledRolesException} will be thrown when the
         *             current {@link org.apache.beehive.netui.pageflow.handler.LoginHandler} returns
         *             <code>false</code> from <code>isUserInRole</code> for each of the roles in the list.  The
         *             default LoginHandler simply calls <code>isUserInRole</code> on
         *             {@link javax.servlet.http.HttpServletRequest}.
         *         </li>
         *     </ul>
         */
        String[] rolesAllowed() default {};
        
        /**
         * If set, then the form bean for this action method will be the named member variable, rather than a
         * newly-created instance.  This is referred to as a "flow-scoped form bean".  Note that if the member variable
         * is null, it will be set with a new instance of the correct type.  Mutually exclusive with
         * {@link #useFormBeanType}.
         */
        String useFormBean() default "";
        
        /**
         * The type of form bean to use for this action.  A new instance of the given class will be created when this
         * action is run.  Mutually exclusive with {@link #useFormBean}.
         */
        Class useFormBeanType() default Void.class;

        /**
         * Array of validation rules that are applied to properties on this action's form bean.
         */
        ValidatableProperty[] validatableProperties() default {};
        
        /**
         * The Forward used when form bean validation fails.  Setting this value automatically enables validation for
         * this action method, unless {@link #doValidation} is set to <code>false</code>.  Validation is always
         * disabled when this value is not set.
         */
        Forward validationErrorForward() default @Jpf.Forward( name="" );


        //----------------------------------------------
        // Optional attributes, like those on Forward...
        //----------------------------------------------

        /**
         * The name of an action to forward to.  Mutually-exclusive with {@link #path}, {@link #navigateTo},
         * {@link #returnAction}, {@link #tilesDefinition}, and {@link #forwardRef}.
         */ 
        String action() default "";

        /**
         * When set to <code>true</code>, then this Forward will redirect to a path that is external to the
         * current webapp; for example, the following path would redirect to /dir/mypage.jsp in webapp "/myapp":
         * <blockquote>
         *     <code>path="/myapp/dir/mypage.jsp", externalRedirect=true</code>
         * </blockquote>
         * With <code>externalRedirect</code> set to <code>false</code>, the path above would forward to
         * /myapp/dir/mypage.jsp under the <i>current</i> webapp.  Note that <code>externalRedirect=true</code>
         * implies that <code>redirect=true</code>.
         */
        boolean externalRedirect() default false;

        /**
         * A symbolic name for the page/action to which to navigate.  Mutually-exclusive with {@link #path},
         * {@link #returnAction}, {@link #action}, {@link #tilesDefinition}, and {@link #forwardRef}.
         * @see NavigateTo
         */
        NavigateTo navigateTo() default NavigateTo.currentPage;
        
        /**
         * The type of form bean that will be passed along (to a page or to another action) with this forward.  The
         * actual form bean instance is provided on the {@link org.apache.beehive.netui.pageflow.Forward} object that
         * is returned from the action method ({@link Action}) or the exception handler method
         * ({@link ExceptionHandler}).  If that object has no output form bean, then a new instance of the given type
         * will be created.
         */
        Class outputFormBeanType() default Void.class;

        /**
         * The name of a member variable whose value will be passed along (to a page or to another action) with this
         * forward.
         */
        String outputFormBean() default "";

        /**
         * The forward path.  Mutually-exclusive with {@link #navigateTo}, {@link #returnAction}, {@link #action},
         * {@link #tilesDefinition}, and {@link #forwardRef}.
         */ 
        String path() default "";
        
        /**
         * If <code>true</code>, there will be a browser redirect (not a server forward) to the destination path.
         */
        boolean redirect() default false;

        /**
         * If <code>true</code>, the original URL query string will be restored when the previous page or action is
         * run.  Only valid when the <code>navigateTo</code> attribute is used.
         */ 
        boolean restoreQueryString() default false;
        
        /**
         * The action to be invoked on the calling page flow.  Mutually-exclusive with {@link #path},
         * {@link #navigateTo}, {@link #action}, {@link #tilesDefinition}, and {@link #forwardRef}, and only valid in a
         * nested page flow ({@link Controller#nested} must be <code>true</code>).
         */ 
        String returnAction() default "";
        
        /**
         * A Tiles definition to forward to.  The Tiles definition is found in one of the config files specified with
         * {@link Controller#tilesDefinitionsConfigs}. Mutually-exclusive with {@link #path}, {@link #navigateTo},
         * {@link #returnAction}, {@link #action}, and {@link #forwardRef}.
         */
        String tilesDefinition() default "";
    }

    /**
     * A declarative "catch" for exceptions thrown from actions ({@link Action}, {@link SimpleAction}).
     */
    @Target( ANNOTATION_TYPE )
    @Retention( RUNTIME )
    public @interface Catch
    {
        //-----------------------
        // Required attributes...
        //-----------------------

        /**
         * The type of Throwable to handle.
         */
        Class<? extends Throwable> type();

        //-----------------------
        // Optional attributes...
        //-----------------------

        /**
         * The exception handler method ({@link ExceptionHandler}) to invoke.  Mutually exclusive with {@link #path}.
         */
        String method() default "";

        /**
         * The JSP 2.0-style expression (e.g., <code>${pageFlow.myProperty}</code> or literal string message.  This
         * message is used in two ways:
         *     <ul>
         *         <li>It is sent as an argument to the handler method, if {@link #method} is specified.</li>
         *         <li>
         *             It is used as the message in an {@link org.apache.struts.action.ActionMessage} object within
         *             an {@link org.apache.struts.action.ActionErrors} that is stored in request attribute
         *             {@link org.apache.struts.Globals#ERROR_KEY}.  The ActionMessage's key is the value of
         *             {@link #messageKey}, if set, or the full class name of the exception type otherwise.
         *         </li>
         *     </ul>
         */ 
        String message() default "";

        /**
         * A message resource within a message bundle {@link MessageBundle} that is used to look up the message.  The
         * message is used in two ways:
         *     <ul>
         *         <li>It is sent as an argument to the handler method, if {@link #method} is specified.</li>
         *         <li>
         *             It is used as the message in an {@link org.apache.struts.action.ActionMessage} object within
         *             an {@link org.apache.struts.action.ActionErrors} that is stored in request attribute
         *             {@link org.apache.struts.Globals#ERROR_KEY}.  <i>The value of <code>messageKey</code> is also
         *             used as the ActionMessage's key.</i>
         *         </li>
         *     </ul>
         */
        String messageKey() default "";

        /**
         * The destination URI to forward to.  Mututally exclusive with {@link #method}.  For more {@link Forward}-style
         * options, use the {@link #method} attribute.
         */
        String path() default "";
    }

    /**
     * Method-level annotation that configures an exception handler method, which is invoked when a {@link Catch} is
     * triggered.  This annotation is required for a method to be recognized as an exception handler.
     */
    @Target( METHOD )
    @Retention( RUNTIME )
    public @interface ExceptionHandler
    {
        /**
         * Array of Forwards that can be used from this exception handler.  The method uses a forward by returning a
         * {@link org.apache.beehive.netui.pageflow.Forward} object whose name matches the one in the Forward
         * annotation.  When an exception handler method returns <code>null</code>, no forwarding is done.
         */
        Forward[] forwards() default {};

        /**
         * If set to <code>true</code>, then by default this exception handler has "promised" that it will not modify
         * member data.  In containers that support clustering, this allows the framework to avoid serializing the
         * controller instance for session failover after the method is run.  This is a performance optimization; it
         * does not have an effect on the behavior of the exception handler itself.
         */
        boolean readOnly() default false;
    }

    /**
     * A destination that is used by actions ({@link Action}, {@link SimpleAction}) and exception handlers
     * {@link ExceptionHandler}.
     */
    @Target( ANNOTATION_TYPE )
    @Retention( RUNTIME )
    public @interface Forward
    {
        //-----------------------
        // Required attributes...
        //-----------------------
        
        /**
         * The forward name.  An action method ({@link Action}) or an exception handler method
         * ({@link ExceptionHandler}) can use this forward by returning a
         * {@link org.apache.beehive.netui.pageflow.Forward} object that is initialized with this name.
         */ 
        String name();
        
        //-----------------------
        // Optional attributes...
        //-----------------------

        /**
         * The name of an action to forward to.  Mutually-exclusive with {@link #path}, {@link #navigateTo},
         * {@link #returnAction}, and {@link #tilesDefinition}.
         */ 
        String action() default "";

        /**
         * Array of action output declarations to be associated with this Forward.  An actual action output is passed on
         * a {@link org.apache.beehive.netui.pageflow.Forward} object.  An action output supplies a "page input" in a
         * page, where it is accessed by databinding to
         * <code>${pageInput.</code><i>action-output-name</i><code>}</code>.
         */
        ActionOutput[] actionOutputs() default {};

        /**
         * When set to <code>true</code>, then this Forward will redirect to a path that is external to the
         * current webapp; for example, the following path would redirect to /dir/mypage.jsp in webapp "/myapp":
         * <blockquote>
         *     <code>path="/myapp/dir/mypage.jsp", externalRedirect=true</code>
         * </blockquote>
         * With <code>externalRedirect</code> set to <code>false</code>, the path above would forward to
         * /myapp/dir/mypage.jsp under the <i>current</i> webapp.  Note that <code>externalRedirect=true</code>
         * implies that <code>redirect=true</code>.
         */
        boolean externalRedirect() default false;

        /**
         * A symbolic name for the page/action to which to navigate.  Mutually-exclusive with {@link #path},
         * {@link #returnAction}, {@link #action}, and {@link #tilesDefinition}.
         * @see NavigateTo
         */
        NavigateTo navigateTo() default NavigateTo.currentPage;
        
        /**
         * The type of form bean that will be passed along (to a page or to another action) with this forward.  The
         * actual form bean instance is provided on the {@link org.apache.beehive.netui.pageflow.Forward} object that
         * is returned from the action method ({@link Action}) or the exception handler method
         * ({@link ExceptionHandler}).  If that object has no output form bean, then a new instance of the given type
         * will be created.
         */
        Class outputFormBeanType() default Void.class;

        /**
         * The name of a member variable whose value will be passed along (to a page or to another action) with this
         * forward.
         */
        String outputFormBean() default "";

        /**
         * The forward path.  Mutually-exclusive with {@link #navigateTo}, {@link #returnAction}, {@link #action},
         * and {@link #tilesDefinition}.
         */ 
        String path() default "";
        
        /**
         * If <code>true</code>, there will be a browser redirect (not a server forward) to the destination path.
         */
        boolean redirect() default false;

        /**
         * If <code>true</code>, the original URL query string will be restored when the previous page or action is
         * run.  Only valid when the <code>navigateTo</code> attribute is used.
         */ 
        boolean restoreQueryString() default false;
        
        /**
         * The action to be invoked on the calling page flow.  Mutually-exclusive with {@link #path},
         * {@link #navigateTo}, {@link #action}, and {@link #tilesDefinition}, and only valid in a nested page flow
         * ({@link Controller#nested} must be <code>true</code>).
         */ 
        String returnAction() default "";
        
        /**
         * A Tiles definition to forward to.  The Tiles definition is found in one of the config files specified with
         * {@link Controller#tilesDefinitionsConfigs}. Mutually-exclusive with {@link #path}, {@link #navigateTo},
         * {@link #returnAction}, and {@link #action}.
         */
        String tilesDefinition() default "";
    }

    /**
     * An action output, which is declared in a {@link Jpf.Forward &#64;Jpf.Forward} annotation and passed from an action
     * method on a {@link org.apache.beehive.netui.pageflow.Forward} object.  An action output may be used as a
     * "page input" in a page, where it is accessed by databinding to
     * <code>${pageInput.</code><i>action-output-name</i><code>}</code>.  The benefit of action outputs (over setting
     * request attributes) is that the annotations are visible to tools, and that there is runtime checking of type and
     * required-presence.
     */
    @Target( ANNOTATION_TYPE )
    @Retention( RUNTIME )
    public @interface ActionOutput
    {
        /**
         * The name of the action output.  This is the <i>action-output-name</i> in the databinding expression
         * <code>${pageInput.</code><i>action-output-name</i><code>}</code>.
         */
        String name();

        /**
         * The type of the action output.  This will be checked by the runtime if the server is not in production mode,
         * and if the type check fails, a {@link org.apache.beehive.netui.pageflow.MismatchedActionOutputException}
         * will be thrown.
         */
        Class type();

        /**
         * A String version of the type information that can be used by tools or as runtime-accessable information,
         * particularly to add generics to the type (generics are "erased" during compilation and are not available
         * to the runtime through reflection).
         */
        String typeHint() default "";

        /**
         * If <code>true</code>, a {@link org.apache.beehive.netui.pageflow.MissingActionOutputException} will be thrown
         * when the associated {@link org.apache.beehive.netui.pageflow.Forward} object does not include a value for
         * this named action output.
         */
        boolean required() default true;
    }

    /**
     * Annotation used within {@link Controller} to declare a message bundle for use in the page flow.
     */
    @Target( ANNOTATION_TYPE )
    @Retention( RUNTIME )
    public @interface MessageBundle
    {
        /**
         * The path to the message bundle, as a ServletContext resource.  This may be specified with either '/' or'.'
         * as the separator, e.g., <code>bundlePath="foo.bar.MyMessages"</code> or
         * <code>bundlePath="foo/bar/MyMessages"</code> (in both cases, foo/bar/MyMessages.properties would be found
         * on classpath).
         */
        String bundlePath();

        /**
         * <p>
         * The name associated with the bundle; if this is <i>not</i> specified, then the bundle becomes the "default
         * bundle" for the page flow, and all validation messages implicitly reference it.  
         * </p>
         * <p>
         * Named message bundles are databound to with expressions like
         * <code>${bundle.</code><i>bundle-name</i><code>.someMessage}</code>, while the default message bundle is
         * databound to with expressions like <code>${bundle.default.someMessage}</code>.
         * </p>
         */
        String bundleName() default "";
    }
    
    /**
     * A message argument used within field validation annotations.
     * @see ValidateRequired
     * @see ValidateMinLength
     * @see ValidateMaxLength
     * @see ValidateMask
     * @see ValidateType
     * @see ValidateDate
     * @see ValidateRange
     * @see ValidateCreditCard
     * @see ValidateEmail
     * @see ValidateValidWhen
     * @see ValidateCustomRule
     */
    @Target( ANNOTATION_TYPE )
    @Retention( RUNTIME )
    public @interface MessageArg
    {
        /**
         * The JSP 2.0-style expression (e.g., <code>${pageFlow.myProperty}</code>) or literal string that will be used
         * as the message argument.  Mutually-exclusive with {@link #argKey}.
         */
        String arg() default "";

        /**
         * A key in the default message bundle or in the bundle specified by {@link #bundleName} that will be
         * used to look up the argument.  Mutually-exclusive with {@link #arg}.
         * @see MessageBundle
         */
        String argKey() default "";

        /**
         * The name of the message bundle in which to look up the argument value.  Requires {@link #argKey} to be set.
         * @see MessageBundle
         */
        String bundleName() default "";

        /**
         * The position of this argument in the associated message; for example, position 2 would replace the string
         * <code>{2}</code> in the message.  This defaults to the position in the containing <code>messageArgs</code>
         * array.
         */
        int position() default -1;
    }

    /**
     * A validation rule that will fail if it is applied to a property that has no value.
     * Used within {@link ValidatableProperty} and {@link ValidationLocaleRules}.
     */
    @Target( ANNOTATION_TYPE )
    @Retention( RUNTIME )
    public @interface ValidateRequired
    {
        /**
         * If set to <code>false</code>, then this rule will not be applied.
         */
        boolean enabled() default true;

        /**
         * The JSP 2.0-style expression (e.g., <code>${pageFlow.myProperty}</code>) or literal string that will be used
         * as the error message.  Mutually-exclusive with {@link #messageKey}.
         */
        String message() default "";

        /**
         * A key in the default message bundle or in the bundle specified by {@link #bundleName} that will be
         * used to look up the error message.  Mutually-exclusive with {@link #message}.
         * @see MessageBundle
         */
        String messageKey() default "";

        /**
         * The name of the message bundle in which to look up the error message. Requires {@link #messageKey} to be set.
         * @see MessageBundle
         */
        String bundleName() default "";

        /**
         * An array of message arguments, which will be used for the message obtained from {@link #message} or 
         * {@link #messageKey}, whichever is specified.
         */
        MessageArg[] messageArgs() default {};
    }
    
    /**
     * A validation rule that will fail if it is applied to a property that has a non-empty value whose length is less
     * than a given number of characters.
     * Used within {@link ValidatableProperty} and {@link ValidationLocaleRules}.
     */
    @Target( ANNOTATION_TYPE )
    @Retention( RUNTIME )
    public @interface ValidateMinLength
    {
        /**
         * If set to <code>false</code>, then this rule will not be applied.
         */
        boolean enabled() default true;

        /**
         * The minimum number of characters for the property value.
         */
        int chars();

        /**
         * The JSP 2.0-style expression (e.g., <code>${pageFlow.myProperty}</code>) or literal string that will be used
         * as the error message.  Mutually-exclusive with {@link #messageKey}.
         */
        String message() default "";

        /**
         * A key in the default message bundle or in the bundle specified by {@link #bundleName} that will be
         * used to look up the error message.  Mutually-exclusive with {@link #message}.
         * @see MessageBundle
         */
        String messageKey() default "";

        /**
         * The name of the message bundle in which to look up the error message. Requires {@link #messageKey} to be set.
         * @see MessageBundle
         */
        String bundleName() default "";

        /**
         * An array of message arguments, which will be used for the message obtained from {@link #message} or 
         * {@link #messageKey}, whichever is specified.
         */
        MessageArg[] messageArgs() default {};
    }

    /**
     * A validation rule that will fail if it is applied to a property that has a non-empty value whose length is
     * greater than a given number of characters.
     * Used within {@link ValidatableProperty} and {@link ValidationLocaleRules}.
     */
    @Target( ANNOTATION_TYPE )
    @Retention( RUNTIME )
    public @interface ValidateMaxLength
    {
        /**
         * If set to <code>false</code>, then this rule will not be applied.
         */
        boolean enabled() default true;

        /**
         * The maximum number of characters for the property value.
         */
        int chars();

        /**
         * The JSP 2.0-style expression (e.g., <code>${pageFlow.myProperty}</code>) or literal string that will be used
         * as the error message.  Mutually-exclusive with {@link #messageKey}.
         */
        String message() default "";

        /**
         * A key in the default message bundle or in the bundle specified by {@link #bundleName} that will be
         * used to look up the error message.  Mutually-exclusive with {@link #message}.
         * @see MessageBundle
         */
        String messageKey() default "";

        /**
         * The name of the message bundle in which to look up the error message. Requires {@link #messageKey} to be set.
         * @see MessageBundle
         */
        String bundleName() default "";

        /**
         * An array of message arguments, which will be used for the message obtained from {@link #message} or 
         * {@link #messageKey}, whichever is specified.
         */
        MessageArg[] messageArgs() default {};
    }

    /**
     * A validation rule that will fail if it is applied to a property that has a non-empty value which does not match
     * a given regular expression.
     * Used within {@link ValidatableProperty} and {@link ValidationLocaleRules}.
     */
    @Target( ANNOTATION_TYPE )
    @Retention( RUNTIME )
    public @interface ValidateMask
    {
        /**
         * If set to <code>false</code>, then this rule will not be applied.
         */
        boolean enabled() default true;

        /**
         * The regular expression that must be matched.
         */
        String regex();

        /**
         * The JSP 2.0-style expression (e.g., <code>${pageFlow.myProperty}</code>) or literal string that will be used
         * as the error message.  Mutually-exclusive with {@link #messageKey}.
         */
        String message() default "";

        /**
         * A key in the default message bundle or in the bundle specified by {@link #bundleName} that will be
         * used to look up the error message.  Mutually-exclusive with {@link #message}.
         * @see MessageBundle
         */
        String messageKey() default "";

        /**
         * The name of the message bundle in which to look up the error message. Requires {@link #messageKey} to be set.
         * @see MessageBundle
         */
        String bundleName() default "";

        /**
         * An array of message arguments, which will be used for the message obtained from {@link #message} or 
         * {@link #messageKey}, whichever is specified.
         */
        MessageArg[] messageArgs() default {};
    }

    /**
     * A validation rule that will fail if it is applied to a property that has a non-empty value which cannot be
     * converted to a given primitive type.
     * Used within {@link ValidatableProperty} and {@link ValidationLocaleRules}.
     */
    @Target( ANNOTATION_TYPE )
    @Retention( RUNTIME )
    public @interface ValidateType
    {
        /**
         * If set to <code>false</code>, then this rule will not be applied.
         */
        boolean enabled() default true;

        /**
         * The type to which the property must be convertible; must be a primitive type, e.g., <code>int.class</code>.
         */
        Class type();

        /**
         * The JSP 2.0-style expression (e.g., <code>${pageFlow.myProperty}</code>) or literal string that will be used
         * as the error message.  Mutually-exclusive with {@link #messageKey}.
         */
        String message() default "";

        /**
         * A key in the default message bundle or in the bundle specified by {@link #bundleName} that will be
         * used to look up the error message.  Mutually-exclusive with {@link #message}.
         * @see MessageBundle
         */
        String messageKey() default "";

        /**
         * The name of the message bundle in which to look up the error message. Requires {@link #messageKey} to be set.
         * @see MessageBundle
         */
        String bundleName() default "";

        /**
         * An array of message arguments, which will be used for the message obtained from {@link #message} or 
         * {@link #messageKey}, whichever is specified.
         */
        MessageArg[] messageArgs() default {};
    }

    /**
     * A validation rule that will fail if it is applied to a property that has a non-empty value which is not a date
     * in a given format.
     * Used within {@link ValidatableProperty} and {@link ValidationLocaleRules}.
     */
    @Target( ANNOTATION_TYPE )
    @Retention( RUNTIME )
    public @interface ValidateDate
    {
        /**
         * If set to <code>false</code>, then this rule will not be applied.
         */
        boolean enabled() default true;

        /**
         * The date pattern which will be used to initialize a <code>java.text.SimpleDateFormat</code>.
         */
        String pattern();

        /**
         * If set to <code>true</code>, then every element of the date must match the pattern exactly; for example,
         * "9/10/1973" would not match the pattern "MM/dd/yyyy".
         */
        boolean strict() default false;

        /**
         * The JSP 2.0-style expression (e.g., <code>${pageFlow.myProperty}</code>) or literal string that will be used
         * as the error message.  Mutually-exclusive with {@link #messageKey}.
         */
        String message() default "";

        /**
         * A key in the default message bundle or in the bundle specified by {@link #bundleName} that will be
         * used to look up the error message.  Mutually-exclusive with {@link #message}.
         * @see MessageBundle
         */
        String messageKey() default "";

        /**
         * The name of the message bundle in which to look up the error message. Requires {@link #messageKey} to be set.
         * @see MessageBundle
         */
        String bundleName() default "";

        /**
         * An array of message arguments, which will be used for the message obtained from {@link #message} or 
         * {@link #messageKey}, whichever is specified.
         */
        MessageArg[] messageArgs() default {};
    }

    /**
     * A validation rule that will fail if it is applied to a property that has a non-empty value which is not a number
     * within a given range.
     * Used within {@link ValidatableProperty} and {@link ValidationLocaleRules}.
     */
    @Target( ANNOTATION_TYPE )
    @Retention( RUNTIME )
    public @interface ValidateRange
    {
        /**
         * If set to <code>false</code>, then this rule will not be applied.
         */
        boolean enabled() default true;

        /**
         * The minimum integer value; requires {@link #maxInt}, and mutually-exclusive with {@link #minFloat}.
         */
        long minInt() default 0;

        /**
         * The maximum integer value; requires {@link #minInt}, and mutually-exclusive with {@link #maxFloat}.
         */
        long maxInt() default -1;
        
        /**
         * The minimum floating-point value; requires {@link #maxFloat}, and mutually-exclusive with {@link #minInt}.
         */
        double minFloat() default 0;

        /**
         * The maximum floating-point value; requires {@link #minFloat}, and mutually-exclusive with {@link #maxInt}.
         */
        double maxFloat() default -1;

        /**
         * The JSP 2.0-style expression (e.g., <code>${pageFlow.myProperty}</code>) or literal string that will be used
         * as the error message.  Mutually-exclusive with {@link #messageKey}.
         */
        String message() default "";

        /**
         * A key in the default message bundle or in the bundle specified by {@link #bundleName} that will be
         * used to look up the error message.  Mutually-exclusive with {@link #message}.
         * @see MessageBundle
         */
        String messageKey() default "";

        /**
         * The name of the message bundle in which to look up the error message. Requires {@link #messageKey} to be set.
         * @see MessageBundle
         */
        String bundleName() default "";

        /**
         * An array of message arguments, which will be used for the message obtained from {@link #message} or 
         * {@link #messageKey}, whichever is specified.
         */
        MessageArg[] messageArgs() default {};
    }

    /**
     * A validation rule that will fail if it is applied to a property that has a non-empty value which is not a valid
     * credit card number.  Note that this performs checks against the string only; it does not consult any sort of
     * service to verify the actual credit card number.
     * Used within {@link ValidatableProperty} and {@link ValidationLocaleRules}.
     */
    @Target( ANNOTATION_TYPE )
    @Retention( RUNTIME )
    public @interface ValidateCreditCard
    {
        /**
         * If set to <code>false</code>, then this rule will not be applied.
         */
        boolean enabled() default true;

        /**
         * The JSP 2.0-style expression (e.g., <code>${pageFlow.myProperty}</code>) or literal string that will be used
         * as the error message.  Mutually-exclusive with {@link #messageKey}.
         */
        String message() default "";

        /**
         * A key in the default message bundle or in the bundle specified by {@link #bundleName} that will be
         * used to look up the error message.  Mutually-exclusive with {@link #message}.
         * @see MessageBundle
         */
        String messageKey() default "";

        /**
         * The name of the message bundle in which to look up the error message. Requires {@link #messageKey} to be set.
         * @see MessageBundle
         */
        String bundleName() default "";

        /**
         * An array of message arguments, which will be used for the message obtained from {@link #message} or 
         * {@link #messageKey}, whichever is specified.
         */
        MessageArg[] messageArgs() default {};
    }

    /**
     * A validation rule that will fail if it is applied to a property that has a non-empty value which is not a valid
     * email address.  Note that this performs checks against the string only; it does not consult any sort of service
     * to verify the actual address.
     * Used within {@link ValidatableProperty} and {@link ValidationLocaleRules}.
     */
    @Target( ANNOTATION_TYPE )
    @Retention( RUNTIME )
    public @interface ValidateEmail
    {
        /**
         * If set to <code>false</code>, then this rule will not be applied.
         */
        boolean enabled() default true;

        /**
         * The JSP 2.0-style expression (e.g., <code>${pageFlow.myProperty}</code>) or literal string that will be used
         * as the error message.  Mutually-exclusive with {@link #messageKey}.
         */
        String message() default "";

        /**
         * A key in the default message bundle or in the bundle specified by {@link #bundleName} that will be
         * used to look up the error message.  Mutually-exclusive with {@link #message}.
         * @see MessageBundle
         */
        String messageKey() default "";

        /**
         * The name of the message bundle in which to look up the error message. Requires {@link #messageKey} to be set.
         * @see MessageBundle
         */
        String bundleName() default "";

        /**
         * An array of message arguments, which will be used for the message obtained from {@link #message} or 
         * {@link #messageKey}, whichever is specified.
         */
        MessageArg[] messageArgs() default {};
    }

    /**
     * A validation rule that will fail when it is applied to a property that has a non-empty value, and when a given
     * expression does not evaluate to <code>true</code>.
     * Used within {@link ValidatableProperty} and {@link ValidationLocaleRules}.
     */
    @Target( ANNOTATION_TYPE )
    @Retention( RUNTIME )
    public @interface ValidateValidWhen
    {
        /**
         * If set to <code>false</code>, then this rule will not be applied.
         */
        boolean enabled() default true;

        /**
         * The JSP 2.0-style expression (e.g., <code>${actionForm.someProperty==pageFlow.anotherProperty}</code>) that
         * must evaluate to <code>true</code>.
         */
        String condition(); // required

        /**
         * The JSP 2.0-style expression (e.g., <code>${pageFlow.myProperty}</code>) or literal string that will be used
         * as the error message.  Mutually-exclusive with {@link #messageKey}.
         */
        String message() default "";

        /**
         * A key in the default message bundle or in the bundle specified by {@link #bundleName} that will be
         * used to look up the error message.  Mutually-exclusive with {@link #message}.
         * @see MessageBundle
         */
        String messageKey() default "";

        /**
         * The name of the message bundle in which to look up the error message. Requires {@link #messageKey} to be set.
         * @see MessageBundle
         */
        String bundleName() default "";

        /**
         * An array of message arguments, which will be used for the message obtained from {@link #message} or 
         * {@link #messageKey}, whichever is specified.
         */
        MessageArg[] messageArgs() default {};
    }

    /**
     * A validation rule that will fail when it is applied to a property that has a non-empty value that is an invalid
     * URL.
     * Used within {@link ValidatableProperty} and {@link ValidationLocaleRules}.
     */
    @Target( ANNOTATION_TYPE )
    @Retention( RUNTIME )
    public @interface ValidateURL
    {
        /**
         * If set to <code>false</code>, then this rule will not be applied.
         */
        boolean enabled() default true;

        /**
         * If <code>true</code>, then all schemes are allowed.  Mutually-exclusive with {@link #schemes}.
         */
        boolean allowAllSchemes() default false;

        /**
         * An array of schemes that are allowed in the URL.  Defaults to <code>{"http", "https", "ftp"}</code>.
         * Mutually-exclusive with {@link #allowAllSchemes}.
         */
        String[] schemes() default {"http", "https", "ftp"};

        /**
         * If <code>true</code>, then double '/' characters are allowed in the URL.
         */
        boolean allowTwoSlashes() default false;

        /**
         * If <code>true</code>, then fragments are <strong>not</strong> allowed.
         */
        boolean disallowFragments() default false;
        
        /**
         * The JSP 2.0-style expression (e.g., <code>${pageFlow.myProperty}</code>) or literal string that will be used
         * as the error message.  Mutually-exclusive with {@link #messageKey}.
         */
        String message() default "";

        /**
         * A key in the default message bundle or in the bundle specified by {@link #bundleName} that will be
         * used to look up the error message.  Mutually-exclusive with {@link #message}.
         * @see MessageBundle
         */
        String messageKey() default "";

        /**
         * The name of the message bundle in which to look up the error message. Requires {@link #messageKey} to be set.
         * @see MessageBundle
         */
        String bundleName() default "";

        /**
         * An array of message arguments, which will be used for the message obtained from {@link #message} or 
         * {@link #messageKey}, whichever is specified.
         */
        MessageArg[] messageArgs() default {};
    }

    /**
     * A variable name/value that is used by {@link ValidateCustomRule}.
     */
    @Target( ANNOTATION_TYPE )
    @Retention( RUNTIME )
    public @interface ValidateCustomVariable
    {
        /**
         * The variable name.
         */
        String name();

        /**
         * The variable value.
         */
        String value();
    }
    
    /**
     * A validation rule that will fail when a given custom ValidatorPlugIn rule fails.
     * Used within {@link ValidatableProperty} and {@link ValidationLocaleRules}.
     */
    @Target( ANNOTATION_TYPE )
    @Retention( RUNTIME )
    public @interface ValidateCustomRule
    {
        /**
         * The name of the custom rule to run.  This rule may be specified in a ValidatorPlugIn config that is declared
         * with {@link Controller#customValidatorConfigs}.
         */
        String rule();

        /**
         * An array of variables that will be passed to the custom rule.
         */
        ValidateCustomVariable[] variables() default {};

        /**
         * The JSP 2.0-style expression (e.g., <code>${pageFlow.myProperty}</code>) or literal string that will be used
         * as the error message.  Mutually-exclusive with {@link #messageKey}.
         */
        String message() default "";

        /**
         * A key in the default message bundle or in the bundle specified by {@link #bundleName} that will be
         * used to look up the error message.  Mutually-exclusive with {@link #message}.
         * @see MessageBundle
         */
        String messageKey() default "";

        /**
         * The name of the message bundle in which to look up the error message. Requires {@link #messageKey} to be set.
         * @see MessageBundle
         */
        String bundleName() default "";

        /**
         * An array of message arguments, which will be used for the message obtained from {@link #message} or 
         * {@link #messageKey}, whichever is specified.
         */
        MessageArg[] messageArgs() default {};
    }

    /**
     * A set of validation rules that will be applied for a particular locale.  Used within a
     * {@link ValidatableProperty}.
     */
    @Target( ANNOTATION_TYPE )
    @Retention( RUNTIME )
    public @interface ValidationLocaleRules
    {
        /** A {@link ValidateRequired} rule that will be applied for the given locale. */
        ValidateRequired validateRequired() default @ValidateRequired(enabled=false);

        /** A {@link ValidateMinLength} rule that will be applied for the given locale. */
        ValidateMinLength validateMinLength() default @ValidateMinLength(enabled=false, chars=-1);

        /** A {@link ValidateMaxLength} rule that will be applied for the given locale. */
        ValidateMaxLength validateMaxLength() default @ValidateMaxLength(enabled=false, chars=-1);

        /** A {@link ValidateMask} rule that will be applied for the given locale. */
        ValidateMask validateMask() default @ValidateMask(enabled=false, regex="");

        /** A {@link ValidateType} rule that will be applied for the given locale. */
        ValidateType validateType() default @ValidateType(enabled=false, type=void.class);

        /** A {@link ValidateDate} rule that will be applied for the given locale. */
        ValidateDate validateDate() default @ValidateDate(enabled=false, pattern="");

        /** A {@link ValidateRange} rule that will be applied for the given locale. */
        ValidateRange validateRange() default @ValidateRange(enabled=false);

        /** A {@link ValidateCreditCard} rule that will be applied for the given locale. */
        ValidateCreditCard validateCreditCard() default @ValidateCreditCard(enabled=false);

        /** A {@link ValidateEmail} rule that will be applied for the given locale. */
        ValidateEmail validateEmail() default @ValidateEmail(enabled=false);

        /** A {@link ValidateValidWhen} rule that will be applied for the given locale. */
        ValidateValidWhen validateValidWhen() default @ValidateValidWhen(enabled=false, condition="");
        
        /** A {@link ValidateURL} rule that will be applied for the given locale. */
        ValidateURL validateURL() default @ValidateURL(enabled=false);

        /** A {@link ValidateCustomRule} rule that will be applied for the given locale. */
        ValidateCustomRule[] validateCustomRules() default {};
        
        /**
         * The language of the locale for which to apply the rules.  Mutually-exclusive with
         * {@link #applyToUnhandledLocales}.
         */
        String language() default "";
        
        /**
         * The country of the locale for which to apply the rules.  Requires {@link #language}.
         */
        String country() default "";
        
        /**
         * The variant of the locale for which to apply the rules.  Requires {@link #language}.
         */
        String variant() default "";

        /**
         * If set to <code>true</code>, then these rules will be run only for a locale that has <i>no rules</i> defined
         * for it specifically.  Mutually-exclusive with {@link #language}.
         */
        boolean applyToUnhandledLocales() default false;
    }

    /**
     * A set of validation rules that will be applied against a property.  Used directly on a property getter method,
     * or within a {@link Action}, {@link SimpleAction}, or {@link ValidatableBean} annotation.  Contains rules to be
     * applied for every locale, and sets of locale-specific rules.
     */
    @Target( { ANNOTATION_TYPE, METHOD } )
    @Retention( RUNTIME )
    public @interface ValidatableProperty
    {
        /**
         * The name of the property to run rules against.  When this annotation is used on a property getter method,
         * <code>propertyName</code> is illegal because the property name is inferred from the method name.
         */
        String propertyName() default "";

        /**
         * The JSP 2.0-style expression (e.g., <code>${bundle.default.someMessageResource}</code>) or literal string
         * that will be used as the first argument to all error messages for this property.  When this is specified,
         * the individual rules can avoid providing specific messages; instead, a default message will be used.
         * Mutually-exclusive with {@link #displayNameKey}.
         */
        String displayName() default "";

        /**
         * A key in the default message bundle or in the bundle specified by {@link MessageBundle#bundleName} that will be
         * used as the first argument to all error messages for this property.  When this is specified,
         * the individual rules can avoid providing specific messages; instead, a default message will be used.
         * Mutually-exclusive with {@link #displayName}.
         * @see MessageBundle
         */
        String displayNameKey() default "";

        /** A {@link ValidateRequired} rule that will be applied for all locales. */
        ValidateRequired validateRequired() default @ValidateRequired(enabled=false);

        /** A {@link ValidateMinLength} rule that will be applied for all locales. */
        ValidateMinLength validateMinLength() default @ValidateMinLength(enabled=false, chars=-1);

        /** A {@link ValidateMaxLength} rule that will be applied for all locales. */
        ValidateMaxLength validateMaxLength() default @ValidateMaxLength(enabled=false, chars=-1);

        /** A {@link ValidateMask} rule that will be applied for all locales. */
        ValidateMask validateMask() default @ValidateMask(enabled=false, regex="");

        /** A {@link ValidateType} rule that will be applied for all locales. */
        ValidateType validateType() default @ValidateType(enabled=false, type=void.class);

        /** A {@link ValidateDate} rule that will be applied for all locales. */
        ValidateDate validateDate() default @ValidateDate(enabled=false, pattern="");

        /** A {@link ValidateRange} rule that will be applied for all locales. */
        ValidateRange validateRange() default @ValidateRange(enabled=false);

        /** A {@link ValidateCreditCard} rule that will be applied for all locales. */
        ValidateCreditCard validateCreditCard() default @ValidateCreditCard(enabled=false);

        /** A {@link ValidateEmail} rule that will be applied for all locales. */
        ValidateEmail validateEmail() default @ValidateEmail(enabled=false);

        /** A {@link ValidateValidWhen} rule that will be applied for all locales. */
        ValidateValidWhen validateValidWhen() default @ValidateValidWhen(enabled=false, condition="");

        /** A {@link ValidateURL} rule that will be applied for all locales. */
        ValidateURL validateURL() default @ValidateURL(enabled=false);

        /** A {@link ValidateCustomRule} rule that will be applied for all locales. */
        ValidateCustomRule[] validateCustomRules() default {};

        /**
         * An array of sets of locale-specific validation rules.
         */
        ValidationLocaleRules[] localeRules() default {};
    }
    
    /**
     * A set of validatable property definitions that will be applied against particular bean type.  Used in
     * {@link Controller#validatableBeans}.
     */
    @Target( ANNOTATION_TYPE )
    @Retention( RUNTIME )
    public @interface ValidatableBean
    {
        /**
         * The bean type for which the validation rules will apply.
         */
        Class type();

        /**
         * An array of properties on which to run validation rules.
         */
        ValidatableProperty[] validatableProperties();
    }
    
    /**
     * An optional class-level annotation that can be used on form bean classes with validation rules.  It allows the
     * form bean class to define its own message bundle that will be used when a message is not found in the default
     * message bundle for the page flow in which a validation error message is generated.
     */
    @Target( TYPE )
    @Retention( RUNTIME )
    @Inherited
    public @interface FormBean
    {
        /**
         * The path to the message bundle, as a ServletContext resource.  This may be specified with either '/' or'.'
         * as the separator, e.g., <code>bundlePath="foo.bar.MyMessages"</code> or
         * <code>bundlePath="foo/bar/MyMessages"</code> (in both cases, foo/bar/MyMessages.properties would be found
         * on classpath).
         */
        String messageBundle() default "";
    }

    /**
     * An annotation that causes a field to get automatically initialized with a reference to a
     * {@link org.apache.beehive.netui.pageflow.SharedFlowController}.  The annotation is valid within a page flow
     * ({@link org.apache.beehive.netui.pageflow.PageFlowController} or a JavaServer Faces backing bean
     * {@link org.apache.beehive.netui.pageflow.FacesBackingBean}.
     */
    @Target( FIELD )
    @Retention( RUNTIME )
    public @interface SharedFlowField
    {
        /**
         * The name of the shared flow, as declared in {@link SharedFlowRef#name} on {@link Controller}. 
         */ 
        String name();
    }

    /**
     * An annotation that causes a field to get automatically initialized with a reference to the current
     * {@link org.apache.beehive.netui.pageflow.PageFlowController}.  The annotation is valid within a JavaServer Faces
     * backing class.
     */
    @Target( FIELD )
    @Retention( RUNTIME )
    public @interface PageFlowField
    {
    }
    
    /**
     * A class-level annotation that denotes a JavaServer Faces backing bean.  An instance of this class will be
     * created whenever a corresponding JSF path is requested (e.g., an instance of foo.MyPage will be created for the
     * webapp-relative path "/foo/MyPage.faces").  The instance will be released (removed from the user session) when
     * a non-matching path is requested.  Faces backing beans can hold component references and event/command handlers.
     * The bean instance can be databound to with a JSF-style expression like <code>#{backing.myComponent}</code>.
     * 
     * @see CommandHandler
     */
    @Target( TYPE )
    @Retention( RUNTIME )
    public @interface FacesBacking
    {
    }
    
    /**
     * Method-level annotation that configures a JavaServerFaces command handler which intends to raise Page Flow
     * actions.  Valid inside a JSF backing bean.  The method signature is the standard one for a JSF command handler:
     * <blockquote>
     *     <code>public String myCommandHandler()</code>
     * </blockquote>
     * where the <code>String</code> returned is the name of a Page Flow action.
     *
     * @see FacesBacking
     */
    @Target( METHOD )
    @Retention( RUNTIME )
    public @interface CommandHandler
    {
        /**
         * An array of RaiseAction annotations, which cause form beans to be sent when particular Page Flow actions
         * are raised.
         */
        RaiseAction[] raiseActions() default {};
    }
    
    /**
     * An annotation used within {@link CommandHandler} to specify that a form bean should be sent when a particular
     * Page Flow action is raised.
     */
    @Target( ANNOTATION_TYPE )
    @Retention( RUNTIME )
    public @interface RaiseAction
    {
        /**
         * The name of the Page Flow action.
         */
        String action();

        /**
         * The name of a member variable that will be sent when the action specified with {@link #action} is raised.
         */
        String outputFormBean() default "";
    }
}
