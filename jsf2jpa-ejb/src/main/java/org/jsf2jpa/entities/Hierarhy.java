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
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Class implements Hierarhy entity
 *
 * <br/>$LastChangedRevision:$
 * <br/>$LastChangedDate:$
 *
 * @author ASementsov
 */
@Entity
@Table(name="HIER")
public class Hierarhy extends BaseEntity implements Serializable
{
    /**
     * Subversion revision number it will be changed automatically when commited
     */
    private static final String REV_NUMBER = "$Revision:$";
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator="HIER_SEQ", strategy=GenerationType.AUTO)
    private Long                        id;
    @ManyToOne
    private Hierarhy                    parent;
    @OneToMany(mappedBy = "parent", cascade=CascadeType.ALL)
    @JoinColumn(name="PARENT_ID")
    private List<Hierarhy>              children = new ArrayList<Hierarhy>();
    @OneToMany(mappedBy = "parent", cascade=CascadeType.ALL)
    private List<HierarhyAttribute>     attributes = new ArrayList<HierarhyAttribute>();

    public List<Hierarhy> getChildren()
    {
        return children;
    }

    public void setChildren(List<Hierarhy> children)
    {
        this.children = children;
    }

    @Override
    public Long getId()
    {
        return id;
    }

    @Override
    public void setId(Long id)
    {
        this.id = id;
    }

    public Hierarhy getParent()
    {
        return parent;
    }

    public void setParent(Hierarhy parent)
    {
        this.parent = parent;
    }

    public List<HierarhyAttribute> getAttributes()
    {
        return attributes;
    }

    public void setAttributes(List<HierarhyAttribute> attributes)
    {
        this.attributes = attributes;
    }
    
    public void addAttribute (HierarhyAttribute attr)
    {
        attr.setParent(this);
        attributes.add(attr);
    }

    public void addAttributes (List<HierarhyAttribute> attrs)
    {
        for (HierarhyAttribute attr : attrs) {
            addAttribute (attr);
        }
    }

    public void removeAttribute (HierarhyAttribute attr)
    {
        int index = attributes.indexOf(attr);
        if (index != -1) {
            HierarhyAttribute a = attributes.get(index);
            attributes.remove(index);
            attr.setParent(null);
            a.setParent(null);
        }
    }
}
