package com.ecommerce.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "merchants")
@PrimaryKeyJoinColumn(name = "user_id")
public class Merchant extends User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //de facut legatura cu users
    @Column(name = "user_id")
    private long user_id;

    @Column(name = "email")
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "user_name")
    private String user_name;

    @Column(name = "isAdmin")
    private boolean isAdmin;

    @Column(name = "isActive")
    private boolean active;

    @Column(name = "cui")
    private long cui;

    @Column(name = "legal_business_name")
    private String legalBusinessName;

    @Column(name = "phone_number")
    private long phoneNumber;

    @Column(name = "nr_reg_com")
    private long nr_reg_com;

    @Column(name = "country")
    private String country;

    @Column(name = "county")
    private String county;

    @Column(name = "city")
    private String city;

    @Column(name = "address")
    private String address;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "industry")
    private String industry;

    @Column(name = "isStoreLaunched")
    private boolean storeLaunched;

    @Column(name = "isStoreActive")
    private boolean storeActive;
}
