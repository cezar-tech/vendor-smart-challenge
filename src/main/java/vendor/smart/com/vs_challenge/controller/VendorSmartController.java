package vendor.smart.com.vs_challenge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vendor.smart.com.vs_challenge.entities.Job;
import vendor.smart.com.vs_challenge.entities.Location;
import vendor.smart.com.vs_challenge.entities.Service;
import vendor.smart.com.vs_challenge.entities.Vendor;
import vendor.smart.com.vs_challenge.repository.VendorSmartRepository;
import vendor.smart.com.vs_challenge.repository.VendorSmartRepository.VendorSmartException;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "vendor-smart", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "VS-Challenge", description = "The Vendor Smart Challenge")
public class VendorSmartController {
  private final VendorSmartRepository repository;

  @Operation(
      summary = "Fetch all locations",
      description = "fetches all locations loaded from the resources file")
  @GetMapping("locations")
  public Collection<Location> getLocations() {
    return this.repository.getAllLocations();
  }

  @Operation(
      summary = "Fetch all services",
      description = "fetches all services loaded from the resources file")
  @GetMapping("services")
  public Collection<Service> getServices() {
    return this.repository.getAllServices();
  }

  @Operation(summary = "Creates a job", description = "Creates a job that doesn't exist")
  @PostMapping(value = "jobs", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Job> createJob(@RequestBody Job job) {
    try {
      return ResponseEntity.ok(this.repository.addJob(job));
    } catch (VendorSmartException e) {
      return ResponseEntity.badRequest().header("details", e.getMessage()).build();
    }
  }

  @Operation(summary = "Creates a vendor", description = "Creates a vendor that doesn't exist")
  @PostMapping(value = "vendors", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Vendor> createVendor(@RequestBody Vendor job) {
    try {
      return ResponseEntity.ok(this.repository.addVendor(job));
    } catch (VendorSmartException e) {
      return ResponseEntity.badRequest().header("details", e.getMessage()).build();
    }
  }

  @Operation(
      summary = "Fetch all available vendors for a job",
      description = "Fetches all compliant vendors first")
  @GetMapping("vendors-for-job")
  public ResponseEntity<List<Vendor>> vendorsForJob(@RequestParam @Min(0) Long jobId) {
    val vendors = this.repository.vendorsForJob(jobId);
    if (vendors == null) {
      return ResponseEntity.notFound().header("details", "No vendors found").build();
    }

    return ResponseEntity.ok(vendors);
  }
}
