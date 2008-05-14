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
package org.apache.directory.shared.ldap.message;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;

import org.apache.directory.shared.ldap.util.AttributeUtils;
import org.apache.directory.shared.ldap.util.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Permanently Lockable ordered JNDI Attribute implementation.
 * 
 * @author <a href="mailto:dev@directory.apache.org"> Apache Directory Project</a>
 * @version $Rev$
 */
public class AttributeImpl implements Attribute
{
    private static final Logger log = LoggerFactory.getLogger( AttributeImpl.class );

    private static final long serialVersionUID = -5158233254341746514L;

    /** the name of the attribute, case sensitive */
    private final String upId;

    /** In case we have only one value, just use this container */
    private Object value;

    /** the list of attribute values, if unordered */
    private List<Object> list;

    /** The number of values stored */
    private int size = 0;


    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Creates a permanently Attribute on id whose locking behavior is
     * dictated by parent.
     * 
     * @param id
     *            the id or name of this attribute.
     */
    public AttributeImpl( String id )
    {
        upId = id;
        value = null;
        list = null;
        size = 0;
    }


    /**
     * Creates a permanently Attribute on id with a single value.
     * 
     * @param id
     *            the id or name of this attribute.
     * @param value
     *            a value for the attribute
     */
    public AttributeImpl( String id, Object value )
    {
        upId = id;
        list = null;
        this.value = AttributeUtils.cloneValue( value );
        size = 1;
    }


    /**
     * Creates a permanently Attribute on id with a single value.
     * 
     * @param id
     *            the id or name of this attribute.
     * @param value
     *            a value for the attribute
     */
    public AttributeImpl( String id, byte[] value )
    {
        upId = id;
        list = null;
        this.value = AttributeUtils.cloneValue( value );
        size = 1;
    }


    /**
     * Create a copy of an Attribute, be it an AttributeImpl
     * instance of a BasicAttribute instance
     * 
     * @param attribute the Attribute instace to copy
     * @throws
     */
    public AttributeImpl( Attribute attribute ) throws NamingException
    {
        if ( attribute == null )
        {
            throw new NamingException( "Null attribute is not allowed" );
        }
        else if ( attribute instanceof AttributeImpl )
        {
            AttributeImpl clone = ( AttributeImpl ) attribute.clone();

            upId = clone.upId;
            list = clone.list;
            size = clone.size;
            value = clone.value;
        }
        else if ( attribute instanceof BasicAttribute )
        {
            upId = attribute.getID();

            NamingEnumeration<?> values = attribute.getAll();

            while ( values.hasMoreElements() )
            {
                add( values.nextElement() );
            }
        }
        else
        {
            throw new NamingException( "Attribute must be an instance of BasicAttribute or AttributeImpl" );
        }
    }


    // ------------------------------------------------------------------------
    // javax.naming.directory.Attribute Interface Method Implementations
    // ------------------------------------------------------------------------

    /**
     * Gets a NamingEnumberation wrapped around the iterator of the value list.
     * 
     * @return the Iterator wrapped as a NamingEnumberation.
     */
    public NamingEnumeration<Object> getAll()
    {
        if ( size < 2 )
        {
            return new IteratorNamingEnumeration<Object>( new Iterator<Object>()
            {
                private boolean more = ( size != 0 );


                public boolean hasNext()
                {
                    return more;
                }


                public Object next()
                {
                    more = false;
                    return value;
                }


                public void remove()
                {
                    value = null;
                    more = true;
                    size = 0;
                }
            } );
        }
        else
        {
            return new IteratorNamingEnumeration<Object>( list.iterator() );
        }
    }


    /**
     * Gets the first value of the list or null if no values exist.
     * 
     * @return the first value or null.
     */
    public Object get()
    {
        if ( size < 2 )
        {
            return value;
        }
        else
        {
            return list.get( 0 );
        }
    }


    /**
     * Gets the size of the value list.
     * 
     * @return size of the value list.
     */
    public int size()
    {
        return size;
    }


    /**
     * Gets the id or name of this Attribute.
     * 
     * @return the identifier for this Attribute.
     */
    public String getID()
    {
        return upId;
    }


    /**
     * Checks to see if this Attribute contains attrVal in the list.
     * 
     * @param attrVal
     *            the value to test for
     * @return true if attrVal is in the list backing store, false otherwise
     */
    public boolean contains( Object attrVal )
    {
        switch ( size )
        {
            case 0:
                return false;

            case 1:
                return AttributeUtils.equals( value, attrVal );

            default:
                Iterator<Object> values = list.iterator();

                while ( values.hasNext() )
                {
                    if ( AttributeUtils.equals( values.next(), attrVal ) )
                    {
                        return true;
                    }
                }

                return false;
        }
    }


    /**
     * Adds attrVal into the list of this Attribute's values at the end of the
     * list.
     * 
     * @param attrVal
     *            the value to add to the end of the list.
     * @return true if attrVal is added to the list backing store, false if it
     *         already existed there.
     */
    public boolean add( Object attrVal )
    {
        boolean exists = false;

        if ( contains( attrVal ) )
        {
            // Do not duplicate values
            return true;
        }

        // First copy the value
        attrVal = AttributeUtils.cloneValue( attrVal );

        switch ( size )
        {
            case 0:
                value = attrVal;
                size++;
                return true;

            case 1:
                exists = contains( attrVal );

                if ( exists )
                {
                    // Don't add two times the same value
                    return true;
                }
                else
                {
                    list = new ArrayList<Object>();
                    list.add( value );
                    list.add( attrVal );
                    size++;
                    value = null;
                    return true;
                }

            default:
                exists = contains( attrVal );

                list.add( attrVal );
                size++;
                return exists;
        }
    }


    /**
     * Removes attrVal from the list of this Attribute's values.
     * 
     * @param attrVal
     *            the value to remove
     * @return true if attrVal is remove from the list backing store, false if
     *         never existed there.
     */
    public boolean remove( Object attrVal )
    {
        switch ( size )
        {
            case 0:
                return false;

            case 1:
                if ( contains( attrVal ) )
                {
                    value = null;
                    size--;
                    return true;
                }
                else
                {
                    return false;
                }

            case 2:
                if ( contains( attrVal ) )
                {
                    if ( attrVal instanceof byte[] )
                    {
                        for ( Object val : list )
                        {
                            if ( val instanceof byte[] )
                            {
                                if ( Arrays.equals( ( byte[] )val, ( byte[] ) attrVal ) )
                                {
                                    list.remove( val );
                                    value = list.get( 0 );
                                    size = 1;
                                    list = null;
                                    return true;
                                }
                            }
                        }
    
                        return false;
                    }
                    else
                    {
                        list.remove( attrVal );
                        value = list.get( 0 );
                        size = 1;
                        list = null;
                        return true;
                    }
                }
                else
                {
                    return false;
                }

            default:
                if ( contains( attrVal ) )
                {
                    if ( attrVal instanceof byte[] )
                    {
                        for ( Object val : list )
                        {
                            if ( val instanceof byte[] )
                            {
                                if ( Arrays.equals( ( byte[] ) val, ( byte[] ) attrVal ) )
                                {
                                    list.remove( val );
                                    size--;
                                    return true;
                                }
                            }
                        }
    
                        return false;
                    }
                    else
                    {
                        list.remove( attrVal );
                        size--;
                        return true;
                    }
                }
                else
                {
                    return false;
                }
        }
    }


    /**
     * Removes all the values of this Attribute from the list backing store.
     */
    public void clear()
    {
        switch ( size )
        {
            case 0:
                return;

            case 1:
                value = null;
                size = 0;
                return;

            default:
                list = null;
                size = 0;
                return;
        }
    }


    /**
     * NOT SUPPORTED - throws OperationNotSupportedException
     * 
     * @see javax.naming.directory.Attribute#getAttributeSyntaxDefinition()
     */
    public DirContext getAttributeSyntaxDefinition() throws NamingException
    {
        throw new OperationNotSupportedException( "Extending subclasses may override this if they like!" );
    }


    /**
     * NOT SUPPORTED - throws OperationNotSupportedException
     * 
     * @see javax.naming.directory.Attribute#getAttributeDefinition()
     */
    public DirContext getAttributeDefinition() throws NamingException
    {
        throw new OperationNotSupportedException( "Extending subclasses may override this if they like!" );
    }


    /**
     * Not a deep clone.
     * 
     * @return a copy of this attribute using the same parent lock and id
     *         containing references to all the values of the original.
     */
    public Object clone()
    {
        try
        {
            AttributeImpl clone = ( AttributeImpl ) super.clone();

            if ( size < 2 )
            {
                clone.value = AttributeUtils.cloneValue( value );
            }
            else
            {
                clone.list = new ArrayList<Object>( size );

                for ( int i = 0; i < size; i++ )
                {
                    Object newValue = AttributeUtils.cloneValue( list.get( i ) );
                    clone.list.add( newValue );
                }
            }

            return clone;
        }
        catch ( CloneNotSupportedException cnse )
        {
            return null;
        }
    }


    /**
     * Always returns true since list is used to preserve value addition order.
     * 
     * @return true.
     */
    public boolean isOrdered()
    {
        return true;
    }


    /**
     * Gets the value at an index.
     * 
     * @param index
     *            the index of the value in the ordered list of attribute
     *            values. 0 <= ix < size().
     * @return this Attribute's value at the index null if no values exist.
     */
    public Object get( int index )
    {
        if ( ( index < 0 ) || ( index > size + 1 ) )
        {
            return null;
        }

        switch ( size )
        {
            case 0:
                return null;

            case 1:
                return value;

            default:
                return list.get( index );
        }
    }


    /**
     * Removes the value at an index.
     * 
     * @param index
     *            the index of the value in the ordered list of attribute
     *            values. 0 <= ix < size().
     * @return this Attribute's value removed at the index
     */
    public Object remove( int index )
    {
        if ( ( index < 0 ) || ( index > size + 1 ) )
        {
            return null;
        }

        switch ( size )
        {
            case 0:
                return null;

            case 1:
                Object result = value;
                value = null;
                size = 0;
                return result;

            case 2:
                Object removed = list.remove( index );
                value = list.get( 0 );
                size = 1;
                list = null;
                return removed;

            default:
                size--;

                return list.remove( index );
        }
    }


    /**
     * Inserts attrVal into the list of this Attribute's values at the specified
     * index in the list.
     * 
     * @param index
     *            the index to add the value at.
     * @param attrVal
     *            the value to add to the end of the list.
     */
    public void add( int index, Object attrVal )
    {
        // First copy the value
        attrVal = AttributeUtils.cloneValue( attrVal );

        switch ( size )
        {
            case 0:
                size++;
                value = attrVal;
                return;

            case 1:
                list = new ArrayList<Object>();

                if ( index == 0 )
                {
                    list.add( attrVal );
                    list.add( value );
                }
                else
                {
                    list.add( value );
                    list.add( attrVal );
                }

                size++;
                value = null;
                return;

            default:
                list.add( index, attrVal );
                size++;
                return;
        }
    }


    /**
     * Sets an attribute value in the ordered list of attribute values.
     * 
     * @param index
     *            the index to set the value to.
     * @param attrVal
     *            the value to set at the index.
     * @return the old value at the specified index.
     */
    public Object set( int index, Object attrVal )
    {
        // First copy the value
        attrVal = AttributeUtils.cloneValue( attrVal );

        switch ( size )
        {
            case 0:
                size++;
                value = attrVal;
                return null;

            case 1:
                if ( index == 0 )
                {
                    Object result = value;
                    value = attrVal;
                    return result;
                }
                else
                {
                    list = new ArrayList<Object>();
                    list.add( value );
                    list.add( attrVal );
                    size = 2;
                    value = null;
                    return null;
                }

            default:
                Object oldValue = list.get( index );
                list.set( index, attrVal );
                return oldValue;
        }
    }
    
    
    /**
     * Compute the hash code for this attribute. It's a combinaison
     * of the ID and all the values' hash codes.
     * 
     * @see Object#hashCode()
     * @return the instance's hash code 
     */
    public int hashCode()
    {
        int hash = 37;
        
        hash += hash*17 + StringTools.toLowerCase( getID() ).hashCode();
        
        if ( ( list != null ) && ( list.size() != 0 ) )
        {
            for ( Object value:list )
            {
                if ( value instanceof byte[] )
                {
                    hash += hash*17 + Arrays.hashCode( (byte[])value );
                }
                else 
                {
                    hash += hash*17 + value.hashCode();
                }
            }
        }
        
        return hash;
    }


    /**
     * Checks for equality between this Attribute instance and another. The 
     * Attribute ID's aren't compared with regard to case.
     * 
     * TODO start looking at comparing syntaxes to determine if attributes are
     *       really equal
     * @param obj the Attribute to test for equality
     * @return true if the obj is an Attribute and equals this Attribute false
     *         otherwise
     */
    public boolean equals( Object obj )
    {
        if ( obj == this )
        {
            return true;
        }

        if ( ( obj == null ) || !( obj instanceof AttributeImpl ) )
        {
            return false;
        }

        Attribute attr = ( Attribute ) obj;

        if ( !StringTools.toLowerCase( upId ).equals( StringTools.toLowerCase( attr.getID() ) ) )
        {
            return false;
        }

        if ( attr.size() != size )
        {
            return false;
        }

        if ( size == 0 )
        {
            return true;
        }
        else if ( size == 1 )
        {
            try
            {
                return ( value.equals( attr.get( 0 ) ) );
            }
            catch ( NamingException ne )
            {
                log.warn( "Failed to get an attribute from the specifid attribute: " + attr, ne );
                return false;
            }
        }
        else
        {
            // We have to create one hashSet to store all the values
            // of the current attribute, and we will remove from this
            // hashSet all the values from the second attribute which
            // are present. At the end, the hashSet should be empty,
            // and we should not have any value remaining in the second
            // attribute. If not, that means the attributes are not
            // equals.
            //
            // We have to do that because attribute's values are
            // not ordered.
            Map<Integer, Object> hash = new HashMap<Integer, Object>();

            for ( Object v : list )
            {
                int h = 0;

                if ( v instanceof String )
                {
                    h = v.hashCode();
                }
                else if ( v instanceof byte[] )
                {
                    byte[] bv = ( byte[] ) v;
                    h = Arrays.hashCode( bv );
                }
                else
                {
                    return false;
                }

                hash.put( Integer.valueOf( h ), v );
            }

            try
            {
                NamingEnumeration<?> attrValues = attr.getAll();

                while ( attrValues.hasMoreElements() )
                {
                    Object val = attrValues.next();

                    if ( val instanceof String )
                    {
                        Integer h = Integer.valueOf( val.hashCode() );

                        if ( !hash.containsKey( h ) )
                        {
                            return false;
                        }
                        else
                        {
                            Object val2 = hash.remove( h );

                            if ( !val.equals( val2 ) )
                            {
                                return false;
                            }
                        }
                    }
                    else if ( val instanceof byte[] )
                    {
                        Integer h = Integer.valueOf( Arrays.hashCode( ( byte[] ) val ) );

                        if ( !hash.containsKey( h ) )
                        {
                            return false;
                        }
                        else
                        {
                            Object val2 = hash.remove( h );

                            if ( !Arrays.equals( ( byte[] ) val, ( byte[] ) val2 ) )
                            {
                                return false;
                            }
                        }
                    }
                    else
                    {
                        return false;
                    }
                }

                if ( hash.size() != 0 )
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }
            catch ( NamingException ne )
            {
                log.warn( "Failed to get an attribute from the specifid attribute: " + attr, ne );
                return false;
            }
        }
    }


    /**
     * @see Object#toString()
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append( "Attribute id : '" ).append( upId ).append( "', " );
        sb.append( " Values : [" );

        switch ( size )
        {
            case 0:
                sb.append( "]\n" );
                break;

            case 1:
                if ( value instanceof String )
                {
                    sb.append( '\'' ).append( value ).append( '\'' );
                }
                else
                {
                    sb.append( StringTools.dumpBytes( ( byte[] ) value ) );
                }

                sb.append( "]\n" );
                break;

            default:
                boolean isFirst = true;

                Iterator<Object> values = list.iterator();

                while ( values.hasNext() )
                {
                    Object v = values.next();

                    if ( isFirst == false )
                    {
                        sb.append( ", " );
                    }
                    else
                    {
                        isFirst = false;
                    }

                    if ( v instanceof String )
                    {
                        sb.append( '\'' ).append( v ).append( '\'' );
                    }
                    else
                    {
                        sb.append( StringTools.dumpBytes( ( byte[] ) v ) );
                    }
                }

                sb.append( "]\n" );
                break;
        }

        return sb.toString();
    }
}
