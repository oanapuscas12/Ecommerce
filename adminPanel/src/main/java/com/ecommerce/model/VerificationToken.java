package com.ecommerce.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class VerificationToken extends BaseEntity {
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column(name = "token")
    private String token;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "token_status")
    @Enumerated(EnumType.STRING)
    private TokenStatus tokenStatus;

    @Column(name = "expiry_date")
    private Date expiryDate;

    String NARROW_STRING_SEPARATOR = "-";

    String APPLICATION_NAME = "E-COMMERCE";


    public enum TokenStatus {
        ACTIVATED_BY_USER,
        ACTIVATED_BY_ADMIN,
        NEW
    }

    // Constructor to create a verification token with a specified token availability in hours
    public VerificationToken(final User user, final int tokenAvailabilityHours) {
        this.user = user;
        expiryDate = calculateExpiryDate(tokenAvailabilityHours);
        token = generateTokenForUser(this.user);
    }

    // Method to calculate the expiry date based on the token availability in hours
    private Date calculateExpiryDate(final int tokenAvailabilityHours) {
        return Date.from(LocalDateTime.now().plusHours(tokenAvailabilityHours).atZone(ZoneId.systemDefault()).toInstant());
    }

    // Method to generate a token for a user
    private String generateTokenForUser(final User user) {
        return MessageFormat.format("{1}{0}{2}{0}{3}", NARROW_STRING_SEPARATOR,
                APPLICATION_NAME, user.getId(), UUID.randomUUID().toString());
    }
}