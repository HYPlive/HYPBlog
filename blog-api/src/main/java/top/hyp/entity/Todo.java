package top.hyp.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class Todo {
	private Long id;
	private String content;
	private String status;
	private Integer priority;
	private Integer sort;
	private Boolean published;
	private Date discoveredTime;
	private Date completedTime;
	private Date updateTime;
	private Long durationMinutes;
}
