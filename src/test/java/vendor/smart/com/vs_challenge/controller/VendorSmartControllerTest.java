package vendor.smart.com.vs_challenge.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.function.Function;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import vendor.smart.com.vs_challenge.entities.Job;
import vendor.smart.com.vs_challenge.entities.Vendor;
import vendor.smart.com.vs_challenge.repository.VendorSmartRepository;

@WebMvcTest(VendorSmartController.class)
@TestPropertySource(
    properties = {"spring.security.user.name=testing", "spring.security.user.password=testpass"})
class VendorSmartControllerTest {
  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper mapper;

  @MockBean VendorSmartRepository repo;

  @Test
  @DisplayName("All endpoints should exist and work properly")
  void allEndpointsExist() throws Exception {
    this.mockMvc
        .perform(authenticatedRequest(MockMvcRequestBuilders::get, "/vendor-smart/locations"))
        .andExpect(status().isOk());
    verify(this.repo).getAllLocations();
    this.mockMvc
        .perform(authenticatedRequest(MockMvcRequestBuilders::get, "/vendor-smart/services"))
        .andExpect(status().isOk());
    verify(this.repo).getAllServices();
    this.mockMvc
        .perform(
            authenticatedRequest(
                MockMvcRequestBuilders::get, "/vendor-smart/vendors-for-job?jobId=2"))
        .andExpect(status().isOk());
    verify(this.repo).vendorsForJob(2L);
    val job = new Job().setServiceId(-1).setLocationId(-3).setId(-4L);
    this.mockMvc
        .perform(
            authenticatedRequest(MockMvcRequestBuilders::post, "/vendor-smart/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(job)))
        .andExpect(status().isOk());
    verify(this.repo).addJob(job);
    val vendor =
        new Vendor().setLocationId(-3).setId(-4).setServicesCompliance(Map.of(1, true, 2, false));
    this.mockMvc
        .perform(
            authenticatedRequest(MockMvcRequestBuilders::post, "/vendor-smart/vendors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(vendor)))
        .andExpect(status().isOk());
    verify(this.repo).addVendor(vendor);

    verifyNoMoreInteractions(this.repo);
  }

  @Test
  @DisplayName("All endpoints should be secure")
  void allEndpointsAreSecure() throws Exception {
    this.mockMvc
        .perform(unAuthenticatedRequest(MockMvcRequestBuilders::get, "/vendor-smart/locations"))
        .andExpect(status().isUnauthorized());
    this.mockMvc
        .perform(unAuthenticatedRequest(MockMvcRequestBuilders::get, "/vendor-smart/services"))
        .andExpect(status().isUnauthorized());
    this.mockMvc
        .perform(
            unAuthenticatedRequest(
                MockMvcRequestBuilders::get, "/vendor-smart/vendors-for-job?jobId=2"))
        .andExpect(status().isUnauthorized());
    this.mockMvc
        .perform(unAuthenticatedRequest(MockMvcRequestBuilders::post, "/vendor-smart/jobs"))
        .andExpect(status().isUnauthorized());
    this.mockMvc
        .perform(unAuthenticatedRequest(MockMvcRequestBuilders::post, "/vendor-smart/vendors"))
        .andExpect(status().isUnauthorized());

    testWrongCredentials(MockMvcRequestBuilders::get, "/vendor-smart/locations");
    testWrongCredentials(MockMvcRequestBuilders::get, "/vendor-smart/services");
    testWrongCredentials(MockMvcRequestBuilders::get, "/vendor-smart/vendors-for-job?jobId=2");
    testWrongCredentials(MockMvcRequestBuilders::post, "/vendor-smart/jobs");
    testWrongCredentials(MockMvcRequestBuilders::post, "/vendor-smart/vendors");

    verifyNoInteractions(this.repo);
  }

  private void testWrongCredentials(
      Function<String, MockHttpServletRequestBuilder> method, String urlTemplate) throws Exception {
    this.mockMvc
        .perform(
            unAuthenticatedRequest(method, urlTemplate).with(httpBasic("shouldfail", "testpass")))
        .andExpect(status().isUnauthorized());
    this.mockMvc
        .perform(
            unAuthenticatedRequest(method, urlTemplate).with(httpBasic("testing", "tttestpass")))
        .andExpect(status().isUnauthorized());
  }

  static MockHttpServletRequestBuilder authenticatedRequest(
      Function<String, MockHttpServletRequestBuilder> method, String urlTemplate) {
    return method.apply(urlTemplate).with(httpBasic("testing", "testpass")).with(csrf());
  }

  static MockHttpServletRequestBuilder unAuthenticatedRequest(
      Function<String, MockHttpServletRequestBuilder> method, String urlTemplate) {
    return method.apply(urlTemplate).with(csrf());
  }

  static MockHttpServletRequestBuilder badAuthenticatedRequest(
      Function<String, MockHttpServletRequestBuilder> method, String urlTemplate) {
    return method.apply(urlTemplate).with(csrf());
  }
}
