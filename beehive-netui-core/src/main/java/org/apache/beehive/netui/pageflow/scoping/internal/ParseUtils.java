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
package org.apache.beehive.netui.pageflow.scoping.internal;

import java.util.Map;
import java.util.StringTokenizer;
import java.io.UnsupportedEncodingException;


class ParseUtils
{
    //-------------------------------------------------------------------------------------------------
    // helpers to parse the query string.  
    
    /**
     * Parses an RFC1630 query string into an existing Map.
     *
     * @param str      Query string
     * @param res      Map into which insert the values.
     * @param encoding Encoding to be used for stored Strings
     */
    public static void parseQueryString( String str, Map res, String encoding )
    {

        // "Within the query string, the plus sign is reserved as
        // shorthand notation for a space. Therefore, real plus signs must
        // be encoded. This method was used to make query URIs easier to
        // pass in systems which did not allow spaces." -- RFC 1630
        int i = str.indexOf( '#' );
        if ( i > 0 )
        {
            str = str.substring( 0, i );
        }
        StringTokenizer st = new StringTokenizer( str.replace( '+', ' ' ), "&" );

        while ( st.hasMoreTokens() )
        {
            String qp = st.nextToken();
            String[] pair = qp.split( "=" );  // was String[] pair = StringUtils.split(qp, '=');
            //String s = unescape(pair[1], encoding);
            res.put( unescape( pair[0], encoding ), unescape( pair[1], encoding ) );
        }
    }

    /**
     * URI-unescapes the specified string, except for +/<space>
     * encoding.
     *
     * @param str      String to be unescaped
     * @param encoding The name of a character encoding
     * @return Unescaped string
     */
    private static String unescape( String str, String encoding )
    {
        //We cannot unescape '+' to space because '+' is allowed in the file name
        //str = str.replace('+', ' ');
        
        //if the str does not contain "%", we don't need to do anything
        if ( str.indexOf( '%' ) < 0 )
        {
            return str;
        }

        if ( encoding == null || encoding.length() == 0 )
        {
            encoding = WLS_DEFAULT_ENCODING;
        }
        
        // Do not assume String only contains ascii.  str.length() <= str.getBytes().length
        int out = 0;

        byte[] strbytes = str.getBytes();
        int len = strbytes.length;

        boolean foundNonAscii = false;
        for ( int in = 0; in < len; in++, out++ )
        {
            if ( strbytes[in] == '%' && ( in + 2 < len ) )
            {
                if ( Hex.isHexChar( strbytes[in + 1] ) &&
                     Hex.isHexChar( strbytes[in + 2] ) )
                {
                    strbytes[out] =
                    ( byte ) ( ( Hex.hexValueOf( strbytes[in + 1] ) << 4 ) +
                               ( Hex.hexValueOf( strbytes[in + 2] ) << 0 ) );
                    in += 2;
                    continue;
                }
            }
            // IE takes non-ASCII URLs. We use the default encoding
            // if non-ASCII characters are contained in URLs.
            if ( !foundNonAscii &&
                 ( strbytes[in] <= 0x1f || strbytes[in] == 0x7f ) )
            {
                encoding = System.getProperty( "file.encoding" );
                foundNonAscii = true;
            }
            strbytes[out] = strbytes[in];
        }

        return newString( strbytes, 0, out, encoding );  // was:  BytesToString.newString(...)
    }

    private static String newString( byte b[], int offset, int length, String enc )
    {
        if ( is8BitUnicodeSubset( enc ) )
        {
            return getString( b, offset, length );
        }
        try
        {
            return new String( b, offset, length, enc );
        }
        catch ( UnsupportedEncodingException uee )
        {
            return getString( b, offset, length );
        }
    }

    private static boolean is8BitUnicodeSubset( String enc )
    {
        return enc == null || "ISO-8859-1".equalsIgnoreCase( enc ) ||
               "ISO8859_1".equalsIgnoreCase( enc ) || "ASCII".equalsIgnoreCase( enc );
    }

    private static final String WLS_DEFAULT_ENCODING = "ISO-8859-1";

    private static String getString( byte b[], int offset, int length )
    {
        try
        {
            return new String( b, offset, length, WLS_DEFAULT_ENCODING );
        }
        catch ( UnsupportedEncodingException uee )
        {
            // every JVM is supposed to support ISO-8859-1
            throw new AssertionError( uee );
        }
    }

    static class Hex
    {

        // this class exists only for its static methods
        private Hex()
        {
        }

        public static int hexValueOf( int c )
        {
            if ( c >= '0' && c <= '9' )
            {
                return c - '0';
            }
            if ( c >= 'a' && c <= 'f' )
            {
                return c - 'a' + 10;
            }
            if ( c >= 'A' && c <= 'F' )
            {
                return c - 'A' + 10;
            }
            return 0;
        }


        /**
         * Test a character to see whether it is a possible hex char.
         *
         * @param c char (int actually) to test.
         */
        public static final boolean isHexChar( int c )
        {
            // trade space for speed !!!!
            switch ( c )
            {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                    return true;
                default:
                    return false;
            }
        }

    }
}
