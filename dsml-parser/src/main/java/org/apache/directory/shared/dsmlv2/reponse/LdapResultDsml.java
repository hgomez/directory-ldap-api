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

package org.apache.directory.shared.dsmlv2.reponse;


import java.util.List;

import org.apache.directory.shared.dsmlv2.DsmlDecorator;
import org.apache.directory.shared.dsmlv2.ParserUtils;
import org.apache.directory.shared.ldap.message.LdapResult;
import org.apache.directory.shared.ldap.message.Message;
import org.apache.directory.shared.ldap.message.ResultCodeEnum;
import org.apache.directory.shared.ldap.name.DN;
import org.apache.directory.shared.ldap.util.LdapURL;
import org.dom4j.Element;


/**
 * DSML Decorator for the LdapResult class.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapResultDsml implements DsmlDecorator
{
    /** The LDAP Result to decorate */
    private LdapResult result;

    /** The associated LDAP Message */
    private Message message;


    /**
     * Creates a new instance of LdapResultDsml.
     *
     * @param result
     *      the LdapResult to decorate
     * @param message
     *      the associated message
     */
    public LdapResultDsml( LdapResult result, Message message )
    {
        this.result = result;
        this.message = message;
    }


    /* (non-Javadoc)
     * @see org.apache.directory.shared.dsmlv2.reponse.DsmlDecorator#toDsml(org.dom4j.Element)
     */
    public Element toDsml( Element root )
    {

        // RequestID
        int requestID = message.getMessageId();
        if ( requestID != 0 )
        {
            root.addAttribute( "requestID", "" + requestID );
        }

        // Matched DN
        String matchedDN = result.getMatchedDn().getName();

        if ( !matchedDN.equals( "" ) )
        {
            root.addAttribute( "matchedDN", matchedDN );
        }

        // Controls
        ParserUtils.addControls( root, message.getControls().values() );

        // ResultCode
        Element resultCodeElement = root.addElement( "resultCode" );
        resultCodeElement.addAttribute( "code", "" + result.getResultCode().getResultCode() );
        resultCodeElement.addAttribute( "descr", LdapResultEnum.getResultCodeDescr( result.getResultCode() ) );

        // ErrorMessage
        String errorMessage = ( result.getErrorMessage() );
        if ( ( errorMessage != null ) && ( !errorMessage.equals( "" ) ) )
        {
            Element errorMessageElement = root.addElement( "errorMessage" );
            errorMessageElement.addText( errorMessage );
        }

        // Referals
        List<String> referals = ( List<String> ) result.getReferral().getLdapUrls();

        if ( referals != null )
        {
            for ( String url : referals )
            {
                Element referalElement = root.addElement( "referal" );
                referalElement.addText( url );
            }
        }

        return root;
    }


    /**
     * Get the error message
     * 
     * @return Returns the errorMessage.
     */
    public String getErrorMessage()
    {
        return result.getErrorMessage();
    }


    /**
     * Set the error message
     * 
     * @param errorMessage The errorMessage to set.
     */
    public void setErrorMessage( String errorMessage )
    {
        result.setErrorMessage( errorMessage );
    }


    /**
     * Get the matched DN
     * 
     * @return Returns the matchedDN.
     */
    public String getMatchedDn()
    {
        return result.getMatchedDn().getName();
    }


    /**
     * Set the Matched DN
     * 
     * @param matchedDN The matchedDN to set.
     */
    public void setMatchedDn( DN matchedDN )
    {
        result.setMatchedDn( matchedDN );
    }


    /**
     * Get the referrals
     * 
     * @return Returns the referrals.
     */
    public List<String> getReferrals()
    {
        return ( List<String> ) result.getReferral().getLdapUrls();
    }


    /**
     * Add a referral
     * 
     * @param referral The referral to add.
     */
    public void addReferral( LdapURL referral )
    {
        result.getReferral().addLdapUrl( referral.toString() );
    }


    /**
     * Get the result code
     * 
     * @return Returns the resultCode.
     */
    public ResultCodeEnum getResultCode()
    {
        return result.getResultCode();
    }


    /**
     * Set the result code
     * 
     * @param resultCode The resultCode to set.
     */
    public void setResultCode( ResultCodeEnum resultCode )
    {
        result.setResultCode( resultCode );
    }
}