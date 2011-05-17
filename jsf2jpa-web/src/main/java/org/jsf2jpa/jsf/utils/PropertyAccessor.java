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
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * <br/>$LastChangedRevision:$
 * <br/>$LastChangedDate:$
 *
 * @author ASementsov
 */
public class PropertyAccessor implements Serializable
{
    /**
     * Subversion revision number it will be changed automatically when commited
     */
    private static final String REV_NUMBER = "$Revision:$";
    private static final long serialVersionUID = 1L;

    private Method get;
    private Method set;

    /**
     * Pribvate constructor to avoid instantiation
     */
    private PropertyAccessor()
    {
    }

    public static PropertyAccessor findPropertyAccessors(Field field, Class<?> entityClass)
    {
        return findPropertyAccessors(field.getName(), field.getType(), entityClass);
    }

    public static PropertyAccessor findPropertyAccessors(String fieldName, Class<?> fieldClass, Class<?> entityClass)
    {
        String capitalizedName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        PropertyAccessor pd = new PropertyAccessor();

        try {
            pd.get = entityClass.getMethod("get" + capitalizedName);
        }
        catch (NoSuchMethodException e) {
            Logger.getAnonymousLogger().log(Level.INFO, "{0}: {1}", new Object[]{e, capitalizedName});
        }

        if (fieldClass != null) {
            try {
                pd.set = entityClass.getMethod("set" + capitalizedName, fieldClass);
            }
            catch (NoSuchMethodException e) {
                Logger.getAnonymousLogger().log(Level.INFO, "{0}: {1}", new Object[]{e, capitalizedName});
            }
        }

        return pd;
    }

    public Method getGet()
    {
        return get;
    }

    public Method getSet()
    {
        return set;
    }
}
