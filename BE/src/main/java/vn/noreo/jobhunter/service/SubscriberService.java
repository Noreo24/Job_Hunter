package vn.noreo.jobhunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import vn.noreo.jobhunter.domain.Job;
import vn.noreo.jobhunter.domain.Skill;
import vn.noreo.jobhunter.domain.Subscriber;
import vn.noreo.jobhunter.domain.response.email.ResEmailJobDTO;
import vn.noreo.jobhunter.repository.JobRepository;
import vn.noreo.jobhunter.repository.SkillRepository;
import vn.noreo.jobhunter.repository.SubscriberRepository;
import vn.noreo.jobhunter.util.error.IdInvalidException;

@Service
public class SubscriberService {

    private final SubscriberRepository subscriberRepository;
    private final SkillRepository skillRepository;
    private final JobRepository jobRepository;
    private final EmailService emailService;

    public SubscriberService(
            SubscriberRepository subscriberRepository,
            SkillRepository skillRepository,
            JobRepository jobRepository,
            EmailService emailService) {
        this.subscriberRepository = subscriberRepository;
        this.skillRepository = skillRepository;
        this.jobRepository = jobRepository;
        this.emailService = emailService;
    }

    // @Scheduled(cron = "*/10 * * * * *")
    // public void testCron() {
    // System.out.println(">>>>>>>>>>>>>>>>>>>> TEST CRON");
    // }

    public Subscriber getSubscriberByEmail(String email) {
        return this.subscriberRepository.findByEmail(email);
    }

    public Optional<Subscriber> handleFetchSubscriberById(long id) {
        return this.subscriberRepository.findById(id);
    }

    public Subscriber handleCreateSubscriber(Subscriber newSubscriber) throws IdInvalidException {

        // Check email exists
        boolean isEmailExists = this.subscriberRepository.existsByEmail(newSubscriber.getEmail());
        if (isEmailExists) {
            throw new IdInvalidException("Email " + newSubscriber.getEmail() + " already exists");
        }

        // Check skill exists
        if (newSubscriber.getSkills() != null) {
            List<Long> reqSkills = newSubscriber.getSkills().stream()
                    .map(skill -> skill.getId())
                    .collect(Collectors.toList());
            List<Skill> listSkill = this.skillRepository.findAllById(reqSkills);
            newSubscriber.setSkills(listSkill);
        }

        return this.subscriberRepository.save(newSubscriber);
    }

    public Subscriber handeUpdateSubscriber(Subscriber updatedSubscriber) throws IdInvalidException {
        Optional<Subscriber> subscriberOpt = this.handleFetchSubscriberById(updatedSubscriber.getId());
        if (subscriberOpt.isEmpty()) {
            throw new IdInvalidException("Subscriber with id " + updatedSubscriber.getId() + " not found");
        }
        Subscriber currentSubscriber = subscriberOpt.get();

        if (updatedSubscriber.getSkills() != null) {
            List<Long> reqSkills = updatedSubscriber.getSkills().stream()
                    .map(skill -> skill.getId())
                    .collect(Collectors.toList());
            List<Skill> listSkill = this.skillRepository.findAllById(reqSkills);
            currentSubscriber.setSkills(listSkill);
        }
        return this.subscriberRepository.save(currentSubscriber);
    }

    public ResEmailJobDTO convertJobToSendEmail(Job job) {
        ResEmailJobDTO response = new ResEmailJobDTO();
        response.setName(job.getName());
        response.setSalary(job.getSalary());
        response.setCompany(new ResEmailJobDTO.CompanyEmail(job.getCompany().getName()));
        List<Skill> listkills = job.getSkills();
        List<ResEmailJobDTO.SkillEmail> eachSkill = listkills.stream()
                .map(skill -> new ResEmailJobDTO.SkillEmail(skill.getName()))
                .collect(Collectors.toList());
        response.setSkills(eachSkill);
        return response;
    }

    public void sendSubscribersEmailJobs() {
        List<Subscriber> listSubs = this.subscriberRepository.findAll();
        if (listSubs != null && listSubs.size() > 0) {
            for (Subscriber sub : listSubs) {
                List<Skill> listSkills = sub.getSkills();
                if (listSkills != null && listSkills.size() > 0) {
                    List<Job> listJobs = this.jobRepository.findBySkillsIn(listSkills);
                    if (listJobs != null && listJobs.size() > 0) {
                        List<ResEmailJobDTO> listJobDTOs = listJobs.stream().map(
                                job -> this.convertJobToSendEmail(job)).collect(Collectors.toList());
                        this.emailService.sendEmailFromTemplateSync(
                                sub.getEmail(),
                                "Cơ hội việc làm hot đang chờ đón bạn, khám phá ngay",
                                "job",
                                sub.getName(),
                                listJobDTOs);
                    }
                }
            }
        }
    }
}
