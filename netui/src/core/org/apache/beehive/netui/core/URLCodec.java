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
package org.apache.beehive.netui.core;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.DecoderException;

import org.apache.beehive.netui.util.Bundle;

/**
 * Class that provides static methods for URL encoding/decoding
 */
public final class URLCodec {

    private final static org.apache.commons.codec.net.URLCodec s_codec =
        new org.apache.commons.codec.net.URLCodec();

    /**
     * URL encodes a string.
     * @param decoded the string to encode
     * @param charset the character set to use
     * @return the encoded string
     */
    public static String encode(final String decoded, final String charset) 
        throws UnsupportedEncodingException {
        return s_codec.encode(decoded, charset);
    }

    /**
     * URL encodes a string using the default character set
     * @param decoded the string to encode
     * @return the encoded string
     */
    public static String encode(final String decoded) {
        try {
            return s_codec.encode(decoded);
        } catch (EncoderException e) {
            throw new IllegalStateException(Bundle.getErrorString("URLCodec_encodeException", new String[] {e.getMessage()}), e);
        }
    }

    /**
     * URL decodes a string.
     * @param encoded the string to decode
     * @param charset the character set to use
     * @return the decoded string
     */
    public static String decode(final String encoded, final String charset) 
        throws UnsupportedEncodingException {
        try {
            return s_codec.decode(encoded, charset);
        } catch (DecoderException e) {
            throw new IllegalStateException(Bundle.getErrorString("URLCodec_decodeException", new String[] {e.getMessage()}), e);
        }
    }


    /**
     * URL decodes a string using the default character set
     * @param encoded the string to decode
     * @return the decoded string
     */
    public static String decode(final String encoded) {
        try {
            return s_codec.decode(encoded);
        } catch (DecoderException e) {
            throw new IllegalStateException(Bundle.getErrorString("URLCodec_decodeException", new String[] {e.getMessage()}), e);
        }
    }
}
