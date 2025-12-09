package vn.noreo.jobhunter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.noreo.jobhunter.domain.Company;
import vn.noreo.jobhunter.domain.User;
import vn.noreo.jobhunter.domain.response.ResultPaginationDTO;
import vn.noreo.jobhunter.repository.CompanyRepository;
import vn.noreo.jobhunter.repository.UserRepository;

@Service
public class CompanyService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository, UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    public Company handleCreateCompany(Company newCompany) {
        return this.companyRepository.save(newCompany);
    }

    public ResultPaginationDTO handleFetchAllCompanies(Specification<Company> specification, Pageable pageable) {
        Page<Company> companyPage = this.companyRepository.findAll(specification, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(companyPage.getTotalPages());
        meta.setTotal(companyPage.getTotalElements());

        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(companyPage.getContent());

        return resultPaginationDTO;
    }

    public Company handleFetchCompanyById(long id) {
        return this.companyRepository.findById(id).orElse(null);
    }

    public Company handleUpdateCompany(Company updatedCompany) {
        // Company currentCompany = this.handleFetchCompanyById(updatedCompany.getId());
        // if (currentCompany != null) {
        // currentCompany.setName(updatedCompany.getName());
        // currentCompany.setDescription(updatedCompany.getDescription());
        // currentCompany.setAddress(updatedCompany.getAddress());
        // currentCompany.setLogo(updatedCompany.getLogo());

        // currentCompany = this.companyRepository.save(currentCompany);
        // }
        // return currentCompany;

        // Hoặc có thể viết như sau (không cần gọi hàm handleFetchCompanyById):
        Optional<Company> currentCompany = this.companyRepository.findById(updatedCompany.getId());
        if (currentCompany.isPresent()) {
            Company company = currentCompany.get();
            company.setName(updatedCompany.getName());
            company.setDescription(updatedCompany.getDescription());
            company.setAddress(updatedCompany.getAddress());
            company.setLogo(updatedCompany.getLogo());
            return this.companyRepository.save(company);
        }
        return null;
    }

    public void handleDeleteCompany(long id) {
        Optional<Company> companyOptional = this.companyRepository.findById(id);
        if (companyOptional.isPresent()) {
            Company company = companyOptional.get();
            // Fetch all users of this company
            List<User> listUsers = this.userRepository.findByCompany(company);
            this.userRepository.deleteAll(listUsers);
        }
        this.companyRepository.deleteById(id);
    }

    public Optional<Company> findById(long id) {
        return this.companyRepository.findById(id);
    }
}
