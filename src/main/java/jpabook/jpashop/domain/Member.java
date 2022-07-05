package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "member_id") // 따로 설정하지 않으면 id
    private Long id;

    private String name;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "member") // 1명 회원이 여러 상품 주문, mappedBy : 읽기 전용(order table의 member 필드에 의해 매핑된 것)
    private List<Order> orders = new ArrayList<>();
}