/**
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech) (7.5.0).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
package com.thorgil.openapi.mwnz.companies.api;

import com.thorgil.openapi.mwnz.companies.model.Company;
import com.thorgil.openapi.mwnz.companies.model.Error;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Generated;
import java.math.BigDecimal;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2024-06-18T07:37:57.168198+12:00[Pacific/Auckland]", comments = "Generator version: 7.5.0")
@Validated
@Controller
@Tag(name = "Companies", description = "the Companies API")
public interface CompaniesApi {

    default CompaniesApiDelegate getDelegate() {
        return new CompaniesApiDelegate() {};
    }

    /**
     * GET /companies/{id}
     *
     * @param id Company ID (required)
     * @return OK (status code 200)
     *         or Not Found (status code 404)
     */
    @Operation(
        operationId = "companiesIdGet",
        tags = { "Companies" },
        responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = Company.class))
            }),
            @ApiResponse(responseCode = "404", description = "Not Found", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))
            })
        }
    )
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/companies/{id}",
        produces = { "application/json" }
    )
    
    default ResponseEntity<Company> companiesIdGet(
        @Parameter(name = "id", description = "Company ID", required = true, in = ParameterIn.PATH) @PathVariable("id") BigDecimal id
    ) {
        return getDelegate().companiesIdGet(id);
    }

}
