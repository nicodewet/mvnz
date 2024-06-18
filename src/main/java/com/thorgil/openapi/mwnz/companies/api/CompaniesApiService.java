package com.thorgil.openapi.mwnz.companies.api;

import com.thorgil.openapi.mwnz.companies.model.Company;
import com.thorgil.openapi.mwnz.xml.client.api.CompaniesXmlApi;
import com.thorgil.openapi.mwnz.xml.client.model.ActualXmlCompany;
import com.thorgil.openapi.mwnz.xml.client.model.XmlCompany;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

@Service
public class CompaniesApiService implements CompaniesApiDelegate {

    @Autowired
    CompaniesXmlApi companiesXmlApi;

    public Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    /**
     * GET /companies/{id}
     *
     * @param id Company ID (required)
     * @return OK (status code 200)
     *         or Not Found (status code 404)
     * @see CompaniesApi#companiesIdGet
     */
    public ResponseEntity<Company> companiesIdGet(BigDecimal id) {

        ActualXmlCompany xmlCompany = companiesXmlApi.xmlApiIdXmlGet(id);
        if (xmlCompany != null) {
            Company company = new Company();
            company.setDescription(xmlCompany.getDescription());
            company.setId(xmlCompany.getId());
            company.setName(xmlCompany.getName());
            // TODO set header here
            return ResponseEntity.ok().body(company);
        } else {
            throw new IllegalArgumentException();
        }
    }
}
