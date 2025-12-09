package vn.noreo.jobhunter.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import vn.noreo.jobhunter.domain.User;
import vn.noreo.jobhunter.domain.request.ReqLoginDTO;
import vn.noreo.jobhunter.domain.response.ResCreateUserDTO;
import vn.noreo.jobhunter.domain.response.ResLoginDTO;
import vn.noreo.jobhunter.service.UserService;
import vn.noreo.jobhunter.util.SecurityUtil;
import vn.noreo.jobhunter.util.annotation.ApiMessage;
import vn.noreo.jobhunter.util.error.IdInvalidException;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Authentication", description = "Authentication APIs")
@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final UserService userService;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    public AuthController(
            AuthenticationManagerBuilder authenticationManagerBuilder,
            SecurityUtil securityUtil,
            UserService userService) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.userService = userService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO loginRequest) {

        // Nạp input gồm username/password vào Security
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), loginRequest.getPassword());

        // Xác thực người dùng => cần viết hàm loadUserByUsername
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // Lưu thông tin vào SecurityContextHolder (Có thể sử dụng sau này)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResLoginDTO resLoginDTO = new ResLoginDTO();
        User currentUser = this.userService.handleFetchUserByUsername(loginRequest.getUsername());
        if (currentUser != null) {
            ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                    currentUser.getId(),
                    currentUser.getEmail(),
                    currentUser.getName(),
                    currentUser.getRole());
            resLoginDTO.setUser(userLogin);
        }

        // Create access token
        String accessToken = this.securityUtil.createAccessToken(authentication.getName(), resLoginDTO);
        resLoginDTO.setAccessToken(accessToken);

        // Create refresh token
        String refreshToken = this.securityUtil.createRefreshToken(loginRequest.getUsername(), resLoginDTO);
        // Update refresh token to database
        this.userService.updateUserRefreshToken(refreshToken, loginRequest.getUsername());
        // Không lưu access token vào database vì trong db không có access token và ...

        // Set cookie
        ResponseCookie responseCookie = ResponseCookie
                .from("refreshToken", refreshToken)
                .httpOnly(true) // Cho phép cookie được truy cập từ http (server), k cho phép truy cập từ js
                .secure(true) // Chỉ gửi cookie qua https, k gửi qua http
                .path("/") // Đường dẫn cookie, sử dụng với tất cả các request trong dự án
                .maxAge(refreshTokenExpiration) // Thời gian sống của cookie, ở đây = thời gian sống của refresh token
                .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString()).body(resLoginDTO);
    }

    // Lấy thông tin tài khoản
    @GetMapping("/auth/account")
    @ApiMessage("Fetch account information")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";

        User currentUser = this.userService.handleFetchUserByUsername(email);
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();

        if (currentUser != null) {
            userLogin.setId(currentUser.getId());
            userLogin.setEmail(currentUser.getEmail());
            userLogin.setName(currentUser.getName());
            userLogin.setRole(currentUser.getRole());

            userGetAccount.setUser(userLogin);
        }
        return ResponseEntity.ok().body(userGetAccount);
    }

    @GetMapping("/auth/refresh")
    @ApiMessage("Refresh access token")
    public ResponseEntity<ResLoginDTO> getRefreshToken(
            @CookieValue(name = "refreshToken", defaultValue = "noCookies") String refreshToken)
            throws IdInvalidException {

        // Check cookies exist
        if (refreshToken.equals("noCookies")) {
            throw new IdInvalidException("You don't have refresh token in cookies!");
        }

        // Check refresh token
        Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refreshToken);
        String email = decodedToken.getSubject();

        // Check user exist by email & refresh token
        User currentUser = this.userService.getUserByRefreshTokenAndEmail(refreshToken, email);
        if (currentUser == null) {
            throw new IdInvalidException("Refresh token is invalid");
        }

        // Create new access token/set refresh token cookies
        ResLoginDTO resLoginDTO = new ResLoginDTO();
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                currentUser.getId(),
                currentUser.getEmail(),
                currentUser.getName(),
                currentUser.getRole());
        resLoginDTO.setUser(userLogin);

        // Create access token
        String accessToken = this.securityUtil.createAccessToken(email, resLoginDTO);
        resLoginDTO.setAccessToken(accessToken);

        // Create refresh token
        String newRefreshToken = this.securityUtil.createRefreshToken(email, resLoginDTO);
        // Update refresh token to database
        this.userService.updateUserRefreshToken(newRefreshToken, email);
        // Không lưu access token vào database vì trong db không có access token và ...

        // Set cookies
        ResponseCookie responseCookie = ResponseCookie
                .from("refreshToken", newRefreshToken)
                .httpOnly(true) // Cho phép cookie được truy cập từ http (server), k cho phép truy cập từ js
                .secure(true) // Chỉ gửi cookie qua https, k gửi qua http
                .path("/") // Đường dẫn cookie, sử dụng với tất cả các request trong dự án
                .maxAge(refreshTokenExpiration) // Thời gian sống của cookie, ở đây = thời gian sống của refresh token
                .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString()).body(resLoginDTO);
    }

    @PostMapping("/auth/logout")
    @ApiMessage("Logout")
    public ResponseEntity<Void> logout() throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";

        if (email.equals("")) {
            throw new IdInvalidException("Email is null");
        }

        // Update refresh token to null
        this.userService.updateUserRefreshToken(null, email);

        // Xóa cookie
        ResponseCookie deleteCookie = ResponseCookie
                .from("refreshToken", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) // Xóa cookie
                .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, deleteCookie.toString()).body(null);
    }

    @PostMapping("/auth/register")
    @ApiMessage("Register new user")
    public ResponseEntity<ResCreateUserDTO> register(@Valid @RequestBody User newUser) throws IdInvalidException {
        boolean isEmailExists = this.userService.checkUserExistsByEmail(newUser.getEmail());
        if (isEmailExists) {
            throw new IdInvalidException("Email " + newUser.getEmail() + " already exists");
        }
        User createdUser = this.userService.handleCreateUser(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertToResCreateUserDTO(createdUser));
    }
}
