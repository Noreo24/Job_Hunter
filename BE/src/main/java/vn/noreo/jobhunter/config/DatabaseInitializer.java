package vn.noreo.jobhunter.config;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import vn.noreo.jobhunter.domain.Permission;
import vn.noreo.jobhunter.domain.Role;
import vn.noreo.jobhunter.domain.User;
import vn.noreo.jobhunter.repository.PermissionRepository;
import vn.noreo.jobhunter.repository.RoleRepository;
import vn.noreo.jobhunter.repository.UserRepository;
import vn.noreo.jobhunter.util.constant.GenderEnum;

@Service
public class DatabaseInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    public DatabaseInitializer(PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // System.out.println(">>>>>>>>>> START INIT DATABASE <<<<<<<<<<");
        log.info(">>>>>>>>>> START INIT DATABASE <<<<<<<<<<");
        long countPermissions = permissionRepository.count();
        long countRoles = roleRepository.count();
        long countUsers = userRepository.count();

        if (countPermissions == 0) {
            ArrayList<Permission> listPermissions = new ArrayList<>();
            listPermissions.add(new Permission("Create a company", "/api/v1/companies", "POST", "COMPANIES"));
            listPermissions.add(new Permission("Update a company", "/api/v1/companies", "PUT", "COMPANIES"));
            listPermissions.add(new Permission("Delete a company", "/api/v1/companies/{id}", "DELETE", "COMPANIES"));
            listPermissions
                    .add(new Permission("Get all companies with pagination", "/api/v1/companies", "GET", "COMPANIES"));
            listPermissions.add(new Permission("Get a company by id", "/api/v1/companies/{id}", "GET", "COMPANIES"));

            listPermissions.add(new Permission("Create a job", "/api/v1/jobs", "POST", "JOBS"));
            listPermissions.add(new Permission("Update a job", "/api/v1/jobs", "PUT", "JOBS"));
            listPermissions.add(new Permission("Delete a job", "/api/v1/jobs/{id}", "DELETE", "JOBS"));
            listPermissions.add(new Permission("Get a job by id", "/api/v1/jobs/{id}", "GET", "JOBS"));
            listPermissions.add(new Permission("Get all jobs with pagination", "/api/v1/jobs", "GET", "JOBS"));

            listPermissions.add(new Permission("Create a permission", "/api/v1/permissions", "POST", "PERMISSIONS"));
            listPermissions.add(new Permission("Update a permission", "/api/v1/permissions", "PUT", "PERMISSIONS"));
            listPermissions
                    .add(new Permission("Delete a permission", "/api/v1/permissions/{id}", "DELETE", "PERMISSIONS"));
            listPermissions
                    .add(new Permission("Get a permission by id", "/api/v1/permissions/{id}", "GET", "PERMISSIONS"));
            listPermissions.add(
                    new Permission("Get all permissions with pagination", "/api/v1/permissions", "GET", "PERMISSIONS"));

            listPermissions.add(new Permission("Create a resume", "/api/v1/resumes", "POST", "RESUMES"));
            listPermissions.add(new Permission("Update a resume", "/api/v1/resumes", "PUT", "RESUMES"));
            listPermissions.add(new Permission("Delete a resume", "/api/v1/resumes/{id}", "DELETE", "RESUMES"));
            listPermissions.add(new Permission("Get a resume by id", "/api/v1/resumes/{id}", "GET", "RESUMES"));
            listPermissions.add(new Permission("Get all resumes with pagination", "/api/v1/resumes", "GET", "RESUMES"));

            listPermissions.add(new Permission("Create a role", "/api/v1/roles", "POST", "ROLES"));
            listPermissions.add(new Permission("Update a role", "/api/v1/roles", "PUT", "ROLES"));
            listPermissions.add(new Permission("Delete a role", "/api/v1/roles/{id}", "DELETE", "ROLES"));
            listPermissions.add(new Permission("Get a role by id", "/api/v1/roles/{id}", "GET", "ROLES"));
            listPermissions.add(new Permission("Get all roles with pagination", "/api/v1/roles", "GET", "ROLES"));

            listPermissions.add(new Permission("Create a user", "/api/v1/users", "POST", "USERS"));
            listPermissions.add(new Permission("Update a user", "/api/v1/users", "PUT", "USERS"));
            listPermissions.add(new Permission("Delete a user", "/api/v1/users/{id}", "DELETE", "USERS"));
            listPermissions.add(new Permission("Get a user by id", "/api/v1/users/{id}", "GET", "USERS"));
            listPermissions.add(new Permission("Get all users with pagination", "/api/v1/users", "GET", "USERS"));

            listPermissions.add(new Permission("Create a subscriber", "/api/v1/subscribers", "POST", "SUBSCRIBERS"));
            listPermissions.add(new Permission("Update a subscriber", "/api/v1/subscribers", "PUT", "SUBSCRIBERS"));
            listPermissions
                    .add(new Permission("Delete a subscriber", "/api/v1/subscribers/{id}", "DELETE", "SUBSCRIBERS"));
            listPermissions
                    .add(new Permission("Get a subscriber by id", "/api/v1/subscribers/{id}", "GET", "SUBSCRIBERS"));
            listPermissions.add(
                    new Permission("Get all subscribers with pagination", "/api/v1/subscribers", "GET", "SUBSCRIBERS"));

            listPermissions.add(new Permission("Download a file", "/api/v1/files", "POST", "FILES"));
            listPermissions.add(new Permission("Upload a file", "/api/v1/files", "GET", "FILES"));

            this.permissionRepository.saveAll(listPermissions);

        }

        if (countRoles == 0) {
            List<Permission> allPermissions = this.permissionRepository.findAll();

            Role adminRole = new Role();
            adminRole.setName("SUPER_ADMIN");
            adminRole.setDescription("Admin thì full permissions");
            adminRole.setActive(true);
            adminRole.setPermissions(allPermissions);
            this.roleRepository.save(adminRole);
        }

        if (countUsers == 0) {
            User adminUser = new User();
            adminUser.setEmail("admin@gmail.com");
            adminUser.setAddress("Hòa Bình");
            adminUser.setAge(22);
            adminUser.setGender(GenderEnum.MALE);
            adminUser.setName("Bùi Anh Tuấn");
            adminUser.setPassword(this.passwordEncoder.encode("123"));

            Role adminRole = this.roleRepository.findByName("SUPER_ADMIN");
            if (adminRole != null) {
                adminUser.setRole(adminRole);
            }
            this.userRepository.save(adminUser);
        }

        if (countUsers == 0 && countRoles == 0 && countPermissions == 0) {
            log.info(">>>>>>>>>> INIT DATABASE SUCCESS <<<<<<<<<<");
        } else {
            log.info(">>>>>>>>>> INIT DATABASE SKIPPED <<<<<<<<<<");
        }
    }

}
