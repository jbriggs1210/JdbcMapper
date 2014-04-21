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
package org.apache.beehive.netui.compiler.grammar;

import org.apache.beehive.netui.compiler.typesystem.env.CoreAnnotationProcessorEnv;
import org.apache.beehive.netui.compiler.Diagnostics;
import org.apache.beehive.netui.compiler.RuntimeVersionChecker;


public class ValidateValidWhenGrammar
        extends BaseValidationRuleGrammar
{
    private static final String[][] REQUIRED_ATTRS = { { CONDITION_ATTR }, { MESSAGE_ATTR, MESSAGE_KEY_ATTR } };
    
    public ValidateValidWhenGrammar( CoreAnnotationProcessorEnv env, Diagnostics diagnostics,
                                     RuntimeVersionChecker rvc )
    {
        super( env, diagnostics, rvc );
    }

    public String[][] getRequiredAttrs()
    {
        return REQUIRED_ATTRS;
    }
}
