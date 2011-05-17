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
package org.jsf2jpa.jsf.utils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsf2jpa.entities.AbstractAttribute;
import org.jsf2jps.utils.NamingConstants;

/**
 *
 * <br/>$LastChangedRevision:$
 * <br/>$LastChangedDate:$
 *
 * @author ASementsov
 */
public class BaseObjectJSF<T> implements Serializable
{
    /**
     * Subversion revision number it will be changed automatically when commited
     */
    private static final String REV_NUMBER = "$Revision:$";
    
    private final Class<? extends T>                entityClass;
    private T                                       wrappedObject;
    private String                                  attrPropertyName;
    private Class<?>                                attrPropertyClass = List.class;
    private PropertyAccessor                        attrPropertyMethods;
    private Map                                     attributeMap;
    /**
     * Properties map
     */
    private Map<String, PropertyAccessor>           propertiesMap = new HashMap<String, PropertyAccessor>();

    /**
     * Ctor
     * @param entityClass - entiry class
     */
    public BaseObjectJSF(Class<? extends T> entityClass)
    {
        this(entityClass, NamingConstants.ATTRIBUTES, null);
    }

    /**
     *  Ctor
     * @param entityClass - entity class
     * @param objToWrap - object to wrap
     */
    public BaseObjectJSF(Class<? extends T> entityClass, T objToWrap)
    {
        this(entityClass, NamingConstants.ATTRIBUTES, objToWrap);
    }

    /**
     * Ctor
     * @param entityClass - entiry class
     * @param attrPropertyName - attribute's field name
     */
    public BaseObjectJSF(Class<? extends T> entityClass, String attrPropertyName)
    {
        this(entityClass, attrPropertyName, null);
    }

    /**
     * Ctor
     * @param entityClass - entity class
     * @param attrPropertyName - atrribute's field name
     * @param objToWrap - object to wrap
     */
    public BaseObjectJSF(Class<? extends T> entityClass, String attrPropertyName, T objToWrap)
    {
        this.entityClass = entityClass;
        this.attrPropertyName = attrPropertyName;
        this.wrappedObject = objToWrap;
    }

    /**
     * Retrieves attributes propety class
     * @return property class
     */
    public Class<?> getAttrPropertyClass()
    {
        return attrPropertyClass;
    }

    /**
     * Sets attributes propery class. By default this property has List.class value.
     * User must set this property if wrapped object has attributes
     * @param attrPropertyClass - new property class
     */
    public void setAttrPropertyClass(Class<?> attrPropertyClass)
    {
        this.attrPropertyClass = attrPropertyClass;
    }

    /**
     * Fucntion retrieves MAP object which used to manipulate object's attributes
     * @return map of objct's attributes
     */
    public Map getAttributes ()
    {
        if (attributeMap == null) {
             attributeMap = new DummyMap() {
                @Override
                public Object get(Object key)
                {
                    Object ret = null;

                    if (key == null)
                        return ret;

                    if (attrPropertyMethods == null) 
                        attrPropertyMethods = PropertyAccessor.findPropertyAccessors(attrPropertyName, attrPropertyClass, entityClass);

                    if (attrPropertyMethods != null && attrPropertyMethods.getGet() != null) {
                        try {
                            List<?> attrs = (List<?>) attrPropertyMethods.getGet().invoke(wrappedObject);
                            for (Object attr : attrs) {
                                if (attr instanceof AbstractAttribute) {
                                    AbstractAttribute a = (AbstractAttribute)attr;
                                    if (a.getName().equals(key.toString())) {
                                        ret = a;
                                        break;
                                    }
                                }
                            }
                        }
                        catch (Exception ex) {
                            Logger.getLogger(BaseObjectJSF.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    return ret;
                }
            };
        }

        return attributeMap;
    }

    /**
     * Retrieves wrapped object
     * @return wrapped object
     */
    public T getWrappedObject()
    {
        return wrappedObject;
    }

    /**
     * Sets wrapped objct
     * @param wrappedObject - object to wrap
     */
    public void setWrappedObject(T wrappedObject)
    {
        this.wrappedObject = wrappedObject;
    }
    
    public Object getProperty (String property)
    {
       try {
            if (propertiesMap.containsKey(property)) {
                PropertyAccessor pac = propertiesMap.get(property);
                if (pac.getGet() != null)
                    return pac.getGet().invoke(wrappedObject);
            }
            else {
                for (Field f : entityClass.getDeclaredFields()) {
                    if (f.getName().equals(property)) {
                        PropertyAccessor pac = PropertyAccessor.findPropertyAccessors(f, entityClass);
                        if (pac != null) {
                            propertiesMap.put(property, pac);
                            if (pac.getGet() != null)
                                return pac.getGet().invoke(wrappedObject);
                        }
                    }
                }
            }
        }
        catch (Exception ex) {
            Logger.getLogger(BaseObjectJSF.class.getName()).log(Level.SEVERE, null, ex);
        }
       
       return null;
    }
}
