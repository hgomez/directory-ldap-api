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
package org.apache.directory.api.ldap.codec.abandon;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.Map;

import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.EncoderException;
import org.apache.directory.api.asn1.ber.Asn1Decoder;
import org.apache.directory.api.asn1.util.Asn1Buffer;
import org.apache.directory.api.ldap.codec.api.AbstractMessageDecorator;
import org.apache.directory.api.ldap.codec.api.CodecControl;
import org.apache.directory.api.ldap.codec.api.LdapEncoder;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.decorators.AbandonRequestDecorator;
import org.apache.directory.api.ldap.codec.osgi.AbstractCodecServiceTest;
import org.apache.directory.api.ldap.model.message.AbandonRequest;
import org.apache.directory.api.ldap.model.message.AbandonRequestImpl;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.Message;
import org.apache.directory.api.util.Strings;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mycila.junit.concurrent.Concurrency;
import com.mycila.junit.concurrent.ConcurrentJunitRunner;


/**
 * Test an AbandonRequest
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrency()
public class AbandonRequestTest extends AbstractCodecServiceTest
{
    /**
     * Test the decoding of a AbandonRequest with controls
     */
    @Test
    public void testDecodeAbandonRequestWithControls() throws DecoderException, EncoderException
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x64 );
        stream.put( new byte[]
            {
              0x30, 0x62,                               // LDAPMessage ::=SEQUENCE {
                0x02, 0x01, 0x03,                       // messageID MessageID
                0x50, 0x01, 0x02,                       // CHOICE { ..., abandonRequest
                ( byte ) 0xA0, 0x5A,                    // controls [0] Controls OPTIONAL }
                  0x30, 0x1A,                           // Control ::= SEQUENCE {
                    0x04, 0x0D,                         // controlType LDAPOID,
                      '1', '.', '3', '.', '6', '.', '1', '.', '5', '.', '5', '.', '1',
                    0x01, 0x01, ( byte ) 0xFF,          // criticality BOOLEAN DEFAULT FALSE,
                    0x04, 0x06,                         // controlValue OCTET STRING OPTIONAL }
                      'a', 'b', 'c', 'd', 'e', 'f',
                  0x30, 0x17,                           // Control ::= SEQUENCE {
                    0x04, 0x0D,                         // controlType LDAPOID,
                      '1', '.', '3', '.', '6', '.', '1', '.', '5', '.', '5', '.', '2',
                    0x04, 0x06,                         // controlValue OCTET STRING OPTIONAL }
                      'g', 'h', 'i', 'j', 'k', 'l',
                  0x30, 0x12,                           // Control ::= SEQUENCE {
                    0x04, 0x0D,                         // controlType LDAPOID,
                      '1', '.', '3', '.', '6', '.', '1', '.', '5', '.', '5', '.', '3',
                    0x01, 0x01, ( byte ) 0xFF,          // criticality BOOLEAN DEFAULT FALSE }
                  0x30, 0x0F,                           // Control ::= SEQUENCE {
                    0x04, 0x0D,                         // controlType LDAPOID}
                      '1', '.', '3', '.', '6', '.', '1', '.', '5', '.', '5', '.', '4'
            } );

        stream.flip();

        // Allocate a LdapMessageContainer Container
        LdapMessageContainer<AbandonRequestDecorator> ldapMessageContainer =
            new LdapMessageContainer<AbandonRequestDecorator>( codec );

        // Decode the PDU
        ldapDecoder.decode( stream, ldapMessageContainer );

        // Check that everything is OK
        AbandonRequestDecorator abandonRequest = ldapMessageContainer.getMessage();

        // Copy the message
        AbandonRequest internalAbandonRequest = new AbandonRequestImpl( abandonRequest.getAbandoned() );
        internalAbandonRequest.setMessageId( abandonRequest.getMessageId() );

        assertEquals( 3, abandonRequest.getMessageId() );
        assertEquals( 2, abandonRequest.getAbandoned() );

        // Check the Controls
        Map<String, Control> controls = abandonRequest.getControls();

        assertEquals( 4, controls.size() );

        CodecControl<? extends Control> control = ( CodecControl<?> ) controls
            .get( "1.3.6.1.5.5.1" );
        assertEquals( "1.3.6.1.5.5.1", control.getOid() );
        assertEquals( "0x61 0x62 0x63 0x64 0x65 0x66 ", Strings.dumpBytes( control.getValue() ) );
        assertTrue( control.isCritical() );
        internalAbandonRequest.addControl( control );

        control = ( CodecControl<?> ) controls.get( "1.3.6.1.5.5.2" );
        assertEquals( "1.3.6.1.5.5.2", control.getOid() );
        assertEquals( "0x67 0x68 0x69 0x6A 0x6B 0x6C ", Strings.dumpBytes( control.getValue() ) );
        assertFalse( control.isCritical() );
        internalAbandonRequest.addControl( control );

        control = ( CodecControl<?> ) controls.get( "1.3.6.1.5.5.3" );
        assertEquals( "1.3.6.1.5.5.3", control.getOid() );
        assertEquals( "", Strings.dumpBytes( control.getValue() ) );
        assertTrue( control.isCritical() );
        internalAbandonRequest.addControl( control );

        control = ( CodecControl<?> ) controls.get( "1.3.6.1.5.5.4" );
        assertEquals( "1.3.6.1.5.5.4", control.getOid() );
        assertEquals( "", Strings.dumpBytes( control.getValue() ) );
        assertFalse( control.isCritical() );
        internalAbandonRequest.addControl( control );

        // Check the encoding
        ByteBuffer bb = LdapEncoder.encodeMessage( codec, new AbandonRequestDecorator( codec, internalAbandonRequest ) );

        // Check the length
        assertEquals( 0x64, bb.limit() );

        // Don't check the PDU, as control are in a Map, and can be in a different order
        // So we decode the generated PDU, and we compare it with the initial message
        ldapDecoder.decode( bb, ldapMessageContainer );

        AbandonRequest abandonRequest2 = ldapMessageContainer.getMessage();
        assertEquals( abandonRequest, abandonRequest2 );
    }


    /**
     * Test the decoding of a AbandonRequest with no controls
     */
    @Test
    public void testDecodeAbandonRequestNoControlsHighMessageId() throws DecoderException, EncoderException
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x0A );
        stream.put( new byte[]
            {
              0x30, 0x08,                                   // LDAPMessage ::=SEQUENCE {
                0x02, 0x03, 0x00, ( byte ) 0x80, 0x13,      // messageID MessageID
                0x50, 0x01, 0x02                            // CHOICE { ..., abandonRequest
                                                            // AbandonRequest,...
                                                            // AbandonRequest ::= [APPLICATION 16] MessageID
            } );

        stream.flip();

        // Allocate a LdapMessageContainer Container
        LdapMessageContainer<AbandonRequestDecorator> ldapMessageContainer =
            new LdapMessageContainer<AbandonRequestDecorator>( codec );

        // Decode the PDU
        ldapDecoder.decode( stream, ldapMessageContainer );

        // Check that everything is OK
        AbandonRequest abandonRequest = ldapMessageContainer.getMessage();

        assertEquals( 32787, abandonRequest.getMessageId() );
        assertEquals( 2, abandonRequest.getAbandoned() );

        // Check the length
        AbandonRequest internalAbandonRequest = new AbandonRequestImpl( abandonRequest.getAbandoned() );
        internalAbandonRequest.setMessageId( abandonRequest.getMessageId() );

        // Check the encoding
        ByteBuffer bb = LdapEncoder.encodeMessage( codec, new AbandonRequestDecorator( codec, internalAbandonRequest )  );

        // Check the length
        assertEquals( 0x0A, bb.limit() );

        assertArrayEquals( stream.array(), bb.array() );

        // Check the reverse encoding
        Asn1Buffer buffer = new Asn1Buffer();
        LdapEncoder.encodeMessageReverse( buffer, codec, internalAbandonRequest );

        assertArrayEquals( stream.array(), buffer.getBytes().array() );
    }


    /**
     * Test the decoding of a AbandonRequest with a null messageId
     */
    @Test( expected=DecoderException.class )
    public void testDecodeAbandonRequestNoMessageId() throws DecoderException
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x0A );
        stream.put( new byte[]
            {
              0x30, 0x08,               // LDAPMessage ::=SEQUENCE {
                0x02, 0x01, 0x01,       // messageID MessageID
                0x50, 0x00              // CHOICE { ..., abandonRequest AbandonRequest,...
                                        // AbandonRequest ::= [APPLICATION 16] MessageID
            } );

        stream.flip();

        // Allocate a LdapMessageContainer Container
        LdapMessageContainer<AbstractMessageDecorator<? extends Message>> ldapMessageContainer =
            new LdapMessageContainer<AbstractMessageDecorator<? extends Message>>( codec );

        // Decode the PDU
        ldapDecoder.decode( stream, ldapMessageContainer );
    }


    /**
     * Test the decoding of a AbandonRequest with a bad Message Id
     */
    @Test( expected=DecoderException.class )
    public void testDecodeAbandonRequestBadMessageId() throws DecoderException
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x0B );
        stream.put( new byte[]
            {
              0x30, 0x09,                   // LDAPMessage ::=SEQUENCE {
                0x02, 0x01, 0x01,           // messageID MessageID
                0x50, 0x01, ( byte ) 0xFF   // CHOICE { ..., abandonRequest AbandonRequest,...
                                            // AbandonRequest ::= [APPLICATION 16] MessageID
            } );

        stream.flip();

        // Allocate a LdapMessageContainer Container
        LdapMessageContainer<AbstractMessageDecorator<? extends Message>> ldapMessageContainer =
            new LdapMessageContainer<AbstractMessageDecorator<? extends Message>>( codec );

        // Decode the PDU
        ldapDecoder.decode( stream, ldapMessageContainer );
    }
}
