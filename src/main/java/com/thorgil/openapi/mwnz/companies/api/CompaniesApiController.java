package com.thorgil.openapi.mwnz.companies.api;

import java.math.BigDecimal;
import com.thorgil.openapi.mwnz.companies.model.Company;
import com.thorgil.openapi.mwnz.companies.model.Error;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.annotation.Generated;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2024-06-18T07:37:55.802577+12:00[Pacific/Auckland]", comments = "Generator version: 7.5.0")
@Controller
@RequestMapping("${openapi.mWNZCompanies.base-path:/v1}")
public class CompaniesApiController implements CompaniesApi {

    private final CompaniesApiDelegate delegate;

    public CompaniesApiController(@Autowired(required = false) CompaniesApiDelegate delegate) {
        this.delegate = Optional.ofNullable(delegate).orElse(new CompaniesApiDelegate() {});
    }

    @Override
    public CompaniesApiDelegate getDelegate() {
        return delegate;
    }

}
