package customDesign.E_commerce.service;
/*
import customDesign.E_commerce.dto.RegisterRequest;
import customDesign.E_commerce.dto.RegisterResponse;
import customDesign.E_commerce.entity.Role;
import customDesign.E_commerce.entity.User;
import customDesign.E_commerce.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse registerBuyer(RegisterRequest request) {

        // 2. Create User entity
        User user = new User();

        user.setName(request.getFullName());
        user.setEmail(request.getEmail());
        // 3. Encrypt password
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // 4. Assign default role
        user.setRole(Role.BUYER);
        // 5. Save user
        User savedUser = userRepository.save(user);

        // 6. Return response
        return new RegisterResponse(
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                "User registered successfully"
        );
    }
}
*/