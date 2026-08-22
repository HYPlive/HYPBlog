package top.hyp.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class TodoBoard {
	private List<Column> columns;

	@NoArgsConstructor
	@Getter
	@Setter
	@ToString
	public static class Column {
		private String status;
		private List<Long> todoIds;
	}
}
