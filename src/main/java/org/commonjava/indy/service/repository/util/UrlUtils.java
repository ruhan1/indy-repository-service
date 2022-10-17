/**
 * Copyright (C) 2011-2022 Red Hat, Inc. (https://github.com/Commonjava/service-parent)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.indy.service.repository.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

public final class UrlUtils
{

    private UrlUtils()
    {
    }

    public static String buildUrl( final String baseUrl, final String... parts )
            throws MalformedURLException
    {
        return buildUrl( baseUrl, null, parts );
    }

    public static String buildUrl( final String baseUrl, final Map<String, String> params, final String... parts )
            throws MalformedURLException
    {
        if ( parts == null || parts.length < 1 )
        {
            return baseUrl;
        }

        final StringBuilder urlBuilder = new StringBuilder();

        if ( parts[0] == null || !parts[0].startsWith( baseUrl ) )
        {
            urlBuilder.append( baseUrl );
        }

        for ( String part : parts )
        {
            if ( part == null || part.trim().length() < 1 )
            {
                continue;
            }
            part = part.trim();
            if ( part.startsWith( "/" ) )
            {
                part = part.substring( 1 );
            }

            if ( urlBuilder.length() > 0 && urlBuilder.charAt( urlBuilder.length() - 1 ) != '/' )
            {
                urlBuilder.append( "/" );
            }

            urlBuilder.append( part );
        }

        if ( params != null && !params.isEmpty() )
        {
            urlBuilder.append( "?" );
            boolean first = true;
            for ( final Map.Entry<String, String> param : params.entrySet() )
            {
                if ( first )
                {
                    first = false;
                }
                else
                {
                    urlBuilder.append( "&" );
                }

                urlBuilder.append( param.getKey() ).append( "=" ).append( param.getValue() );
            }
        }

        return new URL( urlBuilder.toString() ).toExternalForm();
    }

    /**
     * This is copied from
     * https://github.com/spring-projects/spring-framework/blob/581fa1419fb36d685af07554369a3fe77f4af68f/spring-core/src/main/java/org/springframework/util/StringUtils.java#L793
     * to handle uri segment decode problem
     *
     * @param source
     * @param charset
     * @return
     */
    public static String uriDecode( String source, Charset charset )
    {
        if ( source == null || source.length() == 0 )
        {
            return source;
        }
        int length = source.length();
        Charset providedCharSet = charset;
        if ( providedCharSet == null )
        {
            providedCharSet = Charset.defaultCharset();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream( length );
        boolean changed = false;
        for ( int i = 0; i < length; i++ )
        {
            int ch = source.charAt( i );
            if ( ch == '%' )
            {
                if ( i + 2 < length )
                {
                    char hex1 = source.charAt( i + 1 );
                    char hex2 = source.charAt( i + 2 );
                    int u = Character.digit( hex1, 16 );
                    int l = Character.digit( hex2, 16 );
                    if ( u == -1 || l == -1 )
                    {
                        throw new IllegalArgumentException(
                                "Invalid encoded sequence \"" + source.substring( i ) + "\"" );
                    }
                    baos.write( (char) ( ( u << 4 ) + l ) );
                    i += 2;
                    changed = true;
                }
                else
                {
                    throw new IllegalArgumentException( "Invalid encoded sequence \"" + source.substring( i ) + "\"" );
                }
            }
            else
            {
                baos.write( ch );
            }
        }
        return ( changed ? baos.toString( providedCharSet ) : source );
    }

}
