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
package org.apache.directory.api.ldap.extras.controls.ad;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.EncoderException;
import org.apache.directory.api.asn1.util.Asn1Buffer;
import org.apache.directory.api.ldap.extras.AbstractCodecServiceTest;
import org.apache.directory.api.ldap.extras.controls.ad_impl.AdPolicyHintsDecorator;
import org.apache.directory.api.ldap.extras.controls.ad_impl.AdPolicyHintsFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mycila.junit.concurrent.Concurrency;
import com.mycila.junit.concurrent.ConcurrentJunitRunner;


/**
 *
 * TestCase for AdPolicyHints Control.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrency()
public class AdPolicyHintsControlTest extends AbstractCodecServiceTest
{
    @Before
    public void init()
    {
        codec.registerRequestControl( new AdPolicyHintsFactory( codec ) );
    }
    
    
    @Test( expected=DecoderException.class )
    public void testAdPolicyHiontsControlNoFlag() throws DecoderException
    {
        ByteBuffer bb = ByteBuffer.allocate( 0x02 );

        bb.put( new byte[]
            {
                0x30, 0x00                  // PolicyHintsRequestValue ::= SEQUENCE {
            } );

        bb.flip();

        AdPolicyHints decorator = new AdPolicyHintsDecorator( codec );

        ( ( AdPolicyHintsDecorator ) decorator ).decode( bb.array() );
    }


    @Test( expected=DecoderException.class )
    public void testAdPolicyHiontsControlEmptyFlag() throws DecoderException
    {
        ByteBuffer bb = ByteBuffer.allocate( 0x04 );

        bb.put( new byte[]
            {
                0x30, 0x02,                  // PolicyHintsRequestValue ::= SEQUENCE {
                  0x02, 0x00                 //      Flags    INTEGER
            } );

        bb.flip();

        AdPolicyHints decorator = new AdPolicyHintsDecorator( codec );

        ( ( AdPolicyHintsDecorator ) decorator ).decode( bb.array() );
    }


    @Test
    public void testAdPolicyHintsControlLengthConstraintEnforced() throws DecoderException, EncoderException
    {
        ByteBuffer bb = ByteBuffer.allocate( 0x05 );

        bb.put( new byte[]
            {
                0x30, 0x03,                  // PolicyHintsRequestValue ::= SEQUENCE {
                  0x02, 0x01, 0x01           //      Flags    INTEGER
            } );

        bb.flip();

        AdPolicyHints decorator = new AdPolicyHintsDecorator( codec );

        AdPolicyHints adPolicyHints = ( AdPolicyHints ) ( ( AdPolicyHintsDecorator ) decorator ).decode( bb.array() );
        
        assertEquals( 1, adPolicyHints.getFlags() );

        // test encoding
        ByteBuffer buffer = ( ( AdPolicyHintsDecorator ) adPolicyHints ).encode( ByteBuffer
            .allocate( ( ( AdPolicyHintsDecorator ) adPolicyHints ).computeLength() ) );
        assertArrayEquals( bb.array(), buffer.array() );

        // Check the reverse encoding
        Asn1Buffer asn1Buffer = new Asn1Buffer();

        AdPolicyHintsFactory factory = ( AdPolicyHintsFactory ) codec.getRequestControlFactories().get( AdPolicyHints.OID );
        factory.encodeValue( asn1Buffer, adPolicyHints );

        assertArrayEquals( bb.array(),  asn1Buffer.getBytes().array() );
    }


    @Test
    public void testAdPolicyHintsControlLengthConstraintNotEnforced() throws DecoderException, EncoderException
    {
        ByteBuffer bb = ByteBuffer.allocate( 0x05 );

        bb.put( new byte[]
            {
                0x30, 0x03,                  // PolicyHintsRequestValue ::= SEQUENCE {
                  0x02, 0x01, 0x00           //      Flags    INTEGER
            } );

        bb.flip();

        AdPolicyHints decorator = new AdPolicyHintsDecorator( codec );

        AdPolicyHints adPolicyHints = ( AdPolicyHints ) ( ( AdPolicyHintsDecorator ) decorator ).decode( bb.array() );
        
        assertEquals( 0, adPolicyHints.getFlags() );

        // test encoding
        ByteBuffer buffer = ( ( AdPolicyHintsDecorator ) adPolicyHints ).encode( ByteBuffer
            .allocate( ( ( AdPolicyHintsDecorator ) adPolicyHints ).computeLength() ) );
        assertArrayEquals( bb.array(), buffer.array() );

        // Check the reverse encoding
        Asn1Buffer asn1Buffer = new Asn1Buffer();

        AdPolicyHintsFactory factory = ( AdPolicyHintsFactory ) codec.getRequestControlFactories().get( AdPolicyHints.OID );
        factory.encodeValue( asn1Buffer, adPolicyHints );

        assertArrayEquals( bb.array(),  asn1Buffer.getBytes().array() );
    }
}
