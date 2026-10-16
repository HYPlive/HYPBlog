package top.hyp.controller.admin;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.hyp.annotation.OperationLogger;
import top.hyp.entity.CommonWebsite;
import top.hyp.model.vo.Result;
import top.hyp.service.CommonWebsiteService;

/**
 * @Description: 常用网站后台管理
 * @Author: HYP
 * @Date: 2026-10-13
 */
@RestController
@RequestMapping("/admin")
public class CommonWebsiteAdminController {
	@Autowired
	CommonWebsiteService commonWebsiteService;

	/**
	 * 分页获取常用网站列表
	 */
	@GetMapping("/websites")
	public Result websites(@RequestParam(defaultValue = "1") Integer pageNum,
	                       @RequestParam(defaultValue = "10") Integer pageSize) {
		String orderBy = "sort asc, create_time asc";
		PageHelper.startPage(pageNum, pageSize, orderBy);
		PageInfo<CommonWebsite> pageInfo = new PageInfo<>(commonWebsiteService.getWebsiteList());
		return Result.ok("请求成功", pageInfo);
	}

	/**
	 * 更新常用网站公开状态
	 */
	@OperationLogger("更新常用网站公开状态")
	@PutMapping("/website/published")
	public Result updatePublished(@RequestParam Long id, @RequestParam Boolean published) {
		commonWebsiteService.updatePublishedById(id, published);
		return Result.ok("操作成功");
	}

	/**
	 * 添加常用网站
	 */
	@OperationLogger("添加常用网站")
	@PostMapping("/website")
	public Result saveWebsite(@RequestBody CommonWebsite website) {
		commonWebsiteService.saveWebsite(website);
		return Result.ok("添加成功");
	}

	/**
	 * 更新常用网站
	 */
	@OperationLogger("更新常用网站")
	@PutMapping("/website")
	public Result updateWebsite(@RequestBody CommonWebsite website) {
		commonWebsiteService.updateWebsite(website);
		return Result.ok("修改成功");
	}

	/**
	 * 按id删除常用网站
	 */
	@OperationLogger("删除常用网站")
	@DeleteMapping("/website")
	public Result deleteWebsite(@RequestParam Long id) {
		commonWebsiteService.deleteWebsite(id);
		return Result.ok("删除成功");
	}
}
