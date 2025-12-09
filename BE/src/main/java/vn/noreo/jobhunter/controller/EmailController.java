package vn.noreo.jobhunter.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
// import jakarta.transaction.Transactional;
import vn.noreo.jobhunter.service.SubscriberService;
import vn.noreo.jobhunter.util.annotation.ApiMessage;

// import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Email", description = "Email management APIs")
@RestController
@RequestMapping("/api/v1")
public class EmailController {

    private final SubscriberService subscriberService;

    public EmailController(SubscriberService subscriberService) {
        this.subscriberService = subscriberService;
    }

    @GetMapping("/email")
    @ApiMessage("Send email")
    // @Scheduled(cron = "*/30 * * * * *") // Auto run this API after 30s
    // @Transactional
    public String sendEmail() {
        this.subscriberService.sendSubscribersEmailJobs();
        return "ok";
    }

}
