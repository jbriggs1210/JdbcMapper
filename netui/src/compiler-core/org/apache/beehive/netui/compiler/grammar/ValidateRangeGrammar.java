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

import org.apache.beehive.netui.compiler.RuntimeVersionChecker;
import org.apache.beehive.netui.compiler.Diagnostics;
import org.apache.beehive.netui.compiler.AnnotationMemberType;
import org.apache.beehive.netui.compiler.typesystem.env.CoreAnnotationProcessorEnv;
import org.apache.beehive.netui.compiler.typesystem.declaration.AnnotationTypeElementDeclaration;
import org.apache.beehive.netui.compiler.typesystem.declaration.AnnotationValue;
import org.apache.beehive.netui.compiler.typesystem.declaration.AnnotationInstance;
import org.apache.beehive.netui.compiler.typesystem.declaration.MemberDeclaration;


public class ValidateRangeGrammar
        extends BaseValidationRuleGrammar
{
    private static String[][] MUTUALLY_EXCLUSIVE_ATTRS =
            { { MIN_INT_ATTR, MIN_FLOAT_ATTR }, { MAX_INT_ATTR, MAX_FLOAT_ATTR } };
    
    private static String[][] ATTR_DEPENDENCIES = 
            {
                { MIN_INT_ATTR, MAX_INT_ATTR },
                { MIN_FLOAT_ATTR, MAX_FLOAT_ATTR },
                { MAX_INT_ATTR, MIN_INT_ATTR },
                { MAX_FLOAT_ATTR, MIN_FLOAT_ATTR }
            };
    
    public ValidateRangeGrammar( CoreAnnotationProcessorEnv env, Diagnostics diags,
                                 RuntimeVersionChecker runtimeVersionChecker )
    {
        super( env, diags, runtimeVersionChecker );
        
        // The annotation defines these as doubles to avoid forcing us to type 'f' after every initialization value.
        addMemberType( MIN_FLOAT_ATTR, new FloatType() );
        addMemberType( MAX_FLOAT_ATTR, new FloatType() );
        
        // no custom types needed for minInt, maxInt
    }

    public String[][] getMutuallyExclusiveAttrs()
    {
        return MUTUALLY_EXCLUSIVE_ATTRS;
    }

    public String[][] getRequiredAttrs()
    {
        return null;
    }
    
    public String[][] getAttrDependencies()
    {
        return ATTR_DEPENDENCIES;
    }
    
    private class FloatType
            extends AnnotationMemberType
    {
        public FloatType()
        {
            super( ValidateRangeGrammar.this.getRequiredRuntimeVersion(), ValidateRangeGrammar.this );
        }

        
        public Object onCheck( AnnotationTypeElementDeclaration valueDecl, AnnotationValue member,
                               AnnotationInstance[] parentAnnotations, MemberDeclaration classMember,
                               int annotationArrayIndex )
        {
            double d = ( ( Double ) member.getValue() ).doubleValue();
            
            if ( d < -Float.MAX_VALUE ) addError( member, "error.min-float", new Double( -Float.MAX_VALUE ) );
            else if ( d > Float.MAX_VALUE ) addError( member, "error.max-float", new Double( Float.MAX_VALUE ) );
            
            return null;
        }
    }
}
