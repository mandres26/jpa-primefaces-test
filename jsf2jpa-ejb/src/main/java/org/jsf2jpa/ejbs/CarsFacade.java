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

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import org.jsf2jpa.entities.Car;
import org.jsf2jpa.entities.CarAttribute;
import org.jsf2jpa.entities.CarModel;
import org.jsf2jpa.entities.Manufacturer;
import org.jsf2jps.utils.NamingConstants;

/**
 * Class implements CarsFacade functions
 *
 * <br/>$LastChangedRevision:$
 * <br/>$LastChangedDate:$
 *
 * @author ASementsov
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class CarsFacade extends AbstractFacade<Car, CarAttribute>
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
    
    private Manufacturer createManufacture (String name)
    {
        Manufacturer mf = new Manufacturer();
        mf.setName(name);
        return mf;
    }
    
    private CarModel createModel (Manufacturer mf, String name)
    {
        CarModel cm = new CarModel();
        cm.setName(name);
        cm.setManufacturer(mf);
        mf.getModels().add(cm);
        return cm;
    }

    private Car createCar (String name, CarModel model)
    {
        Car car = new Car();
        car.setName(name);
        car.setModel(model);
        model.getCars().add(car);
        return car;
    }
    
    /*
     * Initial data
     */
    private static final String[] MODELS = {
        "Antara",
        "Astra",
        "Corsa",
        "Insignia",
        "Zafira"
    };

    private static final String[] ATTRIBUTES = {
        "Price",
        "Weight",
        "Length",
    };

    public CarsFacade()
    {
        super(Car.class, CarAttribute.class);
    }

    /**
     * Function to produce initial table data for cars, manufactures and cars models
     */
    public void initData ()
    {
        /*
         * Create initial data
         */
        Manufacturer mf = createManufacture("Opel");
        for (String model : MODELS) {
            CarModel m = createModel(mf, model);

            /*
             * Five cars for each model
             */
            for (int i=0;i<5;i++) {
                Car car = createCar(model, m);

                /*
                 * attributes for each car
                 */
                Random rnd = new Random();
                for (String attr : ATTRIBUTES) {
                    CarAttribute a = new CarAttribute();
                    a.setName(attr);
                    a.setStringValue(String.valueOf(rnd.nextInt(99999)));
                    a.setParent(car);
                    car.getAttributes().add (a);
                }
            }
        }
        
        boolean flag = false;

        try {
            flag = beginTransaction();
            getEntityManager().persist(mf);
            commitTransaction(flag);
        }
        catch (Exception ex) {
            Logger.getLogger(CarsFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
