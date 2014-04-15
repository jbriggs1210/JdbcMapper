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
import org.apache.beehive.netui.compiler.CompilerUtils;
import org.apache.beehive.netui.compiler.Diagnostics;
import org.apache.beehive.netui.compiler.RuntimeVersionChecker;
import org.apache.beehive.netui.compiler.typesystem.env.CoreAnnotationProcessorEnv;
import org.apache.beehive.netui.compiler.typesystem.declaration.AnnotationInstance;
import org.apache.beehive.netui.compiler.typesystem.declaration.MemberDeclaration;

import java.util.Map;


public class ValidationMessageArgsGrammar
        extends AnnotationGrammar
{
    private static final String[][] MUTUALLY_EXCLUSIVE_ATTRS =
            {
                { ARG_KEY_ATTR, ARG_ATTR }
            };

    private static final String[][] REQUIRED_ATTRS =
            {
                { ARG_KEY_ATTR, ARG_ATTR },
            };

    private static final String[][] ATTR_DEPENDENCIES =
            {
                { BUNDLE_NAME_ATTR, ARG_KEY_ATTR }
            };


    public ValidationMessageArgsGrammar( CoreAnnotationProcessorEnv env, Diagnostics diags,
                                         RuntimeVersionChecker rvc )
    {
        super( env, diags, VERSION_9_0_STRING, rvc );

        // ARG_KEY_ATTR, ARG_ATTR do not need a custom type.
        addMemberType( POSITION_ATTR, new UniqueValueType( MESSAGE_ARGS_ATTR, false, true, null, this ) );
        addMemberType( BUNDLE_NAME_ATTR, new BundleNameType( null, this ) );
    }

    
    public String[][] getMutuallyExclusiveAttrs()
    {
        return MUTUALLY_EXCLUSIVE_ATTRS;
    }

    
    public String[][] getRequiredAttrs()
    {
        return REQUIRED_ATTRS;
    }

    
    public String[][] getAttrDependencies()
    {
        return ATTR_DEPENDENCIES;
    }

    
    protected Object onEndCheck( AnnotationInstance annotation, AnnotationInstance[] parentAnnotations,
                                 MemberDeclaration classMember, Map checkResults )
    {
        Integer position = CompilerUtils.getInteger( annotation, POSITION_ATTR, true );

        if ( position == null )
        {
            //
            // Note, GenValidationModel.addMessageArgs() infers the position for
            // a null position attribute from the postion of the arg in the array.
            //
        }
        else if ( position.intValue() < 0 || position.intValue() > 3 )
        {
            addError( annotation, "error.integer-attribute-not-in-range", POSITION_ATTR,
                      new Integer( 0 ), new Integer( 3 ) );
        }

        return null;
    }
}
