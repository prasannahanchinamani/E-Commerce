package customDesign.E_commerce.service;

import customDesign.E_commerce.Exceptions.UserNotFoundException;
import customDesign.E_commerce.dto.AccountResponse;
import customDesign.E_commerce.dto.ChangePasswordRequest;
import customDesign.E_commerce.dto.UpdateAccountRequest;
import customDesign.E_commerce.entity.User;
import customDesign.E_commerce.repository.UserRepository;
//import jakarta.transaction.Transactional;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService{

    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public String changePassword(String email,  ChangePasswordRequest req) {
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (passwordEncoder.matches(req.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from the current one");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        return "Password changed successfully";
    }
     @Transactional(readOnly = true)
     public AccountResponse getMyAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return new AccountResponse( user.getId(), user.getName(), user.getEmail(), user.getNumber(),user.getRole() );
    }

    @Transactional
    public AccountResponse updateMyAccount(String email, UpdateAccountRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setName(req.getName().trim());
        if (req.getPhone() != null) {
            user.setNumber(Long.parseLong(req.getPhone().replaceAll("\\+", "")));
        }
        userRepository.save(user);
        return new AccountResponse( user.getId(), user.getName(), user.getEmail(), user.getNumber(), user.getRole());
    }

}
