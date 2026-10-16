package top.hyp.model.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @Description: 常用网站VO
 * @Author: HYP
 * @Date: 2026-10-13
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class CommonWebsite {
	private String name;//网站名称
	private String description;//网站介绍
	private String url;//网址
	private String icon;//图标（图片URL或iconfont类名）
	private String color;//主题色
}
