/*
 * The MIT License
 *
 * Copyright 2011 ASementsov.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jsf2jpa.entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * <br/>$LastChangedRevision:$
 * <br/>$LastChangedDate:$
 *
 * @author ASementsov
 */
@MappedSuperclass
@Inheritance(strategy= InheritanceType.TABLE_PER_CLASS)
@DiscriminatorColumn(name="DT_TYPE", discriminatorType=DiscriminatorType.STRING)
public abstract class AbstractAttribute extends BaseEntity implements Serializable
{
    /**
     * Subversion revision number it will be changed automatically when commited
     */
    private static final String REV_NUMBER = "$Revision:$";

    @Column(name="DT_TYPE")
    @Enumerated(EnumType.STRING)
    private DataType        dataType;
    @Column(name="STR_VAL")
    private String          stringValue;
    @Column(name="NUM_VAL")
    private Number          numberValue;
    @Column(name="DT_VAL")
    @Temporal(TemporalType.TIMESTAMP)
    private Date            dateValue;

    public DataType getDataType()
    {
        return dataType;
    }

    public void setDataType(DataType dataType)
    {
        this.dataType = dataType;
    }

    public Date getDateValue()
    {
        return dateValue;
    }

    public void setDateValue(Date dateValue)
    {
        this.dateValue = dateValue;
    }

    public Number getNumberValue()
    {
        return numberValue;
    }

    public void setNumberValue(Number numberValue)
    {
        this.numberValue = numberValue;
    }

    public String getStringValue()
    {
        return stringValue;
    }

    public void setStringValue(String stringValue)
    {
        this.stringValue = stringValue;
    }
    
    public Object getAsObject ()
    {
        switch (dataType) {
            case DATE:
                return dateValue;
                
            case NUMBER:
                return numberValue;
                
            case STRING:
                return stringValue;
                    
            default:
                return null;
        }
    }
}
