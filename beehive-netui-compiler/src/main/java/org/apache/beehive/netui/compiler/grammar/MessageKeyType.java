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

import org.apache.beehive.netui.compiler.AnnotationMemberType;
import org.apache.beehive.netui.compiler.AnnotationGrammar;
import org.apache.beehive.netui.compiler.typesystem.declaration.AnnotationValue;
import org.apache.beehive.netui.compiler.typesystem.declaration.AnnotationInstance;
import org.apache.beehive.netui.compiler.typesystem.declaration.MemberDeclaration;
import org.apache.beehive.netui.compiler.typesystem.declaration.AnnotationTypeElementDeclaration;



public class MessageKeyType
        extends AnnotationMemberType
{
    public MessageKeyType( String requiredRuntimeVersion, AnnotationGrammar parentGrammar )
    {
        super( requiredRuntimeVersion, parentGrammar );
    }
    
    
    public Object onCheck( AnnotationTypeElementDeclaration valueDecl, AnnotationValue member,
                           AnnotationInstance[] parentAnnotations, MemberDeclaration classMember,
                           int annotationArrayIndex )
    {
        if ( ( ( String ) member.getValue() ).length() == 0 )
        {
            addError( member, "error.empty-string-not-allowed" );
        }
        // We're not currently supporting generation of validation messages.
        /*
        //
        // If there is a list of (generated) validation messages, and if the current message key isn't in it,
        // output a warning.
        //
        TypeDeclaration outerType = CompilerUtils.getOutermostClass( classMember );
        Collection validationMessages = 
                CompilerUtils.getAnnotationArrayValue( outerType, CONTROLLER_TAG_NAME, VALIDATION_MESSAGES_ATTR, true );
        
        if ( validationMessages != null )
        {
            String value = ( String ) member.getValue();
            
            for ( java.util.Iterator ii = validationMessages.iterator(); ii.hasNext(); )  
            {
                AnnotationInstance validationMessage = ( AnnotationInstance ) ii.next();
                String msgKey = CompilerUtils.getString( validationMessage, KEY_ATTR, true );
                assert msgKey != null;
                if ( value.equals( msgKey ) ) return null;  // ok, we found it.
            }
        
            addWarning( member, "warning.missing-validation-message", VALIDATION_MESSAGE_TAG_NAME, value );
        }
        */
        
        return null;
    }
}
