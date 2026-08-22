package top.hyp.controller.admin;

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
import top.hyp.entity.Todo;
import top.hyp.model.dto.TodoBoard;
import top.hyp.model.vo.Result;
import top.hyp.service.TodoService;

@RestController
@RequestMapping("/admin")
public class TodoAdminController {
	@Autowired
	TodoService todoService;

	@GetMapping("/todos")
	public Result todos() {
		return Result.ok("请求成功", todoService.getTodoList());
	}

	@GetMapping("/todo")
	public Result todo(@RequestParam Long id) {
		return Result.ok("获取成功", todoService.getTodoById(id));
	}

	@OperationLogger("添加Todo")
	@PostMapping("/todo")
	public Result saveTodo(@RequestBody Todo todo) {
		todoService.saveTodo(todo);
		return Result.ok("添加成功");
	}

	@OperationLogger("修改Todo")
	@PutMapping("/todo")
	public Result updateTodo(@RequestBody Todo todo) {
		todoService.updateTodo(todo);
		return Result.ok("修改成功");
	}

	@OperationLogger("删除Todo")
	@DeleteMapping("/todo")
	public Result deleteTodo(@RequestParam Long id) {
		todoService.deleteTodoById(id);
		return Result.ok("删除成功");
	}

	@OperationLogger("更新Todo公开状态")
	@PutMapping("/todo/published")
	public Result updatePublished(@RequestParam Long id, @RequestParam Boolean published) {
		todoService.updatePublishedById(id, published);
		return Result.ok("操作成功");
	}

	@OperationLogger("更新Todo状态和排序")
	@PutMapping("/todo/board")
	public Result updateBoard(@RequestBody TodoBoard board) {
		todoService.moveTodo(board);
		return Result.ok("操作成功");
	}
}
