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
package org.apache.beehive.netui.compiler.model.validation;

public interface ValidatorConstants
{
    String RULENAME_INT_RANGE = "netui_longRange";
    String RULENAME_FLOAT_RANGE = "floatRange";
    String RULENAME_REQUIRED = "required";
    String RULENAME_EMAIL = "email";
    String RULENAME_CREDIT_CARD = "creditCard";
    String RULENAME_MINLENGTH = "minlength";
    String RULENAME_MAXLENGTH = "maxlength";
    String RULENAME_MASK = "mask";
    String RULENAME_BYTE = "byte";
    String RULENAME_SHORT = "short";
    String RULENAME_INTEGER = "integer";
    String RULENAME_LONG = "long";
    String RULENAME_FLOAT = "float";
    String RULENAME_DOUBLE = "double";
    String RULENAME_DATE = "date";
    String RULENAME_VALID_WHEN = "netui_validwhen";
    String RULENAME_URL = "url";
    
    String VARNAME_MIN = "min";
    String VARNAME_MAX = "max";
    String VARNAME_MINLENGTH = "minlength";
    String VARNAME_MAXLENGTH = "maxlength";
    String VARNAME_MASK = "mask";
    String VARNAME_DATE_PATTERN = "datePattern";
    String VARNAME_DATE_PATTERN_STRICT = "datePatternStrict";
    String VARNAME_VALID_WHEN = RULENAME_VALID_WHEN;
    String VARNAME_ALLOW_ALL_SCHEMES = "allowallschemes";
    String VARNAME_ALLOW_TWO_SLASHES = "allow2slashes";
    String VARNAME_DISALLOW_FRAGMENTS = "nofragments";
    String VARNAME_SCHEMES = "schemes";
    
    String EXPRESSION_KEY_PREFIX = "NETUI-EXPRESSION:";
}
