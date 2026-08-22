package top.hyp.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.hyp.constant.TodoStatusConstants;
import top.hyp.entity.Todo;
import top.hyp.exception.NotFoundException;
import top.hyp.exception.PersistenceException;
import top.hyp.mapper.TodoMapper;
import top.hyp.model.dto.TodoBoard;
import top.hyp.service.TodoService;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TodoServiceImpl implements TodoService {
	@Autowired
	TodoMapper todoMapper;

	@Override
	public List<Todo> getTodoList() {
		return todoMapper.getTodoList();
	}

	@Override
	public List<Todo> getPublishedTodoList() {
		return todoMapper.getPublishedTodoList();
	}

	@Override
	public Todo getTodoById(Long id) {
		Todo todo = todoMapper.getTodoById(id);
		if (todo == null) {
			throw new NotFoundException("Todo不存在");
		}
		return todo;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void saveTodo(Todo todo) {
		Date now = new Date();
		todo.setStatus(TodoStatusConstants.TODO);
		todo.setPriority(todo.getPriority() == null ? 1 : normalizePriority(todo.getPriority()));
		todo.setSort(todo.getSort() == null ? 0 : todo.getSort());
		todo.setPublished(todo.getPublished() == null || todo.getPublished());
		todo.setDiscoveredTime(now);
		todo.setUpdateTime(now);
		if (todoMapper.saveTodo(todo) != 1) {
			throw new PersistenceException("Todo添加失败");
		}
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updateTodo(Todo todo) {
		getTodoById(todo.getId());
		todo.setPriority(normalizePriority(todo.getPriority()));
		todo.setUpdateTime(new Date());
		if (todoMapper.updateTodo(todo) != 1) {
			throw new PersistenceException("Todo修改失败");
		}
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void deleteTodoById(Long id) {
		if (todoMapper.deleteTodoById(id) != 1) {
			throw new PersistenceException("Todo删除失败");
		}
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updatePublishedById(Long id, Boolean published) {
		getTodoById(id);
		if (todoMapper.updatePublishedById(id, published) != 1) {
			throw new PersistenceException("Todo公开状态更新失败");
		}
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void moveTodo(TodoBoard board) {
		if (board == null || board.getColumns() == null || board.getColumns().isEmpty()) {
			throw new IllegalArgumentException("Todo看板数据不能为空");
		}
		Set<Long> seenTodoIds = new HashSet<>();
		for (TodoBoard.Column column : board.getColumns()) {
			if (column == null || !isValidStatus(column.getStatus()) || column.getTodoIds() == null) {
				throw new IllegalArgumentException("Todo看板数据不合法");
			}
			for (int i = 0; i < column.getTodoIds().size(); i++) {
				Long id = column.getTodoIds().get(i);
				if (id == null || !seenTodoIds.add(id)) {
					throw new IllegalArgumentException("Todo不能重复排序");
				}
				Todo todo = getTodoById(id);
				updateStatusAndSort(todo, column.getStatus(), i);
			}
		}
	}

	private void updateStatusAndSort(Todo todo, String status, int sort) {
		String oldStatus = todo.getStatus();
		todo.setStatus(status);
		if (TodoStatusConstants.DONE.equals(status)) {
			todo.setCompletedTime(TodoStatusConstants.DONE.equals(oldStatus) ? todo.getCompletedTime() : new Date());
		} else {
			todo.setCompletedTime(null);
		}
		todo.setSort(sort);
		if (todoMapper.updateTodoStatus(todo) != 1 || todoMapper.updateTodoSort(todo) != 1) {
			throw new PersistenceException("Todo排序更新失败");
		}
	}

	private Integer normalizePriority(Integer priority) {
		if (priority == null || priority < 1 || priority > 5) {
			throw new IllegalArgumentException("Todo星级必须为1到5");
		}
		return priority;
	}

	private boolean isValidStatus(String status) {
		return TodoStatusConstants.TODO.equals(status)
				|| TodoStatusConstants.IN_PROGRESS.equals(status)
				|| TodoStatusConstants.DONE.equals(status);
	}
}
