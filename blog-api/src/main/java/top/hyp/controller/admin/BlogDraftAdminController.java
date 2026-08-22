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
import top.hyp.entity.Blog;
import top.hyp.entity.Category;
import top.hyp.model.vo.Result;
import top.hyp.service.BlogDraftService;
import top.hyp.service.CategoryService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class BlogDraftAdminController {
	@Autowired
	BlogDraftService blogDraftService;
	@Autowired
	CategoryService categoryService;

	@GetMapping("/drafts")
	public Result drafts(@RequestParam(defaultValue = "") String title,
	                     @RequestParam(defaultValue = "") Integer categoryId,
	                     @RequestParam(defaultValue = "1") Integer pageNum,
	                     @RequestParam(defaultValue = "10") Integer pageSize) {
		String orderBy = "update_time desc";
		PageHelper.startPage(pageNum, pageSize, orderBy);
		PageInfo<Blog> pageInfo = new PageInfo<>(blogDraftService.getListByTitleAndCategoryId(title, categoryId));
		List<Category> categories = categoryService.getCategoryList();
		Map<String, Object> map = new HashMap<>(4);
		map.put("drafts", pageInfo);
		map.put("categories", categories);
		return Result.ok("请求成功", map);
	}

	@GetMapping("/draft")
	public Result getDraft(@RequestParam Long id) {
		Blog blog = blogDraftService.getDraftById(id);
		return Result.ok("获取成功", blog);
	}

	@OperationLogger("保存草稿")
	@PostMapping("/draft")
	public Result saveDraft(@RequestBody top.hyp.model.dto.Blog blog) {
		blogDraftService.saveDraft(blog);
		return Result.ok("草稿保存成功");
	}

	@OperationLogger("更新草稿")
	@PutMapping("/draft")
	public Result updateDraft(@RequestBody top.hyp.model.dto.Blog blog) {
		blogDraftService.updateDraft(blog);
		return Result.ok("草稿更新成功");
	}

	@OperationLogger("删除草稿")
	@DeleteMapping("/draft")
	public Result deleteDraft(@RequestParam Long id) {
		blogDraftService.deleteDraftById(id);
		return Result.ok("删除成功");
	}
}
