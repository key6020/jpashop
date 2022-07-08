package jpabook.jpashop.repository.query;

import jpabook.jpashop.dto.OrderFlatDto;
import jpabook.jpashop.dto.OrderItemQueryDto;
import jpabook.jpashop.dto.OrderQueryDto;
import jpabook.jpashop.dto.SimpleOrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class QueryRepository {
    private final EntityManager em;

    // 화면관련 로직은 Repository(Entity)와 분리
    public List<SimpleOrderDto> findAllSimpleOrderDto() {
        return em.createQuery("select new jpabook.jpashop.dto.SimpleOrderDto(o.id, m.name, o.orderDate, o.orderStatus, d.address) from Order o" +
                        " join o.member m" +
                        " join o.delivery d", SimpleOrderDto.class)
                .getResultList();
    }

    public List<OrderQueryDto> findAllOrderCollectionDto() {
        List<OrderQueryDto> orders = em.createQuery("select new jpabook.jpashop.dto.OrderQueryDto(o.id, m.name, o.orderDate, o.orderStatus, d.address) from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderQueryDto.class)
                .getResultList();

        orders.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });

        return orders;
    }

    public List<OrderQueryDto> findAllOrderCollectionDtoOptimized() {
        // total query = 2
        List<OrderQueryDto> orders = em.createQuery("select new jpabook.jpashop.dto.OrderQueryDto(o.id, m.name, o.orderDate, o.orderStatus, d.address) from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderQueryDto.class)
                .getResultList();

        List<Long> orderIds = orders.stream().map(OrderQueryDto::getOrderId).collect(Collectors.toList());
        List<OrderItemQueryDto> orderItems = em.createQuery("select new jpabook.jpashop.dto.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream().collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));
        orders.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return orders;
    }

    public List<OrderFlatDto> findAllOrderCollectionDtoOptimizedFlat() {
        // total query = 1
        return em.createQuery("select new jpabook.jpashop.dto.OrderFlatDto(o.id, m.name, o.orderDate, o.orderStatus, d.address, i.name, oi.orderPrice, oi.count) " +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d" +
                        " join o.orderItems oi" +
                        " join oi.item i", OrderFlatDto.class)
                .getResultList();
    }

    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery("select new jpabook.jpashop.dto.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }
}
