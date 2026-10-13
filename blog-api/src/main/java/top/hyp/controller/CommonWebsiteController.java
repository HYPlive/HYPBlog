package top.hyp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.hyp.annotation.VisitLogger;
import top.hyp.enums.VisitBehavior;
import top.hyp.model.vo.CommonWebsite;
import top.hyp.model.vo.Result;
import top.hyp.service.CommonWebsiteService;

import java.util.List;

/**
 * @Description: 常用网站
 * @Author: HYP
 * @Date: 2026-10-13
 */
@RestController
public class CommonWebsiteController {
	@Autowired
	CommonWebsiteService commonWebsiteService;

	/**
	 * 获取公开的常用网站列表
	 */
	@VisitLogger(VisitBehavior.UNKNOWN)
	@GetMapping("/websites")
	public Result websites() {
		List<CommonWebsite> websiteList = commonWebsiteService.getWebsiteVOList();
		return Result.ok("获取成功", websiteList);
	}
}
