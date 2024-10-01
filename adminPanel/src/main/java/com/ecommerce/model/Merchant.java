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

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Getter
    @Column(name = "cui")
    private String cui;

    @Setter
    @Getter
    @Column(name = "legal_business_name")
    private String legalBusinessName;

    @Setter
    @Getter
    @Column(name = "phone_number")
    private String phoneNumber;

    @Setter
    @Getter
    @Column(name = "nr_reg_com")
    private String nr_reg_com;

    @Setter
    @Getter
    @Column(name = "country")
    private String country;

    @Setter
    @Getter
    @Column(name = "county")
    private String county;

    @Setter
    @Getter
    @Column(name = "city")
    private String city;

    @Getter
    @Setter
    @Column(name = "address")
    private String address;

    @Getter
    @Setter
    @Column(name = "postal_code")
    private String postalCode;

    @Getter
    @Setter
    @Column(name = "industry")
    private String industry;

    @Getter
    @Setter
    @Column(name = "isStoreLaunched")
    private boolean storeLaunched;

    @Getter
    @Setter
    @Column(name = "isStoreActive")
    private boolean storeActive;

    @Column(name = "isMerchantMode")
    private boolean isMerchantMode;

    public boolean isMerchantMode() {
        return isMerchantMode;
    }

    public void setMerchantMode(boolean merchantMode) {
        isMerchantMode = merchantMode;
    }

    public boolean getMerchantMode() {
        return isMerchantMode;
    }
}
