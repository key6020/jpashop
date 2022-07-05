package jpabook.jpashop.service;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    // Order
    /**
     * Order
     */
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {
        // count = 주문 수량

        // Entity 조회 (영속 상태로 진행)
        Member member = memberRepository.findOne(memberId);
        Item item = itemRepository.findOne(itemId);

        // Delivery Info
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());

        // Create OrderItem
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count); // pucblic static method

        // Create Order
        Order order = Order.createOrder(member, delivery, orderItem);

        // Save Order
        orderRepository.save(order); // cascade option -> orderitem&delivery 자동 persist

        return order.getId();
    }

    // Cancel
    /**
     * Cancel
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        // Order Entity 조회
        Order order = orderRepository.findOne(orderId);

        // Order Cancel
        order.cancel(); // JPA *dirty checking*
    }

    /**
     * Search
     */
    public List<Order> findOrders(OrderSearch orderSearch) {
//        return orderRepository.findAllByString(orderSearch);
        return orderRepository.findAllByCriteria(orderSearch);
    }
}
