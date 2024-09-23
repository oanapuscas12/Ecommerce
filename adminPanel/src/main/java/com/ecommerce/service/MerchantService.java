package com.ecommerce.service;

import com.ecommerce.model.Merchant;
import com.ecommerce.model.User;
import com.ecommerce.repository.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MerchantService {

    @Autowired
    MerchantRepository merchantRepository;

//    public void updateMerchant(Long id, Merchant updatedMerchant) {
//        Optional<Merchant> optionalUser = merchantRepository.findById(id);
//        if (optionalUser.isPresent()) {
//            Merchant existingMerchant = optionalUser.get();
//            existingMerchant.setUsername(updatedMerchant.getUsername());
//            existingMerchant.setEmail(updatedMerchant.getEmail());
//            existingMerchant.setAdmin(updatedMerchant.isAdmin());
//            existingMerchant.setActive(updatedMerchant.isActive());
//            existingMerchant.setIsMerchantMode(updatedMerchant.getIsMerchantMode());
//            merchantRepository.save(existingMerchant);
//        }
//    }

}
