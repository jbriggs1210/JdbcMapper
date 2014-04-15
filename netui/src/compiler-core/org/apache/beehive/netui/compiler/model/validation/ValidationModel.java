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

import org.apache.beehive.netui.compiler.model.XmlElementSupport;
import org.apache.beehive.netui.compiler.model.XmlModelWriter;
import org.apache.beehive.netui.compiler.model.XmlModelWriterException;
import org.apache.beehive.netui.compiler.JpfLanguageConstants;
import org.apache.beehive.netui.compiler.FatalCompileTimeException;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public abstract class ValidationModel
        extends XmlElementSupport
        implements JpfLanguageConstants
{
    private Map _localeSets = new HashMap();
    private LocaleSet _defaultLocaleSet = new LocaleSet();
    private List _rulesToAddForAllLocales = new ArrayList();       // list of RuleAdd
    private boolean _empty = true;
    private ValidatorVersion _validatorVersion = ValidatorVersion.oneOne;

    public static class ValidatorVersion
    {
        private static final int INT_ONE_ZERO = 0;
        private static final int INT_ONE_ONE = 1;
        
        private int _val;
        
        private ValidatorVersion( int val )
        {
            _val = val;
        }
        
        public boolean equals( ValidatorVersion vv )
        {
            return _val == vv._val;
        }
        
        public static final ValidatorVersion oneZero = new ValidatorVersion( INT_ONE_ZERO );
        public static final ValidatorVersion oneOne = new ValidatorVersion( INT_ONE_ONE );
    }

    public static class RuleInfo
    {
        private String _entityName;
        private String _fieldName;
        private String _fieldDisplayName;
        private String _fieldDisplayNameKey;

        public RuleInfo( String entityName, String fieldName, String fieldDisplayName, String fieldDisplayNameKey )
        {
            _entityName = entityName;
            _fieldName = fieldName;
            _fieldDisplayName = fieldDisplayName;
            _fieldDisplayNameKey = fieldDisplayNameKey;
        }

        public String getEntityName()
        {
            return _entityName;
        }

        public String getFieldName()
        {
            return _fieldName;
        }

        public String getFieldDisplayName()
        {
            return _fieldDisplayName;
        }

        public String getFieldDisplayNameKey()
        {
            return _fieldDisplayNameKey;
        }
    }

    private static class RuleAdd
    {
        public RuleAdd( RuleInfo ruleInfo, ValidatorRule rule )
        {
            this.ruleInfo = ruleInfo;
            this.rule = rule;
        }

        public RuleInfo ruleInfo;
        public ValidatorRule rule;
    }

    public ValidatorVersion getValidatorVersion()
    {
        return _validatorVersion;
    }

    public void setValidatorVersion( String validatorVersion )
    {
        //
        // default to the least common denominator (validator v1.0) unless
        // explicitly set to v1.1.
        //
        if ( validatorVersion != null && validatorVersion.equals( VALIDATOR_VERSION_ONE_ONE_STR ) )
        {
            _validatorVersion = ValidatorVersion.oneOne;
        }
        else
        {
            _validatorVersion = ValidatorVersion.oneZero;
        }
    }

    public void addFieldRuleForAllLocales( RuleInfo ruleInfo, ValidatorRule rule )
    {
        _rulesToAddForAllLocales.add( new RuleAdd( ruleInfo, rule ) );
    }
    
    public void addFieldRule( RuleInfo ruleInfo, ValidatorRule rule, Locale locale )
    {
        LocaleSet localeSet = null;
        
        if ( locale == null )   // default locale
        {
            localeSet = _defaultLocaleSet;
        }
        else
        {
            localeSet = ( LocaleSet ) _localeSets.get( locale );
            
            if ( localeSet == null )
            {
                localeSet = new LocaleSet( locale );
                _localeSets.put( locale, localeSet );
            }

            //
            // The Commons Validator uses specific locale rules for a field only if there
            // is a rule for the same field in the default formset. Therefor,  we need to
            // keep a place holder for each locale specific field we find in the default
            // formset entity so that the Commons Validator will behave as desired.
            //
            if ( getField( ruleInfo, _defaultLocaleSet ) == null )
            {
                //
                // create a simple placeholder for the field, without any rules
                //
                addFieldRule( ruleInfo, ( ValidatorRule ) null, _defaultLocaleSet );
            }
        }
        
        addFieldRule( ruleInfo, rule, localeSet );
    }

    private ValidatableField getField( RuleInfo ruleInfo, LocaleSet localeSet )
    {
        String entityName = ruleInfo.getEntityName();
        ValidatableEntity entity = localeSet.getEntity( entityName );
        if ( entity == null ) { return null; }

        String fieldName = ruleInfo.getFieldName();
        return entity.getField( fieldName );
    }

    private boolean hasFieldRule( RuleInfo ruleInfo, ValidatorRule rule, LocaleSet localeSet )
    {
        ValidatableField field = getField( ruleInfo, localeSet );
        if ( field == null ) { return false; }

        return field.hasRule( rule );
    }

    private void addFieldRule( RuleInfo ruleInfo, ValidatorRule rule, LocaleSet localeSet )
    {
        String entityName = ruleInfo.getEntityName();
        ValidatableEntity entity = localeSet.getEntity( entityName );
        if ( entity == null ) localeSet.addValidatableEntity( entity = new ValidatableEntity( entityName ) );
        
        String fieldName = ruleInfo.getFieldName();
        ValidatableField field = entity.getField( fieldName );
        if ( field == null )
        {
            field = new ValidatableField( fieldName, ruleInfo.getFieldDisplayName(), ruleInfo.getFieldDisplayNameKey(),
                                          ! getValidatorVersion().equals(ValidatorVersion.oneZero) );
            entity.addField( field );
        }

        //
        // A field element without rules is OK, but we don't want to add a null rule.
        //
        if ( rule != null ) { field.addRule( rule ); }
    }
    
    
    public void writeXml( PrintWriter writer, File mergeFile )
        throws IOException, FatalCompileTimeException, XmlModelWriterException
    {
        //
        // First, if we haven't written the all-locale rules to each locale, do so now.
        // However, before we add a rule, check that it does not already exist. We don't
        // want to overload a rule explicitly defined for a specific locale with
        // an all-locale rule of the same name.
        //
        if ( _rulesToAddForAllLocales != null )
        {
            for ( int i = 0; i < _rulesToAddForAllLocales.size(); i++ )
            {
                RuleAdd ruleAdd = ( RuleAdd  ) _rulesToAddForAllLocales.get( i );
                
                for ( Iterator j = _localeSets.values().iterator(); j.hasNext(); )
                {
                    LocaleSet localeSet = ( LocaleSet ) j.next();
                    if ( !hasFieldRule( ruleAdd.ruleInfo, ruleAdd.rule, localeSet ) ) {
                        addFieldRule( ruleAdd.ruleInfo, ruleAdd.rule, localeSet );
                    }
                }

                if ( !hasFieldRule( ruleAdd.ruleInfo, ruleAdd.rule, _defaultLocaleSet ) ) {
                    addFieldRule( ruleAdd.ruleInfo, ruleAdd.rule, _defaultLocaleSet );
                }
            }
            
            _rulesToAddForAllLocales = null;
        }
        
        String publicID;
        String systemID;
        
        if ( _validatorVersion.equals( ValidatorVersion.oneZero ) )
        {
            publicID = "-//Apache Software Foundation//DTD Commons Validator Rules Configuration 1.0//EN";
            systemID = "http://jakarta.apache.org/commons/dtds/validator_1_0.dtd";
        }
        else
        {
            publicID = "-//Apache Software Foundation//DTD Commons Validator Rules Configuration 1.1//EN";
            systemID = "http://jakarta.apache.org/commons/dtds/validator_1_1.dtd";
        }
        
        String comment = getHeaderComment(mergeFile);
        XmlModelWriter xw = new XmlModelWriter( mergeFile, "form-validation", publicID, systemID, comment);
        writeXML(xw, xw.getRootElement());
        xw.simpleFastWrite(writer);
    }
    
    protected void writeToElement(XmlModelWriter xw, Element element)
    {
        //
        // Now write out all the LocaleSets, which contain the forms/fields/rules.
        //
        writeLocaleSets(xw, element);
        writeLocaleSet(xw, element, _defaultLocaleSet);
    }
    
    protected String getHeaderComment( File mergeFile )
            throws FatalCompileTimeException
    {
        return null;
    }
    
    private void writeLocaleSets(XmlModelWriter xw, Element element)
    {
        //
        // Commons Validator behavior is to build a key from the locale of a FormSet
        // or uses the default Locale (Locale.getDefault() - the system locale) to
        // track different  elements. This implies that the 
        // without language or country attributes could be mapped to "en_US"
        // if that's the default locale.
        // See org.apache.commons.validator.ValidatorResources.buildKey()
        //
        // Therefor, to ensure the validator uses  rules for of a specific
        // locale before the FormSet with no language or country attributes (even
        // if it is the locale of the system), write the most specific locales first.
        //
        List allLocales = new ArrayList( _localeSets.keySet() );
        List langCountryVariant = new ArrayList();
        List langCountry = new ArrayList();
        List lang = new ArrayList();

        for ( java.util.Iterator ii = allLocales.iterator(); ii.hasNext(); )  
        {
            Locale locale = ( Locale ) ii.next();
            if ( locale.getCountry().length() > 0 )
            {
                if ( locale.getVariant().length() > 0 )
                {
                    langCountryVariant.add( locale );
                }
                else
                {
                    langCountry.add( locale );
                }
            }
            else
            {
                lang.add( locale );
            }
        }

        writeLocaleSets(xw, element, langCountryVariant);
        writeLocaleSets(xw, element, langCountry);
        writeLocaleSets(xw, element, lang);
    }

    private void writeLocaleSets(XmlModelWriter xw, Element element, Collection locales)
    {
        for ( java.util.Iterator ii = locales.iterator(); ii.hasNext(); )  
        {
            Locale locale = ( Locale ) ii.next();
            LocaleSet localeSet = ( LocaleSet ) _localeSets.get( locale );
            writeLocaleSet(xw, element, localeSet);
        }
    }

    private void writeLocaleSet(XmlModelWriter xw, Element element, LocaleSet localeSet)
    {
        Locale locale = localeSet.getLocale();
        Element formSetElement = null;
        
        if (locale == null) {
            formSetElement = findChildElement(xw, element, "formset", "language", null, false, null);
        } else {
            Element possibleMatch = findChildElement(xw, element, "formset", "language", locale.getLanguage());
            if (possibleMatch != null) {
                String country = getElementAttribute(possibleMatch, "country");
                String variant = getElementAttribute(possibleMatch, "variant");
                String localeCountry = locale.getCountry();
                String localeVariant = locale.getVariant();
                
                if (((localeCountry.length() == 0 && country == null) || localeCountry.equals(country))
                    && ((localeVariant.length() == 0 && variant == null) || localeVariant.equals(variant))) {
                    formSetElement = possibleMatch;
                }
            }
        }
        
        if (formSetElement == null) {
            formSetElement = xw.addElement(element, "formset");
        }
        
        localeSet.writeXML(xw, formSetElement);
    }

    public boolean isEmpty()
    {
        return _empty;
    }

    protected void setEmpty( boolean empty )
    {
        _empty = empty;
    }
    
    public abstract String getOutputFileURI();
}    
