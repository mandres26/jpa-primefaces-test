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

import java.io.Serializable;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJBException;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.EntityType;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.jsf2jpa.entities.AbstractAttribute;
import org.jsf2jpa.entities.BaseEntity;
import org.jsf2jps.utils.EJBUtils;
import org.jsf2jps.utils.NamingConstants;

/**
 * Class defines base EJB functions. This fucntions include several methods to find entity,
 * to persist, remove and merge. This class uses JPA 2.0 Criteria API to make queries.
 * 
 * T - type of the entity
 * A - type of the entity attributes
 * 
 * <br/>$LastChangedRevision: 59 $
 * <br/>$LastChangedDate: 2011-05-04 19:15:03 +0400 (Wed, 04 May 2011) $
 *
 * @author ASementsov
 */
public abstract class AbstractFacade<T extends BaseEntity, A extends AbstractAttribute> implements Serializable
{
    private static final long   serialVersionUID = 1L;
    private static final String REV_NUMBER = "$Revision: 59 $";

    /**
     * Function retrieves entity manager
     * @return entity manager
     */
    protected abstract EntityManager getEntityManager();
    protected abstract UserTransaction getUserTransaction();

    /**
     * Entity class
     */
    private Class<T> entityClass;
    /**
     * Entity attributes class
     */
    private Class<A> attributesClass;
    
    /**
     * Constructor
     * @param entityClass - entity class
     * @param attributesClass - entity attributes class
     */
    public AbstractFacade(Class<T> entityClass, Class<A> attributesClass)
    {
        this.entityClass = entityClass;
        this.attributesClass = attributesClass;
    }

    /**
     * Function used to add order by to criteria query
     * @param cq - criteria query object
     * @param builder - criteria query builder
     * @param from - root object
     * @param sortField - field used to sort
     * @param descend - flag indicates sort direction
     */
    protected void setOrderBy (CriteriaQuery<T> cq, CriteriaBuilder builder, Root<T> from, String sortField, boolean descend)
    {
        if (sortField != null)
            cq.orderBy(descend ? builder.desc(from.get(sortField)) : builder.asc(from.get(sortField)));
    }

    /**
     * Function used to add predicates to criteria query
     * @param cq - criteria query object
     * @param builder - criteria query builder
     * @param from - root object
     * @param model - entity model
     * @param filters - filters map
     * @param preList - predicate list (in, out)
     */
    @SuppressWarnings("rawtypes")
    protected void addSimpleFilter (CriteriaQuery cq, CriteriaBuilder builder, Path<T> from, EntityType<T> model, Map<String, ?> filters, List<Predicate> preList)
    {
        if (filters != null && filters.size() > 0) {
            for (String column : filters.keySet()) {
                Object var = filters.get(column);
                if (column.indexOf('.') != -1) {
                    /*
                     * This is not simple query because this field could become a reason for table join
                     */
                    String[] fields = column.split("\\.");
                    /*
                     * This is simple join. This filter allowed only simple joins
                     */
                    if (fields.length == 2) {
                        /*
                         * Get main attribute
                         */
                        Path attr = from.get(fields[0]);
                        /*
                         * If filterable column is id then just get desired object by it's identifier
                         */
                        if (fields[1].equals(NamingConstants.ID)) {
                            try {
                                BigInteger id = BigInteger.valueOf(NumberFormat.getInstance().parse(var.toString()).longValue());
                                Object value = getEntityManager().find(attr.getJavaType(), id);
                                preList.add(builder.equal(from.get(fields[0]), value));
                            }
                            catch (ParseException ex) {
                                Logger.getLogger(AbstractFacade.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        else {
                            /*
                             * Create subquery to find by attribute
                             */
                            Subquery subquery = cq.subquery(attr.getJavaType());
                            Root fromAttr = subquery.from(attr.getJavaType());
                            subquery.select(fromAttr.get(NamingConstants.ID));

                            if (var instanceof String && ((String)var).contains("%")) {
                                /*
                                 * This is a like query
                                 */
                                subquery.where(builder.like(builder.lower(fromAttr.get(fields[1])), var.toString().toLowerCase()));   
                            }
                            else {
                                /*
                                 * This is strong equals query
                                 */
                                subquery.where(builder.equal(fromAttr.get(fields[1]), var));
                            }

                            /*
                             * Add where to main query
                             */
                            preList.add(builder.in(from.get(fields[0]).get(NamingConstants.ID)).value(subquery));
                        }
                    }
                    else
                        /*
                         * Just skip this filter becauseit's unusable
                         */
                        continue;
                }
                else {
                    if (var instanceof String && ((String)var).contains("%")) {
                        Expression<String> literal = from.get(column);
                        preList.add(builder.like(builder.lower(literal), var.toString().toLowerCase()));
                    }
                    else {
                        preList.add(builder.equal(from.get(column), var));
                    }
                }
            }
        }
    }

    /**
     * Function used to add predicates to criteria query
     * @param cq - criteria query
     * @param builder - criteria query builder
     * @param from - root object
     * @param filters - filter map
     * @param preList - predicate list (in, out)
     */
    @SuppressWarnings("rawtypes")
    protected void addSimpleFilter (CriteriaQuery cq, CriteriaBuilder builder, Root<T> from, Map<String, ?> filters, List<Predicate> preList)
    {
        addSimpleFilter(cq, builder, from, from.getModel(), filters, preList);
    }

    /**
     * Fucntion used to add subquery filter by entity attributes 
     * @param cq - criteria query
     * @param builder - criteria query builder
     * @param from - root object
     * @param attributesFilter - attributes for filter
     * @param joinColumn - column of base object in attribute object
     * @param preList  - predicates list
     */
    protected void addAttributeFilter (CriteriaQuery cq, 
                                       CriteriaBuilder builder, 
                                       Root<T> from, 
                                       List attributesFilter,
                                       String joinColumn,
                                       List<Predicate> preList)
    {
        Subquery<A> subquery = cq.subquery(attributesClass);
        Root fromAttr = subquery.from(attributesClass);

        subquery.select(fromAttr.get(joinColumn).get(NamingConstants.ID));
        subquery.groupBy(fromAttr.get(joinColumn).get(NamingConstants.ID));
        subquery.having(builder.equal(builder.count(fromAttr.get(joinColumn).get(NamingConstants.ID)), attributesFilter.size()));

        Predicate wherePredicates = null;

        for (Object a : attributesFilter) {
            if (!(a instanceof AbstractAttribute))
                continue;

            AbstractAttribute attr = (AbstractAttribute)a;
            Predicate pp = null;
            switch (attr.getDataType()) {
                case STRING:
                    pp = builder.and (
                            builder.equal(fromAttr.get(NamingConstants.NAME), attr.getName()),
                            builder.equal(
                                    fromAttr.get(NamingConstants.STRING_VALUE), 
                                    attr.getStringValue())
                        );
                    break;
                    
                case NUMBER:
                    pp = builder.and (
                            builder.equal(fromAttr.get(NamingConstants.NAME), attr.getName()),
                            builder.equal(
                                    fromAttr.get(NamingConstants.NUMBER_VALUE), 
                                    attr.getNumberValue())
                        );
                    break;
                    
                case DATE:
                    pp = builder.and (
                            builder.equal(fromAttr.get(NamingConstants.NAME), attr.getName()),
                            builder.equal(
                                    fromAttr.get(NamingConstants.DATE_VALUE),
                                    attr.getDateValue())
                        );
                    break;
            }
            
            if (pp == null) 
                continue;

            if (wherePredicates == null)
                wherePredicates = pp;
            else
                wherePredicates = builder.or(wherePredicates, pp);
        }
        
        subquery.where(wherePredicates);
        preList.add(builder.in(from.get(NamingConstants.ID)).value(subquery));
    }

    /**
     * Function used to create new entity on <T> type
     * @param entity - entity to create
     */
    public void create(T entity)
    {
        try {
            boolean isOwnTran = EJBUtils.beginTransaction(getUserTransaction());
            getEntityManager().persist(entity);
            EJBUtils.commit(getUserTransaction(), isOwnTran);
        }
        catch (Exception ex) {
            throw (new EJBException(ex));
        }
    }

    /**
     * Function used to merge entity
     * @param entity - entity to merge
     */
    public void merge(T entity)
    {
        try {
            boolean isOwnTran = EJBUtils.beginTransaction(getUserTransaction());
            getEntityManager().merge(entity);
            EJBUtils.commit(getUserTransaction(), isOwnTran);
        }
        catch (Exception ex) {
            throw (new EJBException(ex));
        }
    }

    /**
     * Function removes entity
     * @param entity - entity to remove
     */
    public void remove(T entity)
    {
        try {
            boolean isOwnTran = EJBUtils.beginTransaction(getUserTransaction());
            getEntityManager().remove(getEntityManager().merge(entity));
            EJBUtils.commit(getUserTransaction(), isOwnTran);
        }
        catch (Exception ex) {
            throw (new EJBException(ex));
        }
    }

    /**
     * Function find entity by primary key
     * @param id - primary key object
     * @return found entity
     */
    public T find(Object id)
    {
        return getEntityManager().find(entityClass, id);
    }

    /**
     * Function find all entities
     * @return list of entities
     */
    public List<T> findAll()
    {
        CriteriaQuery<T> cq = getEntityManager().getCriteriaBuilder().createQuery(entityClass);
        cq.select(cq.from(entityClass));
        return getEntityManager().createQuery(cq).getResultList();
    }

    /**
     * Function retrieves list of entities from database. It uses sorting, filtering and paging to do this.
     * @param first - first row number
     * @param pageSize - page size
     * @param sortField - field to sort by
     * @param descend - flag indicates sort direction
     * @param filters - map of filters
     * @return list of entities
     */
    @SuppressWarnings("unchecked")
    public List<T> findFilteredRange(int first, int pageSize, String sortField, boolean descend, Map<String, Object> filters)
    {
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> cq = builder.createQuery(entityClass);
        Root<T> from = cq.from(entityClass);
        cq.select(from);

        if (filters != null && !filters.isEmpty()) {
            List<Predicate> predicates = new ArrayList<Predicate>();
            addSimpleFilter (cq, builder, from, filters, predicates);
            cq.where(predicates.toArray(new Predicate[predicates.size()]));
        }

        if (sortField != null && !sortField.isEmpty())
            setOrderBy(cq, builder, from, sortField, descend);

        Query q = getEntityManager().createQuery(cq);
        q.setMaxResults(pageSize);
        q.setFirstResult(first);
        return q.getResultList();
    }

    /**
     * Function retrieves count of rows for entities. Result set was pre-filtered using map of filters
     * @param filters - map of filters
     * @return count rows
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public int countFiltered(Map<String, ?> filters)
    {
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery();
        Root<T> from = cq.from(entityClass);
        cq.select(builder.count(from));

        if (filters != null && !filters.isEmpty()) {
            List<Predicate> predicates = new ArrayList<Predicate>();
            addSimpleFilter (cq, builder, from, filters, predicates);
            cq.where(predicates.toArray(new Predicate[predicates.size()]));
        }

        Query q = getEntityManager().createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

    /**
     * Function retrieves list of entities without any filtering and storing
     * @param first - first row number
     * @param pageSize - page size
     * @return list of entities
     */
    public List<T> findRange(int first, int pageSize)
    {
        return findFilteredRange(first, pageSize, null, false, null);
    }

    /**
     * Function retrieves count of entities without any filtering
     * @return
     */
    public int count()
    {
        return countFiltered(null);
    }

    /**
     * Refresh entity. Reload it from database and overwrite all changes made on it if exists
     * @param entity - entity to refresh
     * @return refresed entity
     */
    public T refresh (T entity)
    {
        getEntityManager().refresh(entity);
        return entity;
    }
    
    /**
     * Function find range of entities filtered with two dimensions: 
     * <ul>
     * <li>Simple filtration use embedded attributes
     * <li>Extended filtration use values of external attributes
     * </ul>
     * For extended filtration it uses subquery, e.g. if it need to find all equipment
     * with external attribute STATE=AVAILABLE and attribute PERFORMANCE_RATE=100 select 
     * statement for Oracle will seems as follow
     * <code>
     * select *
     * from eq 
     * where eq_id in (
     *              select e.id
     *              from eq_attr a, eq e
     *              where (
     *                      (a.name = 'STATE' and value = 'AVAILABLE')
     *                      or
     *                      (a.name = 'PERFORMANCE_RATE' and value = 100)
     *                     )
     *                     and a.eq_id = e.id
     *              group by e.id
     *              having count(e.id) = 2)
     * </code>
     * @param first - first row number
     * @param pageSize - page size
     * @param sortField - field to sort with
     * @param descend - filter direction
     * @param filters - simple filters list
     * @param attributesFilter - attributes metadata with names and values which will be used to make additional filter
     * @param joinColumn - column used to join between attribute and container class
     * @return list of selected entities
     */
    public List<T> findExtendedFilteredRange (int first, int pageSize, 
                                          String sortField, boolean descend, 
                                          Map<String, Object> filters,
                                          List attributesFilter,
                                          String joinColumn)
    {
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();

        CriteriaQuery<T> cq = builder.createQuery(entityClass);
        Root<T> fromEntity = cq.from(entityClass);
        cq.select(fromEntity);
        
        List<Predicate> predicates = new ArrayList<Predicate>();
        /*
        * Add Subquery predicate
        */
        addAttributeFilter(cq, builder, fromEntity, attributesFilter, joinColumn, predicates);

        if (filters != null && !filters.isEmpty()) {
            /*
            * Add other filters
            */
            addSimpleFilter (cq, builder, fromEntity, filters, predicates);
        }

        /*
         * Set where clause for query
         */
        cq.where(predicates.toArray(new Predicate[predicates.size()]));
         
        /*
         * Set sorting clause
         */
        if (sortField != null && !sortField.isEmpty())
            setOrderBy(cq, builder, fromEntity, sortField, descend);

        Query q = getEntityManager().createQuery(cq);
        q.setMaxResults(pageSize);
        q.setFirstResult(first);
        return q.getResultList();
    }

    /**
     * Calculates count of the entities. Functionality like as findExtendedFilteredRange fucntion
     * @param filters - simple filter
     * @param attributesFilter - extendsed filter by external attributes
     * @param joinColumn - column used to join between attribute and container class
     * @return count of filtered entities
     */
    public int countExtendedFiltered(Map<String, Object> filters, List attributesFilter, String joinColumn)
    {
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery();
        Root<T> from = cq.from(entityClass);
        cq.select(builder.count(from));

        /*
        * Add Subquery predicate
        */
        List<Predicate> predicates = new ArrayList<Predicate>();
        addAttributeFilter(cq, builder, from, attributesFilter, joinColumn, predicates);
        
        /*
         * Set where clause for query
         */
        if (filters != null && !filters.isEmpty()) {
            addSimpleFilter (cq, builder, from, filters, predicates);
        }
        
        cq.where(predicates.toArray(new Predicate[predicates.size()]));

        Query q = getEntityManager().createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

    /**
     * get children entities by parent entity
     * @param parent - parent entity
     * @return children
     */
    public List<T> findChildren(T parent)
    {
        CriteriaQuery<T> cq = getEntityManager().getCriteriaBuilder().createQuery(entityClass);
        Root<T> from = cq.from(entityClass);
        cq.select(from);
        cq.where(getEntityManager().getCriteriaBuilder().equal(from.get(NamingConstants.PARENT), parent));
        return getEntityManager().createQuery(cq).getResultList();
    }
    
    /**
     * Retrieve count children
     * @param parent- parent entity
     * @return children count
     */
    public int countChildren(T parent)
    {
        CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery(entityClass);
        Root<T> from = cq.from(entityClass);
        cq.select(getEntityManager().getCriteriaBuilder().count(from));
        cq.where(getEntityManager().getCriteriaBuilder().equal(from.get(NamingConstants.PARENT), parent));
        Query q = getEntityManager().createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }
    
    public boolean beginTransaction () throws SystemException, NotSupportedException
    {
        return EJBUtils.beginTransaction(getUserTransaction());
    }

    public void commitTransaction (boolean flag) throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException
    {
        EJBUtils.commit(getUserTransaction(), flag);
    }
}