package com.thorgil.mwnz;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.thorgil.openapi.mwnz.companies.api.CompaniesApiController;
import com.thorgil.openapi.mwnz.companies.api.CompaniesApiDelegate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CompaniesApiController.class)
class CompaniesApiControllerWebMvcTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompaniesApiDelegate companiesApiDelegate;

    @Test
    @DisplayName("Given an incorrectly formatted company id for a Company lookup then we get an Error model response")
    void incorrectIdFormatShouldBeBadRequest() throws Exception {
        // arrange
        String incorrectFormatForId = "aa";
        // act
        this.mockMvc.perform(get("/v1/companies/" + incorrectFormatForId).contentType(MediaType.APPLICATION_JSON)).andDo(print())
        .andExpect(status().isBadRequest()).andExpect(content().string(is("""
                        {"error":"Bad Request","error_description":"Failed to convert 'id' with value: 'aa'"}""")));

        // assert
        Mockito.verifyNoInteractions(companiesApiDelegate);
    }

}
