package jpabook.jpashop.repository.query;

import jpabook.jpashop.dto.SimpleOrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

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
}
