package com.sales.sales.validation;

import com.sales.sales.Repositories.UserRepository;
import com.sales.sales.dto.UserRequest;
import com.sales.sales.exceptions.ExistDataException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class Validation {

    private final UserRepository userRepository;

    public Validation(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void userValidation(UserRequest userRequest) throws Exception {

        if (!StringUtils.hasText(userRequest.getFullName())) {
            throw new IllegalArgumentException("fullname name is invalid");
        }

        if (!StringUtils.hasText(userRequest.getEmail()) || !userRequest.getEmail().matches(ConstantsUtil.EMAIL_REGEX)) {
            throw new IllegalArgumentException("email is invalid");
        } else {
            boolean userEmail = userRepository.existsByEmail(userRequest.getEmail());
            if (userEmail) {
                throw new ExistDataException("Email already exist");
            }
        }

    }

}
