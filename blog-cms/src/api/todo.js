import axios from '@/util/request'

export function getTodoList() {
	return axios({url: 'todos', method: 'GET'})
}

export function getTodoById(id) {
	return axios({url: 'todo', method: 'GET', params: {id}})
}

export function saveTodo(todo) {
	return axios({url: 'todo', method: 'POST', data: todo})
}

export function updateTodo(todo) {
	return axios({url: 'todo', method: 'PUT', data: todo})
}

export function deleteTodoById(id) {
	return axios({url: 'todo', method: 'DELETE', params: {id}})
}

export function updatePublished(id, published) {
	return axios({url: 'todo/published', method: 'PUT', params: {id, published}})
}

export function updateTodoBoard(columns) {
	return axios({url: 'todo/board', method: 'PUT', data: {columns}})
}
