package vn.noreo.jobhunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import vn.noreo.jobhunter.domain.Company;
import vn.noreo.jobhunter.domain.Role;
import vn.noreo.jobhunter.domain.User;
import vn.noreo.jobhunter.domain.response.ResCreateUserDTO;
import vn.noreo.jobhunter.domain.response.ResFetchUserDTO;
import vn.noreo.jobhunter.domain.response.ResUpdateUserDTO;
import vn.noreo.jobhunter.domain.response.ResultPaginationDTO;
import vn.noreo.jobhunter.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompanyService companyService;
    private final RoleService roleService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CompanyService companyService,
            RoleService roleService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.companyService = companyService;
        this.roleService = roleService;
    }

    public boolean checkUserExistsByEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public ResCreateUserDTO convertToResCreateUserDTO(User user) {
        ResCreateUserDTO userDTO = new ResCreateUserDTO();
        ResCreateUserDTO.CompanyUser companyUser = new ResCreateUserDTO.CompanyUser();
        ResCreateUserDTO.RoleUser roleUser = new ResCreateUserDTO.RoleUser();

        if (user.getCompany() != null) {
            companyUser.setId(user.getCompany().getId());
            companyUser.setName(user.getCompany().getName());
            userDTO.setCompany(companyUser);
        }

        if (user.getRole() != null) {
            roleUser.setId(user.getRole().getId());
            roleUser.setName(user.getRole().getName());
            userDTO.setRole(roleUser);
        }

        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setAge(user.getAge());
        userDTO.setGender(user.getGender());
        userDTO.setAddress(user.getAddress());
        userDTO.setCreatedAt(user.getCreatedAt());
        return userDTO;
    }

    public ResFetchUserDTO convertToResFetchUserDTO(User user) {
        ResFetchUserDTO userDTO = new ResFetchUserDTO();
        ResFetchUserDTO.CompanyUser companyUser = new ResFetchUserDTO.CompanyUser();
        ResFetchUserDTO.RoleUser roleUser = new ResFetchUserDTO.RoleUser();

        if (user.getCompany() != null) {
            companyUser.setId(user.getCompany().getId());
            companyUser.setName(user.getCompany().getName());
            userDTO.setCompany(companyUser);
        }

        if (user.getRole() != null) {
            roleUser.setId(user.getRole().getId());
            roleUser.setName(user.getRole().getName());
            userDTO.setRole(roleUser);
        }

        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setAge(user.getAge());
        userDTO.setGender(user.getGender());
        userDTO.setAddress(user.getAddress());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setUpdatedAt(user.getUpdatedAt());
        return userDTO;
    }

    public ResUpdateUserDTO convertToResUpdateUserDTO(User user) {
        ResUpdateUserDTO userDTO = new ResUpdateUserDTO();
        ResUpdateUserDTO.CompanyUser companyUser = new ResUpdateUserDTO.CompanyUser();
        ResUpdateUserDTO.RoleUser roleUser = new ResUpdateUserDTO.RoleUser();

        if (user.getCompany() != null) {
            companyUser.setId(user.getCompany().getId());
            companyUser.setName(user.getCompany().getName());
            userDTO.setCompany(companyUser);
        }

        if (user.getRole() != null) {
            roleUser.setId(user.getRole().getId());
            roleUser.setName(user.getRole().getName());
            userDTO.setRole(roleUser);
        }

        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setAge(user.getAge());
        userDTO.setGender(user.getGender());
        userDTO.setAddress(user.getAddress());
        userDTO.setUpdatedAt(user.getUpdatedAt());
        return userDTO;
    }

    public User handleCreateUser(User newUser) {
        newUser.setPassword(this.passwordEncoder.encode(newUser.getPassword()));

        // Check company exists
        if (newUser.getCompany() != null) {
            Optional<Company> companyOptional = this.companyService.findById(newUser.getCompany().getId());
            newUser.setCompany(companyOptional.isPresent() ? companyOptional.get() : null);
        }

        // Check role exists
        if (newUser.getRole() != null) {
            Optional<Role> roleOptional = this.roleService.handleFetchRoleById(newUser.getRole().getId());
            newUser.setRole(roleOptional.isPresent() ? roleOptional.get() : null);
        }
        return this.userRepository.save(newUser);
    }

    public void handleDeleteUser(long id) {
        this.userRepository.deleteById(id);
    }

    public User handleFetchUserById(long id) {
        return this.userRepository.findById(id).orElse(null);
    }

    public User handleFetchUserByUsername(String username) {
        return this.userRepository.findByEmail(username);
    }

    public ResultPaginationDTO handleFetchAllUsers(Specification<User> specification, Pageable pageable) {
        Page<User> userPage = this.userRepository.findAll(specification, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(userPage.getTotalPages());
        meta.setTotal(userPage.getTotalElements());

        resultPaginationDTO.setMeta(meta);

        List<ResFetchUserDTO> userDTOs = userPage.getContent().stream()
                .map(this::convertToResFetchUserDTO)
                .collect(Collectors.toList());
        resultPaginationDTO.setResult(userDTOs);
        return resultPaginationDTO;
    }

    public User handleUpdateUser(User updatedUser) {
        User currentUser = this.handleFetchUserById(updatedUser.getId());
        if (currentUser != null) {
            currentUser.setName(updatedUser.getName());
            currentUser.setGender(updatedUser.getGender());
            currentUser.setAge(updatedUser.getAge());
            currentUser.setAddress(updatedUser.getAddress());

            // Check company exists
            if (updatedUser.getCompany() != null) {
                Optional<Company> companyOptional = this.companyService.findById(updatedUser.getCompany().getId());
                currentUser.setCompany(companyOptional.isPresent() ? companyOptional.get() : null);
            }

            // Check role exists
            if (updatedUser.getRole() != null) {
                Optional<Role> roleOptional = this.roleService.handleFetchRoleById(updatedUser.getRole().getId());
                currentUser.setRole(roleOptional.isPresent() ? roleOptional.get() : null);
            }
            currentUser = this.userRepository.save(currentUser);
        }
        return currentUser;
    }

    // Save refresh token to database
    public void updateUserRefreshToken(String token, String email) {
        User currentUser = this.handleFetchUserByUsername(email);
        if (currentUser != null) {
            currentUser.setRefreshToken(token);
            this.userRepository.save(currentUser);
        }
    }

    public User getUserByRefreshTokenAndEmail(String refreshToken, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(refreshToken, email);
    }
}