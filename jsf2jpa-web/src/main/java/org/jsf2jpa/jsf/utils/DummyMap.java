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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author ASementsov
 */
public abstract class DummyMap implements Map
{
    private static final String REV_NUMBER = "$Revision: 34 $";

    @Override
    public int size()
    {
        return 0;
    }

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    @Override
    public boolean containsKey(Object key)
    {
        return false;
    }

    @Override
    public boolean containsValue(Object value)
    {
        return false;
    }

    @Override
    public abstract Object get(Object key);

    @Override
    public Object put(Object key, Object value)
    {
        return null;
    }

    @Override
    public Object remove(Object key)
    {
        return null;
    }

    @Override
    public void putAll(Map m)
    {
    }

    @Override
    public void clear()
    {
    }

    @Override
    public Set keySet()
    {
        return null;
    }

    @Override
    public Collection values()
    {
        return null;
    }

    @Override
    public Set entrySet()
    {
        return null;
    }
}
