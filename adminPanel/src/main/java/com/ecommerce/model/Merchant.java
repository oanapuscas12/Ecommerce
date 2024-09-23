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
public class Merchant extends User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cui")
    private String cui;

    @Column(name = "legal_business_name")
    private String legalBusinessName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "nr_reg_com")
    private String nr_reg_com;

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

    @Column(name = "isMerchantMode")
    private boolean isMerchantMode;
}
