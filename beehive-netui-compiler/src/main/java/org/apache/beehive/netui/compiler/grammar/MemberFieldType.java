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
import org.apache.beehive.netui.compiler.CompilerUtils;
import org.apache.beehive.netui.compiler.AnnotationGrammar;
import org.apache.beehive.netui.compiler.typesystem.declaration.AnnotationValue;
import org.apache.beehive.netui.compiler.typesystem.declaration.AnnotationInstance;
import org.apache.beehive.netui.compiler.typesystem.declaration.MemberDeclaration;
import org.apache.beehive.netui.compiler.typesystem.declaration.FieldDeclaration;
import org.apache.beehive.netui.compiler.typesystem.declaration.AnnotationTypeElementDeclaration;
import org.apache.beehive.netui.compiler.typesystem.type.TypeInstance;

import java.util.Collection;
import java.util.Iterator;


public class MemberFieldType
        extends AnnotationMemberType
{
    private String _requiredSuperclassName;


    public MemberFieldType( String requiredSuperclassName, String requiredRuntimeVersion,
                            AnnotationGrammar parentGrammar )
    {
        super( requiredRuntimeVersion, parentGrammar );
        _requiredSuperclassName = requiredSuperclassName;
    }

    
    public Object onCheck( AnnotationTypeElementDeclaration valueDecl, AnnotationValue member,
                           AnnotationInstance[] parentAnnotations, MemberDeclaration classMember,
                           int annotationArrayIndex )
    {
        String fieldName = ( String ) member.getValue();        
        Collection fields =
                CompilerUtils.getClassFields( CompilerUtils.getOuterClass( classMember ) );
        
        for ( Iterator ii = fields.iterator(); ii.hasNext(); )  
        {
            FieldDeclaration field = ( FieldDeclaration ) ii.next();
            if ( field.getSimpleName().equals( fieldName ) )
            {
                TypeInstance fieldType = CompilerUtils.getGenericBoundsType( field.getType() );
                
                if ( _requiredSuperclassName != null
                     && ! CompilerUtils.isAssignableFrom( _requiredSuperclassName, fieldType, getEnv() ) )
                {
                    addError( member, "error.wrong-field-type", new Object[]{ fieldName, _requiredSuperclassName } );
                    return null;
                }
                
                return field;
            }
        }
        
        addError( member, "error.unresolved-field", fieldName );
        return null;
    }
}
