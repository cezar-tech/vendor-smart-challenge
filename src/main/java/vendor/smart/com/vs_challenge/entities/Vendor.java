package vendor.smart.com.vs_challenge.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;

@With
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Vendor {
  @NotNull private Integer id;
  @NotBlank private int locationId;
  @NotNull @NotEmpty private Map<@NotNull Integer, Boolean> servicesCompliance;
}
