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

import org.apache.beehive.netui.compiler.AnnotationGrammar;
import org.apache.beehive.netui.compiler.Diagnostics;
import org.apache.beehive.netui.compiler.RuntimeVersionChecker;
import org.apache.beehive.netui.compiler.typesystem.env.CoreAnnotationProcessorEnv;

public abstract class ValidationRulesContainerGrammar
        extends AnnotationGrammar
{
    protected ValidationRulesContainerGrammar( CoreAnnotationProcessorEnv env, Diagnostics diags,
                                               RuntimeVersionChecker rvc )
    {
        super( env, diags, VERSION_9_0_STRING, rvc );
        
        addMemberGrammar( VALIDATE_REQUIRED_ATTR, new ValidateRequiredGrammar( env, diags, rvc ) );
        addMemberGrammar( VALIDATE_RANGE_ATTR, new ValidateRangeGrammar( env, diags, rvc ) );
        addMemberGrammar( VALIDATE_MIN_LENGTH_ATTR,
                          new BaseValidationRuleGrammar( env, diags, rvc, new String[][]{ { CHARS_ATTR } } ) );
        addMemberGrammar( VALIDATE_MAX_LENGTH_ATTR,
                          new BaseValidationRuleGrammar( env, diags, rvc, new String[][]{ { CHARS_ATTR } } ) );
        addMemberGrammar( VALIDATE_CREDIT_CARD_ATTR, new BaseValidationRuleGrammar( env, diags, rvc ) );
        addMemberGrammar( VALIDATE_EMAIL_ATTR, new BaseValidationRuleGrammar( env, diags, rvc ) );
        addMemberGrammar( VALIDATE_MASK_ATTR,
                          new BaseValidationRuleGrammar( env, diags, rvc, new String[][]{ { REGEX_ATTR } } ) );
        addMemberGrammar( VALIDATE_DATE_ATTR,
                          new BaseValidationRuleGrammar( env, diags, rvc, new String[][]{ { PATTERN_ATTR } } ) );
        addMemberGrammar( VALIDATE_TYPE_ATTR, new ValidateTypeGrammar( env, diags, rvc ) );
        addMemberGrammar( VALIDATE_VALID_WHEN_ATTR, new ValidateValidWhenGrammar( env, diags, rvc ) );
        addMemberGrammar( VALIDATE_URL_ATTR, new ValidateURLGrammar( env, diags, rvc ) );
        addMemberArrayGrammar( VALIDATE_CUSTOM_ATTR, new ValidateCustomGrammar( env, diags, rvc ) );
    }
}
