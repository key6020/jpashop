package jpabook.jpashop.controllers;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter @Setter
public class MemberForm {

    @NotEmpty(message = "Member Name is necessary.")
    private String name;

    private String city;
    private String street;
    private String zipcode;
}
