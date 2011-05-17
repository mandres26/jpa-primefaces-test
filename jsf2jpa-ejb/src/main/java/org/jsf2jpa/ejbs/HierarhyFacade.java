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
package org.jsf2jpa.ejbs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import org.jsf2jpa.entities.Hierarhy;
import org.jsf2jpa.entities.HierarhyAttribute;
import org.jsf2jps.utils.NamingConstants;

/**
 * Class implements HierarhyFacade functions
 *
 * <br/>$LastChangedRevision:$
 * <br/>$LastChangedDate:$
 *
 * @author ASementsov
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class HierarhyFacade extends AbstractFacade<Hierarhy, HierarhyAttribute>
{
    /**
     * Subversion revision number it will be changed automatically when commited
     */
    private static final String REV_NUMBER = "$Revision:$";
    /**
     * User transaction manager
     */
    @Resource
    private UserTransaction userTx;
    /**
     * Entity manager
     */
    @PersistenceContext(unitName = NamingConstants.PERSICTENCE_UNIT)
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager()
    {
        return em;
    }

    @Override
    protected UserTransaction getUserTransaction()
    {
        return userTx;
    }
    
    public HierarhyFacade()
    {
        super(Hierarhy.class, HierarhyAttribute.class);
    }
    
    public void initData ()
    {
        if (hierarhies == null)
            createHierarhies ();

        boolean flag = false;
        try {
            flag = beginTransaction();
            
            for (Hierarhy hier : hierarhies) {
                getEntityManager().persist(hier);
            }

            commitTransaction(flag);
        }
        catch (Exception ex) {
            throw (new EJBException(ex));
        }
    }
    
    /**
     * Initial data
     */
    private static List<Hierarhy> hierarhies;
    private static void addAttributes (Hierarhy hier)
    {
        for (int i=0;i<3;i++) {
            HierarhyAttribute attr = new HierarhyAttribute();
            attr.setName("Attr: " + i);
            attr.setDateValue(new Date());
            hier.addAttribute(attr);
        }
    }

    private static void createHierarhies()
    {
        hierarhies = new ArrayList<Hierarhy>();

        /*
         * First level
         */
        for (int i=0;i<20;i++) {
            Hierarhy hier = new Hierarhy();
            hier.setName(i + " leaf");
            addAttributes (hier);
            /*
             * Second level
             */
            for (int j=0;j<5;j++) {
                Hierarhy hier1 = new Hierarhy();
                hier1.setName(i + "." + j + " leaf");
                hier1.setParent(hier);
                addAttributes (hier1);
                
                for (int k=0;k<5;k++) {
                    Hierarhy hier2 = new Hierarhy();
                    hier2.setName(i + "." + j + "." + k + " leaf");
                    hier2.setParent(hier1);
                    addAttributes (hier2);
                }
            }
        }
     }
}
