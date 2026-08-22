package top.hyp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import top.hyp.entity.Todo;

import java.util.List;

@Mapper
@Repository
public interface TodoMapper {
	List<Todo> getTodoList();

	List<Todo> getPublishedTodoList();

	Todo getTodoById(Long id);

	int saveTodo(Todo todo);

	int updateTodo(Todo todo);

	int deleteTodoById(Long id);

	int updatePublishedById(Long id, Boolean published);

	int updateTodoStatus(Todo todo);

	int updateTodoSort(Todo todo);
}
