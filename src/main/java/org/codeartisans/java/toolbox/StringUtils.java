/*
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codeartisans.java.toolbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codeartisans.java.toolbox.io.IO;

public final class StringUtils
{

    public static final String EMPTY = "";
    public static final String SPACE = " ";
    public static final String TAB = "\t";
    public static final String NEWLINE = "\n";
    public static final char[] EMPTY_CHAR_ARRAY = EMPTY.toCharArray();
    private static final String TEMPLATE_TOKEN_PATTERN_STR = "\\$\\{([^}]+)\\}";
    private static final Pattern TEMPLATE_TOKEN_PATTERN = Pattern.compile( TEMPLATE_TOKEN_PATTERN_STR );
    private static final String ERROR_STRINGREADER_ON_STRING = "Unable to read a String using a StringReader, something went really bad!";

    public static boolean isEmpty( String s )
    {
        return s == null || s.length() <= 0;
    }

    public static boolean isEmpty( char[] array )
    {
        return array == null || array.length <= 0;
    }

    public static String toString( Reader input )
            throws IOException
    {
        StringWriter builder = new StringWriter();
        IO.copy( input, builder );
        return builder.toString();
    }

    public static String indentTwoSpaces( String input, int level )
    {
        try {
            return indentTwoSpaces( new StringReader( input ), level );
        } catch ( IOException ex ) {
            throw new RuntimeException( ERROR_STRINGREADER_ON_STRING, ex );
        }
    }

    public static String indentTab( String input, int level )
    {
        try {
            return indentTab( new StringReader( input ), level );
        } catch ( IOException ex ) {
            throw new RuntimeException( ERROR_STRINGREADER_ON_STRING, ex );
        }
    }

    public static String indent( String input, int level, String tab )
    {
        try {
            return indent( new StringReader( input ), level, tab, EMPTY );
        } catch ( IOException ex ) {
            throw new RuntimeException( ERROR_STRINGREADER_ON_STRING, ex );
        }
    }

    public static String indent( String input, int level, String tab, String prefix )
    {
        try {
            return indent( new StringReader( input ), level, tab, prefix );
        } catch ( IOException ex ) {
            throw new RuntimeException( ERROR_STRINGREADER_ON_STRING, ex );
        }
    }

    public static String indentTwoSpaces( Reader input, int level )
            throws IOException
    {
        return indent( input, level, "  " );
    }

    public static String indentTab( Reader input, int level )
            throws IOException
    {
        return indent( input, level, TAB );
    }

    public static String indent( Reader input, int level, String tab )
            throws IOException
    {
        return indent( input, level, tab, EMPTY );
    }

    public static String indent( Reader input, int level, String tab, String prefix )
            throws IOException
    {
        BufferedReader reader = new BufferedReader( input );
        StringBuilder output = new StringBuilder();
        try {

            String eachLine = reader.readLine();
            if ( !isEmpty( eachLine ) ) {
                appendIndent( output, level, tab ).append( prefix ).append( eachLine );
                while ( ( eachLine = reader.readLine() ) != null ) {
                    output.append( NEWLINE );
                    if ( !isEmpty( eachLine ) ) {
                        appendIndent( output, level, tab ).append( prefix ).append( eachLine );
                    }
                }
            }
            return output.toString();

        } finally {
            IO.closeSilently( reader );
        }
    }

    private static StringBuilder appendIndent( StringBuilder output, int level, String tab )
    {
        for ( int indent = 0; indent < level; indent++ ) {
            output.append( tab );
        }
        return output;
    }

    public static String join( String[] strings )
    {
        return join( Arrays.asList( strings ) );
    }

    public static String join( String[] strings, String delimiter )
    {
        return join( Arrays.asList( strings ), delimiter );
    }

    public static String join( Iterable<? extends CharSequence> strings )
    {
        return join( strings, "" );
    }

    public static String join( Iterable<? extends CharSequence> strings, String delimiter )
    {
        int capacity = 0;
        int delimLength = delimiter.length();
        Iterator<? extends CharSequence> iter = strings.iterator();
        if ( iter.hasNext() ) {
            capacity += iter.next().length() + delimLength;
        }
        StringBuilder buffer = new StringBuilder( capacity );
        iter = strings.iterator();
        if ( iter.hasNext() ) {
            buffer.append( iter.next() );
            while ( iter.hasNext() ) {
                buffer.append( delimiter );
                buffer.append( iter.next() );
            }
        }
        return buffer.toString();
    }

    public static StringBuffer renderTemplate( StringBuffer template, Map<String, String> dict, boolean removeUnknown )
    {
        final Matcher matcher = TEMPLATE_TOKEN_PATTERN.matcher( template );
        final StringBuffer buffer = new StringBuffer();
        while ( matcher.find() ) {
            final String token = matcher.group( 1 );
            if ( token != null ) {
                final String replacement = dict.get( token );
                if ( replacement != null ) {
                    // Escape \ and $ because they are interpreted by the matcher object :
                    String quotedReplacement = Matcher.quoteReplacement( replacement );
                    matcher.appendReplacement( buffer, quotedReplacement );
                } else if ( removeUnknown ) {
                    matcher.appendReplacement( buffer, EMPTY );
                }
            }
        }
        matcher.appendTail( buffer );
        return buffer;
    }

    public static StringBuffer renderTemplate( StringBuffer template, Map<String, String> dict )
    {
        return renderTemplate( template, dict, false );
    }

    public static String random( int count, int start, int end, boolean letters, boolean numbers, char[] chars, Random random )
    {
        if ( count == 0 ) {
            return "";
        } else if ( count < 0 ) {
            throw new IllegalArgumentException( "Requested random string length " + count + " is less than 0." );
        }
        if ( ( start == 0 ) && ( end == 0 ) ) {
            end = 'z' + 1;
            start = ' ';
            if ( !letters && !numbers ) {
                start = 0;
                end = Integer.MAX_VALUE;
            }
        }

        StringBuilder buffer = new StringBuilder();
        int gap = end - start;

        while ( count-- != 0 ) {
            char ch;
            if ( chars == null ) {
                ch = ( char ) ( random.nextInt( gap ) + start );
            } else {
                ch = chars[random.nextInt( gap ) + start];
            }
            if ( ( letters && numbers && Character.isLetterOrDigit( ch ) )
                    || ( letters && Character.isLetter( ch ) )
                    || ( numbers && Character.isDigit( ch ) )
                    || ( !letters && !numbers ) ) {
                buffer.append( ch );
            } else {
                count++;
            }
        }
        return buffer.toString();
    }

    private StringUtils()
    {
    }

}
