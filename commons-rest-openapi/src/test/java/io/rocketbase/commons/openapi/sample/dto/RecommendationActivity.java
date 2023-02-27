package io.rocketbase.commons.openapi.sample.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class RecommendationActivity extends Activity {

    private String objectId;
    
    private String objectTitle;

    private String message;
}
