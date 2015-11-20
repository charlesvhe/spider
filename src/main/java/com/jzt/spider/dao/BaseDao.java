package com.jzt.spider.dao;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Charles on 2015/11/19.
 *
 * @param <T>
 */
public abstract class BaseDao<T> {
    public enum Order{
        ASC("asc"), DESC("desc");

        private String name;

        Order(String name){
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    @PersistenceContext
    private EntityManager em = null;

    protected Class<T> entityClass = null;
    protected String entityName = null;

    public BaseDao(Class<T> entityClass) {
        this.entityClass = entityClass;
        entityName = this.entityClass.getSimpleName();
    }

    public void persist(T entity) {
        em.persist(entity);
    }

    public void remove(Serializable... entityIds) {
        for (Serializable entityId : entityIds) {
            em.remove(em.getReference(entityClass, entityId));
        }
    }

    public void merge(T entity) {
        em.merge(entity);
    }

    public T find(Serializable entityId) {
        return em.find(entityClass, entityId);
    }

    public List<T> query(int fistResult, int maxResult, String whereSql,
                         List<Object> params, LinkedHashMap<String, Order> orderBy) {
        return buildQuery(whereSql, params, orderBy)
                .setFirstResult(fistResult)
                .setMaxResults(maxResult)
                .getResultList();
    }

    public Long count(String whereSql, List<Object> params) {
        return Long.valueOf(
                buildQuery(whereSql, params, null)
                        .getSingleResult()
                        .toString()
        );
    }

    private TypedQuery<T> buildQuery(String whereSql, List<Object> params, LinkedHashMap<String, Order> orderBy) {
        StringBuilder sb = new StringBuilder("");
        if (!StringUtils.isEmpty(whereSql)) {
            sb.append(" where 1=1 and ").append(whereSql.trim()).append(" ");
        }
        if (!CollectionUtils.isEmpty(orderBy)) {
            sb.append(" order by ");
            for (Map.Entry<String, Order> entry : orderBy.entrySet()) {
                sb.append("o.").append(entry.getKey()).append(" ").append(entry.getValue()).append(",");
            }
            sb.deleteCharAt(sb.length() - 1).append(" ");
        }

        String jpql = "select o from " + entityName + " o ";
        TypedQuery<T> query = em.createQuery(jpql + sb.toString(), entityClass);

        if (!CollectionUtils.isEmpty(params)) {
            setParameters(query, params);
        }
        return query;
    }

    private static void setParameters(Query query, List<Object> params) {
        int position = 1;
        for (Object param : params) {
            query.setParameter(position, param);
            position++;
        }
    }
}
