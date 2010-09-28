/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.shared.ldap.jndi;


import java.util.Hashtable;
import java.util.Map;

import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.ContextNotEmptyException;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.NoPermissionException;
import javax.naming.OperationNotSupportedException;
import javax.naming.PartialResultException;
import javax.naming.ReferralException;
import javax.naming.ServiceUnavailableException;
import javax.naming.TimeLimitExceededException;
import javax.naming.directory.AttributeInUseException;
import javax.naming.directory.InvalidAttributeIdentifierException;
import javax.naming.directory.InvalidAttributeValueException;
import javax.naming.directory.InvalidSearchFilterException;
import javax.naming.directory.NoSuchAttributeException;
import javax.naming.directory.SchemaViolationException;
import javax.naming.ldap.BasicControl;
import javax.naming.ldap.ExtendedRequest;
import javax.naming.ldap.ExtendedResponse;
import javax.naming.ldap.LdapName;

import org.apache.directory.shared.ldap.codec.MessageTypeEnum;
import org.apache.directory.shared.ldap.codec.controls.ControlImpl;
import org.apache.directory.shared.ldap.exception.LdapAffectMultipleDsaException;
import org.apache.directory.shared.ldap.exception.LdapAliasDereferencingException;
import org.apache.directory.shared.ldap.exception.LdapAliasException;
import org.apache.directory.shared.ldap.exception.LdapAttributeInUseException;
import org.apache.directory.shared.ldap.exception.LdapAuthenticationException;
import org.apache.directory.shared.ldap.exception.LdapAuthenticationNotSupportedException;
import org.apache.directory.shared.ldap.exception.LdapContextNotEmptyException;
import org.apache.directory.shared.ldap.exception.LdapEntryAlreadyExistsException;
import org.apache.directory.shared.ldap.exception.LdapInvalidAttributeTypeException;
import org.apache.directory.shared.ldap.exception.LdapInvalidAttributeValueException;
import org.apache.directory.shared.ldap.exception.LdapInvalidDnException;
import org.apache.directory.shared.ldap.exception.LdapInvalidSearchFilterException;
import org.apache.directory.shared.ldap.exception.LdapLoopDetectedException;
import org.apache.directory.shared.ldap.exception.LdapNoPermissionException;
import org.apache.directory.shared.ldap.exception.LdapNoSuchAttributeException;
import org.apache.directory.shared.ldap.exception.LdapNoSuchObjectException;
import org.apache.directory.shared.ldap.exception.LdapOperationErrorException;
import org.apache.directory.shared.ldap.exception.LdapOtherException;
import org.apache.directory.shared.ldap.exception.LdapPartialResultException;
import org.apache.directory.shared.ldap.exception.LdapProtocolErrorException;
import org.apache.directory.shared.ldap.exception.LdapReferralException;
import org.apache.directory.shared.ldap.exception.LdapSchemaViolationException;
import org.apache.directory.shared.ldap.exception.LdapServiceUnavailableException;
import org.apache.directory.shared.ldap.exception.LdapTimeLimitExceededException;
import org.apache.directory.shared.ldap.exception.LdapUnwillingToPerformException;
import org.apache.directory.shared.ldap.message.LdapResult;
import org.apache.directory.shared.ldap.message.ResultResponse;
import org.apache.directory.shared.ldap.message.control.Control;
import org.apache.directory.shared.ldap.name.DN;


/**
 * An utility class to convert back and forth JNDI classes to ADS classes.
 *
 * TODO: We have a similar class in shared-ldap!!!
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class JndiUtils
{
    /**
     * Private constructor for utility class.
     */
    private JndiUtils()
    {
    }


    /**
     * Converts Apache Directory control the to an JNDI control.
     *
     * @param control the Apache Directory control
     * @return the JNDI control
     */
    public static javax.naming.ldap.Control toJndiControl( Control control )
    {
        byte[] value = control.getValue();
        javax.naming.ldap.Control jndiControl = new BasicControl( control.getOid(), control.isCritical(), value );

        return jndiControl;
    }


    /**
     * Converts the Apache Directory controls to JNDI controls.
     *
     * @param controls the Apache Directory controls
     * @return the JNDI controls
     */
    public static javax.naming.ldap.Control[] toJndiControls( Control... controls )
    {
        if ( controls != null )
        {
            javax.naming.ldap.Control[] jndiControls = new javax.naming.ldap.Control[controls.length];
            int i = 0;

            for ( Control control : controls )
            {
                jndiControls[i++] = toJndiControl( control );
            }

            return jndiControls;
        }
        else
        {
            return null;
        }
    }


    /**
     * Converts the JNDI control to an Apache Directory control.
     *
     * @param jndiControl the JNDI control
     * @return the Apache Directory control
     */
    public static Control fromJndiControl( javax.naming.ldap.Control jndiControl )
    {
        Control control = new ControlImpl( jndiControl.getID() );

        control.setValue( jndiControl.getEncodedValue() );

        return control;
    }


    /**
     * Converts the JNDI controls to Apache Directory controls.
     *
     * @param jndiControls the JNDI controls
     * @return the Apache Directory controls
     */
    public static Control[] fromJndiControls( javax.naming.ldap.Control... jndiControls )
    {
        if ( jndiControls != null )
        {
            Control[] controls = new Control[jndiControls.length];
            int i = 0;

            for ( javax.naming.ldap.Control jndiControl : jndiControls )
            {
                controls[i++] = fromJndiControl( jndiControl );
            }

            return controls;
        }
        else
        {
            return null;
        }
    }


    /**
     * Converts an Apache Directory extended request to an JNDI extended request.
     * TODO: This is NOT correct ATM
     *
     * @param request the Apache Directory extended request
     * @return the JDNI extended response
     */
    public static ExtendedResponse toJndiExtendedResponse(
        final org.apache.directory.shared.ldap.message.ExtendedRequest request )
    {
        class JndiExtendedResponse implements ExtendedResponse
        {
            private static final long serialVersionUID = 2535213952391003045L;


            /**
             * {@inheritDoc}
             */
            public byte[] getEncodedValue()
            {
                return request.getRequestValue();
            }


            /**
             * {@inheritDoc}
             */
            public String getID()
            {
                return request.getRequestName();
            }
        }

        return new JndiExtendedResponse();
    }


    /**
     * Transform a JNDI extended request to an Apache Directory extended request
     *
     * @param request The JNDI extended request
     * @return An Apache Directory extended request
     */
    public static ExtendedRequest toJndiExtendedRequest(
        final org.apache.directory.shared.ldap.message.ExtendedRequest request )
    {
        class JndiExtendedRequest implements ExtendedRequest
        {
            private static final long serialVersionUID = 2887613172871934108L;


            /**
             * {@inheritDoc}
             */
            public ExtendedResponse createExtendedResponse( String id, byte[] berValue, int offset, int length )
                throws NamingException
            {
                return toJndiExtendedResponse( request );
            }


            /**
             * {@inheritDoc}
             */
            public byte[] getEncodedValue()
            {
                return request.getRequestValue();
            }


            /**
             * {@inheritDoc}
             */
            public String getID()
            {
                return request.getRequestName();
            }

        }

        return new JndiExtendedRequest();
    }


    /**
     * Transform a JNDI extended response to an internal ExtendedResponse
     *
     * @param request The JNDI extendedRequest
     * @return An internal ExtendedResponse
     */
    public static org.apache.directory.shared.ldap.message.ExtendedResponse fromJndiExtendedResponse(
        final ExtendedRequest request )
    {
        class ServerExtendedResponse implements org.apache.directory.shared.ldap.message.ExtendedResponse
        {
            public static final long serialVersionUID = 1L;


            /**
             * {@inheritDoc}
             */
            public String getResponseName()
            {
                return request.getID();
            }


            /**
             * {@inheritDoc}
             */
            public byte[] getResponseValue()
            {
                return request.getEncodedValue();
            }


            /**
             * {@inheritDoc}
             */
            public void setResponseName( String oid )
            {
            }


            /**
             * {@inheritDoc}
             */
            public void setResponseValue( byte[] responseValue )
            {
            }


            /**
             * {@inheritDoc}
             */
            public LdapResult getLdapResult()
            {
                return null;
            }


            /**
             * {@inheritDoc}
             */
            public void addAllControls( Control[] controls )
            {
            }


            /**
             * {@inheritDoc}
             */
            public void addControl( Control control )
            {
            }


            /**
             * {@inheritDoc}
             */
            public Object get( Object key )
            {
                return null;
            }


            /**
             * {@inheritDoc}
             */
            public Control getControl( String oid )
            {
                return null;
            }


            /**
             * {@inheritDoc}
             */
            public Map<String, Control> getControls()
            {
                return null;
            }


            /**
             * {@inheritDoc}
             */
            public Control getCurrentControl()
            {
                return null;
            }


            /**
             * {@inheritDoc}
             */
            public int getMessageId()
            {
                return 0;
            }


            /**
             * {@inheritDoc}
             */
            public MessageTypeEnum getType()
            {
                return null;
            }


            /**
             * {@inheritDoc}
             */
            public boolean hasControl( String oid )
            {
                return false;
            }


            /**
             * {@inheritDoc}
             */
            public Object put( Object key, Object value )
            {
                return null;
            }


            /**
             * {@inheritDoc}
             */
            public void removeControl( Control control )
            {
            }


            /**
             * {@inheritDoc}
             */
            public void setMessageId( int messageId )
            {
            }


            /**
             * {@inheritDoc}
             */
            public byte[] getEncodedValue()
            {
                return null;
            }


            /**
             * {@inheritDoc}
             */
            public String getID()
            {
                return null;
            }
        }

        return new ServerExtendedResponse();
    }


    /**
     * Transform a JNDI extended request to an internal ExtendedRequest
     *
     * @param request The JNDI extendedRequest
     * @return An internal ExtendedRequest
     */
    public static org.apache.directory.shared.ldap.message.ExtendedRequest fromJndiExtendedRequest(
        final ExtendedRequest request )
    {
        class ServerExtendedRequest implements org.apache.directory.shared.ldap.message.ExtendedRequest
        {
            /**
             * {@inheritDoc}
             */
            public String getRequestName()
            {
                return request.getID();
            }


            /**
             * {@inheritDoc}
             */
            public byte[] getRequestValue()
            {
                return request.getEncodedValue();
            }


            /**
             * {@inheritDoc}
             */
            public void setRequestName( String oid )
            {
            }


            /**
             * {@inheritDoc}
             */
            public void setRequestValue( byte[] requestValue )
            {
            }


            /**
             * {@inheritDoc}
             */
            public MessageTypeEnum getResponseType()
            {
                return null;
            }


            /**
             * {@inheritDoc}
             */
            public ResultResponse getResultResponse()
            {
                return null;
            }


            /**
             * {@inheritDoc}
             */
            public boolean hasResponse()
            {
                return false;
            }


            /**
             * {@inheritDoc}
             */
            public void addAllControls( Control[] controls )
            {
            }


            /**
             * {@inheritDoc}
             */
            public void addControl( Control control )
            {
            }


            /**
             * {@inheritDoc}
             */
            public Object get( Object key )
            {
                return null;
            }


            /**
             * {@inheritDoc}
             */
            public Control getControl( String oid )
            {
                return null;
            }


            /**
             * {@inheritDoc}
             */
            public Map<String, Control> getControls()
            {
                return null;
            }


            /**
             * {@inheritDoc}
             */
            public Control getCurrentControl()
            {
                return null;
            }


            /**
             * {@inheritDoc}
             */
            public int getMessageId()
            {
                return 0;
            }


            /**
             * {@inheritDoc}
             */
            public MessageTypeEnum getType()
            {
                return null;
            }


            /**
             * {@inheritDoc}
             */
            public boolean hasControl( String oid )
            {
                return false;
            }


            /**
             * {@inheritDoc}
             */
            public Object put( Object key, Object value )
            {
                return null;
            }


            /**
             * {@inheritDoc}
             */
            public void removeControl( Control control )
            {
            }


            /**
             * {@inheritDoc}
             */
            public void setMessageId( int messageId )
            {
            }
        }

        return new ServerExtendedRequest();
    }


    /**
     * Wrap an internal LdapException to a JNDI Exception
     *
     * @param t The internal exception
     * @throws NamingException The equivalent NamingException
     */
    public static void wrap( Throwable t ) throws NamingException
    {
        if ( t instanceof NamingException )
        {
            throw ( NamingException ) t;
        }

        NamingException ne = null;

        if ( t instanceof LdapAffectMultipleDsaException )
        {
            ne = new NamingException( t.getLocalizedMessage() );
        }
        else if ( t instanceof LdapAliasDereferencingException )
        {
            ne = new NamingException( t.getLocalizedMessage() );
        }
        else if ( t instanceof LdapAliasException )
        {
            ne = new NamingException( t.getLocalizedMessage() );
        }
        else if ( t instanceof LdapAttributeInUseException )
        {
            ne = new AttributeInUseException( t.getLocalizedMessage() );
        }
        else if ( t instanceof LdapAuthenticationException )
        {
            ne = new AuthenticationException( t.getLocalizedMessage() );
        }
        else if ( t instanceof LdapAuthenticationNotSupportedException )
        {
            ne = new AuthenticationNotSupportedException( t.getLocalizedMessage() );
        }
        else if ( t instanceof LdapContextNotEmptyException )
        {
            ne = new ContextNotEmptyException( t.getLocalizedMessage() );
        }
        else if ( t instanceof LdapEntryAlreadyExistsException )
        {
            ne = new NameAlreadyBoundException( t.getLocalizedMessage() );
        }
        else if ( t instanceof LdapInvalidAttributeTypeException )
        {
            ne = new InvalidAttributeIdentifierException( t.getLocalizedMessage() );
        }
        else if ( t instanceof LdapInvalidAttributeValueException )
        {
            ne = new InvalidAttributeValueException( t.getLocalizedMessage() );
        }
        else if ( t instanceof LdapInvalidDnException )
        {
            ne = new InvalidNameException( t.getLocalizedMessage() );
        }
        else if ( t instanceof LdapInvalidSearchFilterException )
        {
            ne = new InvalidSearchFilterException( t.getLocalizedMessage() );
        }
        else if ( t instanceof LdapLoopDetectedException )
        {
            ne = new NamingException( t.getLocalizedMessage() );
        }
        else if ( t instanceof LdapNoPermissionException )
        {
            ne = new NoPermissionException( t.getLocalizedMessage() );
        }
        else if ( t instanceof LdapNoSuchAttributeException )
        {
            ne = new NoSuchAttributeException( t.getLocalizedMessage() );
        }
        else if ( t instanceof LdapNoSuchObjectException )
        {
            ne = new NameNotFoundException( t.getLocalizedMessage() );
        }
        else if ( t instanceof LdapOperationErrorException )
        {
            ne = new NamingException( t.getLocalizedMessage() );
        }
        else if ( t instanceof LdapOtherException )
        {
            ne = new NamingException( t.getLocalizedMessage() );
        }
        else if ( t instanceof LdapProtocolErrorException )
        {
            ne = new CommunicationException( t.getLocalizedMessage() );
        }
        else if ( t instanceof LdapReferralException )
        {
            ne = new WrappedReferralException( ( LdapReferralException ) t );
        }
        else if ( t instanceof LdapPartialResultException )
        {
            ne = new WrappedPartialResultException( ( LdapPartialResultException ) t );
        }
        else if ( t instanceof LdapSchemaViolationException )
        {
            ne = new SchemaViolationException( t.getLocalizedMessage() );
        }
        else if ( t instanceof LdapServiceUnavailableException )
        {
            ne = new ServiceUnavailableException( t.getLocalizedMessage() );
        }
        else if ( t instanceof LdapTimeLimitExceededException )
        {
            ne = new TimeLimitExceededException( t.getLocalizedMessage() );
        }
        else if ( t instanceof LdapUnwillingToPerformException )
        {
            ne = new OperationNotSupportedException( t.getLocalizedMessage() );
        }
        else
        {
            ne = new NamingException( t.getLocalizedMessage() );
        }

        ne.setRootCause( t );

        throw ne;
    }


    /**
     * Convert a {@link DN} to a {@link javax.naming.Name}
     *
     * @param dn The DN to convert
     * @return A Name
     */
    public static Name toName( DN dn )
    {
        try
        {
            Name name = new LdapName( dn.toString() );

            return name;
        }
        catch ( InvalidNameException ine )
        {
            // Logically, the DN must be valid.
            return null;
        }
    }


    /**
     * Convert a {@link javax.naming.Name} to a DN
     *
     * @param name The Name to convert
     * @return A DN
     */
    public static DN fromName( Name name )
    {
        try
        {
            DN dn = new DN( name.toString() );

            return dn;
        }
        catch ( LdapInvalidDnException lide )
        {
            // Logically, the Name must be valid.
            return null;
        }
    }
}

// a ReferralException around the LdapReferralException to be used in tests
class WrappedReferralException extends ReferralException
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The wrapped referral exception. */
    private LdapReferralException lre;


    /**
     * Creates a new instance of WrappedReferralException.
     *
     * @param lre the wrapped referral exception
     */
    public WrappedReferralException( LdapReferralException lre )
    {
        this.lre = lre;
    }


    @Override
    public boolean skipReferral()
    {
        return lre.skipReferral();
    }


    @Override
    public void retryReferral()
    {
        lre.retryReferral();
    }


    @Override
    public Object getReferralInfo()
    {
        return lre.getReferralInfo();
    }


    @Override
    public Context getReferralContext( Hashtable<?, ?> env ) throws NamingException
    {
        return lre.getReferralContext( env );
    }


    @Override
    public Context getReferralContext() throws NamingException
    {
        return lre.getReferralContext();
    }


    @Override
    public Name getRemainingName()
    {
        return JndiUtils.toName( lre.getRemainingDn() );
    }


    @Override
    public Object getResolvedObj()
    {
        return lre.getResolvedObject();
    }


    @Override
    public Name getResolvedName()
    {
        return JndiUtils.toName( lre.getResolvedDn() );
    }
}

// a PartialResultException around the LdapPartialResultException to be used in tests
class WrappedPartialResultException extends PartialResultException
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The wrapped partial result exception. */
    private LdapPartialResultException lpre;


    /**
     * Instantiates a new wrapped partial result exception.
     *
     * @param lpre the wrapped partial result exception
     */
    public WrappedPartialResultException( LdapPartialResultException lpre )
    {
        this.lpre = lpre;
    }


    @Override
    public Name getRemainingName()
    {
        return JndiUtils.toName( lpre.getRemainingDn() );
    }


    @Override
    public Object getResolvedObj()
    {
        return lpre.getResolvedObject();
    }


    @Override
    public Name getResolvedName()
    {
        return JndiUtils.toName( lpre.getResolvedDn() );
    }
}