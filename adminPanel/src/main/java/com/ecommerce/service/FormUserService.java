package com.ecommerce.service;

import com.ecommerce.model.FormUser;
import com.ecommerce.repository.FormUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class FormUserService {

    @Autowired
    private FormUserRepository formUserRepository;

    public Page<FormUser> getAllFormUsers(Pageable pageable) {
        return formUserRepository.findAll(pageable);
    }

    public FormUser saveFormUser(FormUser formUser) {
        return formUserRepository.save(formUser);
    }
}
