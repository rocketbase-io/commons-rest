package io.rocketbase.commons.openapi.sample.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class CommentActivity extends Activity {

    private String comment;

    private List<String> attachments;
}
