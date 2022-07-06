package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.dto.SimpleOrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    // 검색 로직 (Dynamic Query)

    /**
     * 1. String Query
     */
    public List<Order> findAllByString(OrderSearch orderSearch) {

        // NOT DYNAMIC
//        List<Order> resultList =  em.createQuery("select o from Order o join o.member m" +
//                 " where o.status = :status " +
//                 " and m.name like :name", Order.class)
//                .setParameter("status", orderSearch.getOrderStatus())
//                .setParameter("name", orderSearch.getMemberName())
//                .setMaxResults(1000) // paging 가능
//                .getResultList();

        String jpql = "select o from Order o join o.member m"; // 기본 쿼리
        boolean isFirstCondition = true;

        // Order Status Search
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }

        // Member Name Search
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000);

        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("orderStatus", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }

        List<Order> resultList = query.getResultList();
        return resultList;
    }

    /**
     * 2. JPA Criteria
     * 단점 : 유지보수 어려움
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Object, Object> m = o.join("member", JoinType.INNER);

        List<Predicate> criteria = new ArrayList<>();

        // Order Status Search
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("orderStatus"), orderSearch.getOrderStatus());
            criteria.add(status);
        }

        // Member Name Search
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name = cb.like(o.get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);
        }

        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);

        return query.getResultList();
    }

    public List<Order> findAllWithMemberAndDelivery() {
        return em.createQuery("select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class)
                .getResultList();
    }

    public List<SimpleOrderDto> findAllSimpleOrderDto() {
        return em.createQuery("select new jpabook.jpashop.dto.SimpleOrderDto(o.id, m.name, o.orderDate, o.orderStatus, d.address) from Order o" +
                        " join o.member m" +
                        " join o.delivery d", SimpleOrderDto.class)
                .getResultList();
    }
}
