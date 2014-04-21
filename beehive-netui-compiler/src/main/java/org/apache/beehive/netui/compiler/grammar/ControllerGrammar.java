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
import org.apache.beehive.netui.compiler.AnnotationMemberType;
import org.apache.beehive.netui.compiler.FlowControllerInfo;
import org.apache.beehive.netui.compiler.Diagnostics;
import org.apache.beehive.netui.compiler.typesystem.env.CoreAnnotationProcessorEnv;


public class ControllerGrammar
        extends BaseFlowControllerGrammar
{
    public ControllerGrammar( CoreAnnotationProcessorEnv env, Diagnostics diags, RuntimeVersionChecker rvc,
                              FlowControllerInfo fcInfo )
    {
        super( env, diags, null, rvc, fcInfo );

        addMemberType( LOGIN_REQUIRED_ATTR, new AnnotationMemberType( null, this ) );
        addMemberType( ROLES_ALLOWED_ATTR, new RolesAllowedType( this ) );
        addMemberType( READONLY_ATTR, new AnnotationMemberType( null, this ) );
        addMemberType( STRUTSMERGE_ATTR, new ValidXmlFileType( null, null, this, fcInfo ) );
        addMemberType( VALIDATOR_VERSION_ATTR, new AnnotationMemberType( null, this ) );
        addMemberType( VALIDATOR_MERGE_ATTR, new ValidXmlFileType( null, null, this, fcInfo ) );
        addMemberType( TILES_DEFINITIONS_CONFIGS_ATTR, new TilesDefinitionsConfigsType( null, this, fcInfo ) );
        addMemberType( MULTIPART_HANDLER_ATTR, new AnnotationMemberType( null, this ) );

        addMemberArrayGrammar( SHARED_FLOW_REFS_ATTR, new SharedFlowRefGrammar( env, diags, rvc ) );
        addMemberArrayGrammar( FORWARDS_ATTR, new ForwardGrammar( env, diags, null, rvc, fcInfo ) );
        addMemberArrayGrammar( CATCHES_ATTR, new CatchGrammar( env, diags, null, rvc, CONTROLLER_TAG_NAME, fcInfo ) );
        addMemberArrayGrammar( MESSAGE_BUNDLES_ATTR, new MessageBundleGrammar( env, diags, null, rvc, fcInfo ) );
        addMemberArrayGrammar( VALIDATABLE_BEANS_ATTR, new ValidatableBeanGrammar( env, diags, rvc ) );
        addMemberArrayGrammar( SIMPLE_ACTIONS_ATTR, new SimpleActionGrammar( env, diags, rvc, fcInfo ) );
    }

    public String[][] getMutuallyExclusiveAttrs()
    {
        return null;
    }

    public String[][] getRequiredAttrs()
    {
        return null;
    }

    public String[][] getAttrDependencies()
    {
        return null;
    }
}
