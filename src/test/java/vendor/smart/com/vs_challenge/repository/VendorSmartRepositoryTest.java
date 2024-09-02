package vendor.smart.com.vs_challenge.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.annotation.UserConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import vendor.smart.com.vs_challenge.entities.Job;
import vendor.smart.com.vs_challenge.entities.Location;
import vendor.smart.com.vs_challenge.entities.Service;
import vendor.smart.com.vs_challenge.entities.Vendor;
import vendor.smart.com.vs_challenge.repository.VendorSmartRepository.VendorSmartException;

class VendorSmartRepositoryTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  @DisplayName("Should load resources correctly")
  void resourcesAreLoaded() {
    new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
        .withConfiguration(UserConfigurations.of(VendorSmartRepository.class))
        .run(
            ctx -> {
              assertEquals(
                  MAPPER.readValue(
                      new String(
                          VendorSmartRepository.class
                              .getResourceAsStream("locations.json")
                              .readAllBytes()),
                      new TypeReference<Set<Location>>() {}),
                  new HashSet<>(ctx.getBean(VendorSmartRepository.class).getAllLocations()),
                  "Locations should be loaded correctly");
              assertEquals(
                  MAPPER.readValue(
                      new String(
                          VendorSmartRepository.class
                              .getResourceAsStream("services.json")
                              .readAllBytes()),
                      new TypeReference<Set<Service>>() {}),
                  new HashSet<>(ctx.getBean(VendorSmartRepository.class).getAllServices()),
                  "Services should be loaded correctly");
            });
  }

  @Test
  @DisplayName("addJob should check for location and service")
  void addJobFailures() {
    val repo = new VendorSmartRepository(MAPPER);
    var e =
        assertThrows(
            VendorSmartException.class,
            () -> repo.addJob(new Job().setId(99L).setLocationId(321).setServiceId(1)));
    assertEquals("Invalid service reference for this newJob", e.getMessage());

    repo.services.put(1, new Service());

    e =
        assertThrows(
            VendorSmartException.class,
            () -> repo.addJob(new Job().setId(99L).setLocationId(321).setServiceId(1)));
    assertEquals("Invalid location reference for this newJob", e.getMessage());

    repo.locations.put(321, new Location());
    repo.addJob(new Job().setId(99L).setLocationId(321).setServiceId(1));

    e =
        assertThrows(
            VendorSmartException.class,
            () -> repo.addJob(new Job().setId(99L).setLocationId(321).setServiceId(1)));
    assertEquals("A job for this location and service exists: 99", e.getMessage());
  }

  @Test
  @DisplayName("existsJob4LocAndSvc should behave correctly")
  void existsJob4LocAndSvc() {
    val repo = new VendorSmartRepository(MAPPER);
    repo.services.put(1, new Service());
    repo.locations.put(321, new Location());

    assertNull(repo.existsJob4LocAndSvc(new Job()));

    repo.addJob(new Job().setId(99L).setLocationId(321).setServiceId(1));

    assertEquals(
        new Job().setId(99L).setLocationId(321).setServiceId(1),
        repo.existsJob4LocAndSvc(new Job().setId(123456L).setLocationId(321).setServiceId(1)));
  }

  @Test
  @DisplayName("addVendor should behave correctly")
  void addVendorFailures() {
    val repo = new VendorSmartRepository(MAPPER);
    var e =
        assertThrows(
            VendorSmartException.class,
            () ->
                repo.addVendor(
                    new Vendor()
                        .setLocationId(1)
                        .setServicesCompliance(Map.of(1, false, 2, true, 3, false))));
    assertEquals("Invalid location reference for this vendor", e.getMessage());

    repo.locations.put(1, new Location());
    repo.services.put(1, new Service());
    repo.services.put(3, new Service());

    e =
        assertThrows(
            VendorSmartException.class,
            () ->
                repo.addVendor(
                    new Vendor()
                        .setLocationId(1)
                        .setServicesCompliance(Map.of(1, false, 2, true, 3, false))));
    assertEquals("Invalid service compliance reference", e.getMessage());

    repo.services.put(2, new Service());
    repo.addVendor(
        new Vendor().setLocationId(1).setServicesCompliance(Map.of(1, false, 2, true, 3, false)));

    e =
        assertThrows(
            VendorSmartException.class,
            () ->
                repo.addVendor(
                    new Vendor()
                        .setLocationId(1)
                        .setServicesCompliance(Map.of(1, false, 2, true, 3, false))));
    assertEquals("This vendor already exists", e.getMessage());
  }

  @Test
  @DisplayName("addVendor/vendorsForJob should behave correctly")
  void addVendorAndVendorsForJob() {
    val repo = new VendorSmartRepository(MAPPER);
    repo.locations.put(1, new Location());
    repo.services.put(1, new Service());
    repo.services.put(3, new Service());

    repo.addVendor(
        new Vendor().setId(55).setLocationId(1).setServicesCompliance(Map.of(1, false, 3, false)));
    final var expectedVendor =
        new Vendor().setId(55).setLocationId(1).setServicesCompliance(Map.of(1, false, 3, false));
    assertEquals(expectedVendor, repo.vendors.get(55));

    assertEquals(
        List.of(expectedVendor),
        repo.vendorsForJob(1L),
        "Should have auto created the job and mapped to the vendor");
    assertEquals(
        List.of(expectedVendor),
        repo.vendorsForJob(2L),
        "Should have auto created the job and mapped to the vendor");
  }

  @Test
  @DisplayName("vendorsForJob should behave correctly")
  void vendorsForJob() {
    val repo = new VendorSmartRepository(MAPPER);
    repo.locations.put(1, new Location());
    repo.locations.put(2, new Location());
    repo.services.put(1, new Service());
    repo.services.put(3, new Service());
    repo.services.put(4, new Service());
    repo.addJob(new Job().setId(1234L).setLocationId(2).setServiceId(3));
    repo.addJob(new Job().setId(97L).setLocationId(1).setServiceId(1));
    repo.addJob(new Job().setId(35L).setLocationId(1).setServiceId(3));

    repo.addVendor(
        new Vendor()
            .setId(57)
            .setLocationId(1)
            .setServicesCompliance(Map.of(1, false, 3, false, 4, true)));
    repo.addVendor(
        new Vendor().setId(56).setLocationId(1).setServicesCompliance(Map.of(1, true, 3, false)));
    repo.addVendor(
        new Vendor().setId(55).setLocationId(2).setServicesCompliance(Map.of(1, true, 3, true)));

    val expectedVendor1 =
        new Vendor()
            .setId(57)
            .setLocationId(1)
            .setServicesCompliance(Map.of(1, false, 3, false, 4, true));
    val expectedVendor2 =
        new Vendor().setId(56).setLocationId(1).setServicesCompliance(Map.of(1, true, 3, false));
    val expectedVendor3 =
        new Vendor().setId(55).setLocationId(2).setServicesCompliance(Map.of(1, true, 3, true));

    assertEquals(
        List.of(expectedVendor3),
        repo.vendorsForJob(1234L),
        "Should have auto created the job and mapped to the vendor");
    assertEquals(
        List.of(expectedVendor2, expectedVendor1),
        repo.vendorsForJob(97L),
        "Should have auto created the job and mapped to the vendor and prioritized the compliant one");
    assertEquals(
        2, repo.reachable(1, 1), "Should calculate the proper amount of reachable vendors");
    assertEquals(
        1, repo.reachable(2, 3), "Should calculate the proper amount of reachable vendors");
  }
}
