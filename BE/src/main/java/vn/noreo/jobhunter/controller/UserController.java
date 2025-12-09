package vn.noreo.jobhunter.controller;

import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import vn.noreo.jobhunter.domain.User;
import vn.noreo.jobhunter.domain.response.ResCreateUserDTO;
import vn.noreo.jobhunter.domain.response.ResFetchUserDTO;
import vn.noreo.jobhunter.domain.response.ResUpdateUserDTO;
import vn.noreo.jobhunter.domain.response.ResultPaginationDTO;
import vn.noreo.jobhunter.service.UserService;
import vn.noreo.jobhunter.util.annotation.ApiMessage;
import vn.noreo.jobhunter.util.error.IdInvalidException;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Tag(name = "User", description = "User management APIs")
@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    @ApiMessage("Create new user")
    public ResponseEntity<ResCreateUserDTO> createNewUser(@Valid @RequestBody User newUser) throws IdInvalidException {

        boolean isEmailExists = this.userService.checkUserExistsByEmail(newUser.getEmail());
        if (isEmailExists) {
            throw new IdInvalidException("Email " + newUser.getEmail() + " already exists");
        }
        User user = this.userService.handleCreateUser(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertToResCreateUserDTO(user));
    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("Delete user by id")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") long id) throws IdInvalidException {
        User currentUser = this.userService.handleFetchUserById(id);
        if (currentUser == null) {
            throw new IdInvalidException("User with id " + id + " not found");
        }
        this.userService.handleDeleteUser(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/users/{id}")
    @ApiMessage("Fetch user by id")
    public ResponseEntity<ResFetchUserDTO> fetchUserById(@PathVariable("id") long id) throws IdInvalidException {
        User currentUser = this.userService.handleFetchUserById(id);
        if (currentUser == null) {
            throw new IdInvalidException("User with id " + id + " not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.convertToResFetchUserDTO(currentUser));
    }

    @GetMapping("/users")
    @ApiMessage("Fetch all users")
    public ResponseEntity<ResultPaginationDTO> fetchAllUser(
            @Filter Specification<User> specification,
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.handleFetchAllUsers(specification, pageable));
    }

    @PutMapping("/users")
    @ApiMessage("Update user")
    public ResponseEntity<ResUpdateUserDTO> updateUser(@RequestBody User updatedUser) throws IdInvalidException {
        User currentUser = this.userService.handleUpdateUser(updatedUser);
        if (currentUser == null) {
            throw new IdInvalidException("User with id " + updatedUser.getId() + " not found");
        }
        return ResponseEntity.ok(this.userService.convertToResUpdateUserDTO(currentUser));
    }
}
