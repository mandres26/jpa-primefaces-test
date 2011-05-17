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
package org.jsf2jps.utils;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.transaction.RollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

/**
 * Class implements EJBUtils functions
 *
 * <br/>$LastChangedRevision:$
 * <br/>$LastChangedDate:$
 *
 * @author ASementsov
 */
public class EJBUtils
{
    /**
     * Subversion revision number it will be changed automatically when commited
     */
    private static final String REV_NUMBER = "$Revision: 57 $:";
    
    public static boolean beginTransaction (UserTransaction tx) throws SystemException, NotSupportedException
    {
        if (tx.getStatus() == Status.STATUS_NO_TRANSACTION) {
            tx.begin();
            return true;
        }
        
        return false;
    }
    
    public static void rollback(UserTransaction tx, boolean flag)
    {
        try {
            if (flag)
                tx.rollback();
        }
        catch (IllegalStateException ex) {
            Logger.getLogger(EJBUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (SecurityException ex) {
            Logger.getLogger(EJBUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (SystemException ex) {
            Logger.getLogger(EJBUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void commit(UserTransaction tx, boolean flag) throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException
    {
        if (flag)
            tx.commit();
    }
}
