package vn.noreo.jobhunter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import vn.noreo.jobhunter.domain.Subscriber;
import vn.noreo.jobhunter.service.SubscriberService;
import vn.noreo.jobhunter.util.SecurityUtil;
import vn.noreo.jobhunter.util.annotation.ApiMessage;
import vn.noreo.jobhunter.util.error.IdInvalidException;

@Tag(name = "Subscriber", description = "Subscriber management APIs")
@RestController
@RequestMapping("/api/v1")
public class SubscriberController {

    private final SubscriberService subscriberService;

    public SubscriberController(SubscriberService subscriberService) {
        this.subscriberService = subscriberService;
    }

    @PostMapping("/subscribers")
    @ApiMessage("Create new subscriber")
    public ResponseEntity<Subscriber> createNewSubscriber(@Valid @RequestBody Subscriber newSubscriber)
            throws IdInvalidException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.subscriberService.handleCreateSubscriber(newSubscriber));
    }

    @PutMapping("/subscribers")
    @ApiMessage("Update subscriber")
    public ResponseEntity<Subscriber> updateSubscriber(@RequestBody Subscriber newSubscriber)
            throws IdInvalidException {
        return ResponseEntity.ok().body(this.subscriberService.handeUpdateSubscriber(newSubscriber));
    }

    @PostMapping("/subscribers/skills")
    @ApiMessage("Fetch all subscriber's skills")
    public ResponseEntity<Subscriber> fetchAllSubscribersSkills() throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() == true ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        return ResponseEntity.ok().body(this.subscriberService.getSubscriberByEmail(email));
    }
}
