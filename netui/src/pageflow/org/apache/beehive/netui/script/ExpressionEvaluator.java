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
package org.apache.beehive.netui.script;

import javax.servlet.jsp.el.VariableResolver;

/**
 * Interface for implementing a runtime for evaluating expressions.  An ExpressionEvaluator is
 * used to execute <i>read</i> and <i>update</i> expressions.  It also provides a set of methods
 * for performing utility type operations on expressions.
 */
public interface ExpressionEvaluator {

    /**
     * Evaluate an expression and return the result.
     *
     * @param expression       the expression to evaluate
     * @param variableResolver the set of contexts that may be used in expression evaluation.  This set
     *                         is not necessarily complete as some objects that can be used as top-level expression contexts
     *                         may be contained within an object available in this JavaBean.
     * @throws ExpressionEvaluationException when an error occurs
     */
    public Object evaluateStrict(String expression, VariableResolver variableResolver)
        throws ExpressionEvaluationException;

    /**
     * Update an expression with the given value.  This will apply the parameter <code>value</code>
     * to the object referenced by the expression <code>expression</code>.  The <code>requestParameter</code>
     * flag is used by a caller to restrict the set of contexts into which an update can occur on
     * the request.
     *
     * @param expression       the expression whose value to update
     * @param value            the new value for the update
     * @param variableResolver the set of contexts that may be used in expression evaluation.  This set
     *                         is not necessarily complete as some objects that can be used as top-level expression contexts
     *                         may be contained within an object available in this JavaBean.
     * @param requestParameter a boolean that marks this update as occurring from data in the request, if
     *                         <code>true</code> or simply as a regular update.
     * @throws ExpressionUpdateException when an exception occurs updating a value
     */
    public void update(String expression, Object value, VariableResolver variableResolver, boolean requestParameter)
        throws ExpressionUpdateException;

    /**
     * Change the evaluation context of an expression.  This is used to rewrite some expressions
     * that need to be qualified into a different context for evaluation.  The context
     * of an expression is its first identifier, up to the first delimiter.
     *
     * @param expression  the expression whose context to change
     * @param oldContext  the old context to replace, if present
     * @param newContext  the new context to replace if the expression starts with the <code>oldContext</code>
     * @param lookupIndex an index used to qualify an expression into an array look-up.
     */
    public String changeContext(String expression, String oldContext, String newContext, int lookupIndex)
        throws ExpressionEvaluationException;

    /**
     * Qualify the expression into the given context.  This will take the <code>expression</code>
     * and simply qualify it into the new context <code>implicitObjectName</code>.
     *
     * @param implicitObjectName the new context
     * @param expression  the expression to qualify
     * @return the new expression created by adding the implicit object name <code>implicitObjectName</code>
     *         to the expression string <code>expression</code>
     * @throws ExpressionEvaluationException if an exception occurs qualifying the expression with the implict object name
     */
    public String qualify(String implicitObjectName, String expression)
        throws ExpressionEvaluationException;

    /**
     * Checks to see if a particular String is exactly an expression.
     *
     * @param expression the expression to check
     * @return <code>true</code> if the expression is exactly an expression; <code>false</code> otherwise.
     * @throws IllegalExpressionException if the given expression <code>expression</code> is not legal.
     */
    public boolean isExpression(String expression);

    /**
     * Checks to see if a particular expression contains an expression.  This method will return
     * <code>true</code> if there is an expression surrounded by whitespace, other expressions,
     * or literal text.
     *
     * @param expression the expression to check
     * @return <code>true</code> if the expression contains an expression; <code>false</code> otherwise.
     */
    public boolean containsExpression(String expression);

    /**
     * Parse an expression into its object representation as a {@link Expression}.
     *
     * @param expression the String expression to parse
     * @return the parsed expression
     */
    public Expression parseExpression(String expression);
}