/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.apache.directory.api.ldap.codec.search;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.EncoderException;
import org.apache.directory.api.asn1.ber.Asn1Decoder;
import org.apache.directory.api.asn1.util.Asn1Buffer;
import org.apache.directory.api.ldap.codec.api.CodecControl;
import org.apache.directory.api.ldap.codec.api.LdapEncoder;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.decorators.SearchResultDoneDecorator;
import org.apache.directory.api.ldap.codec.osgi.AbstractCodecServiceTest;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.SearchResultDone;
import org.apache.directory.api.ldap.model.message.SearchResultDoneImpl;
import org.apache.directory.api.util.Strings;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mycila.junit.concurrent.Concurrency;
import com.mycila.junit.concurrent.ConcurrentJunitRunner;


/**
 * Test the SearchResultDone codec
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrency()
public class SearchResultDoneTest extends AbstractCodecServiceTest
{
    /**
     * Test the decoding of a SearchResultDone
     */
    @Test
    public void testDecodeSearchResultDoneSuccess() throws DecoderException, EncoderException
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x0E );

        stream.put( new byte[]
            {
                0x30, 0x0C,                 // LDAPMessage ::=SEQUENCE {
                  0x02, 0x01, 0x01,         // messageID MessageID
                  0x65, 0x07,               // CHOICE { ..., searchResDone SearchResultDone, ...
                                            // SearchResultDone ::= [APPLICATION 5] LDAPResult
                    0x0A, 0x01, 0x00,       // LDAPResult ::= SEQUENCE {
                                            // resultCode ENUMERATED {
                                            // success (0), ...
                                            // },
                    0x04, 0x00,             // matchedDN LDAPDN,
                    0x04, 0x00              // errorMessage LDAPString,
                                            // referral [3] Referral OPTIONAL }
                                            // }
            } );

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a SearchResultDone Container
        LdapMessageContainer<SearchResultDoneDecorator> ldapMessageContainer =
            new LdapMessageContainer<SearchResultDoneDecorator>( codec );

        ldapDecoder.decode( stream, ldapMessageContainer );

        SearchResultDone searchResultDone = ldapMessageContainer.getMessage();

        assertEquals( 1, searchResultDone.getMessageId() );
        assertEquals( ResultCodeEnum.SUCCESS, searchResultDone.getLdapResult().getResultCode() );
        assertEquals( "", searchResultDone.getLdapResult().getMatchedDn().getName() );
        assertEquals( "", searchResultDone.getLdapResult().getDiagnosticMessage() );

        // Check the encoding
        ByteBuffer bb = LdapEncoder.encodeMessage( codec, searchResultDone );

        // Check the length
        assertEquals( 0x0E, bb.limit() );

        String encodedPdu = Strings.dumpBytes( bb.array() );

        assertEquals( encodedPdu, decodedPdu );

        // Check encode reverse
        Asn1Buffer buffer = new Asn1Buffer();

        SearchResultDone response = new SearchResultDoneImpl( searchResultDone.getMessageId() );
        response.getLdapResult().setResultCode( ResultCodeEnum.SUCCESS );

        LdapEncoder.encodeMessageReverse( buffer, codec, response );

        assertTrue( Arrays.equals( stream.array(), buffer.getBytes().array() ) );
    }


    /**
     * Test the decoding of a SearchResultDone with controls
     */
    @Test
    public void testDecodeSearchResultDoneSuccessWithControls() throws DecoderException, EncoderException
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x2B );

        stream.put( new byte[]
            {
                0x30, 0x29,                 // LDAPMessage ::=SEQUENCE {
                  0x02, 0x01, 0x01,         // messageID MessageID
                  0x65, 0x07,               // CHOICE { ..., searchResDone SearchResultDone, ...
                                            // SearchResultDone ::= [APPLICATION 5] LDAPResult
                    0x0A, 0x01, 0x00,       // LDAPResult ::= SEQUENCE {
                                            // resultCode ENUMERATED {
                                            // success (0), ...
                                            // },
                    0x04, 0x00,             // matchedDN LDAPDN,
                    0x04, 0x00,              // errorMessage LDAPString,
                                            // referral [3] Referral OPTIONAL }
                                            // }
                    ( byte ) 0xA0, 0x1B,    // A control
                      0x30, 0x19,
                        0x04, 0x17,
                          '2', '.', '1', '6', '.', '8', '4', '0', '.', '1', '.',
                          '1', '1', '3', '7', '3', '0', '.', '3', '.', '4', '.', '2'
            } );

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a BindRequest Container
        LdapMessageContainer<SearchResultDoneDecorator> ldapMessageContainer =
            new LdapMessageContainer<SearchResultDoneDecorator>( codec );

        ldapDecoder.decode( stream, ldapMessageContainer );

        SearchResultDone searchResultDone = ldapMessageContainer.getMessage();

        assertEquals( 1, searchResultDone.getMessageId() );
        assertEquals( ResultCodeEnum.SUCCESS, searchResultDone.getLdapResult().getResultCode() );
        assertEquals( "", searchResultDone.getLdapResult().getMatchedDn().getName() );
        assertEquals( "", searchResultDone.getLdapResult().getDiagnosticMessage() );

        // Check the Control
        Map<String, Control> controls = searchResultDone.getControls();

        assertEquals( 1, controls.size() );

        @SuppressWarnings("unchecked")
        CodecControl<Control> control = ( org.apache.directory.api.ldap.codec.api.CodecControl<Control> ) controls
            .get( "2.16.840.1.113730.3.4.2" );
        assertEquals( "2.16.840.1.113730.3.4.2", control.getOid() );
        assertEquals( "", Strings.dumpBytes( control.getValue() ) );

        // Check the encoding
        ByteBuffer bb = LdapEncoder.encodeMessage( codec, searchResultDone );

        // Check the length
        assertEquals( 0x2B, bb.limit() );

        String encodedPdu = Strings.dumpBytes( bb.array() );

        assertEquals( encodedPdu, decodedPdu );
    }


    /**
     * Test the decoding of a SearchResultDone with no LdapResult
     */
    @Test( expected=DecoderException.class )
    public void testDecodeSearchResultDoneEmptyResult() throws DecoderException
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x07 );

        stream.put( new byte[]
            {
                0x30, 0x05,             // LDAPMessage ::=SEQUENCE {
                  0x02, 0x01, 0x01,     // messageID MessageID
                  0x65, 0x00            // CHOICE { ..., searchResDone SearchResultDone, ...
        } );

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<SearchResultDoneDecorator> ldapMessageContainer =
            new LdapMessageContainer<SearchResultDoneDecorator>( codec );

        // Decode a SearchResultDone message
        ldapDecoder.decode( stream, ldapMessageContainer );
    }


    /**
     * Test the decoding of a SearchResultDone with a result code of length 2 bytes
     */
    @Test
    public void testDecodeSearchResultDoneEsyncRefresh() throws DecoderException, EncoderException
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x0F );

        stream.put( new byte[]
            {
                0x30, 0x0D,                 // LDAPMessage ::=SEQUENCE {
                  0x02, 0x01, 0x01,         // messageID MessageID
                  0x65, 0x08,               // CHOICE { ..., searchResDone SearchResultDone, ...
                                            // SearchResultDone ::= [APPLICATION 5] LDAPResult
                    0x0A, 0x02, 0x10, 0x00, // LDAPResult ::= SEQUENCE {
                                            // resultCode ENUMERATED {
                                            // eSyncRefreshRequired (4096), ...
                                            // },
                    0x04, 0x00,             // matchedDN LDAPDN,
                    0x04, 0x00              // errorMessage LDAPString,
                                            // referral [3] Referral OPTIONAL }
                                            // }
            } );

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a SearchResultDone Container
        LdapMessageContainer<SearchResultDoneDecorator> ldapMessageContainer =
            new LdapMessageContainer<SearchResultDoneDecorator>( codec );

        ldapDecoder.decode( stream, ldapMessageContainer );

        SearchResultDone searchResultDone = ldapMessageContainer.getMessage();

        assertEquals( 1, searchResultDone.getMessageId() );
        assertEquals( ResultCodeEnum.E_SYNC_REFRESH_REQUIRED, searchResultDone.getLdapResult().getResultCode() );
        assertEquals( "", searchResultDone.getLdapResult().getMatchedDn().getName() );
        assertEquals( "", searchResultDone.getLdapResult().getDiagnosticMessage() );

        // Check the encoding
        ByteBuffer bb = LdapEncoder.encodeMessage( codec, searchResultDone );

        // Check the length
        assertEquals( 0x0F, bb.limit() );

        String encodedPdu = Strings.dumpBytes( bb.array() );

        assertEquals( encodedPdu, decodedPdu );

        // Check encode reverse
        Asn1Buffer buffer = new Asn1Buffer();

        SearchResultDone response = new SearchResultDoneImpl( searchResultDone.getMessageId() );
        response.getLdapResult().setResultCode( ResultCodeEnum.E_SYNC_REFRESH_REQUIRED );

        LdapEncoder.encodeMessageReverse( buffer, codec, response );

        assertTrue( Arrays.equals( stream.array(), buffer.getBytes().array() ) );
    }
}
