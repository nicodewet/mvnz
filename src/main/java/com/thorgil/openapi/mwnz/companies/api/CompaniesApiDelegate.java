package com.thorgil.openapi.mwnz.companies.api;

import java.math.BigDecimal;
import com.thorgil.openapi.mwnz.companies.model.Company;
import com.thorgil.openapi.mwnz.companies.model.Error;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.annotation.Generated;

/**
 * A delegate to be called by the {@link CompaniesApiController}}.
 * Implement this interface with a {@link org.springframework.stereotype.Service} annotated class.
 */
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2024-06-18T07:37:57.168198+12:00[Pacific/Auckland]", comments = "Generator version: 7.5.0")
public interface CompaniesApiDelegate {

    default Optional<NativeWebRequest> getRequest() {
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
    default ResponseEntity<Company> companiesIdGet(BigDecimal id) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"name\" : \"name\", \"description\" : \"description\", \"id\" : 0 }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"error_description\" : \"error_description\", \"error\" : \"error\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

}
