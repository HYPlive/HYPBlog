package top.hyp.model.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @Description: 提交日历条目
 * @Author: HYP
 * @Date: 2026-10-13
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class BlogContribution {
	private Long id;
	private String title;//文章标题
	private String date;//发布日期 yyyy-MM-dd
}
