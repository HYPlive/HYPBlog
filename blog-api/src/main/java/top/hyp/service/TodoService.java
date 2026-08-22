package top.hyp.service;

import top.hyp.entity.Todo;

import top.hyp.model.dto.TodoBoard;

import java.util.List;

public interface TodoService {
	List<Todo> getTodoList();

	List<Todo> getPublishedTodoList();

	Todo getTodoById(Long id);

	void saveTodo(Todo todo);

	void updateTodo(Todo todo);

	void deleteTodoById(Long id);

	void updatePublishedById(Long id, Boolean published);

	void moveTodo(TodoBoard board);
}
