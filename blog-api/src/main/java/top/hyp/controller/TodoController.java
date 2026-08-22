package top.hyp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.hyp.annotation.VisitLogger;
import top.hyp.entity.Todo;
import top.hyp.enums.VisitBehavior;
import top.hyp.model.vo.Result;
import top.hyp.service.TodoService;

import java.util.List;

@RestController
public class TodoController {
	@Autowired
	TodoService todoService;

	@VisitLogger(VisitBehavior.UNKNOWN)
	@GetMapping("/todos")
	public Result todos() {
		List<Todo> todoList = todoService.getPublishedTodoList();
		return Result.ok("获取成功", todoList);
	}
}
