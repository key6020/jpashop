package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.dto.SimpleOrderDto;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.query.QueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * XToOne 조회 성능 최적화 (ManyToOne, OneToOne) : Not Collection
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final QueryRepository queryRepository;

//    @GetMapping("/api/v1/simple-orders")
//    public List<Order> getOrdersV1() {
//        // 무한루프
//        // -> @JsonIgnore
//        // -> LAZY : Order만 가져오고 실제 Member는 가져오지 않음. MemberProxy[Type Definition Error].
//        // -> hibernate module bean 등록
//        // 문제점 : Entity 그대로 노출 + Over-Fetching(사용하지 않는 데이터 DB 조회)
//        return orderRepository.findAllByString(new OrderSearch());
//    }

//    @GetMapping("/api/v1/simple-orders")
//    public List<Order> getOrdersV1() {
//        List<Order> all = orderRepository.findAllByString(new OrderSearch());
//        for (Order order : all) {
//            order.getMember().getName(); // LAZY 강제 초기화 (Hibernate5Module.Feature.FORCE_LAZY_LOADING 은 종료)
//            order.getDelivery().getAddress();
//        }
//        return all;
//    }

    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> getOrdersV2() {
        // 여전히 LAZY LOADING 으로 인한 쿼리 과잉 조회
        // N+1 or 1+N issue
        // N = 주문 수, 1 + Member * N + Delivery * N (worst case)
        //  단, LAZY LOADING 은 영속성 컨텍스트 조회이므로 이미 조회된 경우에는 쿼리 생략
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        return orders.stream().map(SimpleOrderDto::new).collect(Collectors.toList());
    }

    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> getOrdersV3() {
        // fetch join
        List<Order> orders = orderRepository.findAllWithMemberAndDelivery();
        return orders.stream().map(SimpleOrderDto::new).collect(Collectors.toList());
    }

    @GetMapping("/api/v4/simple-orders")
    // JPA 에서 바로 DTO 조회 (재사용성은 다소 떨어짐)
    public List<SimpleOrderDto> getOrdersV4() {
        return queryRepository.findAllSimpleOrderDto();
    }
}
