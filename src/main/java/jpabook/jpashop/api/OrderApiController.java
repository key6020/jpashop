package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.dto.OrderFlatDto;
import jpabook.jpashop.dto.OrderItemQueryDto;
import jpabook.jpashop.dto.OrderQueryDto;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.query.QueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderApiController {
    private final OrderRepository orderRepository;
    private final QueryRepository queryRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> getOrdersV1() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        for (Order order : orders) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return orders;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> getOrdersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        return orders.stream().map(OrderDto::new).collect(toList());
    }

//    @GetMapping("/api/v3/orders")
//    // collection fetch join (collection 하나 일 때만 사용. 둘 이상은 X)
//    // 단점 : paging 불가 HHH000104: firstResult/maxResults specified with collection fetch; applying in memory! <-- Out Of Memory
//    public List<OrderDto> getOrdersV3() {
//        List<Order> orders = orderRepository.findAllWithItem();
//        return orders.stream().map(OrderDto::new).collect(Collectors.toList());
//    }

    @GetMapping("/api/v3/orders")
    // collection fetch join + paging available
    public List<OrderDto> getOrdersV3(@RequestParam(value = "offset", defaultValue = "0") int offset, @RequestParam(value = "limit", defaultValue = "100") int limit) {
        // XToOne은 fetch join
        List<Order> orders = orderRepository.findAllWithMemberAndDeliveryAndPaging(offset, limit);
        // default_batch_fetch_size: 100 <-- in query
        return orders.stream().map(OrderDto::new).collect(toList());
    }

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> getOrdersV4() {
        // N+1
        return queryRepository.findAllOrderCollectionDto();
    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> getOrdersV5() {
        // N+1 Solved
        return queryRepository.findAllOrderCollectionDtoOptimized();
    }

    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> getOrdersV6() {
        // N+1 Solved
        List<OrderFlatDto> flat = queryRepository.findAllOrderCollectionDtoOptimizedFlat();
        // 중복 제거
        return flat.stream().collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()), mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())))
                .keySet().stream()
                .map(orderItemQueryDtos -> new OrderQueryDto(orderItemQueryDtos.getOrderId(), orderItemQueryDtos.getName(), orderItemQueryDtos.getOrderDate(), orderItemQueryDtos.getOrderStatus(), orderItemQueryDtos.getAddress(), orderItemQueryDtos.getOrderItems())).collect(toList());

        // 단점 : join으로 중복 발생 / 느릴 수 있음 / application에서 분해 작업/ Order 기준 페이징은 불가
    }

    @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getOrderStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream().map(OrderItemDto::new).collect(toList());
        }
    }

    @Data
    static class OrderItemDto {
        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
