package com.ecommerce.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "form_users")
public class FormUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cui")
    private String cui;

    @Column(name = "denumire")
    private String denumire;

    @Column(name = "adresa")
    private String adresa;

    @Column(name = "nr_reg_com")
    private String nrRegCom;

    @Column(name = "telefon")
    private String telefon;

    @Column(name = "cod_postal")
    private String codPostal;

    @Column(name = "email")
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "user_name", nullable = false)
    private String username;
}
