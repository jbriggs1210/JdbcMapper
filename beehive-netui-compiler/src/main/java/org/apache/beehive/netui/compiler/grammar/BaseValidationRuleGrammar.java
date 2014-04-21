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
import org.apache.beehive.netui.compiler.FatalCompileTimeException;
import org.apache.beehive.netui.compiler.typesystem.declaration.AnnotationInstance;
import org.apache.beehive.netui.compiler.typesystem.declaration.MemberDeclaration;
import org.apache.beehive.netui.compiler.typesystem.env.CoreAnnotationProcessorEnv;

import java.util.List;
import java.util.Iterator;


public class BaseValidationRuleGrammar
        extends AnnotationGrammar
{
    private static final String[][] MUTUALLY_EXCLUSIVE_ATTRS =
            {
                { MESSAGE_KEY_ATTR, MESSAGE_ATTR }
            };

    private static final String[][] ATTR_DEPENDENCIES =
            {
                { BUNDLE_NAME_ATTR, MESSAGE_KEY_ATTR }
            };

    private String[][] _requiredAttrs = null;

    public BaseValidationRuleGrammar( CoreAnnotationProcessorEnv env, Diagnostics diags,
                                      RuntimeVersionChecker rvc )
    {
        super( env, diags, VERSION_9_0_STRING, rvc );

        addMemberType( MESSAGE_KEY_ATTR, new MessageKeyType( null, this ) );
        addMemberArrayGrammar( MESSAGE_ARGS_ATTR, new ValidationMessageArgsGrammar( env, diags, rvc ) );
        addMemberType( BUNDLE_NAME_ATTR, new BundleNameType( null, this ) );
    }

    public BaseValidationRuleGrammar( CoreAnnotationProcessorEnv env, Diagnostics diags,
                                      RuntimeVersionChecker rvc, String[][] requiredAttrs )
    {
        this( env, diags, rvc );

        _requiredAttrs = requiredAttrs;
    }

    public String[][] getRequiredAttrs()
    {
        return _requiredAttrs;
    }

    public String[][] getMutuallyExclusiveAttrs()
    {
        return MUTUALLY_EXCLUSIVE_ATTRS;
    }

    public String[][] getAttrDependencies()
    {
        return ATTR_DEPENDENCIES;
    }


    protected boolean onBeginCheck( AnnotationInstance annotation, AnnotationInstance[] parentAnnotations,
                                    MemberDeclaration classMember )
            throws FatalCompileTimeException
    {
        //
        // Check to make sure that either the parent ValidatableProperty annotation has a displayName property,
        // or this rule specifies a first argument to the default message, or this rule specifies its own message.
        // If none of these are true, output a warning.
        //
        assert parentAnnotations.length > 0;
        AnnotationInstance immediateParent = parentAnnotations[parentAnnotations.length - 1];

        if ( CompilerUtils.getString( immediateParent, DISPLAY_NAME_ATTR, true ) == null
             && CompilerUtils.getString( immediateParent, DISPLAY_NAME_KEY_ATTR, true ) == null
             && CompilerUtils.getString( annotation, MESSAGE_KEY_ATTR, true ) == null
             && CompilerUtils.getString( annotation, MESSAGE_ATTR, true ) == null )
        {
            boolean useDefaultDisplayName = true;
            List messageArgs =
                    CompilerUtils.getAnnotationArray( annotation, MESSAGE_ARGS_ATTR, true );

            if ( messageArgs != null )
            {
                boolean firstArg = true;

                for ( Iterator ii = messageArgs.iterator(); ii.hasNext(); )
                {
                    AnnotationInstance messageArg = ( AnnotationInstance ) ii.next();
                    Integer position = CompilerUtils.getInteger( messageArg, POSITION_ATTR, true );

                    if ( ( position == null && firstArg ) || ( position != null && position.intValue() == 0 ) )
                    {
                        useDefaultDisplayName = false;
                        break;
                    }

                    firstArg = false;
                }
            }

            if ( useDefaultDisplayName )
            {
                addWarning( annotation, "warning.using-default-display-name",
                            CompilerUtils.getDeclaration( immediateParent.getAnnotationType() ).getSimpleName() );
            }
        }

        return super.onBeginCheck( annotation, parentAnnotations, classMember );
    }
}
