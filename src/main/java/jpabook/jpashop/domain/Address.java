package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable // JPA 내장 타입
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;

    // @NoArgs~ annotatio => protected 설정 가능?
    protected Address() {

    }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
