package vendor.smart.com.vs_challenge.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import vendor.smart.com.vs_challenge.entities.Job;
import vendor.smart.com.vs_challenge.entities.Location;
import vendor.smart.com.vs_challenge.entities.Service;
import vendor.smart.com.vs_challenge.entities.Vendor;

/**
 * In real case scenarios, each entity would have its own repository, I am merging them for
 * simplicity's sake
 */
@Repository
@RequiredArgsConstructor
public class VendorSmartRepository {
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Accessors(chain = true)
  private static class LocationService {
    private int locationId;
    private int serviceId;
  }

  public static class VendorSmartException extends RuntimeException {
    public VendorSmartException(String msg) {
      super(msg);
    }
  }

  @Value(PATH_2_RESOURCES + "services.json")
  Resource servicesResource;

  @Value(PATH_2_RESOURCES + "locations.json")
  Resource locationsResource;

  /** An emulation for a real database of locations */
  final Map<Integer, Location> locations = new HashMap<>();

  /** An emulation for a real database of services */
  final Map<Integer, Service> services = new HashMap<>();

  /** An emulation for a real database of jobs */
  private final Map<Long, Job> jobs = new HashMap<>();

  private final Map<LocationService, Job> svcLoc2jobs = new HashMap<>();

  /** An emulation for a real database of vendors */
  final Map<Integer, Vendor> vendors = new HashMap<>();

  /** An emulation for a real database of vendors */
  private final Map<Long, List<Integer>> job2VendorMapping = new HashMap<>();

  private final ObjectMapper mapper;

  @PostConstruct()
  public void init() throws IOException {
    this.locations.putAll(
        this.mapper
            .readValue(
                this.locationsResource.getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<List<Location>>() {})
            .stream()
            .collect(Collectors.toMap(Location::getId, Function.identity())));
    this.services.putAll(
        this.mapper
            .readValue(
                this.servicesResource.getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<List<Service>>() {})
            .stream()
            .collect(Collectors.toMap(Service::getId, Function.identity())));
  }

  public Collection<Location> getAllLocations() {
    return this.locations.values();
  }

  public Collection<Service> getAllServices() {
    return this.services.values();
  }

  public Job existsJob4LocAndSvc(Job job) {
    return this.svcLoc2jobs.get(
        LocationService.builder()
            .locationId(job.getLocationId())
            .serviceId(job.getServiceId())
            .build());
  }

  public Job addJob(final Job newJob) {
    if (this.services.get(newJob.getServiceId()) == null) {
      throw new VendorSmartException("Invalid service reference for this newJob");
    }
    if (this.locations.get(newJob.getLocationId()) == null) {
      throw new VendorSmartException("Invalid location reference for this newJob");
    }

    val existingJob = existsJob4LocAndSvc(newJob);
    if (existingJob != null) {
      throw new VendorSmartException(
          String.format("A job for this location and service exists: %d", existingJob.getId()));
    }
    if (newJob.getId() == null) {
      newJob.setId(this.jobs.keySet().stream().reduce(0L, Long::sum));
      if (newJob.getId() <= 1 || this.jobs.size() == 1) {
        newJob.setId(newJob.getId() + 1);
      }
    }

    this.jobs.put(newJob.getId(), newJob);
    this.svcLoc2jobs.put(
        LocationService.builder()
            .locationId(newJob.getLocationId())
            .serviceId(newJob.getServiceId())
            .build(),
        newJob);
    return newJob;
  }

  public Vendor addVendor(final Vendor newVendor) {
    if (this.locations.get(newVendor.getLocationId()) == null) {
      throw new VendorSmartException("Invalid location reference for this vendor");
    }
    if (this.vendors.get(newVendor.getId()) != null) {
      throw new VendorSmartException("This vendor already exists");
    }

    for (Integer svcId : newVendor.getServicesCompliance().keySet()) {
      if (this.services.get(svcId) == null) {
        throw new VendorSmartException("Invalid service compliance reference");
      }

      val locSvc =
          LocationService.builder().locationId(newVendor.getLocationId()).serviceId(svcId).build();
      var job = this.svcLoc2jobs.get(locSvc);
      if (job == null) {
        job =
            this.addJob(
                Job.builder().locationId(newVendor.getLocationId()).serviceId(svcId).build());
      }

      this.job2VendorMapping
          .computeIfAbsent(job.getId(), ignored -> new ArrayList<>())
          .add(newVendor.getId());
    }

    this.vendors.put(newVendor.getId(), newVendor);
    return newVendor;
  }

  public @Nullable List<Vendor> vendorsForJob(final Long jobId) {
    val foundVendors = this.job2VendorMapping.get(jobId);
    if (foundVendors == null) {
      return null;
    }

    val sortedVendors = new ArrayList<>(foundVendors);
    val svcId = this.jobs.get(jobId).getServiceId();

    sortedVendors.sort(
        (vendor1, vendor2) -> {
          val v1IsCompliant =
              Boolean.TRUE.equals(this.vendors.get(vendor1).getServicesCompliance().get(svcId));
          val v2IsCompliant =
              Boolean.TRUE.equals(this.vendors.get(vendor2).getServicesCompliance().get(svcId));
          if (v1IsCompliant && v2IsCompliant) {
            return 0;
          }
          if (v1IsCompliant) {
            return -1;
          }
          if (v2IsCompliant) {
            return 1;
          }

          return 0;
        });

    return sortedVendors.stream()
        .map(this.vendors::get)
        .collect(Collectors.toCollection(() -> new ArrayList<>(sortedVendors.size())));
  }

  public static final String PATH_2_RESOURCES =
      "classpath:vendor/smart/com/vs_challenge/repository/";
}
